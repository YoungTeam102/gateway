package com.igniubi.gateway.user.controller;

import com.igniubi.gateway.common.ServerConstant;
import com.igniubi.model.CommonRsp;
import com.igniubi.model.user.request.RegisterReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class RegisterController {

    private final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @Autowired
    RestTemplate restTemplate;

    @RequestMapping("/register")
    @ResponseBody
    public CommonRsp register(@RequestParam("mobile") String mobile,
                           @RequestParam("password") String password){
        RegisterReq req = new RegisterReq();
        req.setMobile(mobile);
        req.setPassword(password);
        CommonRsp result = restTemplate.postForObject(ServerConstant.USER+ "user/registerRest/register",req,CommonRsp.class);
        return result;
    }
}
