package com.summer.common.helper;

import com.google.common.collect.Sets;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 身份证校验器
 **/
public final class IdentityHelper {
    //地区、省编码
    private static final Set<String> PROVINCE_CODE = Sets.newHashSet("11", "12", "13", "14", "15", "21", "22", "23", "31", "32", "33", "34", "35", "36", "37", "41", "42", "43", "44", "45", "46", "50", "51", "52", "53", "54", "61", "62", "63", "64", "65", "71", "81", "82", "91");
    //基本正则匹配
    private static final Pattern NO_MATCHER = Pattern.compile("^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([\\d|X])$");
    //校验位码
    private static final String[] VERIFY_CODE = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
    private static final int[] CARD_WI = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final String Y_NO = "19";

    private IdentityHelper() {
    }

    /**
     * 根据身份证号得到出生日期
     **/
    public static LocalDate birthday(final String cardNo) {
        if (!verify(cardNo)) {
            return LocalDate.MIN;
        }
        return cardNoBirthday(cardNo);
    }

    /**
     * 身份证取性别
     **/
    public static String gender(final String cardNo) {
        if (!verify(cardNo)) {
            return StringHelper.EMPTY;
        }
        char code = cardNo.length() == 18 ? cardNo.charAt(16) : cardNo.charAt(14);
        return (code % 2 == 0) ? "女" : "男";
    }

    /**
     * 身份证计算年龄
     **/
    public static int age(final String cardNo) {
        if (!verify(cardNo)) {
            return -1;
        }
        return age(cardNoBirthday(cardNo));
    }

    /**
     * 生日计算年龄
     **/
    public static int age(LocalDate birthday) {
        if (null == birthday) {
            return -1;
        }
        return LocalDate.now().getYear() - birthday.getYear();
    }

    /**
     * 15位身份证转化为18位标准证件号
     **/
    public static String transTo18(final String cardNo) {
        if (15 == cardNo.length()) {
            StringBuffer cNo = new StringBuffer(cardNo);
            cNo.insert(6, Y_NO);
            cNo.append(lastNum(cNo.toString()));
            return cNo.toString();
        }
        return cardNo;
    }

    /**
     * 校验身份证的正确性
     **/
    public static boolean verify(final String cardNo) {
        if (!lengthVerify(cardNo)) {
            return false;
        }
        String cNo = transTo18(cardNo);
        if (NO_MATCHER.matcher(cNo).find()) {
            if (!PROVINCE_CODE.contains(cNo.substring(0, 2))) {
                return false;
            }
            LocalDate birthday = cardNoBirthday(cNo);
            if (birthday.getYear() < LocalDate.MIN.getYear() + 1) {
                return false;
            }
            return verifyCardIdLastNum(cNo);
        }
        return false;
    }

    /**
     * 校验身份证第18位是否正确(只适合18位身份证)
     **/
    private static boolean verifyCardIdLastNum(final String cardNo) {
        if (cardNo.length() < 17) {
            return false;
        }
        return String.valueOf(cardNo.charAt(17)).equals(lastNum(cardNo));
    }

    /**
     * 长度验证
     **/
    private static boolean lengthVerify(final String cardNo) {
        String cNo = StringHelper.defaultString(cardNo);
        return cNo.length() == 15 || cNo.length() == 18;
    }

    /**
     * 身份证生日
     **/
    private static LocalDate cardNoBirthday(String cardNo) {
        int yOffset = 6, yLen = cardNo.length() == 18 ? 4 : 2, mOffset = yOffset + yLen, mLen = 2, dOffset = mOffset + mLen, dLen = 2;
        String ys = cardNo.substring(yOffset, yOffset + yLen);
        String year = (yLen == 2) ? (Y_NO + ys) : ys;
        String month = cardNo.substring(mOffset, mOffset + mLen);
        String day = cardNo.substring(dOffset, dOffset + dLen);
        int yearInt = Integer.parseInt(year), monthInt = Integer.parseInt(month), dayInt = Integer.parseInt(day);
        if (yearInt < 1900 || yearInt > LocalDate.now().getYear() || monthInt < 1 || monthInt > 12 || dayInt < 1 || dayInt > monthMaxDay(yearInt, monthInt)) {
            return LocalDate.MIN;
        }
        return LocalDate.of(yearInt, monthInt, dayInt);
    }

    private static int monthMaxDay(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        return calendar.getActualMaximum(Calendar.DATE);
    }

    private static String lastNum(String cardNo) {
        int result = 0;
        for (int i = 0; i < CARD_WI.length; i++) {
            result += CARD_WI[i] * Integer.parseInt(String.valueOf(cardNo.charAt(i)));
        }
        return VERIFY_CODE[(result % 11)];
    }
}
