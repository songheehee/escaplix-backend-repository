package com.escaplix.backend.common.auth;

import com.escaplix.backend.entity.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 로그인이 필요한 API 에 붙인다. 없으면 "공개".
 * <pre>
 * &#64;Auth                              로그인한 누구나
 * &#64;Auth(Role.USER)                   손님만
 * &#64;Auth({Role.OWNER, Role.ADMIN})    사장 + 관리자 직접 등록 (API.md 의 OWNER*)
 * </pre>
 * 토큰이 없거나 틀리면 401, 역할이 안 맞으면 403. "내 매장인지" 같은 주인 확인은 서비스에서 따로 한다.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Auth {
    Role[] value() default {};
}
