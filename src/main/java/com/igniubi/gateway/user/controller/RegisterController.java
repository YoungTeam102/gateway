package com.igniubi.gateway.user.controller;

import com.igniubi.gateway.common.ServerConstant;
import com.igniubi.model.CommonRsp;
import com.igniubi.model.user.request.RegisterReqBO;
import com.igniubi.model.user.request.UserProfileReqBO;
import com.igniubi.rest.client.AsyncFuture;
import com.igniubi.rest.client.RestServiceCaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class RegisterController {

    private final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @Autowired
    RestServiceCaller serviceCaller;

    private static String REGISTER_URL = "registerRest/register";

    private static String TEST_URL = "registerRest/asynTest";

    private static String USER_PROFILE_URL = "userProfileRest/getUserProfile";

    @RequestMapping("/register")
    @ResponseBody
    public CommonRsp register(@RequestParam("mobile") String mobile,
                           @RequestParam("password") String password){
        RegisterReqBO req = new RegisterReqBO();
        req.setMobile(mobile);
        req.setPassword(password);
        CommonRsp result = serviceCaller.post(ServerConstant.USER,  REGISTER_URL,req,CommonRsp.class);
        return result;
    }

    @RequestMapping("/asynTest")
    public CommonRsp asynTest(@RequestParam("mobile") String mobile,
                                           @RequestParam("password") String password){
        RegisterReqBO req = new RegisterReqBO();
        req.setMobile(mobile);
        req.setPassword(password);
        long time = System.currentTimeMillis();
        logger.info("begin asyncall test,  time is {}",time);
        AsyncFuture<CommonRsp> result = serviceCaller.asyncCall(ServerConstant.USER,  TEST_URL,req,CommonRsp.class);
        logger.info("end asyncall test,  usedtime is {}",  System.currentTimeMillis()-time);
        CommonRsp rsp = result.get();
        logger.info("return success, rsp is {}", rsp);
        return rsp;
    }

    @RequestMapping("/test")
    public CommonRsp test(@RequestParam("mobile") String mobile,
                              @RequestParam("password") String password){
        RegisterReqBO req = new RegisterReqBO();
        req.setMobile(mobile);
        req.setPassword(password);
        long time = System.currentTimeMillis();
        logger.info("begin call test,  time is {}", time);
        CommonRsp result = serviceCaller.call(ServerConstant.USER,  TEST_URL,req,CommonRsp.class);
        logger.info("end call test,  usedtime is {}",  System.currentTimeMillis()-time);
        return result;
    }

    @RequestMapping("/userProfile")
    public CommonRsp userProfile(@RequestParam("uid") Integer uid){
        UserProfileReqBO req = new UserProfileReqBO();
        req.setUid(uid);
        logger.info("begin call userProfile,  uid is {}", uid);
        CommonRsp result = serviceCaller.call(ServerConstant.USER,  USER_PROFILE_URL,req,CommonRsp.class);
        logger.info("end call test,  rsp is {}", result);
        return result;
    }
}
