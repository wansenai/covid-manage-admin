package com.summer.manage.core;


import com.summer.common.helper.EncryptHelper;
import com.summer.common.helper.SpringHelper;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/27 11:59 下午
 **/
public class LoginUtil {

    private static final String ADMIN_PASSWORD = "admin.password";

    public static String adminPwd(String password) {
        return SpringHelper.confValue(ADMIN_PASSWORD) + EncryptHelper.cryptogram(EncryptHelper.md5(password));
    }
}
