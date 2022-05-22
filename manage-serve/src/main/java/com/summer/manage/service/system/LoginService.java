package com.summer.manage.service.system;

import com.google.common.collect.ImmutableMap;
import com.summer.common.core.BaseService;
import com.summer.common.core.RpcException;
import com.summer.common.helper.BeanHelper;
import com.summer.common.helper.DateHelper;
import com.summer.common.helper.EncryptHelper;
import com.summer.common.helper.SnowIdHelper;
import com.summer.common.helper.StringHelper;
import com.summer.common.redis.RedisFactory;
import com.summer.common.redis.RedisOperations;
import com.summer.common.view.parser.RequestContext;
import com.summer.common.view.parser.RequestSession;
import com.summer.manage.config.IRedis;
import com.summer.manage.core.LoginCodeEnum;
import com.summer.manage.core.LoginCodeProperties;
import com.summer.manage.core.LoginUtil;
import com.summer.manage.core.RsaUtils;
import com.summer.manage.dao.system.SysUserDAO;
import com.summer.manage.dto.request.LoginRequest;
import com.summer.manage.dto.response.CaptchaResponse;
import com.summer.manage.dto.response.UserDTO;
import com.summer.manage.dto.response.UserInfoResponse;
import com.summer.manage.entity.system.SysUser;
import com.summer.manage.kern.CodeMSG;
import com.summer.manage.kern.IConstant;
import com.wf.captcha.base.Captcha;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Objects;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/21 8:02 下午
 **/
@Service
public class LoginService extends BaseService {

    private final LoginCodeProperties loginCodeProperties;
    private final SysUserDAO sysUserDAO;
    private final PermissionService permissionService;

    @Inject
    public LoginService(LoginCodeProperties loginCodeProperties,
                        SysUserDAO sysUserDAO,
                        PermissionService permissionService) {
        this.loginCodeProperties = loginCodeProperties;
        this.sysUserDAO = sysUserDAO;
        this.permissionService = permissionService;
    }

    private RedisOperations redisOperations() {
        return RedisFactory.get(IRedis.DEFAULT);
    }

    public CaptchaResponse getCode() {
        CaptchaResponse captchaResponse = new CaptchaResponse();
        // 获取运算的结果
        Captcha captcha = loginCodeProperties.getCaptcha();
        String uuid = SnowIdHelper.uuid();
        //当验证码类型为 arithmetic时且长度 >= 2 时，captcha.text()的结果有几率为浮点型
        String captchaValue = captcha.text();
        if (captcha.getCharType() - 1 == LoginCodeEnum.arithmetic.ordinal() && captchaValue.contains(".")) {
            captchaValue = captchaValue.split("\\.")[0];
        }
        // 保存
        redisOperations().put(IConstant.Redis.REDIS_LOGIN_CODE + uuid, captchaValue, loginCodeProperties.loginCode.expiration);
        // 验证码信息
        captchaResponse.img = captcha.toBase64();
        captchaResponse.uuid = uuid;
        return captchaResponse;
    }

    public ImmutableMap login(LoginRequest request) {
        // 密码解密
        String password = RsaUtils.decryptByPrivateKey(IConstant.Rsa.PRIVATE_KEY, request.userPassword);
        if (StringHelper.isBlank(password)) {
            throw new RpcException(CodeMSG.RsaError);
        }
        String cryptogram = LoginUtil.adminPwd(password);
        String verifyKey = IConstant.Redis.REDIS_LOGIN_CODE + request.uuid;
        //获取验证码
        String code = redisOperations().one(verifyKey, String.class);
        redisOperations().clear(verifyKey);
        if(StringHelper.isBlank(code)){
            throw new RpcException(CodeMSG.CodeNo);
        }
        if(StringHelper.isBlank(request.code) || !request.code.equalsIgnoreCase(code)){
            throw new RpcException(CodeMSG.CodeError);
        }
        // 用户验证
        SysUser sysUser = sysUserDAO.getUserByUserName(request.userName);
        checkUser(sysUser);
        if (cryptogram.equals(sysUser.userPassword)) {
            long expire = DateHelper.time() + DateHelper.DAY_TIME;
            String token = EncryptHelper.signatureJWT(sysUser.nickName,
                                                      sysUser.userName, "", expire);
            redisOperations().put(IConstant.Redis.REDIS_LOGIN_USER + token, sysUser.getId(), (int) expire);
            return ImmutableMap.of("token", token);
        } else {
            throw new RpcException(CodeMSG.PASSWORD_ERROR);
        }
    }

    public SysUser getUserById(Long id) {
        SysUser sysUser = sysUserDAO.getSysUserById(id);
        checkUser(sysUser);
        return sysUser;
    }

    private void checkUser(SysUser sysUser) {
        if (Objects.isNull(sysUser)) {
            throw new RpcException(CodeMSG.USERNAME_NOT_FIND);
        }
        if (IConstant.UserStatus.DISABLE.getCode().equals(sysUser.status)) {
            throw new RpcException(CodeMSG.UserStatusError);
        }
        if (IConstant.UserStatus.DELETED.getCode().equals(sysUser.isDel)) {
            throw new RpcException(CodeMSG.UserDeL);
        }
    }

    public UserInfoResponse getUserInfo() {
        RequestSession session = RequestContext.get().getSession();
        UserInfoResponse userInfoResponse = new UserInfoResponse();
        Long userId = Long.parseLong(session.uid);
        SysUser sysUser = sysUserDAO.getSysUserById(userId);
        userInfoResponse.user = BeanHelper.castTo(sysUser, UserDTO.class);
        //查询权限
        userInfoResponse.permissions = permissionService.getMenuPermission(sysUser);
        //查询角色
        userInfoResponse.roles = permissionService.getRolePermission(sysUser);
        return userInfoResponse;
    }


    public Integer logout() {
        String signature = RequestContext.get().getSession().signature;
        return redisOperations().clear(IConstant.Redis.REDIS_LOGIN_USER + signature);
    }
}
