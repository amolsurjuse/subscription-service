package com.electrahub.subscription.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserCountryClientTest {

    private static final UUID USER_ID = UUID.fromString("eda84789-2a1c-42de-844f-72efd53cea16");

    @Test
    void usesProfileCountryWithoutCallingWallet() {
        var userBuilder = RestClient.builder().baseUrl("http://user-service");
        var userServer = MockRestServiceServer.bindTo(userBuilder).build();
        var paymentBuilder = RestClient.builder().baseUrl("http://payment-service");
        var paymentServer = MockRestServiceServer.bindTo(paymentBuilder).build();
        var client = new UserCountryClient(userBuilder.build(), paymentBuilder.build());

        userServer.expect(requestTo("http://user-service/api/internal/users/" + USER_ID + "/billing-profile"))
                .andRespond(withSuccess("{\"countryCode\":\"in\"}", MediaType.APPLICATION_JSON));

        assertThat(client.findCountry(USER_ID)).contains("IN");
        userServer.verify();
        paymentServer.verify();
    }

    @Test
    void fallsBackToWalletCountryWhenProfileCountryIsMissing() {
        var userBuilder = RestClient.builder().baseUrl("http://user-service");
        var userServer = MockRestServiceServer.bindTo(userBuilder).build();
        var paymentBuilder = RestClient.builder().baseUrl("http://payment-service");
        var paymentServer = MockRestServiceServer.bindTo(paymentBuilder).build();
        var client = new UserCountryClient(userBuilder.build(), paymentBuilder.build());

        userServer.expect(requestTo("http://user-service/api/internal/users/" + USER_ID + "/billing-profile"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        paymentServer.expect(requestTo("http://payment-service/api/v1/payment/internal/accounts/" + USER_ID + "/wallet-profile"))
                .andRespond(withSuccess("{\"countryCode\":\"us\",\"currency\":\"USD\"}", MediaType.APPLICATION_JSON));

        assertThat(client.findCountry(USER_ID)).contains("US");
        userServer.verify();
        paymentServer.verify();
    }

    @Test
    void returnsEmptyWhenNeitherProfileNorWalletHasCountry() {
        var userBuilder = RestClient.builder().baseUrl("http://user-service");
        var userServer = MockRestServiceServer.bindTo(userBuilder).build();
        var paymentBuilder = RestClient.builder().baseUrl("http://payment-service");
        var paymentServer = MockRestServiceServer.bindTo(paymentBuilder).build();
        var client = new UserCountryClient(userBuilder.build(), paymentBuilder.build());

        userServer.expect(requestTo("http://user-service/api/internal/users/" + USER_ID + "/billing-profile"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        paymentServer.expect(requestTo("http://payment-service/api/v1/payment/internal/accounts/" + USER_ID + "/wallet-profile"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThat(client.findCountry(USER_ID)).isEmpty();
        userServer.verify();
        paymentServer.verify();
    }
}
