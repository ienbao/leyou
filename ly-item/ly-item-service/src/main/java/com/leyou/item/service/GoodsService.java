package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDto;
import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.beans.Transient;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoodsService {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper detailMapper;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> querySpuByPageAndSort(Integer page, Integer rows, Boolean saleable, String key) {
        //分页
        PageHelper.startPage(page, rows);

        //过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //搜索字段过滤
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        //上下架过滤
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        //默认排序
        example.setOrderByClause("last_update_time DESC");
        //查询
        List<Spu> spuList = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(spuList)) {
            throw new LyException(ExceptionEnums.GOODS_NOT_FIND);
        }
        //解析分类和品牌的名称
        loadCategoryAndBrandName(spuList);

        //解析分页结果
        PageInfo<Spu> result = new PageInfo<>(spuList);
        //总条数，和当前页数据
        return new PageResult<>(result.getTotal(), spuList);
    }

    private void loadCategoryAndBrandName(List<Spu> spuList) {
        for (Spu spu : spuList) {
            //处理分类名,是一个字符串拼接
            List<String> list = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(list, "/"));
            //处理品牌名
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }
    }

    /**
     * 新增商品
     * @param spu
     */
    public void saveGoods(Spu spu) {
        //新增 spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setValid(false);
        spu.setLastUpdateTime(new Date());
        spu.setSaleable(true);

        int count = spuMapper.insert(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnums.GOODS_SAVE_ERROR);
        }
        //新增detail
        SpuDetail detail = spu.getSpuDetail();
        detail.setSpuId(spu.getId());
        detailMapper.insert(detail);
        saveSkuAndStock(spu);

        //发送mq消息
        amqpTemplate.convertAndSend("item.insert",spu.getId());


    }

    private void saveSkuAndStock(Spu spu) {
        List<Stock> stockList = new ArrayList<>();
        //新增sku
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setSpuId(spu.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(new Date());

            int skuCount = skuMapper.insert(sku);
            if (skuCount != 1) {
                throw new LyException(ExceptionEnums.GOODS_SAVE_ERROR);
            }
            //新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockList.add(stock);
        }
        //批量新增库存
        int count = stockMapper.insertList(stockList);
        log.info("count :" + count);
        if (count != skus.size()) {
            throw new LyException(ExceptionEnums.GOODS_SAVE_ERROR);
        }
    }

    public SpuDetail querySpuDetailById(Long id) {
        SpuDetail detail = detailMapper.selectByPrimaryKey(id);
        if (detail == null) {
            throw new LyException(ExceptionEnums.DETAIL_CANNOT_BE_FIND);
        }
        return detail;
    }

    public List<Sku> querySkuById(Long supid) {
        Sku sku = new Sku();
        sku.setSpuId(supid);

        List<Sku> skuList = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnums.GOOD_SKU_CANNOT_BE_FIND);
        }
        //查询库存
        //查询出所有的sku中的id
        List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
        List<Stock> stocks = stockMapper.selectByIdList(ids);
        //我们把stock变成一个map，其key是sku的id，值是stock
        Map<Long, Integer> collect = stocks.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skuList.forEach(skus -> skus.setStock(collect.get(skus.getId())));

        return skuList;
    }
    @Transactional
    public void updateGoods(Spu spu) {
        if (spu.getId() == null) {
            throw new LyException(ExceptionEnums.GOODS_ID_CANNOT_BE_NULL);
        }
        //先查询以前的sku
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        List<Sku> skuList = skuMapper.select(sku);
        if (!CollectionUtils.isEmpty(skuList)) {
            //删除sku
            skuMapper.delete(sku);
            //删除stock库存
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
//            throw new LyException(ExceptionEnums.PRICE_CANNOT_BE_NULL)
        }
        //修改spu
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        spu.setValid(null);
        spu.setSaleable(null);
        spuMapper.updateByPrimaryKeySelective(spu);

        //修改detail
        detailMapper.updateByPrimaryKey(spu.getSpuDetail());
        //新增sku和库存
        saveSkuAndStock(spu);

        //发送mq消息
        amqpTemplate.convertAndSend("item.update",spu.getId());

    }

    public Spu querySpuById(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null){
            throw new LyException(ExceptionEnums.SPU_CONNT_FIND);
        }
        //查询sku
        spu.setSkus(querySkuById(id));
        //查询spudetail
        spu.setSpuDetail(querySpuDetailById(id));
        return spu ;
    }

    public List<Sku> querySkuBySpuIds(List<Long> ids) {

        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnums.GOOD_SKU_CANNOT_BE_FIND);
        }

        //查询库存
        //查询出所有的sku中的id
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        //我们把stock变成一个map，其key是sku的id，值是stock
        Map<Long, Integer> collect = stockList.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skus.forEach(s -> s.setStock(collect.get(s.getId())));
        return skus;
    }

    /**
     * 减少库存
     * @param carts
     */
    @Transactional
    public void decreaseStock(List<CartDto> carts) {
        for (CartDto cart : carts) {
            int count = stockMapper.decreaseStock(cart.getSkuId(), cart.getNum());
            if (count != 1){
                throw new LyException(ExceptionEnums.STOCK_CONNOT_ENOUGH);
            }
        }
    }
}
