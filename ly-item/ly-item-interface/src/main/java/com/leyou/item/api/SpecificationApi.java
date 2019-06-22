package com.leyou.item.api;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface SpecificationApi {
    /**
     * 根据组id查询参数
     * @param gid
     * @return
     */
    @GetMapping("spec/params")
    List<SpecParam> queryParamByGid(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid" ,required = false) Long cid,
            @RequestParam(value = "searching",required = false) Boolean searching);

    @GetMapping("spec/group")
    List<SpecGroup> queryGroupListByCid(@RequestParam("cid") Long cid);
}
