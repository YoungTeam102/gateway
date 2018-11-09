package com.igniubi.gateway.Intercepter;

import com.igniubi.gateway.common.ServerConstant;
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
    RestServiceCaller caller;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        startTime.set(System.currentTimeMillis());
        logger.info("accept requst :"+ request.getRequestURI());
        Map<String, String> params = getParam(request);
        String sessionKey = params.get("session");
        String uid = params.get("uid");

        return true;
    }
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        String httpMethod = request.getRequestURI();
        String useTime = String.valueOf((System.currentTimeMillis()-Long.valueOf(startTime.get())));
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

    private  void  valiteSession(String sessonKey, String uid){
        RedisKeyBuilder builder = RedisKeyBuilder.newInstance().appendVar(sessonKey);
        String uidinfo = redisUtil.get(builder);

        if(uidinfo == null){
            uidinfo = caller.call(ServerConstant.USER,"session/getUid",null,String.class);
        }



    }
}
