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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import top.continew.starter.cache.springcache.autoconfigure.SpringCacheAutoConfiguration;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;
import top.continew.starter.json.jackson.serializer.BigNumberSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 缓存值 JSON 序列化往返测试
 *
 * <p>
 * 直接调用 starter SpringCacheAutoConfiguration 的生产 Bean 构造（含 RootTypedWriter），
 * 避免手工复刻序列化器配置与生产漂移，固化多级缓存 L2 命中路径的取值约定：
 * </p>
 * <ul>
 * <li>写侧固定以 Object 为声明类型，根值携带多态类型标记（WRAPPER_ARRAY 形态），
 * 与读取侧对称；不可变集合（List.of/Stream.toList 产物）与不可变 Map 均可正确往返，
 * 业务元素类型经类型标记恢复；</li>
 * <li>Long 根值往返类型保持，int 取值范围内不再退化为 Integer；</li>
 * <li>生产启用大数值序列化模式 FLEXIBLE（spring.jackson.big-number-serialize-mode）时，
 * 超出 JS 安全范围的 Long 以字符串形态写入，读侧仍恢复为 Long，嵌套元素同样保持。</li>
 * </ul>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class CacheSerializerRoundtripTest {

    private static final Long SNOWFLAKE_ID = 547889293968801822L;

    private static RedisSerializationContext.SerializationPair<Object> serializerPair;

    private static RedisSerializationContext.SerializationPair<Object> flexiblePair;

    @BeforeAll
    static void initSerializer() {
        // 默认 mapper 对应未启用大数值序列化模式的生产配置
        serializerPair = newPair(JsonMapper.builder().build());
        // FLEXIBLE 模式复刻 starter JacksonAutoConfiguration#bigNumberModule 的对应分支，
        // 构造与生产配置 spring.jackson.big-number-serialize-mode=FLEXIBLE 一致的容器 mapper
        SimpleModule bigNumberModule = new SimpleModule();
        bigNumberModule.addSerializer(Long.class, BigNumberSerializer.SERIALIZER_INSTANCE);
        bigNumberModule.addSerializer(Long.TYPE, BigNumberSerializer.SERIALIZER_INSTANCE);
        flexiblePair = newPair(JsonMapper.builder().addModule(bigNumberModule).build());
    }

    /**
     * 以 starter 生产 Bean 构造 L2 值序列化对
     *
     * @param mapper 容器 mapper
     * @return 值序列化对
     */
    private static RedisSerializationContext.SerializationPair<Object> newPair(JsonMapper mapper) {
        return new SpringCacheAutoConfiguration(mapper)
            .redisCacheConfiguration(new CacheProperties())
            .getValueSerializationPair();
    }

    /**
     * 可变集合（ArrayList）携带响应对象往返后类型与取值保持
     */
    @Test
    void mutableListRoundtrip() {
        List<LabelValueResp<String>> list = new ArrayList<>();
        list.add(new LabelValueResp<>("k1", "v1"));
        list.add(new LabelValueResp<>("k2", null));

        Object result = serializerPair.read(serializerPair.write(list));

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
     * 不可变集合（List.of/Stream.toList 产物）携带业务对象往返后元素类型与取值恢复
     *
     * <p>
     * 依据：写侧以 Object 声明（RootTypedWriter），根值与嵌套元素携带多态类型标记，
     * List.of/Stream.toList 产物不再缺失类型信息，缓存命中可正确反序列化。
    * 读侧对集合统一以可变实现承载（JDK 不可变集合无反序列化构造器），断言聚焦元素类型
    * 恢复而非集合实现；元素强转失败即代表类型标记丢失。
     * </p>
     */
    @Test
    void immutableListRoundtripKeepsElementType() {
        Object fromListOf = serializerPair.read(serializerPair.write(List
            .of(new LabelValueResp<>("k1", "v1"), new LabelValueResp<>("k2", null))));
        List<?> fromList = assertLabelValueList(fromListOf);
        assertThat(fromList).hasSize(2);

        Object fromStreamToList = serializerPair.read(serializerPair.write(Stream
            .of(new LabelValueResp<>("k1", "v1"), new LabelValueResp<>("k2", null))
            .toList()));
        List<?> fromStream = assertLabelValueList(fromStreamToList);
        assertThat(fromStream).hasSize(2);
    }

    /**
     * 断言往返结果为业务对象列表并返回，元素强转失败即类型标记丢失
     *
     * @param value 往返结果
     * @return 业务对象列表
     */
    private static List<?> assertLabelValueList(Object value) {
        assertThat(value).isInstanceOf(List.class);
        List<?> list = (List<?>) value;
        LabelValueResp<?> first = (LabelValueResp<?>) list.get(0);
        LabelValueResp<?> second = (LabelValueResp<?>) list.get(1);
        assertThat(first.getLabel()).isEqualTo("k1");
        assertThat(first.getValue()).isEqualTo("v1");
        assertThat(second.getLabel()).isEqualTo("k2");
        assertThat(second.getValue()).isNull();
        return list;
    }

    /**
     * int 取值范围内的 Long 根值往返类型保持，不再退化为 Integer
     */
    @Test
    void longWithinIntRangeKeepsLong() {
        Object smallLong = serializerPair.read(serializerPair.write(Long.valueOf(1L)));
        assertThat(smallLong).isInstanceOf(Long.class);
        assertThat(smallLong).isEqualTo(1L);

        Object maxLong = serializerPair.read(serializerPair.write(Long
            .valueOf(Integer.MAX_VALUE)));
        assertThat(maxLong).isInstanceOf(Long.class);
        assertThat(maxLong).isEqualTo((long) Integer.MAX_VALUE);
    }

    /**
     * 雪花 ID（超出 int 范围）根值往返后类型保持 Long
     */
    @Test
    void snowflakeLongRoundtripKeepsLong() {
        Object result = serializerPair.read(serializerPair.write(SNOWFLAKE_ID));
        assertThat(result).isInstanceOf(Long.class);
        assertThat(result).isEqualTo(SNOWFLAKE_ID);
    }

    /**
     * String 形式的长整型 ID 往返类型精确，转回 Long 后取值不变
     */
    @Test
    void stringRoundtripKeepsExactValue() {
        String cachedId = String.valueOf(SNOWFLAKE_ID);
        Object result = serializerPair.read(serializerPair.write(cachedId));
        assertThat(result).isInstanceOf(String.class);
        assertThat(Long.valueOf((String) result)).isEqualTo(SNOWFLAKE_ID);
    }

    /**
     * 不可变 Map 根值往返内容与容量保持，类型标记不混入条目；
     * HashMap（Collectors.toMap 产物）往返类型保持
     */
    @Test
    @SuppressWarnings("unchecked")
    void immutableMapRoundtripKeepsContent() {
        Object fromMapOf = serializerPair.read(serializerPair.write(Map.of("a", "1", "b", "2")));
        assertThat(fromMapOf).isEqualTo(Map.of("a", "1", "b", "2"));
        assertThat(((Map<Object, Object>) fromMapOf)).hasSize(2);

        Object fromHashMap = serializerPair.read(serializerPair.write(new HashMap<>(Map
            .of("a", "1", "b", "2"))));
        assertThat(fromHashMap).isInstanceOf(HashMap.class);
        Map<String, String> resultMap = (Map<String, String>) fromHashMap;
        assertThat(resultMap).containsEntry("a", "1").containsEntry("b", "2");
    }

    /**
     * FLEXIBLE 大数值模式下 Long 根值与嵌套元素往返保持 Long 类型
     *
     * <p>
     * 生产配置 spring.jackson.big-number-serialize-mode=FLEXIBLE：超出 JS 安全范围的 Long
     * 序列化为字符串形态（如 {@code ["java.lang.Long","547889293968801822"]}），读侧按类型
     * 标记恢复为 Long；int 范围内的 Long 保持数字形态，读侧同样恢复为 Long。
     * </p>
     */
    @Test
    void flexibleBigNumberModeRoundtripKeepsLong() {
        Object rootSmall = flexiblePair.read(flexiblePair.write(Long.valueOf(1L)));
        assertThat(rootSmall).isInstanceOf(Long.class);
        assertThat(rootSmall).isEqualTo(1L);

        Object rootSnowflake = flexiblePair.read(flexiblePair.write(SNOWFLAKE_ID));
        assertThat(rootSnowflake).isInstanceOf(Long.class);
        assertThat(rootSnowflake).isEqualTo(SNOWFLAKE_ID);

        Object nestedSnowflake = flexiblePair.read(flexiblePair.write(new ArrayList<>(List
            .of(SNOWFLAKE_ID))));
        assertThat(nestedSnowflake).isEqualTo(List.of(SNOWFLAKE_ID));
        assertThat((List<?>) nestedSnowflake).first().isInstanceOf(Long.class);
    }
}
