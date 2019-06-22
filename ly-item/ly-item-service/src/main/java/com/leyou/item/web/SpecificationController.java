package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.Response;
import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    private SpecificationService specificationService;

    /**
     * 根据cid查询分类组
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@PathVariable("cid") Long cid){
        List<SpecGroup> list = specificationService.queryGroupByCid(cid);
        return ResponseEntity.ok(list);
    }

    /**
     * 根据组id查询参数
     * @param gid
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamByGid(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid" ,required = false) Long cid,
            @RequestParam(value = "searching",required = false) Boolean searching){
        return ResponseEntity.ok(specificationService.queryParamByCid(gid,cid,searching));
    }

    @GetMapping("group")
   public ResponseEntity<List<SpecGroup> >queryGroupListByCid(@RequestParam("cid") Long cid){
        return ResponseEntity.ok(specificationService.queryGroupListByCid(cid));
    }
}
