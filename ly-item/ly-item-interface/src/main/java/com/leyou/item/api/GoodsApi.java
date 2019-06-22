package com.leyou.item.api;

import com.leyou.common.dto.CartDto;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsApi {
    /**
     * 根据spu的id查询spuDetail
     *
     * @param spuid
     * @return
     */
    @GetMapping("/spu/detail/{id}")
    SpuDetail querySpuDetailById(@PathVariable("id") Long spuid);

    /**
     * 根据spuid查询所有的sku
     * @param spuid
     * @return
     */
    @GetMapping("sku/list")
    List<Sku> querySkuById(@RequestParam("id") Long spuid);

    /**
     * 分页查询SPU
     * @param page
     * @param rows
     * @param key
     * @return
     */
    @GetMapping("/spu/page")
    PageResult<Spu> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key) ;

    /**
     * 根据spu id查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    Spu querySpuById(@PathVariable("id") Long id);

    /**
     * 根据ids批量查询sku
     * @param ids
     * @return
     */
    @GetMapping("sku/list/ids")
    List<Sku> querySkuBySpuIds(@RequestParam("ids") List<Long> ids);

    /**
     * 减库存
     * @param carts
     * @return
     */
    @PostMapping("stock/decrease")
    void decreaseStock(@RequestBody List<CartDto> carts);
}
