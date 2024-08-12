package com.hum.quant.controller;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import com.hum.quant.KisConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Controller
public class StockPageController {

    private AccessTokenManager accessTokenManager;
    private final WebClient webClient;
    private String path;
    private String tr_id;

    public StockPageController(AccessTokenManager accessTokenManager) {
        this.accessTokenManager = accessTokenManager;
        this.webClient = WebClient.builder().baseUrl(KisConfig.REST_BASE_URL).build();
    }

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/domestic/index")
    public Mono<String> majorIndices(Model model) {

        List<Tuple2<String, String>> iscdsAndMarketDivCode = Arrays.asList(
                Tuples.of("0001", "U"), //종합지수
                Tuples.of("2001", "U"), //코스피 지수
                Tuples.of("1001", "U") //코스닥 지수
        );

        Flux<IndexData> indicesFlux = Flux.fromIterable(iscdsAndMarketDivCode)
                .flatMap(tuple -> getMajorIndex(tuple.getT1(), tuple.getT2()))
                .map(jsonData -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        return objectMapper.readValue(jsonData, IndexData.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("json parsing error", e);
                    }
                });


      return indicesFlux.collectList()
                .doOnNext(indicesList -> {
                    model.addAttribute("koreaIndexes", indicesList);
                    model.addAttribute("jobDate", getJobDateTime());
                })
              .then(Mono.just("domesticStock")); // 비동기 처리 후 뷰 이름 반환
    }

    private Mono<String> getMajorIndex(String iscd, String fid_cond_mrkt_div_code) {
        if (fid_cond_mrkt_div_code.equals("U")) {
            path = KisConfig.FHKUP03500100_PATH;
            tr_id = "FHKUP03500100"; //업종별 시세 거래ID
        }
//        else if(fid_cond_mrkt_div_code.equals("J")) {
//            path = "/uapi/domestic-stock/v1/quotations/inquire-price"
//            tr_id = "FHKST01010100";
//        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("fid_cond_mrkt_div_code", fid_cond_mrkt_div_code)
                        .queryParam("fid_input_iscd", iscd)
                        .queryParam("fid_input_date_1", getStringToday())
                        .queryParam("fid_input_date_2", getStringToday())
                        .queryParam("fid_period_div_code", "D")
                        .build())
                .header("content-type", "application/json")
                .header("authorization", "Bearer " + accessTokenManager.getAccessToken())
                .header("appkey", KisConfig.APPKEY)
                .header("appsecret", KisConfig.APPSECRET)
                .header("tr_id", tr_id)
                .retrieve()
                .bodyToMono(String.class);
    }

    private String getStringToday() {
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return localDate.format(formatter);
    }

    private String getJobDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    @GetMapping("/equities/{id}")
    public Mono<String> CurrentPrice(@PathVariable("id") String id, Model model) {
        String url = KisConfig.REST_BASE_URL + "/uapi/domestic-stock/v1/quotations/inquire-price" +
                "?fid_cond_mrkt_div_code=J" +
                "&fid_input_iscd=" + id;

        return webClient.get()
                .uri(url)
                .header("content-type","application/json")
                .header("authorization","Bearer " + accessTokenManager.getAccessToken())
                .header("appkey",KisConfig.APPKEY)
                .header("appsecret",KisConfig.APPSECRET)
                .header("tr_id","FHKST01010100")
                .retrieve()
                .bodyToMono(EquityResponse.class)
                .doOnSuccess(equityResponse -> {
                    model.addAttribute("equity", equityResponse.getOutput());
                    model.addAttribute("jobDate", getJobDateTime());
                })
                .doOnError(result -> System.out.println("*** error: " + result))
                .thenReturn("equities");
    }

}
