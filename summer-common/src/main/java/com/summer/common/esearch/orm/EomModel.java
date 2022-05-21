package com.summer.common.esearch.orm;


import com.summer.common.core.CacheSerialize;
import com.summer.common.helper.JsonHelper;

public class EomModel extends CacheSerialize {
    @Property(type = Typical.Keyword, analyzer = "no")
    private String docId;

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
        ofSerializableId();
    }

    @Override
    public String toString() {
        return JsonHelper.toJSONString(this);
    }

    @Override
    public void ofSerializableId() {
        super.serializableId = docId;
    }
}
