package com.cb.gulimall.thirdpart;

import com.aliyun.oss.OSS;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class GulimallThirdPartApplicationTests {

    @Autowired
    private OSS ossClient;

    /**
     * 测试oss文件上传
     *
     * @throws FileNotFoundException
     */
    @Test
    public void testOss() throws FileNotFoundException {
        // 原生oss需要配置信息
//        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
//        String endpoint = "oss-cn-shanghai.aliyuncs.com";
//        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
//        String accessKeyId = "LTAI5tDR9sZfyHXsvtjMDB8N";
//        String accessKeySecret = "wTfzor3WQwICo9qQM2KDGTYSNVPNxH";
//
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);


        // alicloud oss 配置文件配置直接注入OSS使用
        // 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
        InputStream inputStream = new FileInputStream("C:\\Users\\Administrator\\Pictures\\12345.png");
        // 依次填写Bucket名称（例如examplebucket）和Object完整路径（例如exampledir/exampleobject.txt）。Object完整路径中不能包含Bucket名称。
        ossClient.putObject("gulimall-cb", "3.png", inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();

        System.out.println("oss文件上传成功。。。");
    }

    @Test
    void contextLoads() {
    }

}
