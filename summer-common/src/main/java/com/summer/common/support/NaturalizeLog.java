package com.summer.common.support;

import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.CoreConstants;
import com.alibaba.fastjson.annotation.JSONField;
import com.summer.common.esearch.orm.Property;
import com.summer.common.esearch.orm.Typical;
import com.summer.common.helper.DateHelper;
import com.summer.common.helper.ExceptionHelper;
import com.summer.common.helper.JsonHelper;
import com.summer.common.helper.NetworkHelper;
import com.summer.common.helper.SnowIdHelper;
import com.summer.common.helper.SpringHelper;
import com.summer.common.helper.StringHelper;
import com.summer.common.view.parser.RequestContext;
import com.summer.common.view.parser.RequestSession;

import java.io.Serializable;

public final class NaturalizeLog implements Serializable {
    private static final long serialVersionUID = -7609194429024402135L;
    private static final String HOST = StringHelper.defaultIfBlank(NetworkHelper.machineIP(), NetworkHelper.localHostName());

    @Property(type = Typical.Long, desc = "日志ID")
    public long logId;

    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "请求ID")
    public String rid;

    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "spanId")
    public String span;

    @Property(type = Typical.Double, desc = "输出日志时间")
    @JSONField(ordinal = 1, format = "#.000")
    public Double time;

    @JSONField(ordinal = 2)
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "输出日志的类")
    public String clz;

    @JSONField(ordinal = 3)
    @Property(type = Typical.Integer, desc = "输出日志的行号")
    public Integer line;

    @JSONField(ordinal = 4)
    @Property(type = Typical.Text, desc = "日志信息")
    public String msg;

    @JSONField(ordinal = 5)
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "输出日志的线程")
    public String thread;

    @JSONField(ordinal = 6)
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "日志级别")
    public String level;

    @JSONField(ordinal = 7)
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "输出日志的主机")
    public String host;

    @JSONField(ordinal = 8)
    @Property(type = Typical.Keyword, analyzer = "not_analyzed", desc = "输出日志的服务名")
    public String sn;

    @JSONField(ordinal = 9)
    @Property(type = Typical.Long, desc = "接口访问到输出日志耗时")
    public Long cost;

    @JSONField(ordinal = 10)
    @Property(type = Typical.Text, desc = "异常信息")
    public String cause;

    public NaturalizeLog(ILoggingEvent event) {
        RequestSession session = RequestContext.get().getSession();

        this.logId = SnowIdHelper.nextId();
        this.rid = null == session ? "-" : session.rid;
        this.span = null == session ? "-" : session.span;
        this.time = DateHelper.doubleTime(event.getTimeStamp());
        insertLineClazz(event);
        insertCauseMessage(event);
        this.thread = event.getThreadName();
        this.level = event.getLevel().toString();

        this.host = HOST;
        this.sn = SpringHelper.applicationName();
        this.cost = cost(session);
    }

    public String string() {
        return JsonHelper.toJSONString(this) + CoreConstants.LINE_SEPARATOR;
    }

    private Long cost(RequestSession session) {
        return null != session ? DateHelper.time() - session.accessTime : 0L;
    }

    private void insertLineClazz(ILoggingEvent event) {
        StackTraceElement[] cda = event.getCallerData();
        if (cda != null && cda.length > 0) {
            this.clz = cda[0].getClassName();
            this.line =  cda[0].getLineNumber();
        } else {
            this.clz = CallerData.NA; this.line = -1;
        }
    }
    // 异常最大长度为 2K
    private static final int LEN = 1024, M_LEN = 4 * LEN;
    private void insertCauseMessage(ILoggingEvent event) {
        IThrowableProxy tp = event.getThrowableProxy();
        if (null != tp) {
            String es = tp.getClassName() + "[M= " + tp.getMessage() + "]";
            if (tp instanceof ThrowableProxy) {
                Throwable tx = ((ThrowableProxy) tp).getThrowable();
                this.cause = ExceptionHelper.stringify(tx);
            } else {
                StackTraceElementProxy[] proxies = tp.getStackTraceElementProxyArray();
                StringBuilder sb = new StringBuilder(es);
                if (null != proxies && proxies.length > 0) {
                    for (StackTraceElementProxy proxy : proxies) {
                        int csLength = sb.length();
                        if (csLength < M_LEN) {
                            if (csLength > 0) {
                                sb.append(CoreConstants.LINE_SEPARATOR).append(CoreConstants.TAB);
                            }
                            sb.append(proxy.toString());
                        } else {
                            break;
                        }
                    }
                }
                this.cause = sb.toString();
            }
            if("true".equals(SpringHelper.confValue(IConstant.KEY_LOG_CAUSE_TRACE_STDOUT))) {
                System.out.println("@trace " + this.cause);
                this.cause = CoreConstants.EMPTY_STRING;
            }
            this.msg = event.getFormattedMessage() + "; " + es;
        } else {
            this.msg = event.getFormattedMessage();
            this.cause = CoreConstants.EMPTY_STRING;
        }
    }
}
