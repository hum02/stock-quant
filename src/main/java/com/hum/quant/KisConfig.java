package com.hum.quant;

import org.springframework.context.annotation.Configuration;

@Configuration
public class KisConfig {

    public static final String REST_BASE_URL = "https://openapivts.koreainvestment.com:29443";
    //모의 https://openapivts.koreainvestment.com:29443
    //https://openapi.koreainvestment.com:9443

    public static final String APPKEY = "appKey";
    public static final String APPSECRET = "appSecret";
    public static final String FHKUP03500100_PATH = "/uapi/domestic-stock/v1/quotations/inquire-daily-indexchartprice";
}
