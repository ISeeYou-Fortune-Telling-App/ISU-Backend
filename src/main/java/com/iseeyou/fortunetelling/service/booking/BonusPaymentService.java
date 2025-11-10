package com.iseeyou.fortunetelling.service.booking;

import com.iseeyou.fortunetelling.entity.booking.BookingPayment;

import java.util.UUID;

public interface BonusPaymentService {
    /**
     * Tạo bonus payment cho seer
     * @param seerId ID của seer nhận thưởng
     * @param amount Số tiền thưởng (VND)
     * @param reason Lý do thưởng
     * @return BookingPayment đã tạo
     */
    BookingPayment createBonusPayment(UUID seerId, Double amount, String reason) throws Exception;
}

