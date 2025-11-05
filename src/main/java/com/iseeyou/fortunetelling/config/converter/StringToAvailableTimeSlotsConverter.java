package com.iseeyou.fortunetelling.config.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iseeyou.fortunetelling.dto.request.servicepackage.AvailableTimeSlotRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Converter để chuyển đổi JSON string thành List<AvailableTimeSlotRequest>
 * Được sử dụng khi gửi dữ liệu dạng multipart/form-data
 */
@Component
@Slf4j
public class StringToAvailableTimeSlotsConverter implements Converter<String, List<AvailableTimeSlotRequest>> {

    private final ObjectMapper objectMapper;

    public StringToAvailableTimeSlotsConverter() {
        this.objectMapper = new ObjectMapper();
        // Đăng ký module để hỗ trợ LocalTime, LocalDate, LocalDateTime
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public List<AvailableTimeSlotRequest> convert(@NonNull String source) {
        try {
            if (source.trim().isEmpty() || source.equalsIgnoreCase("null")) {
                log.debug("Empty or null availableTimeSlots, returning null");
                return null;
            }

            // Xử lý trường hợp client gửi lên không có dấu [] bao ngoài
            String jsonSource = source.trim();
            if (!jsonSource.startsWith("[")) {
                // Nếu không bắt đầu bằng [, thêm [] để tạo thành array
                jsonSource = "[" + jsonSource + "]";
                log.debug("Added array brackets to source: {}", jsonSource);
            }

            log.debug("Converting JSON string to List<AvailableTimeSlotRequest>: {}", jsonSource);

            List<AvailableTimeSlotRequest> result = objectMapper.readValue(
                jsonSource,
                new TypeReference<>() {}
            );

            log.debug("Successfully converted to {} time slots", result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("Failed to convert string to List<AvailableTimeSlotRequest>: {}", source, e);
            throw new IllegalArgumentException("Invalid availableTimeSlots format. Expected JSON array with format: " +
                "[{\"weekDate\":2,\"availableFrom\":\"09:00:00\",\"availableTo\":\"12:00:00\"}]. Error: " + e.getMessage());
        }
    }
}
