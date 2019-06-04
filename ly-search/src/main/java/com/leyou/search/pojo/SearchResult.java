package com.leyou.search.pojo;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author bystander
 * @date 2018/9/23
 */
@Data
public class SearchResult<Goods> extends PageResult<Goods> {

    private List<Brand> brands;//分类待选项
    private List<Category> categories;//品牌待选项
    private List<Map<String,Object>> specs;//规格参数，key及待选项，对象既是map

    public SearchResult() {
    }


    //全参构造函数
    public SearchResult(Long total, Integer totalPage, List<Goods> items, List<Brand> brands, List<Category> categories, List<Map<String, Object>> specs) {
        super(total, totalPage, items);
        this.brands = brands;
        this.categories = categories;
        this.specs = specs;
    }


}
