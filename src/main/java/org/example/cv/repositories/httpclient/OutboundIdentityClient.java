package org.example.cv.repositories.httpclient;

import org.example.cv.models.requests.ExchangeTokenRequest;
import org.example.cv.models.responses.ExchangeTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import feign.QueryMap;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "outbound-identity", url = "https://oauth2.googleapis.com")
public interface OutboundIdentityClient {
    @GetMapping(
            value = "/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ExchangeTokenResponse exchangeToken(@QueryMap ExchangeTokenRequest request);
}
