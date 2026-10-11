package com.rate.limit.config;

import com.rate.limit.interceptors.RateInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RateConfig implements WebMvcConfigurer {

    private final  RateInterceptor rateInterceptor;
    public RateConfig(RateInterceptor rateInterceptor){
        this.rateInterceptor=rateInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(rateInterceptor).addPathPatterns("/api/**");
    }
}
