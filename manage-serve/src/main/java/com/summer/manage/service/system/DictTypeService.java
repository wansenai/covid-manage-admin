package com.summer.manage.service.system;


import com.summer.common.core.BaseService;
import com.summer.common.core.ICodeMSG;
import com.summer.common.exception.ThinkerException;
import com.summer.common.helper.BeanHelper;
import com.summer.common.redis.RedisFactory;
import com.summer.common.redis.RedisOperations;
import com.summer.common.support.Pagination;
import com.summer.common.view.parser.RequestContext;
import com.summer.manage.config.IRedis;
import com.summer.manage.dao.system.SysDictDataDAO;
import com.summer.manage.dao.system.SysDictTypeDAO;
import com.summer.manage.dto.request.DictTypeListRequest;
import com.summer.manage.dto.request.DictTypeRequest;
import com.summer.manage.dto.response.DictTypeResponse;
import com.summer.manage.entity.system.SysDictType;
import com.summer.manage.kern.CodeMSG;
import com.summer.manage.kern.IConstant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/6 3:05 下午
 **/
@Service
public class DictTypeService extends BaseService {

    @Inject
    private SysDictTypeDAO sysDictTypeDAO;
    @Inject
    private SysDictDataDAO sysDictDataDAO;

    private RedisOperations redisOperations() {
        return RedisFactory.get(IRedis.DEFAULT);
    }

    public Pagination<DictTypeResponse> listType(DictTypeListRequest request) {
        Pagination<DictTypeResponse> pagination = Pagination.create(request.pager, request.size);

        List<SysDictType> dictTypes = sysDictTypeDAO.listType(request.dictName,
                                                              request.dictType,
                                                              request.status,
                                                              request.beginTime,
                                                              request.endTime,
                                                              pagination.getOffset(),
                                                              pagination.getSize());

        pagination.getList().addAll(BeanHelper.castTo(dictTypes, DictTypeResponse.class));
        Long total = sysDictTypeDAO.listTypeCount(request.dictName,
                                                  request.dictType,
                                                  request.status,
                                                  request.beginTime,
                                                  request.endTime);
        pagination.setTotal(null == total ? 0L : total);
        return pagination;
    }

    public Integer add(DictTypeRequest request) {
        if (IConstant.Common.NOT_UNIQUE.equals(checkDictTypeUnique(request))) {
            throw new ThinkerException(CodeMSG.DictTypeRepeat);
        }
        SysDictType sysDictType = BeanHelper.castTo(request, SysDictType.class);
        sysDictType.createdBy = RequestContext.get().getSession().ext;
        return insertDictType(sysDictType);
    }

    private Integer insertDictType(SysDictType sysDictType) {
        int row = sysDictTypeDAO.insertDictType(sysDictType);
        if (row > 0) {
            clearDictCache();
        }
        return row;
    }

    public int clearDictCache() {
        int count = 0;
        Set<String> strings = redisOperations().keysGet(IConstant.Redis.SYS_DICT_KEY + "*");
        for (String string : strings) {
            count += redisOperations().clear(string);
        }
        return count;
    }

    private String checkDictTypeUnique(DictTypeRequest request) {
        SysDictType dictType = sysDictTypeDAO.checkDictTypeUnique(request.dictType);
        if (Objects.nonNull(dictType) && !dictType.getId().equals(request.id)) {
            return IConstant.Common.NOT_UNIQUE;
        }
        return IConstant.Common.UNIQUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer update(DictTypeRequest request) {
        if (IConstant.Common.NOT_UNIQUE.equals(checkDictTypeUnique(request))) {
            throw new ThinkerException(CodeMSG.DictTypeRepeat);
        }
        SysDictType sysDictType = BeanHelper.castTo(request, SysDictType.class);
        sysDictType.updatedBy = RequestContext.get().getSession().ext;
        return updateDictType(sysDictType);
    }

    private Integer updateDictType(SysDictType sysDictType) {
        SysDictType oldDict = sysDictTypeDAO.selectDictTypeById(sysDictType.getId());
        sysDictDataDAO.updateDictDataType(oldDict.dictType, sysDictType.dictType);
        int row = sysDictTypeDAO.updateDictType(sysDictType);
        if (row > 0) {
            clearDictCache();
        }
        return row;
    }

    public DictTypeResponse selectDictTypeById(Long id) {
        return BeanHelper.castTo(sysDictTypeDAO.selectDictTypeById(id), DictTypeResponse.class);
    }

    public Integer del(Long[] ids) {
        for (Long id : ids) {
            SysDictType dictType = sysDictTypeDAO.selectDictTypeById(id);
            if (sysDictDataDAO.countDictDataByType(dictType.dictType) > 0) {
                throw new ThinkerException(ICodeMSG.create(-200001, String.format("%s已分配,不能删除", dictType.dictName)));
            }
        }
        int count = sysDictTypeDAO.deleteDictTypeByIds(ids);
        if (count > 0) {
            clearDictCache();
        }
        return count;
    }
}
