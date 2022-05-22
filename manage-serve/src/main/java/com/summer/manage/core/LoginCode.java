/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.summer.manage.core;


import com.summer.common.core.ExpireKey;

/**
 * 登录验证码配置信息
 */
public class LoginCode {

    /**
     * 验证码配置
     */
    public LoginCodeEnum codeType;
    /**
     * 验证码有效期 分钟
     */
    public int expiration = ExpireKey.Minutes1.expire * 2;
    /**
     * 验证码内容长度
     */
    public int length = 2;
    /**
     * 验证码宽度
     */
    public int width = 111;
    /**
     * 验证码高度
     */
    public int height = 36;
    /**
     * 验证码字体
     */
    public String fontName = "Action Jackson";
    /**
     * 字体大小
     */
    public int fontSize = 25;

    public LoginCodeEnum getCodeType() {
        return codeType;
    }
}
