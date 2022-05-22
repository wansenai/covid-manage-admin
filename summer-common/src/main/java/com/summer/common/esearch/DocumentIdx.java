package com.summer.common.esearch;

public class DocumentIdx {
    /**
     * 索引名
     **/
    final String indexName;
    /**
     * 文档ID
     **/
    final String docId;
    /**
     * ES数据类型
     **/
    String dataType;

    private DocumentIdx(String indexName, String docId) {
        this.indexName = indexName;
        this.docId = docId;
    }

    public static DocumentIdx make(String indexName, String docId) {
        return new DocumentIdx(indexName, docId);
    }

    /**
     * 设置数据类型
     **/
    public DocumentIdx ofDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }
}
