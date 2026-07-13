package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.dto.randomorg.AnswerDto;
import com.innowise.paymentservice.dto.randomorg.RequestDto;
import com.innowise.paymentservice.dto.randomorg.RequestParamsDto;
import com.innowise.paymentservice.service.RandomOrgService;
import lombok.RequiredArgsConstructor;
import com.innowise.paymentservice.feign.RandomOrgServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RandomOrgServiceImpl implements RandomOrgService {
    private final RandomOrgServiceClient randomOrgServiceClient;

    @Value("${services.random.number.min}")
    private Long min;
    @Value("${services.random.number.max}")
    private Long max;
    @Value("${services.random.api.key}")
    private String apiKey;
    @Value("${services.random.number.base}")
    private Integer base;
    @Value("${services.random.api.method}")
    private String method;
    @Value("${services.random.number.amount}")
    private Integer amount;

    /**
     * Imitates payment by sending request to random.org
     * @return boolean
     */
    @Override
    public boolean tryToPay() {
        RequestDto requestDto = new RequestDto();
        requestDto.setId(0L);
        requestDto.setJsonrpc("2.0");
        requestDto.setMethod(method);

        RequestParamsDto params = new RequestParamsDto();
        params.setApiKey(apiKey);
        params.setBase(base);
        params.setMin(min);
        params.setMax(max);
        params.setN(amount);
        params.setPregeneratedRandomization(null);
        params.setReplacement(true);

        requestDto.setParams(params);

        AnswerDto answer = randomOrgServiceClient.getRandomNumber(requestDto);

        return answer.getResult().getRandom().getData().getFirst() % 2 == 0;
    }
}
