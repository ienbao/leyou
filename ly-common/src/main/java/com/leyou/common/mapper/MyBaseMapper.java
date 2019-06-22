package com.leyou.common.mapper;

import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.Mapper;

@RegisterMapper //自己写的mapper 必须添加该注解
public interface MyBaseMapper<T> extends Mapper<T> ,IdListMapper<T,Long> ,InsertListMapper<T> {
}
