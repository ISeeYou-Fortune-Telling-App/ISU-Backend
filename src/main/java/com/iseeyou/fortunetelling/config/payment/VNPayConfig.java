package com.iseeyou.fortunetelling.config.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {
    @Value("${VNP_URL}")
    public String vnp_PayUrl;

    @Value("${SUCCESS_REDIRECT_URL}")
    public String vnp_ReturnUrl;

    @Value("${VNP_TMN_CODE}")
    public String vnp_TmnCode;

    @Value("${VNP_HASH_SECRET}")
    public String secretKey;

    @Value("${VNP_VERSION}")
    public String vnp_Version;

    @Value("${VNP_COMMAND}")
    public String vnp_Command;

    @Value("${VNP_CURRENCY}")
    public String vnp_Currency;

    @Value("${VNP_LOCALE}")
    public String vnp_Locale;

    @Value("${VNP_ORDER_TYPE}")
    public String vnp_OrderType;
}
