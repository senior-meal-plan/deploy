package io.github.tlsdla1235.seniormealplan.config;

import io.netty.channel.ChannelOption; // ChannelOption 임포트
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        // 1. 타임아웃 설정을 위한 HttpClient 생성
        HttpClient httpClient = HttpClient.create()
                // 💡 수정된 부분: ChannelOption을 통해 연결 타임아웃 설정
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 5초 (밀리초 단위)
                .responseTimeout(Duration.ofSeconds(10));           // 응답 타임아웃: 10초

        // 2. 생성한 HttpClient를 기반으로 WebClient 설정
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}