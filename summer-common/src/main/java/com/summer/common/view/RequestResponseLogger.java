package com.summer.common.view;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.summer.common.initializ.SpringBootApplication;
import com.summer.common.support.IConstant;
import com.summer.common.support.OperationLog;
import com.summer.common.helper.BytesHelper;
import com.summer.common.helper.SpringHelper;
import com.summer.common.helper.StringHelper;
import com.summer.common.helper.ThreadFactoryHelper;
import com.summer.common.view.parser.RequestSession;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

final class RequestResponseLogger {
    private static final Logger OUT = LoggerFactory.getLogger(OperationLog.class);
    private static final ExecutorService LOG_EXECUTOR = Executors.newSingleThreadExecutor(ThreadFactoryHelper.threadFactoryOf("operation-log"));

    private static final Set<String> FILE_RESPONSE_TYPE = Sets.newHashSet(MediaType.IMAGE_GIF_VALUE,
                                                                          MediaType.IMAGE_JPEG_VALUE,
                                                                          MediaType.IMAGE_PNG_VALUE,
                                                                          MediaType.APPLICATION_PDF_VALUE,
                                                                          MediaType.APPLICATION_OCTET_STREAM_VALUE);

    private final boolean isSR; private final Pair<String, byte[]> result;
    private final RequestSession session; private final long endTime;
    RequestResponseLogger(boolean isSR, RequestSession session, Pair<String, byte[]> result) {
        this.isSR = isSR; this.session = session; this.result = result; this.endTime = System.currentTimeMillis();
    }

    RequestResponseLogger submit(boolean logged) {
        SpringBootApplication.doStatisticsUriRequest(session.accessTime, session.uri);
        if (logged && SpringHelper.mscResponseUsed()) {
            // 静态资源请求 + 没有名字的mapping + SpringCloudEndpoint 不记录日志
            if (!isSR && !StringHelper.isBlank(session.operate) && !IConstant.isActuatorEndpoint(session.uri)) {
                LOG_EXECUTOR.submit(() -> OUT.info("{}", newOperationLog().toJson()));
            }
        }
        return this;
    }
    // 清理流资源
    void clearGZIP(final GZIPOutputStream gzip, final ByteArrayOutputStream bos) {
        LOG_EXECUTOR.submit(() -> { BytesHelper.close(gzip); BytesHelper.close(bos); });
    }
    private static final Logger LOG = LoggerFactory.getLogger("JSON.parse");
    private OperationLog newOperationLog() {
        // 非文件
        if (!FILE_RESPONSE_TYPE.contains(result.getKey())) {
            String response = BytesHelper.string(result.getValue());
            if (result.getKey().startsWith(MediaType.APPLICATION_JSON_VALUE)) {
                try {
                    if (response.startsWith("[") && response.endsWith("]")) {
                        return OperationLog.newborn(session, endTime, JSON.parseArray(response));
                    } else if (response.startsWith("{") && response.endsWith("}")){
                        return OperationLog.newborn(session, endTime, JSON.parseObject(response));
                    } else {
                        return OperationLog.newborn(session, endTime, response);
                    }
                } catch (Exception e) {
                    LOG.warn("response body to Json or JsonArray error ", e);
                    return OperationLog.newborn(session, endTime, response);
                }
            } else {
                return OperationLog.newborn(session, endTime, response);
            }
        } else {
            return OperationLog.newborn(session, endTime, ("文件类型：" + result.getKey()));
        }
    }
}
