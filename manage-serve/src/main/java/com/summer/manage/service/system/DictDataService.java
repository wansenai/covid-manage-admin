package com.summer.manage.service.system;


import com.google.common.collect.Lists;
import com.summer.common.core.BaseService;
import com.summer.common.core.ExpireKey;
import com.summer.common.helper.BeanHelper;
import com.summer.common.helper.CollectsHelper;
import com.summer.common.helper.JsonHelper;
import com.summer.common.redis.RedisFactory;
import com.summer.common.redis.RedisOperations;
import com.summer.common.support.Pagination;
import com.summer.common.view.parser.RequestContext;
import com.summer.manage.config.IRedis;
import com.summer.manage.dao.system.SysDictDataDAO;
import com.summer.manage.dto.request.DictDataListRequest;
import com.summer.manage.dto.request.DictDataRequest;
import com.summer.manage.dto.response.DictDataResponse;
import com.summer.manage.entity.system.SysDictData;
import com.summer.manage.kern.IConstant;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/6 3:05 下午
 **/
@Service
public class DictDataService extends BaseService {

    @Inject
    private SysDictDataDAO sysDictDataDAO;
    @Inject
    private DictTypeService dictTypeService;

    private RedisOperations redisOperations() {
        return RedisFactory.get(IRedis.DEFAULT);
    }

    public List<DictDataResponse> getDataByType(String dictType) {
        String dictDataJson = redisOperations().one(IConstant.Redis.SYS_DICT_KEY + dictType, String.class);
        List<DictDataResponse> dictDataResponses = JsonHelper.parseArray(dictDataJson, DictDataResponse.class);
        if (!CollectsHelper.isNullOrEmpty(dictDataResponses)) {
            return dictDataResponses;
        }
        List<SysDictData> sysDictData = sysDictDataDAO.selectDictDataByType(dictType);
        List<DictDataResponse> newDictData = BeanHelper.castTo(sysDictData, DictDataResponse.class);
        if (!CollectsHelper.isNullOrEmpty(newDictData)) {
            //设置缓存
            redisOperations().put(IConstant.Redis.SYS_DICT_KEY + dictType, JsonHelper.toJSONString(newDictData), ExpireKey.Forever.expire);
            return newDictData;
        } else {
            return Lists.newArrayList();
        }
    }

    public Pagination<DictDataResponse> listData(DictDataListRequest request) {
        Pagination<DictDataResponse> pagination = Pagination.create(request.pager, request.size);

        List<SysDictData> dictTypes = sysDictDataDAO.listData(request.dictType,
                                                              request.status,
                                                              request.dictLabel,
                                                              pagination.getOffset(),
                                                              pagination.getSize());

        pagination.getList().addAll(BeanHelper.castTo(dictTypes, DictDataResponse.class));
        Long total = sysDictDataDAO.listDataCount(request.dictType,
                                                  request.status,
                                                  request.dictLabel);
        pagination.setTotal(null == total ? 0L : total);
        return pagination;
    }

    public Integer add(DictDataRequest request) {
        SysDictData sysDictData = BeanHelper.castTo(request, SysDictData.class);
        sysDictData.createdBy = RequestContext.get().getSession().ext;
        return insertDictData(sysDictData);
    }

    private Integer insertDictData(SysDictData sysDictData) {
        int row = sysDictDataDAO.insertDictData(sysDictData);
        if (row > 0) {
            dictTypeService.clearDictCache();
        }
        return row;
    }

    public Integer update(DictDataRequest request) {
        SysDictData sysDictData = BeanHelper.castTo(request, SysDictData.class);
        sysDictData.updatedBy = RequestContext.get().getSession().ext;
        return updateDictData(sysDictData);
    }

    private Integer updateDictData(SysDictData sysDictData) {
        int row = sysDictDataDAO.updateDictData(sysDictData);
        if (row > 0) {
            dictTypeService.clearDictCache();
        }
        return row;
    }

    public Integer del(Long[] ids) {
        int row = sysDictDataDAO.deleteDictDataByIds(ids);
        if (row > 0) {
            dictTypeService.clearDictCache();
        }
        return row;
    }

    public DictDataResponse getData(Long id) {
        SysDictData sysDictData = sysDictDataDAO.selectDictDataById(id);
        return BeanHelper.castTo(sysDictData, DictDataResponse.class);
    }
}
