package com.yjy.swagger;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 描   述: Swagger在SpringBoot下的基础配置
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {
    @Autowired
    SwaggerInfo swaggerInfo;

    @Bean
    public Docket addUserDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(null==swaggerInfo?"com":swaggerInfo.getBasePackage()))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(Lists.newArrayList(apiKey()));
    }

    /**
     * apiInfo
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(null==swaggerInfo?"鄢家银-用户工具微服务":swaggerInfo.getApiInfo().getTitle())
                .description(null==swaggerInfo?"用于用户的新增，用户的增量匹配":swaggerInfo.getApiInfo().getDescription())
                .version(null==swaggerInfo?"1.0.0":swaggerInfo.getApiInfo().getVersion())
                .termsOfServiceUrl("")
                .contact(null==swaggerInfo?"联系人":swaggerInfo.getApiInfo().getContact())
                .licenseUrl(null==swaggerInfo?"":swaggerInfo.getApiInfo().getUrl())
                .license(null==swaggerInfo?"":swaggerInfo.getApiInfo().getEmail())
                .build();
    }

    /**
     * apiKey
     */
    private ApiKey apiKey() {
        return new ApiKey("Authorization", "Authorization", "header");
    }
}
