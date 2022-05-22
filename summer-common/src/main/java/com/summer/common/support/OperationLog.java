package com.summer.common.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.summer.common.esearch.orm.Property;
import com.summer.common.esearch.orm.Typical;
import com.summer.common.helper.DateHelper;
import com.summer.common.helper.JsonHelper;
import com.summer.common.helper.NetworkHelper;
import com.summer.common.helper.SnowIdHelper;
import com.summer.common.helper.SpringHelper;
import com.summer.common.view.parser.RequestSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.Serializable;

/**
 * 操作日志
 **/
public class OperationLog implements Serializable {
    private static final long serialVersionUID = -1792967204418671858L;
    private static final Logger LOG = LoggerFactory.getLogger("JSON.parse");
    @Property(type = Typical.Long, desc = "日志ID")
    public long logId;
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "请求ID")
    public String rid;
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "请求深度或宽度")
    public String span;
    @JSONField(ordinal = 1)
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "API接口名")
    public String apiName;
    @Property(type = Typical.Double, desc = "访问的时间")
    @JSONField(ordinal = 2, format = "#.000")
    public Double accessTime;
    @JSONField(ordinal = 3)
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "接口URI")
    public String uri;
    @JSONField(ordinal = 4)
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "接口QUERY数据")
    public String query;
    @JSONField(ordinal = 5)
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "访问的域名")
    public String domain;
    @JSONField(ordinal = 6)
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "客户端IP")
    public String clientIp;
    @JSONField(ordinal = 7)
    @Property(type = Typical.Text, desc = "客户端UserAgent")
    public String userAgent;
    @JSONField(ordinal = 8)
    @Property(type = Typical.Text, desc = "授权加密值")
    public String signature;
    @JSONField(ordinal = 10)
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "用户ID")
    public String uid;

    @JSONField(ordinal = 11)
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "机构租户代码")
    public String tid;

    @JSONField(ordinal = 12)
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "服务名")
    public String sn;

    @JSONField(ordinal = 13)
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "环境")
    public String env;

    @JSONField(ordinal = 14)
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "服务HOST")
    public String host;

    @JSONField(ordinal = 15)
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "服务IP")
    public String serverIp;

    @JSONField(ordinal = 16)
    @Property(type = Typical.Long, desc = "接口耗时")
    public Long cost;
    @JSONField(ordinal = 9)
    @Property(type = Typical.Text, desc = "BODY参数")
    private Object requestBody;
    @JSONField(ordinal = 17)
    @Property(type = Typical.Text, desc = "接口返回数据")
    private Object responseBody;

    public static OperationLog newborn(RequestSession session, long endTime, Object responseBody) {
        OperationLog log = new OperationLog();
        log.logId = SnowIdHelper.nextId();
        log.rid = session.rid;
        log.span = session.span;
        log.apiName = session.operate;
        log.cost = endTime - session.accessTime;
        log.accessTime = DateHelper.doubleTime(session.accessTime);
        log.uri = session.uri;
        log.query = session.query;
        log.domain = session.domain;

        log.clientIp = session.clientIp;
        log.userAgent = session.userAgent;
        log.signature = session.signature;
        if (session.body.length() > 2 && null != session.requestDT && session.requestDT.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
            try {
                if (session.body.startsWith("[") && session.body.endsWith("]")) {
                    log.requestBody = JSON.parseArray(session.body);
                } else {
                    log.requestBody = JSON.parseObject(session.body);
                }
            } catch (Exception e) {
                LOG.warn("request body to Json or JsonArray error ", e);
                log.requestBody = session.body;
            }
        } else {
            log.requestBody = session.body;
        }
        log.uid = session.uid;
        log.tid = session.tid;

        log.sn = SpringHelper.applicationName();
        log.env = SpringHelper.applicationEnv();
        log.host = NetworkHelper.localHostName();
        log.serverIp = NetworkHelper.machineIP();
        if (null != responseBody) {
            log.responseBody = responseBody;
        }
        return log;
    }

    public Object getRequestBody() {
        if (IConstant.APPENDER_STREAM.equalsIgnoreCase(SpringHelper.confValue(IConstant.KEY_LOG_APPENDER))) {
            return requestBody instanceof String ? (String) requestBody : JsonHelper.toJSONString(requestBody);
        }
        return requestBody;
    }

    public Object getResponseBody() {
        if (IConstant.APPENDER_STREAM.equalsIgnoreCase(SpringHelper.confValue(IConstant.KEY_LOG_APPENDER))) {
            return responseBody instanceof String ? (String) responseBody : JsonHelper.toJSONString(responseBody);
        }
        return responseBody;
    }

    public void setResponseBody(Object responseBody) {
        this.responseBody = responseBody;
    }

    public String toJson() {
        return JSON.toJSONString(this);
    }
}
