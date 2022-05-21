package com.summer.common.support;

public interface IConstant {
	/** 开启JVM集群缓存 **/
	String KEY_ENABLE_JVM_CACHE = "enable.jvm.cache";
	/** 输出异常堆栈日志到控制台 **/
	String KEY_LOG_CAUSE_TRACE_STDOUT = "log.cause-trace-stdout";

	/** 配置KEY将日志写入文件，值为 STDOUT | FILE | STREAM 默认值 STDOUT **/
	String KEY_LOG_APPENDER = "server.log-appender";

	/** 配置日志级别, 默认值 INFO **/
	String KEY_LOG_LEVEL = "server.log-level";

	/** 配置 STREAM 日志的地址 **/
	String KEY_LOG_RABBIT_URI = "server.log-rabbit-uri";
	String KEY_LOG_ELASTIC_URI = "server.log-elastic-uri";

	/** 是否启用操作日志 true/false， 默认值 false **/
	String KEY_OPERATIONS_LOG_ENABLE = "operations.log.enable";

	/** SnowFlake唯一ID生成器配置 ip、hostname 默认值 ip， 若配置hostname 则（hostname规则： xxxx.number 如 thinker.001）**/
	String KEY_SNOW_MID_GENERATOR = "snow.machine.id.generator";

	/** 是否打印 IBATIS MAPPER true/false, 默认值 false 執行參數 **/
	String KEY_SHOW_IBATIS_SQL_P = "show.ibatis.arg-sql";

	/** 日志APPENDER **/
	String APPENDER_STDOUT = "STDOUT";
	String APPENDER_FILE = "FILE";
	String APPENDER_STREAM = "STREAM";

	/** 是否为 spring cloud actuator 的端点 **/
	static boolean isActuatorEndpoint(String uri) {
		return null != uri && ("/actuator".equals(uri) || uri.startsWith("/actuator/"));
	}
	/** 是否为 websocket 的端点 **/
	static boolean isStreamEndpoint(String uri) {
		return null != uri && ("/tunnel".equals(uri) || "/tunnel/sjs".equals(uri) || uri.endsWith("/tunnel.sse") || uri.contains("ext"));
	}
	/** 是否是静态资源 **/
	static boolean isStaticsEndpoint(String uri) {
		return isResourceRequest(uri) || isImageRequest(uri) || isAudioRequest(uri) || isVideoRequest(uri);
	}
	/** 是否为图片请求 **/
	static boolean isImageRequest(String uri) {
		if (null != uri) {
			String uriM = uri.toLowerCase();
			return uriM.endsWith(".svg")
					|| uriM.endsWith(".tif")
					|| uriM.endsWith(".tiff")
					|| uriM.endsWith(".png")
					|| uriM.endsWith(".jpg")
					|| uriM.endsWith(".bmp")
					|| uriM.endsWith(".gif")
					|| uriM.endsWith(".ico")
					|| uriM.endsWith(".jpeg");
		}
		return false;
	}
	/** 是否为音频请求**/
	static boolean isAudioRequest(String uri) {
		if (null != uri) {
			String uriM = uri.toLowerCase();
			return uriM.endsWith(".mp3")
					|| uriM.endsWith(".wav")
					|| uriM.endsWith(".ogg")
					|| uriM.endsWith(".aac");
		}
		return false;
	}
	/** 是否为视频请求**/
	static boolean isVideoRequest(String uri) {
		if (null != uri) {
			String uriM = uri.toLowerCase();
			return uriM.endsWith(".mp4")
					|| uriM.endsWith(".webm");
		}
		return false;
	}
	/** 文件资源请求 **/
	static boolean isResourceRequest(String uri) {
		if (null != uri) {
			String uriM = uri.toLowerCase();
			return uriM.endsWith(".html")
					||uriM.endsWith(".css")
					|| uriM.endsWith(".js")

					|| uriM.endsWith(".pdf")

					|| uriM.endsWith(".ttf")
					|| uriM.endsWith(".woff");
		}
		return false;
	}
}
