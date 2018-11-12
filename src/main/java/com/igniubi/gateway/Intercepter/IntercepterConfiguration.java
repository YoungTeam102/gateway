package com.igniubi.gateway.Intercepter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class IntercepterConfiguration extends WebMvcConfigurationSupport {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(accessIntercepter()).addPathPatterns("/**");

        super.addInterceptors(registry);

    }


    @Bean
    public AuthIntercepter accessIntercepter(){
        return  new AuthIntercepter();
    }
}