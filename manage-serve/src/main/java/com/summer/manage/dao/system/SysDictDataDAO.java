package com.summer.manage.dao.system;


import com.summer.manage.entity.system.SysDictData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/28 12:14 上午
 **/
public interface SysDictDataDAO {

    List<SysDictData> selectDictDataByType(@Param("dictType") String dictType);

    void updateDictDataType(@Param("oldDictType") String oldDictType, @Param("newDictType") String newDictType);

    int countDictDataByType(@Param("dictType") String dictType);

    List<SysDictData> listData(@Param("dictType") String dictType,
                               @Param("status") String status,
                               @Param("dictLabel") String dictLabel,
                               @Param("offset") int offset,
                               @Param("size") int size);

    Long listDataCount(@Param("dictType") String dictType,
                       @Param("status") String status,
                       @Param("dictLabel") String dictLabel);

    int insertDictData(SysDictData sysDictData);

    int updateDictData(SysDictData sysDictData);

    int deleteDictDataByIds(Long[] ids);

    SysDictData selectDictDataById(@Param("id") Long id);
}
