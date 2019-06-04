package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
@Slf4j
public class SpecificationController {
    @Autowired
    private SpecificationService specService;

    /**
     * 根据商品分类ID查询规格组
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@PathVariable("cid") long cid){
        log.info("hello-controller-------");
        return ResponseEntity.ok(specService.queryGroupByCid(cid)) ;
    }

    /**
     * 查询商品规格参数
     *
     * @param gid       规格组ID
     * @param cid       商品分类ID
     * @param searching 是否是搜索字段
     * @param generic   是否是通用字段
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> querySpecList(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching,
            @RequestParam(value = "generic", required = false) Boolean generic
    ) {
        return ResponseEntity.ok(specService.querySpecList(gid, cid, searching, generic));
    }

    /**
     * 根据商品分类ID查询规格组及组内参数
     * @param cid
     * @return
     */
    @GetMapping("group")
    public  ResponseEntity<List<SpecGroup>> queryListByCid(@RequestParam("cid") Long cid){
        return ResponseEntity.ok(specService.queryListByCid(cid));
    };
}
