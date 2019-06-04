package com.leyou.item.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

/**
 * @author bystander
 * @date 2018/9/18
 */
@Table(name = "tb_spu")
@Data
public class Spu {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    private String title;
    private String subTitle;
    private Long cid1;
    private Long cid2;
    private Long cid3;
    private Long brandId;
    private Boolean saleable;//是否上架
    @JsonIgnore
    private Boolean valid;//是否有效，逻辑删除使用
    private Date createTime;

    @JsonIgnore//表明是不需要返回的的字段，需要导jackson-annotations包
    private Date lastUpdateTime;


    //spu所属的分类名称
    @Transient
    private String cname;

    //spu所属品牌名
    @Transient//不是数据库字段，注意所在包
    private String bname;

    //spu详情
    @Transient
    private SpuDetail spuDetail;

    //sku集合
    @Transient
    private List<Sku> skus;
}
