package com.summer.common.exception;

import com.summer.common.core.ICodeMSG;

/**
 * 自定义异常，所有已知异常均应继承至{@link ThinkerException}
 *
 */
public class ThinkerException extends RuntimeException {

    private final int code;

    private final String msg;

    /**
     * 此构造方法初始化的异常，不会打印日志：仅仅将code和msg作为响应值返回给客户端
     */
    public ThinkerException(int code, String msg) {
        super();
        this.code = code;
        this.msg = msg;
    }

    /**
     * 此构造方法初始化的异常：code和msg作为响应值返回给客户端；message消息会被打印到日志中
     */
    public ThinkerException(int code, String msg, String message) {
        super(message);
        this.code = code;
        this.msg = msg;
    }

    /**
     * 快捷构造方法，使用{@link ICodeMSG}作为常量保存常用异常定义，可以快速处理异常抛出
     */
    public ThinkerException(ICodeMSG codeMSG) {
        this(codeMSG.code(), codeMSG.msg());
    }

    /**
     * 快捷构造方法，使用{@link ICodeMSG}作为常量保存常用异常定义，可以快速处理异常抛出
     * 其中messages参数作为日志打印数据
     */
    public ThinkerException(ICodeMSG codeMSG, String message) {
        this(codeMSG.code(), codeMSG.msg(), message);
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
