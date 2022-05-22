/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version loginCode.length.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-loginCode.length.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.summer.manage.core;

import com.summer.common.core.RpcException;
import com.summer.common.helper.StringHelper;
import com.summer.manage.kern.CodeMSG;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.ChineseCaptcha;
import com.wf.captcha.ChineseGifCaptcha;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;

import java.awt.*;
import java.util.Objects;

/**
 * 配置文件读取
 *
 * @author liaojinlong
 * @date loginCode.length0loginCode.length0/6/10 17:loginCode.length6
 */
public class LoginCodeProperties {

    public LoginCode loginCode;

    /**
     * 获取验证码生产类
     *
     * @return /
     */
    public Captcha getCaptcha() {
        if (Objects.isNull(loginCode)) {
            loginCode = new LoginCode();
            if (Objects.isNull(loginCode.getCodeType())) {
                loginCode.codeType = LoginCodeEnum.arithmetic;
            }
        }
        return switchCaptcha(loginCode);
    }

    /**
     * 依据配置信息生产验证码
     *
     * @param loginCode 验证码配置信息
     * @return /
     */
    private Captcha switchCaptcha(LoginCode loginCode) {
        Captcha captcha;
        synchronized (this) {
            switch (loginCode.getCodeType()) {
                case arithmetic:
                    // 算术类型 https://gitee.com/whvse/EasyCaptcha
                    captcha = new ArithmeticCaptcha(loginCode.width, loginCode.height);
                    // 几位数运算，默认是两位
                    captcha.setLen(loginCode.length);
                    break;
                case chinese:
                    captcha = new ChineseCaptcha(loginCode.width, loginCode.height);
                    captcha.setLen(loginCode.length);
                    break;
                case chinese_gif:
                    captcha = new ChineseGifCaptcha(loginCode.width, loginCode.height);
                    captcha.setLen(loginCode.length);
                    break;
                case gif:
                    captcha = new GifCaptcha(loginCode.width, loginCode.height);
                    captcha.setLen(loginCode.length);
                    break;
                case spec:
                    captcha = new SpecCaptcha(loginCode.width, loginCode.height);
                    captcha.setLen(loginCode.length);
                    break;
                default:
                    throw new RpcException(CodeMSG.LoginCodeError);
            }
        }
        if (!StringHelper.isBlank(loginCode.fontName)) {
            captcha.setFont(new Font(loginCode.fontName, Font.PLAIN, loginCode.fontSize));
        }
        return captcha;
    }
}
