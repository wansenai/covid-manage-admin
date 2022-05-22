package com.summer.manage.kern;

import com.google.common.collect.Lists;

import java.util.List;

public interface IConstant {

    enum UserStatus {
        OK("0", "正常"), DISABLE("1", "停用"), DELETED("2", "删除");

        private final String code;
        private final String info;

        UserStatus(String code, String info) {
            this.code = code;
            this.info = info;
        }

        public String getCode() {
            return code;
        }

        public String getInfo() {
            return info;
        }
    }

    final class Rsa {
        public static final String PRIVATE_KEY = "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEApBkdlQWYCELEhPOC" +
                "Fyu0eK89em6OZLVy6S+RDjbTu8BMIOyVB7MEjoi3y8RPN3vz70umc70C+gUqm0mS" +
                "ZqxqDwIDAQABAkEAjrW071vNCyodxE4Njd8ZdXdZbPdm6JiQldEjQoxV+Unvj8q4" +
                "ARYP3yrQd35lPi/m7EwjgXjSC84xTUB2TE48sQIhANIN7ttj618scpnH8ffCIMjv" +
                "GtzaDGW3Vu1n9qD0DB9nAiEAx/3X4lf+D+2KCMANe+fC9uEKeGlapZXK4tqf34lF" +
                "PxkCIFUTr5rMbZiut/vxL9/ZkM3Ril/JMRxBlcOySAII1qAhAiBjteED3cEy6cjP" +
                "cgmHBFdFRZA11rk2I4fjMQNSrRsUSQIhAMg5kg4C4j/mDiX5mgYAA0VZ8o4NJxff" +
                "A8HcLWG5Aviw";


    }

    final class Redis {
        public static final String REDIS_LOGIN_CODE = "login-code:";
        public static final String REDIS_LOGIN_USER = "login-user:";
        public static final String SYS_DICT_KEY = "sys-dict:";
        public static final String SYS_NUMBER = "sys-number:";
        /**
         * 参数管理 cache key
         */
        public static final String SYS_CONFIG_KEY = "sys-config:";


    }

    final class UserMenu {
        /**
         * 菜单类型（菜单）
         */
        public static final String TYPE_MENU = "C";

        /**
         * 是否菜单外链（否）
         */
        public static final String NO_FRAME = "1";

        /**
         * 菜单类型（目录）
         */
        public static final String TYPE_DIR = "M";

        /**
         * Layout组件标识
         */
        public final static String LAYOUT = "Layout";

        /**
         * 是否为系统默认（是）
         */
        public static final String YES = "Y";
    }

    final class Common {
        public static final List<String> SYS = Lists.newArrayList("information_schema", "mysql", "performance_schema", "sys");

        /**
         * UTF-8 字符集
         */
        public static final String UTF8 = "UTF-8";

        public final static String UNIQUE = "0";
        public final static String NOT_UNIQUE = "1";

        public final static String AVAtAR_PATH = "/avatar/";

        /**
         * 是否菜单外链（是）
         */
        public static final String YES_FRAME = "0";

        /**
         * http请求
         */
        public static final String HTTP = "http://";

        /**
         * https请求
         */
        public static final String HTTPS = "https://";

        /**
         * 部门正常状态
         */
        public static final String DEPT_NORMAL = "0";

        /**
         * 部门停用状态
         */
        public static final String DEPT_DISABLE = "1";
    }

}
