package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SpecificationService {
    @Autowired
    private SpecParamMapper paramMapper;

    @Autowired
    private SpecGroupMapper specMapper;

    /**
     * 根据商品分类查规格组信息
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupByCid(long cid) {
        SpecGroup group = new SpecGroup();
        group.setCid(cid);
        log.info("hello-service1------");
        List<SpecGroup> list = specMapper.select(group);
        log.info("hello-service2------");
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.GROUP_NOT_FOUNT);
        }
        return list;
    }

    public List<SpecParam> queryParamByGid(Long gid) {
        SpecParam param = new SpecParam();
        param.setGroupId(gid);
        List<SpecParam> list = paramMapper.select(param);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.PARAM_NOT_FOUNT);
        }
        return list;
    }

    /**
     * 条件查询参数列表
     * @param gid
     * @param cid
     * @param searching
     * @param generic
     * @return
     */
    public List<SpecParam> querySpecList(Long gid, Long cid, Boolean searching, Boolean generic) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        specParam.setGeneric(generic);
        List<SpecParam> specParamList = paramMapper.select(specParam);
        if (CollectionUtils.isEmpty(specParamList)) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        return specParamList;
    }

    public List<SpecGroup> queryListByCid(Long cid) {
        //查询规格组
        List<SpecGroup> specGroups = queryGroupByCid(cid);
        //查询当前分类下的参数
        List<SpecParam> specParams = querySpecList(null, cid, null, null);
        //先把规格参数变成map,map的key是规格组的id，map的值是组下的所有参数
        Map<Long, List<SpecParam>> map = new HashMap<>();
        //遍历specParams
        for (SpecParam param : specParams) {
            Long groupId = param.getGroupId();
            if (!map.containsKey(param.getGroupId())) {
                //map中key不包含这个组ID
                map.put(param.getGroupId(), new ArrayList<>());
            }
            //添加进map中
            map.get(param.getGroupId()).add(param);
        }
         //添加参数到规格组中
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }

        return specGroups;
    }
}
