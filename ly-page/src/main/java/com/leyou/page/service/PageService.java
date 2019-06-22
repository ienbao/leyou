package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageService {

    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specsClient;

    public Map<String,Object> toItemPage(Long spuId) {
        //创建一个map
        Map<String,Object> model = new HashMap<>();
       //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //查询skus
        List<Sku> skus = spu.getSkus();
        //查询详情
        SpuDetail detail = spu.getSpuDetail();
        //查询brand
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        //查询商品分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //查询规格参数
        List<SpecGroup> specs = specsClient.queryGroupListByCid(spu.getCid3());

        model.put("title",spu.getTitle());
        model.put("subTitle",spu.getSubTitle());
        model.put("categories",categories);
        model.put("detail",detail);
        model.put("brand",brand);
        model.put("specs",specs);
        model.put("skus",skus);
        return model;
    }

    public void createHtml(Long spuId){
        //上下文
        Context context = new Context();
        context.setVariables(loadModel(spuId));





    }

    private Map<String,Object> loadModel(Long spuId) {


        return null;
    }
}
