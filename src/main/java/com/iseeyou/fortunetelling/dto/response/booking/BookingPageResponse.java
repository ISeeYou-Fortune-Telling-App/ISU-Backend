package com.iseeyou.fortunetelling.dto.response.booking;

import com.iseeyou.fortunetelling.dto.response.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class BookingPageResponse extends PageResponse<BookingResponse> {
    private BookingStats stats;

    @Data
    @Builder
    @AllArgsConstructor
    public static class BookingStats {
        private Long totalBookings;
        private Long completedBookings;
        private Long pendingBookings;
        private Long canceledBookings;
    }

    public BookingPageResponse(int statusCode, String message, List<BookingResponse> data, 
                               PageResponse.PagingResponse paging, BookingStats stats) {
        super(statusCode, message, data, paging);
        this.stats = stats;
    }
}

