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

package top.continew.admin.job.api;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.RegisteredPayload;
import com.aizuda.snailjob.common.core.model.Result;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import top.continew.admin.job.constant.JobConstants;
import top.continew.admin.job.model.JobPageResult;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 任务调度客户端
 *
 * @author Charles7c
 * @since 2024/7/4 23:07
 */
@Slf4j
@Data
public class JobClient {

    public static final Integer STATUS_SUCCESS = 1;
    private static final String AUTH_URL = "/auth/login";
    private final String url;
    private final String username;
    private final String password;

    public JobClient(String url, String username, String password) {
        Assert.notBlank(url, "任务调度中心 URL 不能为空");
        Assert.notBlank(username, "任务调度中心用户名不能为空");
        Assert.notBlank(password, "任务调度中心密码不能为空");
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * 请求
     *
     * @param apiSupplier API 请求
     * @param <T>         响应类型
     * @return 响应信息
     */
    public <T> T request(Supplier<ResponseEntity<Result<T>>> apiSupplier) {
        ResponseEntity<Result<T>> responseEntity = apiSupplier.get();
        this.checkResponse(responseEntity);
        Result<T> result = responseEntity.getBody();
        if (!STATUS_SUCCESS.equals(result.getStatus())) {
            throw new IllegalStateException(result.getMessage());
        }
        return result.getData();
    }

    /**
     * 分页请求
     *
     * @param apiSupplier API 请求
     * @param <T>         响应类型
     * @return 分页列表信息
     */
    public <T> PageResp<T> requestPage(Supplier<ResponseEntity<JobPageResult<List<T>>>> apiSupplier) {
        ResponseEntity<JobPageResult<List<T>>> responseEntity = apiSupplier.get();
        this.checkResponse(responseEntity);
        JobPageResult<List<T>> result = responseEntity.getBody();
        if (!STATUS_SUCCESS.equals(result.getStatus())) {
            throw new IllegalStateException(result.getMessage());
        }
        PageResp<T> page = new PageResp<>();
        page.setList(result.getData());
        page.setTotal(result.getTotal());
        return page;
    }

    /**
     * 获取 Token
     *
     * @return Token
     */
    public String getToken() {
        String token = RedisUtils.get(JobConstants.AUTH_TOKEN_HEADER);
        if (StrUtil.isBlank(token)) {
            token = this.authenticate();
            Object expiresAtSeconds = JWTUtil.parseToken(token).getPayload(RegisteredPayload.EXPIRES_AT);
            RedisUtils.set(JobConstants.AUTH_TOKEN_HEADER, token, Duration.ofSeconds(Convert
                .toLong(expiresAtSeconds) - DateUtil.currentSeconds() - 60));
        }
        return token;
    }

    /**
     * 密码认证
     *
     * @return Token
     */
    private String authenticate() {
        Map<String, Object> paramMap = MapUtil.newHashMap(2);
        paramMap.put("username", username);
        paramMap.put("password", SecureUtil.md5(password));
        HttpRequest httpRequest = HttpUtil.createPost("%s%s".formatted(url, AUTH_URL));
        httpRequest.body(JSONUtil.toJsonStr(paramMap));
        HttpResponse response = httpRequest.execute();
        if (!response.isOk() || response.body() == null) {
            throw new IllegalStateException("连接任务调度中心异常");
        }
        Result<?> result = JSONUtil.toBean(response.body(), Result.class);
        if (!STATUS_SUCCESS.equals(result.getStatus())) {
            log.warn("Password Authentication failed, expected a successful response. error msg: {}", result
                .getMessage());
            throw new IllegalStateException(result.getMessage());
        }
        return JSONUtil.parseObj(result.getData()).getStr("token");
    }

    /**
     * 检查响应
     *
     * @param responseEntity 响应信息
     */
    private void checkResponse(ResponseEntity<?> responseEntity) {
        if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            throw new IllegalStateException("连接任务调度中心异常");
        }
    }
}
