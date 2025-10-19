package com.iseeyou.fortunetelling.service.user.impl;

import com.iseeyou.fortunetelling.dto.response.user.SeerProfileResponse;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.repository.booking.BookingRepository;
import com.iseeyou.fortunetelling.service.user.SeerStatsService;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeerStatsServiceImpl implements SeerStatsService {

    private final BookingRepository bookingRepository;
    private final ModelMapper modelMapper;

    @Override
    public SeerProfileResponse enrichSeerProfile(User seer) {
        if (seer.getSeerProfile() == null) {
            return null;
        }

        // Map basic seer profile
        SeerProfileResponse response = modelMapper.map(seer.getSeerProfile(), SeerProfileResponse.class);

        // Thêm thống kê booking
        Long totalBookings = bookingRepository.countBySeer(seer);
        Long completedBookings = bookingRepository.countBySeerAndStatus(seer, Constants.BookingStatusEnum.COMPLETED);
        Double totalRevenue = bookingRepository.getTotalRevenueBySeer(seer, Constants.PaymentStatusEnum.COMPLETED);

        response.setTotalBookings(totalBookings);
        response.setCompletedBookings(completedBookings);
        response.setTotalRevenue(totalRevenue);

        return response;
    }
}
