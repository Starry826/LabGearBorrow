package com.starry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LabGearBorrowApplication {

    static {
        // 强制设置 JVM 默认编码为 UTF-8
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");
        // 如果使用 Tomcat，设置 URI 编码
        System.setProperty("org.apache.catalina.connector.URI_ENCODING", "UTF-8");
        System.setProperty("org.apache.catalina.connector.USE_BODY_ENCODING_FOR_QUERY_STRING", "true");
    }

    public static void main(String[] args) {
        SpringApplication.run(LabGearBorrowApplication.class, args);
    }

}
