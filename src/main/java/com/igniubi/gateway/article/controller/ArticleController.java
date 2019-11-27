package com.igniubi.gateway.article.controller;

import com.igniubi.gateway.common.ServerConstant;
import com.igniubi.model.CommonRsp;
import com.igniubi.model.article.req.ArticleReq;
import com.igniubi.rest.client.AsyncResult;
import com.igniubi.rest.client.RestClientCaller;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.CannedAccessControlList;
import com.qcloud.cos.model.CreateBucketRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;



@RestController
public class ArticleController {

    private final Logger logger = LoggerFactory.getLogger(ArticleController.class);


    @Autowired
    RestClientCaller clientCaller;

    private static String INDEX_URL = "getIndex";


//    @HystrixCommand(commandProperties = {
//            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000"),
//            @HystrixProperty(name = "execution.timeout.enabled", value = "true")},fallbackMethod = "hiError")
    @RequestMapping("/articleIndex")
    public CommonRsp articleIndex(@RequestParam(value = "date", required = false) String date){
        ArticleReq req = new ArticleReq();
        req.setDate(date);
        long start = System.currentTimeMillis();
        List list = clientCaller.call(ServerConstant.ARTICLE, INDEX_URL, req, List.class);
        long end = System.currentTimeMillis();
        logger.info("call article index use time is {}", end-start);
        return new CommonRsp.CommonrspBuilder().data(list).build();
    }

    @RequestMapping("/articleIndexT")
    public CommonRsp articleIndexT(@RequestParam(value = "date", required = false) String date){
        ArticleReq req = new ArticleReq();
        req.setDate(date);
        long start = System.currentTimeMillis();
        List list = clientCaller.call(ServerConstant.ARTICLE, INDEX_URL, req, List.class, 60);
        long end = System.currentTimeMillis();
        logger.info("call article index use time is {}", end-start);
        return new CommonRsp.CommonrspBuilder().data(list).build();
    }

        @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "2000"),
            @HystrixProperty(name = "execution.timeout.enabled", value = "true")},fallbackMethod = "hiError")
    @RequestMapping("/articleIndexW")
    public CommonRsp articleIndexW(@RequestParam(value = "date", required = false) String date){
        ArticleReq req = new ArticleReq();
        req.setDate(date);
        long start = System.currentTimeMillis();
        List list = clientCaller.call(ServerConstant.ARTICLE, INDEX_URL, req, List.class);
        long end = System.currentTimeMillis();
        logger.info("call article index use time is {}", end-start);
        return new CommonRsp.CommonrspBuilder().data(list).build();
    }


    public CommonRsp hiError(String name) {
        return  new CommonRsp.CommonrspBuilder().code(500).message("系统繁忙，请稍后重试").build();
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

    @RequestMapping("/upload")
    @ResponseBody
    public CommonRsp grassFileUpload(@RequestParam("file") MultipartFile[] file
                                      )throws Exception{
        CommonRsp responseBean = new CommonRsp();
        String fileUrl = file[0].getOriginalFilename();
        String fileName = fileUrl.indexOf("/") > 0 ? fileUrl.substring(fileUrl.lastIndexOf("/")) : fileUrl ;
        fileName = DigestUtils.md5Hex(new Date().getTime() + fileName) +
                "." + fileName.substring(fileName.lastIndexOf(".") + 1);

        String tempDir = System.getProperty("java.io.tmpdir");
        String localAnimatedUrl = tempDir +File.separator+ fileName;

        File file1 = new File(localAnimatedUrl);
        file[0].transferTo(file1);
        String url = this.uploadToTencent(file1, fileName);
        responseBean.setCode(200);
        responseBean.setMessage("上传成功");
        responseBean.setData(url);
        return responseBean;
    }

    private String uploadToTencent(File file , String fileName) {
        // 1 初始化用户身份信息（secretId, secretKey）。
        COSCredentials cred = new BasicCOSCredentials("AKID94kANP7tHPIwLU46ZnlQq1xe8MPEmNaX", "sWu268vUotjHZcWGm46tSnsnUxBBwRhh");
        // 2 设置 bucket 的区域, COS 地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        // clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分。
        Region region = new Region("ap-shanghai");
        ClientConfig clientConfig = new ClientConfig(region);
        // 3 生成 cos 客户端。
        COSClient cosClient = new COSClient(cred, clientConfig);
        // 创建存储桶
        String bucketName = "01-1253461321"; //存储桶名称，格式：BucketName-APPID
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
        // 设置 bucket 的权限为 PublicRead(公有读私有写), 其他可选有私有读写, 公有读写
        createBucketRequest.setCannedAcl(CannedAccessControlList.PublicRead);
        try{
            if(!cosClient.doesBucketExist(bucketName)){
                cosClient.createBucket(createBucketRequest);
            }
            // 指定要上传的文件
//            File localFile = new File(fileUrl);
            // 指定要上传到的存储桶
            // 指定要上传到 COS 上对象键
            String key =  genFileName(fileName);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file);
            cosClient.putObject(putObjectRequest);
            return "https://01-1253461321.cos.ap-shanghai.myqcloud.com" + key;
        } catch (Exception e) {
            logger.error("downloadPicture error , the url is {} , the error is {}",fileName , e);
            throw e;
        } finally {
            cosClient.shutdown();
        }
    }

    private String genFileName(String oriName){
        String saveName = new SimpleDateFormat("/yyyy/MM/dd/HH/").format(new Date());
        return saveName+ oriName;
    }
}
