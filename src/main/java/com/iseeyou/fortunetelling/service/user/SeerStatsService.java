package com.iseeyou.fortunetelling.service.user;

import com.iseeyou.fortunetelling.dto.response.user.SeerProfileResponse;
import com.iseeyou.fortunetelling.entity.user.User;

public interface SeerStatsService {
    SeerProfileResponse enrichSeerProfile(User seer);
}
