package com.escaplix.backend.common.config;

import com.escaplix.backend.common.auth.AuthInterceptor;
import com.escaplix.backend.common.auth.LoginMemberArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final LoginMemberArgumentResolver loginMemberArgumentResolver;

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${app.upload.public-dir}")
    private String publicDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);   // Refresh Token 쿠키 주고받기 (origin 을 * 로 두면 안 됨, 위 목록만)
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

    /**
     * 공개 사진만 /uploads/** 로 내려 준다. 사업자등록증·사업장 증빙 서류(private 폴더)은 절대 여기 넣지 않는다
     *   /uploads/posters/{파일}  테마 사진
     *   /uploads/stores/{파일}   매장 사진
     * 아래 순서로 찾는다
     *   1) {app.upload.public-dir}/     사장·관리자가 올린 사진 (맨 위 uploads/ 폴더, git 에 안 올림)
     *   2) classpath:/uploads/          더미 데이터 사진 (src/main/resources/uploads/, git 에 올림)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        for (String folder : new String[] {"posters", "stores"}) {
            String location = Path.of(publicDir, folder).toAbsolutePath().toUri().toString();
            if (!location.endsWith("/")) {
                location += "/";
            }
            // 사진은 7일 동안 브라우저에 저장해 둠 → 다시 열 때 바로 뜸
            //   (올린 사진은 UUID 이름이라 바꾸면 주소도 바뀌어서 예전 사진이 남지 않음)
            registry.addResourceHandler("/uploads/" + folder + "/**")
                    .addResourceLocations(location, "classpath:/uploads/" + folder + "/")
                    .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
        }
    }
}
