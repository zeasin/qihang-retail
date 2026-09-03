package cn.qihangerp.erp.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.Deserializers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Jackson全局配置：让所有LocalDateTime字段都能接收 yyyy-MM-dd 日期串
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.addModule(new LocalDateTimeModule());
    }

    static class LocalDateTimeModule extends JacksonModule {
        @Override
        public String getModuleName() {
            return "LocalDateTimeLenientModule";
        }

        @Override
        public tools.jackson.core.Version version() {
            return tools.jackson.core.Version.unknownVersion();
        }

        @Override
        public void setupModule(SetupContext context) {
            context.addDeserializers(new Deserializers.Base() {
                @Override
                public boolean hasDeserializerFor(DeserializationConfig config, Class<?> valueType) {
                    return valueType == LocalDateTime.class;
                }

                @Override
                public ValueDeserializer<?> findBeanDeserializer(JavaType type,
                        DeserializationConfig config,
                        tools.jackson.databind.BeanDescription.Supplier beanDesc) {
                    if (type.getRawClass() == LocalDateTime.class) {
                        return new ValueDeserializer<>() {
                            @Override
                            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) {
                                String text = p.getText();
                                if (text == null || text.isEmpty()) return null;
                                if (text.length() == 10 && !text.contains("T")) {
                                    return LocalDateTime.of(LocalDate.parse(text), LocalTime.MIN);
                                }
                                return LocalDateTime.parse(text);
                            }

                            @Override
                            public Class<?> handledType() {
                                return LocalDateTime.class;
                            }
                        };
                    }
                    return null;
                }
            });
        }
    }
}
