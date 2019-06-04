package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchService {
    /**
     * 把查询得到的结果封装成goods对象
     */
    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private GoodsRepository repository;

    /**
     * 根据构建goods对象
     * @param spu
     * @return
     */
    public Goods buildGoods(Spu spu){
        Long spuId = spu.getId();
        //查询商品分类名
        List<String> names = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                .stream()
                .map(Category::getName)
                .collect(Collectors.toList());

        //查询商品品牌
        Brand brand = brandClient.queryById(spu.getBrandId());
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //所有的搜索字段拼接到all中，all存入索引库，并进行分词处理，搜索时与all中的字段进行匹配查询
        String all = spu.getTitle() + StringUtils.join(names, " ") + brand.getName();
        //查询sku
        List<Sku> skuList = goodsClient.querySkuBySpuId(spuId);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //对sku进行处理，只保留需要的数据
        List<Map<String,Object>> skus = new ArrayList<>();
        //价格集合
        Set<Long> priceList = new HashSet<>();

        for ( Sku sku :skuList ) {
            Map<String ,Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            map.put("image",StringUtils.substringBefore(sku.getImages(),","));//取出第一张图片
            skus.add(map);
            //价格处理
            priceList.add(sku.getPrice());
        }

        //存储price的集合，流处理方式
        //Set<Long> priceList = skuList.stream().map(Sku::getPrice).collect(Collectors.toSet());

        //查询规格参数(只有值)
        List<SpecParam> params = specClient.querySpecParams(null, spu.getCid3(), true, null);
        if (CollectionUtils.isEmpty(params)) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        //查询商品详情
        SpuDetail spuDetail = goodsClient.querySpuDetailById(spuId);
        //获取通用规格参数
        Map<Long, String> genericSpec = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, String.class);
        //获取特有规格参数
        Map<Long, List<String>> specialSpec = JsonUtils
                .nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() { });
        //规格参数,key是规格参数的名字，值是规格参数的值
        Map<String, Object> specs = new HashMap<>();
        for (SpecParam param : params){
            //规格名称
            String key = param.getName();
            Object value = "";
            //判断是否是通用规格
            if(param.getGeneric()){
                value = genericSpec.get(param.getId());
                //判断是否是数值类型
                if(param.getNumeric()){
                    log.debug("=================a");
                    //处理成段
                    value = chooseSegment(value.toString(),param);//参数含义：原来的值，规格参数，（规格参数中含有分段信息）
                    log.debug("=================b");
                }
            }else {
                value = specialSpec.get(param.getId());
            }
            //存入map
            specs.put(key,value);
        }

        //构建goods对象
        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spu.getId());
        goods.setAll(all);//搜索字段，包含标题，品牌，规格等
        goods.setPrice(priceList);//所有sku的价格集合
        goods.setSkus(JsonUtils.toString(skus));//所有sku的集合的json格式
        goods.setSpecs(specs);// 所有可搜索的规格参数
        goods.setSubTitle(spu.getSubTitle());
        return goods;
    }

    /**
     * 将规格参数为数值型的参数划分为段
     *
     * @param value
     * @param p
     * @return
     */
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    /**
     * 根据搜索条件进行查询
     * @param request
     * @return
     */
    public SearchResult<Goods> search(SearchRequest request) {
        int page = request.getPage() -1;//elasticseach中分页是从0开始的
        int size = request.getSize();
        String key = request.getKey();
        //创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //0 结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //1 分页
        queryBuilder.withPageable(PageRequest.of(page,size));
        //2 基本搜索条件
         QueryBuilder basicQuery = buildBasicQuery(request);
         queryBuilder.withQuery(basicQuery);

         //3.聚合分类和品牌
        //3.1对分类聚合
        String categoryAggName = "category_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //3.2品牌聚合
        String brandAggName = "brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //对规格参数进行聚合

        //4 查询
        //Page<Goods> result = repository.search(queryBuilder.build()); //拿到聚合结果最好用template
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);

        //解析结果
         //1.解析分页结果
        long total = result.getTotalElements();
        int totalPage = result.getTotalPages();
        List<Goods> goodsList = result.getContent();
        //2.解析聚合结果
        Aggregations aggs = result.getAggregations();
        //解析分类聚合
        List<Category> categories = parseCategoryAgg(aggs.get(categoryAggName));
        //解析品牌聚合
        List<Brand> brands = parseBrandAgg(aggs.get(brandAggName));

        //6.完成规格参数聚合 private List<Map<String,Object>> specs;//规格参数，key及待选项，对象既是map
        List<Map<String,Object>> specs = null;
        if (categories != null && categories.size()==1){
            //商品分类存在并且数量为1，可以聚合规格参数
            specs = buildSpecificationAgg(categories.get(0).getId(),basicQuery);
        }

        return new SearchResult(total, totalPage, goodsList,  brands, categories, specs);//返回搜索的结果

    }

    /**
     * 构建基本查询
     *
     * @param request
     * @return
     */
    private QueryBuilder buildBasicQuery(SearchRequest request) {
        //构建布尔查询
        BoolQueryBuilder basicQuery = QueryBuilders.boolQuery();
        //搜索条件
        basicQuery.must(QueryBuilders.matchQuery("all", request.getKey()));

        //过滤条件
        Map<String, String> filterMap = request.getFilter();

        if (!CollectionUtils.isEmpty(filterMap)) {
            for (Map.Entry<String, String> entry : filterMap.entrySet()) {
                String key = entry.getKey();
                //判断key是否是分类或者品牌过滤条件
                if (!"cid3".equals(key) && !"brandId".equals(key)) {
                    key = "specs." + key + ".keyword";
                }
                //过滤条件
                String value = entry.getValue();
                //因为是keyword类型，使用terms查询
                basicQuery.filter(QueryBuilders.termQuery(key, value));
            }
        }
        return basicQuery;
    }


    private List<Map<String,Object>> buildSpecificationAgg(Long cid, QueryBuilder basicQuery) {
        List<Map<String, Object>> specs = new ArrayList<>();

        //1.查询可过滤的规格参数
        List<SpecParam> params = specClient.querySpecParams(null, cid, true,null);

        //2.聚合基本查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);
        queryBuilder.withPageable(PageRequest.of(0, 1));

        for (SpecParam param : params) {
            //聚合
            String name = param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name + ".keyword"));
        }
        //查询，获取结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);

        //4.对聚合结果进行解析
        Aggregations aggs = result.getAggregations();
        for (SpecParam param : params) {
            String name = param.getName();
            StringTerms terms = aggs.get(name);
            //创建聚合结果
            HashMap<String, Object> map = new HashMap<>();
            map.put("k", name);
            map.put("options", terms.getBuckets()
                    .stream()
                    .map(b -> b.getKey())
                    .collect(Collectors.toList()));
            specs.add(map);
        }
        return specs;
    }

    /**
     * 解析品牌聚合结果
     *
     * @param terms
     * @return
     */
    private List<Brand> parseBrandAgg(LongTerms terms) {
        //获取品牌ID
        try {
            List<Long> ids = terms.getBuckets()
                    .stream()
                    .map(b -> b.getKeyAsNumber().longValue())//通过map转换成long类型的流
                    .collect(Collectors.toList());
            //根据品牌ids查询品牌
            return brandClient.queryBrandByIds(ids);

        } catch (Exception e) {
            log.error("查询品牌信息失败", e);
            return null;
        }
    }

    /**
     * 对分类聚合结果进行解析
     *
     * @param terms
     * @return
     */
    public List<Category> parseCategoryAgg(LongTerms terms) {
        try {
            //获取id
            List<Long> ids = terms.getBuckets()
                    .stream()
                    .map(b -> b.getKeyAsNumber().longValue())
                    .collect(Collectors.toList());
            //根据ID查询分类
            List<Category> categories = categoryClient.queryCategoryByIds(ids);
           /* for (Category category : categories) {
                category.setParentId(null);
                category.setIsParent(null);
                category.setSort(null);
            }*/
            return categories;
        } catch (Exception e) {
            log.error("查询分类信息失败", e);
            return null;
        }

    }
    //创建或更新索引库,不需要抛出异常，ack机制，失败消息回滚，重新尝试
    public void createOrUpdateIndex(Long spuId){
        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //构建goods
        Goods goods = buildGoods(spu);
        //存入索引库
        repository.save(goods);

    }
    //删除索引
    public void deleteIndex(Long spuId) {
        repository.deleteById(spuId);
    }
}
