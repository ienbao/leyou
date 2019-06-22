package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    public List<Category> queryCategoryListByPid(Long id) {
       Category category = new Category();
       category.setParentId(id);
        List<Category> listCategory = categoryMapper.select(category);
        //判断结果
        if (CollectionUtils.isEmpty(listCategory)){
            throw  new LyException(ExceptionEnums.PRICE_CANNOT_BE_NULL);
        }
        return listCategory;
    }

    public List<Category> queryByIds(List<Long> ids){

        List<Category> list = categoryMapper.selectByIdList(ids);

        if (CollectionUtils.isEmpty(list)){
            throw  new LyException(ExceptionEnums.PRICE_CANNOT_BE_NULL);
        }
        return  list;
    }
}
