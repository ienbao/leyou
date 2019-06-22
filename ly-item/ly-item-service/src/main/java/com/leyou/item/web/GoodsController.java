package com.leyou.item.web;

import com.leyou.common.dto.CartDto;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class GoodsController {
    @Autowired
    private GoodsService goodsService;
    /**
     * 分页查询SPU
     * @param page
     * @param rows
     * @param key
     * @return
     */
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<Spu>> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key) {
        return ResponseEntity.ok(goodsService.querySpuByPageAndSort(page, rows, saleable,key));
    }

    /**
     * 新增商品
     * @param spu
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody  Spu spu) {
                goodsService.saveGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * 新增商品
     * @param spu
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody  Spu spu) {
        goodsService.updateGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     *根据spu的id查找spudetail
     * @param id
     * @return
     */
    @GetMapping("spu/detail/{id}")
    public ResponseEntity<SpuDetail> querySpuDetailById(@PathVariable("id") Long id){
        return ResponseEntity.ok( goodsService.querySpuDetailById(id));
    }

    /**
     * 根据spu id 查询下面的所有sku
     * @param supid
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuById(@RequestParam("id") Long supid){
    return ResponseEntity.ok(goodsService.querySkuById(supid));
    }

    /**
     * 根据sku的ids查询所有的sku
     * @param ids
     * @return
     */
    @GetMapping("sku/list/ids")
    public ResponseEntity<List<Sku>> querySkuBySpuIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(goodsService.querySkuBySpuIds(ids));
    }


    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id){
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }

    /**
     * 减少库存
     * @param carts
     * @return
     */
    @PostMapping("stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDto> carts){
        goodsService.decreaseStock(carts);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
