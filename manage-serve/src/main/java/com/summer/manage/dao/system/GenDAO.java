package com.summer.manage.dao.system;


import com.summer.manage.entity.system.GenTable;
import com.summer.manage.entity.system.GenTableColumn;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/28 12:14 上午
 **/
public interface GenDAO {

    List<String> databaseGet();

    List<GenTable> getAllGenTables(@Param("dbName") String dbName,
                                   @Param("tableComment") String tableComment,
                                   @Param("tableName") String tableName,
                                   @Param("beginTime") String beginTime,
                                   @Param("endTime") String endTime,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    Long getAllGenTablesCount(@Param("dbName") String dbName,
                              @Param("tableComment") String tableComment,
                              @Param("tableName") String tableName,
                              @Param("beginTime") String beginTime,
                              @Param("endTime") String endTime);

    List<GenTable> getListDbTable(@Param("sysList") String sysList,
                                  @Param("dbName") String dbName,
                                  @Param("tableComment") String tableComment,
                                  @Param("tableName") String tableName,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    Long getListDbCount(@Param("sysList") String sysList,
                        @Param("dbName") String dbName,
                        @Param("tableComment") String tableComment,
                        @Param("tableName") String tableName);

    List<GenTable> selectDbTableListByNames(@Param("dbName") String dbName, @Param("tables") String tables);

    int insertGenTable(GenTable genTable);

    List<GenTableColumn> selectDbTableColumnsByName(@Param("dbName") String dbName, @Param("tableName") String tableName);

    void insertGenTableColumn(GenTableColumn genTableColumn);

    GenTable selectGenTableById(@Param("id") Long id);
}
