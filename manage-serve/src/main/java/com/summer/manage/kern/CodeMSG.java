package com.summer.manage.kern;


import com.summer.common.core.ICodeMSG;

/**
 * 服务消息， code = port + sequence
 **/
public enum CodeMSG implements ICodeMSG {
    CodeNo(200010001, "验证码已经失效啦~"),
    CodeError(200010002, "验证码好像不对喔~"),
    LoginCodeError(200010003, "验证码配置信息错误！正确配置查看 LoginCodeEnum"),
    RsaError(200010004, "Rsa解密失败"),
    USERNAME_NOT_FIND(200010005, "未找到该账号"),
    PASSWORD_ERROR(200010006, "密码错误"),
    UserStatusError(200010007, "该账号已被禁用"),
    ImportError(200010008, "导入失败"),
    ImportNo(200010009, "请选择要导入的表"),
    UserDeL(200010010, "该账号已被删除"),
    UserRepeat(200010011, "账号已存在"),
    PhoneRepeat(200010012, "手机号已存在"),
    EmailRepeat(200010013, "邮箱已存在"),
    AdminNo(200010014, "不允许操作超级管理员用户"),
    OldPasswordError(200010015, "旧密码错误"),
    newPasswordError(200010016, "新密码不能与旧密码相同"),
    UploadImages(200010017, "上传异常，请联系管理员"),
    MenuRepeat(200010018, "菜单名称已存在"),
    YesFrame(200010019, "新增外链地址失败，地址必须以http(s)://"),
    MenuError(200010020, "上级菜单不能选择自己"),
    MenuSubRepeat(200010021, "存在子菜单,不允许删除"),
    MenuIsUsing(200010022, "菜单已分配,不允许删除"),
    DictTypeRepeat(200010023, "字典类型已存在"),
    RoleRepeat(200010024, "角色已存在"),
    RoleLimitRepeat(200010025, "角色权限已存在"),
    Common(200010026, "通用错误"),
    DeptRepeat(200010027, "部门名称重复"),
    DeptStop(200010028, "部门停用，不能新增"),
    DeptMy(200010029, "上级部门不能是自己"),
    DeptSubNo(200010030, "该部门包含未停用的子部门"),
    DeptNoDel(200010031, "存在下级部门,不允许删除"),
    DeptUserDel(200010032, "部门存在用户,不允许删除"),
    PostRepeat(200010033, "岗位名称已存在"),
    PostCodeRepeat(200010034, "岗位编码已存在"),
    ConfigKeyRepeat(200010035, "参数名称已存在"),
    KindRepeat(200010036, "商品类型名称重复"),
    KindMy(200010037, "上级商品类型不能是自己"),
    KindNoDel(200010038, "存在下级类型,不允许删除"),
    KindSpuDel(200010039, "商品类型存在商品,不允许删除"),
    FormulaCodeRepeat(200010040, "公式编码已存在"),
    FormulaNameRepeat(200010041, "公式名称已存在"),
    OrderNo(200010042, "订单不存在"),
    EditNo(200010043, "工序进行中，不允许修改"),
    PayNo(200010044, "账单不存在"),
    DateRepeat(200010045, "该日期已存在"),
    PurchaseNo(200010046, "采购不存在"),
    PurchaseDetailNo(200010047, "采购详情不存在"),


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
