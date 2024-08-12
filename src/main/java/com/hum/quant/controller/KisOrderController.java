package com.hum.quant.controller;

import com.hum.quant.KisConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequestMapping("/order")
@Controller
public class KisOrderController {
    private AccessTokenManager accessTokenManager;
    private final WebClient webClient;

    public KisOrderController(AccessTokenManager accessTokenManager) {
        this.accessTokenManager = accessTokenManager;
        this.webClient = WebClient.builder().baseUrl(KisConfig.REST_BASE_URL).build();
    }

    @GetMapping("/equities/{id}")
    public Mono<String> orderPage(@PathVariable("id") String id, Model model) {
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
                })
                .doOnError(result -> System.out.println("*** error: " + result))
                .thenReturn("orderStock");
    }
}
