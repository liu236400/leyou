package com.leyou.page.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bystander
 * @date 2018/9/26
 */
@Slf4j
@Service
public class PageService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specClient;

    @Autowired
    private TemplateEngine templateEngine;

/*    @Value("${ly.page.path}")
    private String dest;*/

    public Map<String, Object> loadModel(Long spuId) {
        Map<String, Object> model = new HashMap<>();
        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);

        //上架未上架，则不应该查询到商品详情信息，抛出异常
        if (!spu.getSaleable()) {
            throw new LyException(ExceptionEnum.GOODS_NOT_SALEABLE);
        }
        //查询detail
        SpuDetail detail = spu.getSpuDetail();
        //查询skus
        List<Sku> skus = spu.getSkus();
        //查询brand
        Brand brand = brandClient.queryById(spu.getBrandId());
        //查询三级分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //查询规格参数组及组内参数
        List<SpecGroup> specs = specClient.queryListByCid(spu.getCid3());

        model.put("brand", brand);
        model.put("categories", categories);
        model.put("spu", spu);//此项可以优化
        model.put("skus", skus);
        model.put("detail", detail);
        model.put("specs", specs);
        return model;
    }
   /*
       页面的静态化处理
   */
   public  void createHtml(Long spuId) {
        //thmleaf的上下文
        Context context = new Context();
        Map<String, Object> map = loadModel(spuId);
        context.setVariables(map);
        //输出流
        File dest = new File("D:\\leyou_thymeleaf", spuId + ".html");//参数含义：文件存放路径，文件名
        //如果页面存在，先删除，后进行创建静态页
        if (dest.exists()) {
            dest.delete();
        }
        try (PrintWriter writer = new PrintWriter(dest, "utf-8")) {//自动释放流
            templateEngine.process("item", context, writer);
        } catch (Exception e) {
            log.error("【静态页服务】生成静态页面异常", e);
        }
    }


   //删除静态页
    public void deleteHtml(Long spuId) {
        File dest = new File("D:\\leyou_thymeleaf", spuId + ".html");
        if (dest.exists()) {
            boolean flag = dest.delete();
            if (!flag) {
                log.error("删除静态页面失败");
            }
        }
    }
}
