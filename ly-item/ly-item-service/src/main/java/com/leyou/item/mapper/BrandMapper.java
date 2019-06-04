package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand,Long> {

    @Insert("insert into tb_category_brand (category_id, brand_id) values (#{cid}, #{bid})")
    int saveCategoryBrand(@Param("cid") Long cid, @Param("bid") Long bid);

    @Select("select * from tb_category where id in (select category_id from tb_category_brand where brand_id = #{bid})")
    List<Category> queryCategoryByBid(Long bid);

    @Delete("delete from tb_category_brand where brand_id = #{bid}")
    int deleteCategoryBrandByBid(Long bid);

    //@Select("select * from tb_brand where id in (select brand_id from tb_category_brand where category_id = #{cid})")
    //@Select("SELECT b.* FROM tb_brand b LEFT JOIN tb_category_brand cb ON b.id = cb.brand_id WHERE cb.category_id = #{cid}")
    @Select("select b.* from tb_category_brand cb inner join tb_brand b on b.id = cb.brand_id where cb.category_id = #{cid}")
    List<Brand> queryBrandByCid(@Param("cid") Long cid);
}
