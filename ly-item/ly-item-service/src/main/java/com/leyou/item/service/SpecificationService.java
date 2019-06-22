package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper groupMapper;
    @Autowired
    private SpecParamMapper paramMapper;

    public List<SpecGroup> queryGroupByCid(Long cid) {
        //查询条件
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> list = groupMapper.select(specGroup);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnums.SPEC_GROUP_NOT_FIND);
        }
        return list;
    }

    public List<SpecParam> queryParamByCid(Long gid,Long cid,Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        List<SpecParam> listParam = paramMapper.select(specParam);
        if (CollectionUtils.isEmpty(listParam)){
            throw new LyException(ExceptionEnums.PARAM_CANNOT_FIND);
        }

        return listParam;
    }

    public List<SpecGroup> queryGroupListByCid(Long cid) {
        //查询规格组
        List<SpecGroup> specGroups = queryGroupByCid(cid);
        //查询当前分类下的参数
        List<SpecParam> params = queryParamByCid(null, cid, null);

        Map<Long ,List<SpecParam>> map = new HashMap<>();

        //先把规格参数变成map，map的key是规格组 id， map的值是组下的所有参数
        for (SpecParam param : params){
            if (!map.containsKey(param.getGroupId())){
                //这个组id在map中不存在，新增一个list
                map.put(param.getGroupId(),new ArrayList<>());
            }
            map.get(param.getGroupId()).add(param);
        }
        //填充param到group
        for (SpecGroup group : specGroups ){
            group.setParam(map.get(group.getId()));
        }
        return specGroups;
    }
}
