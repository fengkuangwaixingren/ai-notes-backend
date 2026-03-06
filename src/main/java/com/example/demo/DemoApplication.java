package com.example.demo;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

//@SpringBootApplication
//@EnableCaching
//@EnableAsync
//public class DemoApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(DemoApplication.class, args);
//    }
//}
@SpringBootApplication
@EnableCaching
@EnableAsync
public class DemoApplication {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @PostConstruct
    public void printApiKey() {
        System.out.println("========== API Key 检查 ==========");
        System.out.println("读取到的 API Key: " + apiKey);
        System.out.println("长度: " + (apiKey == null ? 0 : apiKey.length()));
        System.out.println("==================================");
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
