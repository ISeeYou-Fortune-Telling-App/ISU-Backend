package com.iseeyou.fortunetelling.config.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Converter để xử lý trường hợp gửi empty string cho MultipartFile
 * Khi image="" thì trả về null thay vì lỗi
 */
@Component
@Slf4j
public class StringToMultipartFileConverter implements Converter<String, MultipartFile> {

    @Override
    public MultipartFile convert(@NonNull String source) {
        // Nếu là empty string hoặc null string, trả về null
        if (source.trim().isEmpty() || source.equalsIgnoreCase("null")) {
            log.debug("Empty image string, returning null");
            return null;
        }

        // Nếu có giá trị thật, Spring sẽ tự xử lý
        // Trường hợp này không bao giờ xảy ra vì Spring đã handle MultipartFile trước
        log.warn("Unexpected string value for MultipartFile: {}", source);
        return null;
    }
}

