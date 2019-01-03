package com.igniubi.gateway.Intercepter;

import com.igniubi.common.exceptions.IGNBException;
import com.igniubi.gateway.common.ServerConstant;
import com.igniubi.model.enums.common.ResultEnum;
import com.igniubi.model.user.req.SessionReqBO;
import com.igniubi.redis.operations.RedisValueOperations;
import com.igniubi.redis.util.RedisKeyBuilder;
import com.igniubi.rest.client.RestServiceCaller;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
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
    RedisValueOperations redisValueOperations;

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
        Cookie[] cookies = request.getCookies();
        String sessionKey = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                //解析sessionid
                if ("JSESSIONID".equals(cookie.getName())) {
                    sessionKey = cookie.getValue();
                    break;
                }
            }
        }

        String uid = params.get("uid");
        if(uid ==null){
            return true;
        }
        if(StringUtils.isEmpty(sessionKey)){
            logger.info("AuthIntercepter user session failed , sessionkey in cookies is null");
            throw new IGNBException(ResultEnum.SESSION_FAIL);
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
        String uidinfo = redisValueOperations.get(builder, String.class);
        logger.info("AuthIntercepter valiteSession, sessionkey is {}, uid is {}", sessionKey, uidinfo);
        if(uidinfo == null){
            SessionReqBO reqBO = new SessionReqBO();
            reqBO.setSessionKey(sessionKey);
            uidinfo = serviceCaller.call(ServerConstant.USER,"session/getUid",reqBO,String.class);
            logger.info("AuthIntercepter getuid from user, sessionkey is {}, uidinfo is {}", sessionKey, uidinfo);
        }
        if(!uid.equals(uidinfo)){
            logger.info("AuthIntercepter user session failed , sessionkey is {}, uid is {}, uidinfo is{} ", sessionKey, uid, uidinfo);
            throw new IGNBException(ResultEnum.SESSION_FAIL);
        }
        return true;
    }
}
