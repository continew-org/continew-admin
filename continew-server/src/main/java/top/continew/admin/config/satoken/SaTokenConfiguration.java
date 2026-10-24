/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.continew.admin.config.satoken;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.sign.SaSignManager;
import cn.dev33.satoken.sign.template.SaSignTemplate;
import cn.dev33.satoken.sign.template.SaSignUtil;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationUtils;
import top.continew.admin.auth.service.SessionActivityService;
import top.continew.admin.auth.support.ClientPolicyCache;
import top.continew.admin.common.config.crud.CrudApiPermissionPrefixCache;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.common.exception.PasswordExpiredException;
import top.continew.admin.open.sign.OpenApiSignTemplate;
import top.continew.starter.auth.refresh.token.api.AccessSessionValidator;
import top.continew.starter.auth.satoken.autoconfigure.SaTokenExtensionProperties;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Sa-Token 配置
 *
 * @author Charles7c
 * @author chengzi
 * @since 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SaTokenConfiguration {

    private final SaTokenExtensionProperties properties;
    private final LoginPasswordProperties loginPasswordProperties;
    private final OpenApiSignTemplate signTemplate;
    private final ApplicationContext applicationContext;
    private final AccessSessionValidator accessSessionValidator;
    private final SessionActivityService sessionActivityService;
    private final ClientPolicyCache clientPolicyCache;

    /**
     * Sa-Token 权限认证配置
     */
    @Bean
    public StpInterface stpInterface() {
        return new SaTokenPermissionImpl();
    }

    /**
     * SaToken 拦截器配置
     */
    @Bean
    public SaInterceptor saInterceptor() {
        SaSignManager.setSaSignTemplate(signTemplate);
        return new SaExtensionInterceptor(handle -> SaRouter.match(StringConstants.PATH_PATTERN)
            .notMatch(properties.getSecurity().getExcludes())
            .check(r -> {
                // 如果包含 sign，进行 API 接口参数签名验证
                SaRequest saRequest = SaHolder.getRequest();
                Collection<String> paramNames = saRequest.getParamNames();
                if (paramNames.stream().anyMatch(SaSignTemplate.sign::equals)) {
                    try {
                        SaSignUtil.checkRequest(saRequest);
                    } catch (Exception e) {
                        throw new BusinessException(e.getMessage());
                    }
                    return;
                }
                // 不包含 sign 参数，进行普通登录验证
                StpUtil.checkLogin();
                if (SaRouter.isMatchCurrURI(loginPasswordProperties.getExcludes())) {
                    return;
                }
                UserContext userContext = UserContextHolder.getContext();
                if (userContext.isPasswordExpired()) {
                    rejectExpiredPassword();
                }
            }), accessSessionValidator, sessionActivityService, clientPolicyCache,
            loginPasswordProperties);
    }

    /**
     * 密码过期时清理线程本地上下文并中断当前请求。
     *
     * <p>{@code preHandle} 抛出异常时 Spring 不会回调该拦截器的 {@code afterCompletion}
     * （只对已通过 preHandle 的拦截器回调），若不在此清除，上一请求写入的上下文会残留在 Tomcat
     * 池化线程上被下一请求复用，造成跨用户上下文串用。</p>
     *
     * @throws PasswordExpiredException 始终抛出，用于中断请求并让前端跳转改密页
     */
    static void rejectExpiredPassword() {
        UserContextHolder.clearContext();
        throw new PasswordExpiredException();
    }

    /**
     * 配置 sa-token {@link SaIgnore} 注解排除路径
     * <p>主要针对 @CrudRequestMapping 注解</p>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void configureSaTokenExcludes() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        List<String> additionalExcludes = Arrays.stream(beanNames).map(beanName -> {
            Object bean = applicationContext.getBean(beanName);
            Class<?> clazz = bean.getClass();
            if (AopUtils.isAopProxy(bean)) {
                clazz = AopProxyUtils.ultimateTargetClass(bean);
            }
            CrudRequestMapping crudRequestMapping =
                AnnotationUtils.findAnnotation(clazz, CrudRequestMapping.class);
            SaIgnore saIgnore = AnnotationUtils.findAnnotation(clazz, SaIgnore.class);
            if (crudRequestMapping != null) {
                // 缓存权限前缀
                CrudApiPermissionPrefixCache.put(clazz, crudRequestMapping.value());
                // 使用 @CrudRequestMapping 的 Controller，如果使用了 @SaIgnore 注解，则表示忽略认证和权限校验
                if (saIgnore != null) {
                    return crudRequestMapping.value() + StringConstants.PATH_PATTERN;
                }
            }
            return null;
        }).filter(Objects::nonNull).toList();
        if (!additionalExcludes.isEmpty()) {
            // 合并现有的 excludes 和新扫描到的
            String[] existingExcludes = Optional.ofNullable(properties.getSecurity().getExcludes())
                .orElse(new String[0]);
            String[] combinedExcludes =
                Stream.concat(Arrays.stream(existingExcludes), additionalExcludes.stream())
                    .toArray(String[]::new);
            properties.getSecurity().setExcludes(combinedExcludes);
        }
        log.debug("缓存 CRUD API 权限前缀完成：{}", CrudApiPermissionPrefixCache.getAll().values());
    }
}
