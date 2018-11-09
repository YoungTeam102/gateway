package com.igniubi.gateway.Intercepter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IntercepterConfiguration {

    @Bean
    public AuthIntercepter AuthIntercepter(){
        return new AuthIntercepter();
    }
}
