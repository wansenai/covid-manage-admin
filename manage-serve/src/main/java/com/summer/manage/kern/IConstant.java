package com.summer.manage.kern;

import com.summer.common.core.StringEnum;
import com.summer.common.helper.SpringHelper;

public interface IConstant {


    final class Redis{
        public static final String REDIS_LOGIN = "user-login:";
        public static final String REDIS_SEND = "user-send:";
        public static final String SEND_REFUND = "refund-send:";
        public static final String SEND_BINDING = "binding-send:";
        public static final String REDIS_USER_ORDER = "user-unique:";
        public static final String MONEY_TODAY = "money-today:";


        public static final int OLD_EXPIRE = 60;

        public static final int EXPIRE = 30 * 24 * 60 * 60 ;

    }

}
