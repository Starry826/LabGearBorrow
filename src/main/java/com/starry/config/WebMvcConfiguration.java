package com.starry.config;

import com.starry.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login/*", "/email/*");
    }

    // 强制 JSON 消息转换器使用 UTF-8
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 方法1：修改已有的 MappingJackson2HttpMessageConverter
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;
                jsonConverter.setDefaultCharset(StandardCharsets.UTF_8);
                break;
            }
        }
        // 方法2：添加一个新的并置顶（确保优先级最高）
        MappingJackson2HttpMessageConverter utf8Converter = new MappingJackson2HttpMessageConverter();
        utf8Converter.setDefaultCharset(StandardCharsets.UTF_8);
        converters.add(0, utf8Converter);
    }
}