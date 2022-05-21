package com.summer.common.core;

import com.summer.common.exception.ThinkerException;

import java.io.Serializable;

/**
 * 所有controller中参数，只要启用配置<code>thinker.request.validate.enabled=true</code>，
 * 即可自动进行verify进行校验，但建议优先使用validation进行简单的校验(如：
 * {@link javax.validation.constraints.NotNull}
 * {@link javax.validation.constraints.Min}
 * {@link javax.validation.constraints.Max}
 * {@link javax.validation.constraints.Size}
 * )，当出现较复杂的多值关联校验
 * 等类似情况时，再使用此方法进行更复杂的关联校验
 * verify时，抛出{@link ThinkerException}时，异常处理会自动捕获该异常的响应码和消息，返回给客户端
 */
public interface IRequest extends Serializable {

    void verify();
}
