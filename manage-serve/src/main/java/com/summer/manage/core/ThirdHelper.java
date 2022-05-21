package com.summer.manage.core;

import com.alibaba.fastjson.JSONObject;
import com.summer.common.core.RemoteReply;
import com.summer.common.helper.HttpHelper;
import com.summer.manage.kern.IConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/23 11:45 上午
 **/
public class ThirdHelper {

    protected static final Logger LOG = LoggerFactory.getLogger(ThirdHelper.class);

    private static final Integer TIME_OUT = 10;

    public static JSONObject weChatAuth(String code,String appId,String secret){
        LOG.info("the wechat code is {}",code);
        try {
            RemoteReply<JSONObject> get = HttpHelper.get(IConstant.Wechat.getOpenidUrl(appId,secret,code), null, JSONObject.class, TIME_OUT);
            if(get.success()){
                return get.body();
            }else {
                LOG.error("weChatAuth serve error: {} ",get.icm().message());
            }
        }catch (Exception e){
            LOG.error("weChatAuth interface error {}",e.getMessage());
        }
        return new JSONObject();
    }

    public static JSONObject getUserInfoUrl(String accessToken,String openid){
        try {
            RemoteReply<JSONObject> get = HttpHelper.get(IConstant.Wechat.getUserInfoUrl(accessToken,openid), null, JSONObject.class, TIME_OUT);
            if(get.success()){
                return get.body();
            }else {
                LOG.error("getUserInfoUrl serve error: {} ",get.icm().message());
            }
        }catch (Exception e){
            LOG.error("getUserInfoUrl interface error {}",e.getMessage());
        }
        return new JSONObject();
    }
}
