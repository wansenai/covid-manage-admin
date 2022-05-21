package com.summer.common.view;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.summer.common.core.RpcReply;
import com.summer.common.ibatis.DataSourceManager;
import com.summer.common.support.IConstant;
import com.summer.common.view.parser.RequestContext;
import com.summer.common.view.parser.RequestHeader;
import com.summer.common.view.parser.RequestSession;
import com.summer.common.helper.BytesHelper;
import com.summer.common.helper.ExceptionHelper;
import com.summer.common.helper.JsonHelper;
import com.summer.common.helper.SpringHelper;
import com.summer.common.helper.StringHelper;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/** URI Response 大于 2K 的做压缩处理 **/
public class Reply2kCompressionFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(Reply2kCompressionFilter.class);

    private static final String CON_ENCODING = "Content-Encoding", ACCEPT_ENCODING = "Accept-Encoding", GZIP = "gzip";
    private static final Long NEED_COM_SIZE = 2 * 1024L;

    private boolean logged = false;
    public void init(FilterConfig config) {
        logged = "true".equals(SpringHelper.confValue(IConstant.KEY_OPERATIONS_LOG_ENABLE));
        LOG.debug("compression filter init......");
    }
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)req;
        String uri = request.getRequestURI();
        // process spring cloud or websocket endpoint or error path
        if (IConstant.isActuatorEndpoint(uri) || IConstant.isStreamEndpoint(uri) || WebConfigurationSupport.GlobalControllerHandler.ERROR_PATH.equals(uri)) {
            chain.doFilter(request, res); return;
        }
        // 生成 RequestSession
        RequestContext.get().setSession(RequestSession.newborn(request));
        boolean isSR = IConstant.isStaticsEndpoint(uri);
        // 初始化 response
        HttpServletResponse response = (HttpServletResponse) res;
        ResponseWrapper wrapper = new ResponseWrapper(response);
        ExceptionHelper.clearResponseError(response);
        ExceptionHelper.setAccessInTime(response);
        // 进入业务处理
        chain.doFilter(new RequestWrapper(request), wrapper);
        if(!response.isCommitted()) {
            response.setHeader(RequestHeader.Rid.name(), RequestContext.get().getSession().rid);
            // 非资源请求时 设置请求头信息
            if (!isSR) {
                if (StringHelper.isBlank(request.getContentType()) && StringHelper.isBlank(wrapper.getContentType())) {
                    response.setContentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8");
                } else if (!wrapper.getContentType().startsWith(MediaType.TEXT_HTML_VALUE)
                        && !wrapper.getContentType().startsWith(MediaType.TEXT_PLAIN_VALUE)
                        && !wrapper.getContentType().startsWith(MediaType.APPLICATION_PDF_VALUE)
                        && !wrapper.getContentType().startsWith(MediaType.TEXT_EVENT_STREAM_VALUE)
                        && !wrapper.getContentType().startsWith(MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
                    response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
                }
            }else{
                if ( !StringHelper.isBlank(request.getContentType()) && !response.getContentType().contains("charset") ){
                    response.setContentType(response.getContentType() + ";charset=UTF-8");
                }
                if( !StringHelper.isBlank(wrapper.getContentType()) && !wrapper.getContentType().contains("charset")){
                    if(!response.getContentType().contains("charset")){
                        response.setContentType(response.getContentType() + ";charset=UTF-8");
                    }
                }
            }
            boolean errored = ExceptionHelper.errored(wrapper);
            wrapper.flushBuffer(); wrapper.finish(); byte[] bytes = wrapper.body();
            if (errored) {  if (isSR) { return; }
                response.setStatus(HttpStatus.OK.value());
                bytes = ExceptionHelper.errorBody(wrapper);
                ExceptionHelper.clearResponseError(response);
                response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            }
            // while Deprecated if 非空且非基础类型且要求转为下划线
            if (bytes.length > 0
                    && SpringHelper.mscResponseUsed()
                    && RequestContext.keyAnswerUnderline()
                    && response.getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE)) {
                Map<String, Object> jsonMap = ImmutableMap.of("$jst$", JSON.parseObject(BytesHelper.string(bytes)));
                bytes = JsonHelper.toJSONBytes(JsonHelper.parseObject(JsonHelper.toUnderlineJson(jsonMap)).getJSONObject("$jst$"));
            }
            if (!SpringHelper.mscResponseUsed() && RequestContext.keyFrontRequest() && !isSR) {
                JSONObject json = JSON.parseObject(BytesHelper.string(bytes));
                bytes = JsonHelper.toJSONBytes(json.getJSONObject("response"));
            }
            Pair<String, byte[]> result = new Pair<>(response.getContentType(), bytes);
            OutputStream os = null; ByteArrayOutputStream bos = null; GZIPOutputStream gzip = null;
            try {
                os = response.getOutputStream();
                // 如果 Client 支持 GZIP, 压缩
                if(needCompress(request, bytes)) {
                    response.addHeader(CON_ENCODING, GZIP);
                    bos = new ByteArrayOutputStream();
                    gzip = new GZIPOutputStream(bos);
                    gzip.write(bytes);
                    gzip.finish(); gzip.flush(); bos.flush();
                    bytes = bos.toByteArray();
                }
                ExceptionHelper.setSpentTime(response);
                response.setContentLength(bytes.length);
                os.write(bytes); os.flush();
            } finally {
                // 处理请求响应日志
                new RequestResponseLogger(isSR, RequestContext.get().getSession(), result).submit(logged).clearGZIP(gzip, bos);
                // 清理 ThreadLocal
                BytesHelper.close(os); RequestContext.get().clear(); DataSourceManager.get().clear(); RpcReply.Helper.get().clear();
            }
        }
    }

    public void destroy() {
        LOG.debug("compression filter destroy......");
    }

    /** 判断请求返回体是需要压缩 **/
    private static boolean needCompress(HttpServletRequest request, byte[] src) {
        return src.length >= NEED_COM_SIZE && supportGzip(request);
    }

    /** 判断请求是否支持 GZIP **/
    private static boolean supportGzip(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaders(ACCEPT_ENCODING);
        while (headers.hasMoreElements()) {
            String value = headers.nextElement();
            if (value.contains(GZIP)) {
                return true;
            }
        }
        return false;
    }

    // 重新Wrapper Request
    static final class RequestWrapper extends HttpServletRequestWrapper {
        RequestWrapper(HttpServletRequest request) {
            super(request);
        }
        @Override
        public String getContentType() {
            String contentType = super.getContentType();
            return StringHelper.isBlank(contentType) ? MediaType.TEXT_HTML_VALUE : contentType;
        }
    }

    private static final class ResponseWrapper extends HttpServletResponseWrapper {
        private static final int OT_NONE = 0, OT_WRITER = 1, OT_STREAM = 2;
        private int outputType = OT_NONE;
        private ByteArrayOutputStream buffer;
        private ServletOutputStream output = null;
        private PrintWriter writer = null;

        ResponseWrapper(HttpServletResponse response) {
            super(response);
            buffer = new ByteArrayOutputStream();
        }

        @Override
        public String getContentType() {
            return StringHelper.defaultString(super.getContentType());
        }

        @Override
        public PrintWriter getWriter() {
            if (outputType == OT_STREAM)
                throw new IllegalStateException();
            else if (outputType == OT_WRITER)
                return writer;
            else {
                outputType = OT_WRITER;
                writer = new PrintWriter(new OutputStreamWriter(buffer, StandardCharsets.UTF_8));
                return writer;
            }
        }

        @Override
        public ServletOutputStream getOutputStream() {
            if (outputType == OT_WRITER)
                throw new IllegalStateException();
            else if (outputType == OT_STREAM)
                return output;
            else {
                outputType = OT_STREAM;
                output = new WrappedOutputStream(buffer);
                return output;
            }
        }

        @Override
        public void flushBuffer() throws IOException {
            if (outputType == OT_WRITER)
                writer.flush();
            if (outputType == OT_STREAM)
                output.flush();
        }

        @Override
        public void reset() {
            outputType = OT_NONE;
            buffer.reset();
        }

        public byte[] body() {
            return null != buffer ? buffer.toByteArray() : new byte[0];
        }

        void finish() throws IOException {
            if (writer != null) {
                writer.close();
            }
            if (output != null) {
                output.close();
            }
        }

        static class WrappedOutputStream extends ServletOutputStream {
            private ByteArrayOutputStream buffer;

            WrappedOutputStream(ByteArrayOutputStream buffer) {
                this.buffer = buffer;
            }

            public void write(int b) {
                buffer.write(b);
            }

            @Override public boolean isReady() {
                return false;
            }

            @Override public void setWriteListener(WriteListener writeListener) {}
        }
    }
}
