package com.igniubi.gateway.user.controller;

import com.igniubi.gateway.common.ServerConstant;
import com.igniubi.model.CommonRsp;
import com.igniubi.model.user.request.RegisterReq;
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

    private static String REGISTER_URL = "user/registerRest/register";

    @RequestMapping("/register")
    @ResponseBody
    public CommonRsp register(@RequestParam("mobile") String mobile,
                           @RequestParam("password") String password){
        RegisterReq req = new RegisterReq();
        req.setMobile(mobile);
        req.setPassword(password);
        CommonRsp result = serviceCaller.call(ServerConstant.USER,  REGISTER_URL,req,CommonRsp.class);
        return result;
    }
}
