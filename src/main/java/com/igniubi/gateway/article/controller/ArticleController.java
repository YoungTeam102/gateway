package com.igniubi.gateway.article.controller;

import com.igniubi.gateway.common.ServerConstant;
import com.igniubi.model.CommonRsp;
import com.igniubi.model.article.req.ArticleReq;
import com.igniubi.rest.client.AsyncResult;
import com.igniubi.rest.client.RestClientCaller;
import com.igniubi.rest.client.RestServiceCaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class ArticleController {

    private final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    RestServiceCaller serviceCaller;


    @Autowired
    RestClientCaller clientCaller;

    private static String INDEX_URL = "/getIndex";


    @RequestMapping("/articleIndex")
    public CommonRsp articleIndex(@RequestParam(value = "date", required = false) String date){
        ArticleReq req = new ArticleReq();
        req.setDate(date);
        List list = serviceCaller.call(ServerConstant.ARTICLE, INDEX_URL, req, List.class);
        return new CommonRsp.CommonrspBuilder().data(list).build();
    }

    @RequestMapping("/articleIndexW")
    public CommonRsp articleIndexW(@RequestParam(value = "date", required = false) String date){
        ArticleReq req = new ArticleReq();
        req.setDate(date);
        List list = clientCaller.call(ServerConstant.ARTICLE, INDEX_URL, req, List.class);
        return new CommonRsp.CommonrspBuilder().data(list).build();
    }

    @RequestMapping("/articleIndexWa")
    public CommonRsp articleIndexWa(@RequestParam(value = "date", required = false) String date){
        ArticleReq req = new ArticleReq();
        req.setDate(date);
        long start = System.currentTimeMillis();
        AsyncResult<List> list = clientCaller.asyncCall(ServerConstant.ARTICLE, INDEX_URL, req, List.class);
        logger.info("call list success"+"   "+ (System.currentTimeMillis()-start));
        List result = list.get();
        logger.info("get list success"+"   "+ (System.currentTimeMillis()-start));
        return new CommonRsp.CommonrspBuilder().data(result).build();
    }

}
