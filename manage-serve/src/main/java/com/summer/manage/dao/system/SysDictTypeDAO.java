package com.summer.manage.dao.system;


import com.summer.manage.entity.system.SysDictType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/28 12:14 上午
 **/
public interface SysDictTypeDAO {

    List<SysDictType> listType(@Param("dictName") String dictName,
                               @Param("dictType") String dictType,
                               @Param("status") String status,
                               @Param("beginTime") String beginTime,
                               @Param("endTime") String endTime,
                               @Param("offset") int offset,
                               @Param("size") int size);

    Long listTypeCount(@Param("dictName") String dictName,
                       @Param("dictType") String dictType,
                       @Param("status") String status,
                       @Param("beginTime") String beginTime,
                       @Param("endTime") String endTime);

    SysDictType checkDictTypeUnique(@Param("dictType") String dictType);

    int insertDictType(SysDictType sysDictType);

    SysDictType selectDictTypeById(@Param("id") Long id);

    int updateDictType(SysDictType sysDictType);

    int deleteDictTypeByIds(Long[] ids);
}
