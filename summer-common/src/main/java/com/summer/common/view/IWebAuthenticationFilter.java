package com.summer.common.view;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.SettableFuture;
import com.summer.common.core.RpcException;
import com.summer.common.support.CommonCode;
import com.summer.common.support.IConstant;
import com.summer.common.view.parser.RequestContext;
import com.summer.common.view.parser.RequestSession;
import com.summer.common.helper.BytesHelper;
import com.summer.common.helper.DateHelper;
import com.summer.common.helper.EncryptHelper;
import com.summer.common.helper.ExceptionHelper;
import com.summer.common.helper.MathHelper;
import com.summer.common.helper.StrCastHelper;
import com.summer.common.helper.StringHelper;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

public interface IWebAuthenticationFilter extends Filter {
	Logger LOG = LoggerFactory.getLogger(IWebAuthenticationFilter.class);
	//BODY 体数据验签分隔符
	String SIGN_SPLIT = ">#￥&<";

	/** To override implements your own black list **/
	default boolean clientDeny(String clientIp, String deviceId){
		if(LOG.isDebugEnabled()) {
			LOG.debug("To check the clientIp: {} deviceNo: {} permission", clientIp, deviceId);
		}
		return false;
	}

	/** To override implements your own web RequestSession logic **/
	default boolean authentication(RequestSession session, HttpServletRequest request, HttpServletResponse response) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("To authentication the Http client session: {} ", session);
		}
		// 可设置 tid | uid | ext
		return true;
	}

	/** to override implements your own logic **/
	default boolean usingSecretRequest(String uri) {
		return false;
	}

	/** To override implements your own WebSocket token logic **/
	default void doFilterWS(String domain, String token, String did, String sid, SettableFuture<String> uidFuture) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("To authentication the websocket domain: {} token: {} did: {}, sid: {}", domain, token, did, sid);
		}
		// domain: ws server name
		// token: ws token
		// did: ws client device id
		// sid: ws sid
		LOG.warn("You set a token as uid");
		uidFuture.set(token);
	}

	@Override
	default void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		String uri = request.getRequestURI();
		// process spring cloud or websocket endpoint or error path
		if (IConstant.isActuatorEndpoint(uri) || IConstant.isStreamEndpoint(uri) || WebConfigurationSupport.GlobalControllerHandler.ERROR_PATH.equals(uri)) {
			chain.doFilter(request, res); return;
		}

		RequestSession session = RequestContext.get().getSession();
		HttpServletResponse response = (HttpServletResponse) res;
		try {
			// 验证请求IP
			if(clientDeny(session.clientIp, session.deviceNo)) {
				ExceptionHelper.responseWrite(response, new RpcException(CommonCode.Forbidden)); return;
			}
			//权限认证
			if(authentication(session, request, response)) {
				String type = StringHelper.defaultString(request.getContentType());
				// 非文件上传处理
				if(!type.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)) {
					Pair<? extends HttpServletRequest, String> pair = bodyTranslate(request, type);
					session.body = pair.getValue();
					//验证请求数据
					if (usingSecretRequest(session.uri)) {
						verifyDataSecret(pair.getValue(), session);
					}
					chain.doFilter(pair.getKey(), response);
				} else {
					chain.doFilter(request, response);
				}
			}
		} catch (Exception e) {
			ExceptionHelper.responseWrite(response, e);
		}
	}

	/** 校验请求体数据 **/
	default void verifyDataSecret(String body, RequestSession session) {
		if(StringHelper.isBlank(session.dataSecret)) {
			throw new RpcException(CommonCode.Unavailable);
		}
		//校验客户端时间与服务器时间差
		if(!MathHelper.isBetween(session.clientTime, (session.accessTime - DateHelper.HOUR_TIME), (session.accessTime + DateHelper.HOUR_TIME))) {
			throw new RpcException(CommonCode.InvalidTime);
		}
		//对请求数据做（时间奇偶性 + MD5）验签
		String plaintext = session.clientTime % 2 == 0
				? StringHelper.join(SIGN_SPLIT, session.deviceNo, body, session.uri)
				: StringHelper.join(SIGN_SPLIT, session.uri, body, session.deviceNo);
		if (!session.dataSecret.equals(EncryptHelper.md5(plaintext))) {
			throw new RpcException(CommonCode.Unsafety);
		}
	}

	@Override default void init(FilterConfig cnf) { LOG.debug("authentication filter init......"); }
	@Override default void destroy() { LOG.debug("authentication filter destroy......"); }

	Set<String> BODY_METHODS = Sets.newHashSet("POST", "PUT", "PATCH", "DELETE");
	default Pair<? extends HttpServletRequest, String> bodyTranslate(HttpServletRequest request, String type) throws IOException {
		boolean noBody = !BODY_METHODS.contains(request.getMethod().toUpperCase());
		final String body =  noBody ? StringHelper.EMPTY : BytesHelper.string(request.getInputStream());
		return new Pair<>(new RequestWrapper(request, bodyAsJson(body, type)), body);
	}

	// body 数据转换成 JSON
	default String bodyAsJson(String body, String contentType){
		// 请求体为空
		if(StringHelper.isBlank(body)) {
			return body;
		}
		// FORM 请求
		else if (contentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
			return StrCastHelper.form2Json(body).toString();
		}
		// XML 请求
		else if (contentType.startsWith(MediaType.APPLICATION_XML_VALUE) || contentType.startsWith(MediaType.TEXT_XML_VALUE)) {
			return StrCastHelper.xml2Json(body).toString();
		}
		// 默认为 JSON
		else {
			return body;
		}
	}

	// 重新Wrapper Request
	class RequestWrapper extends HttpServletRequestWrapper {
		private final HttpServletRequest request;
		private final byte[] bodies;

		RequestWrapper(HttpServletRequest request, String body) {
			super(request); this.request = request;
			this.bodies = BytesHelper.utf8Bytes(body);
		}
		@Override
		public BufferedReader getReader() throws IOException {
			return new BufferedReader(new InputStreamReader(getInputStream()));
		}
		@Override
		public int getContentLength() {
			return bodies.length;
		}
		@Override
		public String getContentType() {
			return MediaType.APPLICATION_JSON_UTF8_VALUE;
		}
		//过滤 Client 请求体数据
		@Override
		public ServletInputStream getInputStream() throws IOException {
			return 0 == bodies.length ? request.getInputStream() : BytesHelper.castServletInputStream(new ByteArrayInputStream(bodies));
		}
	}
}
