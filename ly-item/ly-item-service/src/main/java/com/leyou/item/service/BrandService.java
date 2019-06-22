package com.leyou.item.service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //1、分页
        PageHelper.startPage(page, rows);
        //2、过滤
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)) {
            example.createCriteria().orLike("name", "%" + key + "%")
                    .orEqualTo("letter", key.toUpperCase());
        }
        //3、排序
        if (StringUtils.isNoneBlank(sortBy)) {
            String orderByClause = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);
        }
        //4、查询
        List<Brand> list = brandMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {

            throw new LyException(ExceptionEnums.BRAND_NOT_FIND);
        }
        //解析分页结果
        PageInfo<Brand> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), list);
    }

    @Transactional
    public void save(Brand brand, List<Long> cids) {
        //新增品牌
        brand.setId(null);
        int count = brandMapper.insert(brand);
        if (count != 1) {
            throw new LyException(ExceptionEnums.BRAND_SAVE_ERROR);
        }

        //新增中间表
        for (Long cid : cids) {
            count = brandMapper.insertCategoryBrand(cid, brand.getId());
            if (count != 1) {
                throw new LyException(ExceptionEnums.BRAND_SAVE_ERROR);
            }
        }


    }

    public Brand queryById(Long id){
        Brand brand1 = brandMapper.selectByPrimaryKey(id);
        if (brand1 == null){
            throw new LyException(ExceptionEnums.BRAND_NOT_FIND);
        }
        return  brand1;
    }

    /**
     * 根据分类查询品牌
     * @param cid
     * @return
     */
    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> brandList = brandMapper.queryByCategoryId(cid);
        if (CollectionUtils.isEmpty(brandList)){
            throw new LyException(ExceptionEnums.BRAND_NOT_FIND);
        }
        return brandList;
    }

    public List<Brand> queryBrandByids(List<Long> ids) {

        List<Brand> Brands = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(Brands)){
            throw new LyException(ExceptionEnums.BRAND_NOT_FIND);
        }
        return Brands;
    }
}
