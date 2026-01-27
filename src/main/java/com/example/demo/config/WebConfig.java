package com.example.demo.config;

import com.example.demo.post.enums.PostSort;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 註冊一個轉換器：把 String 轉成 PostSort
        registry.addConverter(new Converter<String, PostSort>() {
            @Override
            public PostSort convert(String source) {
                // 1. 如果是 null 或空字串，回傳 LATEST 做為預設值
                if (source == null || source.isEmpty()) {
                    return PostSort.LATEST;
                }
                // 2. 轉大寫後比對
                try {
                    return PostSort.valueOf(source.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // 3. 如果亂打 (例如 sort=abc)，這裡決定要報錯還是給預設值
                    // 回傳 LATEST 代表容錯；拋出錯誤代表嚴格檢查
                    throw e;
                }
            }
        });
    }
}
