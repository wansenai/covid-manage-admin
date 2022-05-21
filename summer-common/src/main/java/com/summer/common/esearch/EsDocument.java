package com.summer.common.esearch;

public class EsDocument {
    /** 索引名 **/
    final String indexName;
    /** ES数据类型 **/
    final String dataType;
    /** 文档数据JSON格式 **/
    final String docJson;
    /** 文档ID **/
    String docId;
    /** 文档路由键 **/
    String routing;
    /** 父文档ID **/
    String parent;
    private EsDocument(String indexName, String dataType, String docJson) {
        this.indexName = indexName;
        this.dataType = dataType;
        this.docJson = docJson;
    }

    public static EsDocument newborn(String indexName, String dataType, String docJson) {
        return new EsDocument(indexName, dataType, docJson);
    }

    public static EsDocument deletedBy(String indexName, String dataType, String docId) {
        return new EsDocument(indexName, dataType, null).ofDocumentId(docId);
    }

    public EsDocument ofDocumentId(String docId) {
        this.docId = docId;
        return this;
    }
    public EsDocument ofRouting(String routing) {
        this.routing = routing;
        return this;
    }
    public EsDocument ofParent(String parent) {
        this.parent = parent;
        return this;
    }
    public String getDocId() {
        return docId;
    }
}
