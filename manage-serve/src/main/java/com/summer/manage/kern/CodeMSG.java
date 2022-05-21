package com.summer.manage.kern;

import com.summer.common.core.ICodeMSG;

/** 服务消息， code = port + sequence **/
public enum CodeMSG implements ICodeMSG {
    CodeNo(200000001, "验证码已经失效啦~"),
    CodeError(200000002, "验证码好像不对喔~"),
    PhoneError(200000003, "手机号好像不对喔~"),
    UserStop(200000004, "账号被禁用了~"),
    CodeRepeat(200000005, "验证码不能重复发送～"),
    SendError(200000006, "发送验证码失败～"),
    NetWorkTimeOut(200000007, "网络超时啦~"),
    PhoneRepeat(200000008, "账号已经存在~"),
    UploadError(200000009, "文件上传失败~"),
    NameRepeat(200000010, "已经完成实名认证~"),
    IdCardError(200000011, "请输入正确身份证号~"),
    IdCardNameError(200000012, "认证错误信息-共用"),
    UserNo(200000013, "账号不存在～"),
    ParamError(200000014, "参数不正确-共用"),
    CourseSuccess(200000015, "已授课，不能操作～"),
    ArrangementNo(200000016, "排课信息不存在～"),
    ArrangementMax(200000017, "课程已经排完～"),
    CourseNo(200000018, "课程不存在～"),
    overBookingError(200000019,"请求超时，请重试~"),
    overBookingRepeat(200000020,"请勿重复下单~"),
    OrderNo(200000021,"订单不存在~"),
    StudentNo(200000022,"学生不存在~"),
    NoPay(200000023,"不允许付款~"),
    PayError(200000024,"支付错误~"),
    FavouriteRepeat(200000025,"该老师已存在~"),
    FavouriteNo(200000026,"该老师不存在~"),
    EvaluateNo(200000027,"该评价不存在~"),
    ProcessError(200000028,"非法操作"),
    ProcessNo(200000029,"不允许操作状态审核中、审核失败课程"),
    ProcessNoEdit(200000030,"邀请人不存在"),
    BingDingError(200000031, "绑定失败"),
    AGAING(200000032, "此订单已评价"),
    REFUND_ERROR(200000033, "退款失败"),



    ;

    private final int code;
    private final String msg;
    CodeMSG(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String msg() {
        return msg;
    }
}
