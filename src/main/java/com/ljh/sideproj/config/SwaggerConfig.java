package com.ljh.sideproj.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KAMCO 입찰 정보 시스템 API")
                        .description("한국자산관리공사 입찰 정보 조회 및 관리 시스템")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("이정현")
                                .email("dev_ljh@naver.com")))
                .servers(List.of(
                        new Server().url("http://localhost:9090").description("개발 서버")
                ));
    }
}