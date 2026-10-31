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

package top.continew.admin.common.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 缓存值 JSON 序列化往返测试
 *
 * <p>
 * 复刻 starter SpringCacheAutoConfiguration 的 L2 序列化器配置
 * （GenericJacksonJsonRedisSerializer + enableDefaultTyping），固化多级缓存命中路径的取值约定：
 * </p>
 * <ul>
 * <li>进入缓存的集合必须是可变集合（ArrayList/HashMap），不可变集合（List.of/Map.of/
 * Stream.toList 产物）序列化后不带 {@code @class} 类型标记，缓存命中反序列化必然失败；</li>
 * <li>int 取值范围内的 Long 反序列化退化为 Integer，长整型值（如各表主键）必须以 String
 * 形式缓存、读取处显式转回 Long。</li>
 * </ul>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class CacheSerializerRoundtripTest {

    private static final Long SNOWFLAKE_ID = 547889293968801822L;

    private static GenericJacksonJsonRedisSerializer serializer;

    @BeforeAll
    static void initSerializer() {
        // 与 starter SpringCacheAutoConfiguration#redisCacheConfiguration 保持同一构造方式
        JsonMapper mapper = JsonMapper.builder().build();
        serializer = GenericJacksonJsonRedisSerializer.builder(mapper::rebuild)
            .enableDefaultTyping(BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build())
            .build();
    }

    /**
     * 可变集合（ArrayList）携带响应对象往返后类型与取值保持
     */
    @Test
    void mutableListRoundtrip() {
        List<LabelValueResp<String>> list = new ArrayList<>();
        list.add(new LabelValueResp<>("k1", "v1"));
        list.add(new LabelValueResp<>("k2", null));

        Object result = serializer.deserialize(serializer.serialize(list));

        assertThat(result).isInstanceOf(ArrayList.class);
        List<?> resultList = (List<?>) result;
        assertThat(resultList).hasSize(2);
        LabelValueResp<?> first = (LabelValueResp<?>) resultList.get(0);
        LabelValueResp<?> second = (LabelValueResp<?>) resultList.get(1);
        assertThat(first.getLabel()).isEqualTo("k1");
        assertThat(first.getValue()).isEqualTo("v1");
        assertThat(second.getLabel()).isEqualTo("k2");
        assertThat(second.getValue()).isNull();
    }

    /**
     * 不可变集合序列化后不带 {@code @class} 类型标记，命中反序列化抛出 SerializationException
     *
     * <p>
     * 依据：List.of/Stream.toList 产物序列化为纯 JSON 数组 {@code ["x","y"]}，缺失类型标记后
     * 反序列化无法把元素解析为 java.lang.Object 的子类型。业务方法返回值进入缓存前必须转为可变集合。
     * </p>
     */
    @Test
    void immutableListRoundtripFails() {
        byte[] fromListOf = serializer.serialize(List.of("x", "y"));
        assertThat(new String(fromListOf, StandardCharsets.UTF_8)).doesNotContain("@class");
        assertThatThrownBy(() -> serializer.deserialize(fromListOf))
            .isInstanceOf(SerializationException.class);

        byte[] fromStreamToList = serializer.serialize(Stream.of("x", "y").toList());
        assertThatThrownBy(() -> serializer.deserialize(fromStreamToList))
            .isInstanceOf(SerializationException.class);
    }

    /**
     * int 取值范围内的 Long 反序列化退化为 Integer
     *
     * <p>
     * 依据：Long 标量以纯 JSON 数字写入，读取端按 int 范围就近还原。@Cacheable 方法直接返回
     * Long 时，命中路径会因类型退化抛出 ClassCastException，这正是租户 ID 缓存改用 String
     * 存取的原因。
     * </p>
     */
    @Test
    void longWithinIntRangeDegradesToInteger() {
        // 入参必须是 Long：Integer.MAX_VALUE 本身即 Integer，断言恒真，覆盖不到类型退化
        Object smallLong = serializer.deserialize(serializer.serialize(Long.valueOf(1L)));
        assertThat(smallLong).isInstanceOf(Integer.class);
        assertThat(smallLong).isEqualTo(1);

        Object maxLong = serializer.deserialize(serializer.serialize(Long
            .valueOf(Integer.MAX_VALUE)));
        assertThat(maxLong).isInstanceOf(Integer.class);
        assertThat(maxLong).isEqualTo(Integer.MAX_VALUE);

        // 命中路径按 Long 强转会失败，这正是租户 ID 缓存改用 String 存取的直接原因
        assertThatThrownBy(() -> {
            Long ignored = (Long) smallLong;
        }).isInstanceOf(ClassCastException.class);
    }

    /**
     * 雪花 ID（超出 int 范围）往返后类型保持 Long
     */
    @Test
    void snowflakeLongRoundtripKeepsLong() {
        Object result = serializer.deserialize(serializer.serialize(SNOWFLAKE_ID));
        assertThat(result).isInstanceOf(Long.class);
        assertThat(result).isEqualTo(SNOWFLAKE_ID);
    }

    /**
     * String 形式的长整型 ID 往返类型精确，转回 Long 后取值不变
     */
    @Test
    void stringRoundtripKeepsExactValue() {
        String cachedId = String.valueOf(SNOWFLAKE_ID);
        Object result = serializer.deserialize(serializer.serialize(cachedId));
        assertThat(result).isInstanceOf(String.class);
        assertThat(Long.valueOf((String) result)).isEqualTo(SNOWFLAKE_ID);
    }

    /**
     * 不可变 Map 序列化后缺失 {@code @class}，命中反序列化抛出 SerializationException；
     * HashMap（Collectors.toMap 产物）往返类型保持
     */
    @Test
    @SuppressWarnings("unchecked")
    void mapRoundtripDependsOnMutability() {
        byte[] fromMapOf = serializer.serialize(Map.of("a", "1"));
        assertThatThrownBy(() -> serializer.deserialize(fromMapOf))
            .isInstanceOf(SerializationException.class);

        Object result = serializer.deserialize(serializer.serialize(new HashMap<>(Map
            .of("a", "1", "b", "2"))));
        assertThat(result).isInstanceOf(HashMap.class);
        Map<String, String> resultMap = (Map<String, String>) result;
        assertThat(resultMap).containsEntry("a", "1").containsEntry("b", "2");
    }
}
