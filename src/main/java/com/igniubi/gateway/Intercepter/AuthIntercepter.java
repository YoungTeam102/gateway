package com.igniubi.gateway.Intercepter;

import com.igniubi.gateway.common.ServerConstant;
import com.igniubi.model.user.request.SessionReqBO;
import com.igniubi.redis.util.RedisKeyBuilder;
import com.igniubi.redis.util.RedisUtil;
import com.igniubi.rest.client.RestServiceCaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class AuthIntercepter implements HandlerInterceptor {

    private final Logger logger = LoggerFactory.getLogger(AuthIntercepter.class);

    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();

    private static final String SESSION_KEY_REDIS = "igniubi:sessionKey:";

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RestServiceCaller serviceCaller;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        startTime.set(System.currentTimeMillis());
        logger.info("accept requst :"+ request.getRequestURI());
        Map<String, String> params = getParam(request);
        if (params == null){
            return true;
        }
        String sessionKey = params.get("session");
        String uid = params.get("uid");
        if(sessionKey ==null || uid ==null){
            return true;
        }
        return valiteSession(sessionKey,uid);
    }
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        String httpMethod = request.getRequestURI();
        long useTime = System.currentTimeMillis()- startTime.get();
        logger.info("complete requst  "+ httpMethod +"  use time "+ useTime);
    }

    private  Map<String, String>  getParam(HttpServletRequest request){
        Enumeration paramNames = request.getParameterNames();
        Map<String, String> map =new HashMap<>();
        if(!paramNames.hasMoreElements()){
            return null;
        }
        while (paramNames.hasMoreElements()) {
            String key = (String) paramNames.nextElement();
            String value = request.getParameter(key);
            map.put(key, value);
        }
        return map;
    }

    private  boolean  valiteSession(String sessionKey, String uid){
        RedisKeyBuilder builder = RedisKeyBuilder.newInstance().appendFixed(SESSION_KEY_REDIS).appendVar(sessionKey);
        String uidinfo = redisUtil.get(builder, String.class);
        logger.info("AuthIntercepter valiteSession, sessionkey is {}, uid is {}", sessionKey, uidinfo);
        if(uidinfo == null){
            SessionReqBO reqBO = new SessionReqBO();
            reqBO.setSessionKey(sessionKey);
            uidinfo = serviceCaller.call(ServerConstant.USER,"session/getUid",reqBO,String.class);
            logger.info("AuthIntercepter getuid from user, sessionkey is {}, uid is {}", sessionKey, uidinfo);
        }
        return uid.equals(uidinfo);
    }
}
