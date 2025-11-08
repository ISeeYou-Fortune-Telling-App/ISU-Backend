package com.iseeyou.fortunetelling.service.user.impl;

import com.iseeyou.fortunetelling.dto.response.account.SimpleSeerCardResponse;
import com.iseeyou.fortunetelling.dto.response.report.SeerSimpleRating;
import com.iseeyou.fortunetelling.dto.response.user.SeerProfileResponse;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.repository.booking.BookingRepository;
import com.iseeyou.fortunetelling.service.report.StatisticReportService;
import com.iseeyou.fortunetelling.service.user.SeerStatsService;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeerStatsServiceImpl implements SeerStatsService {

    private final BookingRepository bookingRepository;
    private final ModelMapper modelMapper;
    private final StatisticReportService reportService;

    @Override
    public SeerProfileResponse enrichSeerProfile(User seer) {
        if (seer.getSeerProfile() == null) {
            return null;
        }

        // Map basic seer profile
        SeerProfileResponse response = modelMapper.map(seer.getSeerProfile(), SeerProfileResponse.class);

        // Thêm thống kê booking
        // TODO: Replace this
        Long totalBookings = bookingRepository.countBySeer(seer);
        Long completedBookings = bookingRepository.countBySeerAndStatus(seer, Constants.BookingStatusEnum.COMPLETED);
        Double totalRevenue = bookingRepository.getTotalRevenueBySeer(seer, Constants.PaymentStatusEnum.COMPLETED);

        response.setTotalBookings(totalBookings);
        response.setCompletedBookings(completedBookings);
        response.setTotalRevenue(totalRevenue);

        // Lấy rating data từ Statistic Report Service
        try {
            LocalDate now = LocalDate.now();
            SeerSimpleRating rating = reportService.getSeerSimpleRating(
                    seer.getId().toString(),
                    now.getMonthValue(),
                    now.getYear()
            );

            if (rating != null) {
                response.setAvgRating(rating.getAvgRating() != null ? rating.getAvgRating().doubleValue() : 0.0);
                response.setTotalRates(rating.getTotalRates() != null ? rating.getTotalRates() : 0);
                response.setSeerTier(rating.getPerformanceTier() != null ? rating.getPerformanceTier() : "BRONZE");

                log.debug("Enriched seer profile with rating: seerId={}, avgRating={}, totalRates={}, tier={}",
                        seer.getId(), response.getAvgRating(), response.getTotalRates(), response.getSeerTier());
            }
        } catch (Exception e) {
            log.error("Failed to get rating from Report Service for seer: {}", seer.getId(), e);
            response.setAvgRating(0.0);
            response.setTotalRates(0);
            response.setSeerTier("BRONZE");
        }

        return response;
    }

    @Override
    public SimpleSeerCardResponse enrichSimpleSeerCard(SimpleSeerCardResponse card, String seerId) {
        if (card == null || seerId == null) {
            return card;
        }

        try {
            LocalDate now = LocalDate.now();
            SeerSimpleRating rating = reportService.getSeerSimpleRating(
                    seerId,
                    now.getMonthValue(),
                    now.getYear()
            );

            if (rating != null) {
                card.setRating(rating.getAvgRating() != null ? rating.getAvgRating().doubleValue() : 0.0);
                card.setTotalRates(rating.getTotalRates() != null ? rating.getTotalRates().doubleValue() : 0.0);

                log.debug("Enriched simple seer card with rating: seerId={}, rating={}, totalRates={}",
                        seerId, card.getRating(), card.getTotalRates());
            } else {
                card.setRating(0.0);
                card.setTotalRates(0.0);
            }
        } catch (Exception e) {
            log.error("Failed to get rating from Report Service for seer: {}", seerId, e);
            card.setRating(0.0);
            card.setTotalRates(0.0);
        }

        return card;
    }
}
