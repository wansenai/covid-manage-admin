package com.summer.common.helper;

import com.alibaba.fastjson.JSONException;
import com.summer.common.core.ICodeMSG;
import com.summer.common.core.ResultSet;
import com.summer.common.core.RpcException;
import com.summer.common.core.RpcReply;
import com.summer.common.exception.ThinkerException;
import com.summer.common.support.CommonCode;
import com.summer.common.view.parser.RequestContext;
import com.summer.common.view.parser.RequestSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public final class ExceptionHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHelper.class);
    private static final String X_ERROR_CODE = "x-error-icm", X_ERROR_MSG = "x-error-msg", X_IN_TIME = "x-in-time", X_FEIGN = "x-feign";

    private ExceptionHelper() {
    }

    /**
     * 将错误转写入自定义HEADER中
     **/
    public static void responseWrite(final HttpServletResponse response, final Throwable error) {
        RpcException ex;
        RequestSession session = RequestContext.get().getSession();
        boolean needThrow = false;
        // 抛出自定义异常
        if (error instanceof ThinkerException) {
            ThinkerException thinkerException = (ThinkerException) error;
            if (!StringUtils.isEmpty(thinkerException.getMessage())) {
                // 已知异常，作为警告打印，定期跟踪
                LOG.warn("自定义异常详情: {}", thinkerException.getMessage());
            }
            ex = new RpcException(ICodeMSG.create(thinkerException.getCode(), thinkerException.getMsg()));
            if (!RequestContext.get().getSession().apiIntercept && ex.icm().isPresent()) {
                session.icm = ex.icm().get();
            }
        }
        //直接抛出 RpcException
        else if (error instanceof RpcException) {
            ex = (RpcException) error;
            if (!RequestContext.get().getSession().apiIntercept && isCMPRequest(session) && ex.icm().isPresent()) {
                session.icm = ex.icm().get();
            }
        }
        //请求方法错误
        else if (error instanceof HttpRequestMethodNotSupportedException) {
            final String method = ((HttpRequestMethodNotSupportedException) error).getMethod();
            ex = new RpcException(ICodeMSG.create(405, String.format("接口不支持%s请求方式", method)));
            if (!RequestContext.get().getSession().apiIntercept && isCMPRequest(session) && ex.icm().isPresent()) {
                session.icm = ex.icm().get();
            }
        }
        //请求Content-Type错误
        else if (error instanceof HttpMediaTypeNotSupportedException) {
            HttpMediaTypeNotSupportedException hmE = (HttpMediaTypeNotSupportedException) error;
            final MediaType media = hmE.getContentType();
            List<MediaType> types = hmE.getSupportedMediaTypes();
            ex = new RpcException(ICodeMSG.create(415, String.format("接口不支持 Content-Type: [%s] ，请使用 %s", media, types)));
            if (!RequestContext.get().getSession().apiIntercept && isCMPRequest(session) && ex.icm().isPresent()) {
                session.icm = ex.icm().get();
            }
        }
        // 参数校验异常 MethodArgumentNotValidException
        // TODO 还需要支持{@link org.springframework.validation.BindException}
        else if (error instanceof MethodArgumentNotValidException) {
            StringBuilder sbMSG = new StringBuilder();
            List<ObjectError> errors = ((MethodArgumentNotValidException) error).getBindingResult().getAllErrors();
            for (ObjectError oe : errors) {
                sbMSG.append(String.format(" | %s", oe.getDefaultMessage()));
            }
            ex = new RpcException(ICodeMSG.create(402, sbMSG.substring(3)));
            if (!RequestContext.get().getSession().apiIntercept && isCMPRequest(session) && ex.icm().isPresent()) {
                session.icm = ex.icm().get();
            }
        }
        // 参数不匹配异常
        else if (error instanceof ServletRequestBindingException) {
            ex = new RpcException(ICodeMSG.create(420, error.getMessage()));
            if (!RequestContext.get().getSession().apiIntercept && isCMPRequest(session) && ex.icm().isPresent()) {
                session.icm = ex.icm().get();
            }
        }
        //缺少BODY参数
        else if (error instanceof HttpMessageNotReadableException) {
            ex = new RpcException(ICodeMSG.create(419, "请求BODY体内容缺失"));
            if (!RequestContext.get().getSession().apiIntercept && isCMPRequest(session) && ex.icm().isPresent()) {
                session.icm = ex.icm().get();
            }
        }
        // JSON数据不符合接口参数
        else if (error instanceof JSONException) {
            LOG.warn("JSON error={}", error.getMessage(), error);
            ex = new RpcException(ICodeMSG.create(421, "请求参数无法解析，请仔细检查JSON数据格式/值/类型"));
            if (!RequestContext.get().getSession().apiIntercept && isCMPRequest(session) && ex.icm().isPresent()) {
                session.icm = ex.icm().get();
            }
        } else if (error instanceof MaxUploadSizeExceededException) {
            ex = new RpcException(CommonCode.RequestMaxExceeded, error);
            if (!RequestContext.get().getSession().apiIntercept && isCMPRequest(session) && ex.icm().isPresent()) {
                session.icm = ex.icm().get();
            }
        }
        //服务未知错误
        else {
            ex = new RpcException(CommonCode.SvError, error);
            if (!RequestContext.get().getSession().apiIntercept && ex.icm().isPresent()) {
                session.icm = ex.icm().get();
            }
        }
        if (null != ex.cause) {
            LOG.warn("responseBody error ", ex);
        }
        response.setHeader(X_ERROR_CODE, String.valueOf(ex.code()));
        response.setHeader(X_ERROR_MSG, ex.msg());
    }

    public static boolean isCMPRequest(RequestSession session) {
        return !SpringHelper.mscResponseUsed() && "POST".equals(session.method) && ("/".equals(session.uri) || "".equals(session.uri) || "/rest".equals(session.uri));
    }

    /**
     * 错误 BODY 信息
     **/
    public static byte[] errorBody(HttpServletResponse response) {
        int code = MathHelper.toInt(response.getHeader(X_ERROR_CODE), 0);
        ICodeMSG mc = ICodeMSG.create(code, response.getHeader(X_ERROR_MSG));
        return JsonHelper.toJSONBytes(SpringHelper.mscResponseUsed() ? ResultSet.onFail(code, mc.msg()) : RpcReply.onFail(mc));
    }

    /**
     * 判断中是否有错误信息
     **/
    public static boolean errored(final HttpServletResponse response) {
        long code = MathHelper.toLong(response.getHeader(X_ERROR_CODE), 0L);
        if (0 == code) {
            int status = response.getStatus();
            return status < 200 || status >= 300;
        }
        return true;
    }

    /**
     * 重置 HttpServletResponse 中的错误信息
     **/
    public static void clearResponseError(final HttpServletResponse response) {
        response.setHeader(X_ERROR_CODE, null);
        response.setHeader(X_ERROR_MSG, null);
    }

    /**
     * 设置当前访问时间
     **/
    public static void setAccessInTime(final HttpServletResponse response) {
        response.setHeader(X_IN_TIME, (DateHelper.time() + ""));
    }

    /**
     * 设置服务处理消耗时长
     **/
    public static void setSpentTime(HttpServletResponse response) {
        long inTime = MathHelper.toLong(response.getHeader(X_IN_TIME), 0L);
        response.setHeader(X_IN_TIME, null);
        response.setHeader(timeSpentHeader(), ((DateHelper.time() - inTime) + ""));
    }

    /**
     * Throwable 信息序列化
     **/
    public static String stringify(Throwable ex) {
        if (null == ex) {
            return StringHelper.EMPTY;
        }
        if (ex instanceof RpcException) {
            Throwable e = ((RpcException) ex).cause;
            if (e != null) {
                return printStringEx(e);
            } else {
                return StringHelper.EMPTY;
            }
        }
        return printStringEx(ex);
    }

    private static String printStringEx(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            ex.printStackTrace(pw);
            return sw.toString();
        } finally {
            pw.flush();
            sw.flush();
            BytesHelper.close(pw);
            BytesHelper.close(sw);
        }
    }

    private static String timeSpentHeader() {
        return String.format("X-%s-Time-Spent", StringHelper.defaultIfBlank(SpringHelper.applicationName(), "SN"));
    }
}
