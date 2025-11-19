package com.ljh.sideproj.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.ljh.sideproj.mapper")
public class MyBatisConfig {
}
