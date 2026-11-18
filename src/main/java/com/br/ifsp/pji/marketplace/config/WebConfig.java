package com.br.ifsp.pji.marketplace.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Centraliza regras MVC: protecao de rotas administrativas e acesso as imagens enviadas.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads/produtos}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new AcessoAdminConfig())
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + uploadDir.replace("\\", "/");
        if (!location.endsWith("/")) location += "/";
        registry.addResourceHandler("/imagens/produtos/**")
                .addResourceLocations(location);
    }
}