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

package top.charles7c.cnadmin.common.config.jackson;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

/**
 * Jackson 配置
 *
 * @author Charles7c
 * @since 2022/12/11 13:23
 */
@Slf4j
@Configuration
public class JacksonConfiguration {

    /**
     * 全局配置序列化返回 JSON 处理
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        String dateTimeFormatPattern = "yyyy-MM-dd HH:mm:ss";
        String dateFormatPattern = "yyyy-MM-dd";
        String timeFormatPattern = "HH:mm:ss";

        return builder -> {
            // 针对 java.util.Date 的转换
            builder.locale(Locale.CHINA);
            builder.timeZone(TimeZone.getDefault());
            builder.simpleDateFormat(dateTimeFormatPattern);

            // 针对 Long、BigInteger、BigDecimal 的转换
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(Long.class, BigNumberSerializer.SERIALIZER_INSTANCE);
            javaTimeModule.addSerializer(Long.TYPE, BigNumberSerializer.SERIALIZER_INSTANCE);
            javaTimeModule.addSerializer(BigInteger.class, BigNumberSerializer.SERIALIZER_INSTANCE);
            javaTimeModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);

            // 针对 LocalDateTime、LocalDate、LocalTime 的转换
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormatPattern);
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateFormatPattern);
            javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
            javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(timeFormatPattern);
            javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));
            javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));
            builder.modules(javaTimeModule);
            log.info(">>>初始化 Jackson 配置<<<");
        };
    }
}
