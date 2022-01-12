package com.identifix.rabbitstreams;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import static java.util.Map.entry;

@Slf4j
@Configuration
public class BrokerConfig {

    @Bean
    public Supplier<Flux<String>> pingProducer() {
        return () -> Flux.interval(Duration.ofSeconds(5)).map(value -> "Pingable").log();
    }

    @Bean
    public Consumer<String> pongConsumer() {
        return (value) -> log.info("Consumer Received : " + value);
    }

    @Bean
    public Function<Flux<String>, Flux<String>> pongProcessor(){
        return longFlux -> longFlux
            .map(i -> sendResponse(i))
            .log();
    }

    private String sendResponse(String str) {
        return "Ping %s".formatted(str);
    }

    @Bean
    public Function<Flux<String>, Flux<Map<String, String>>> mapProcessor(){
        return longFlux -> longFlux
            .map(i -> sendDataThroughQueue(i))
            .log();
    }

    @Bean
    public Function<Flux<String>, Flux<byte[]>> fileProcessor(){
        return longFlux -> longFlux
            .map(i -> sendFile())
            .log();
    }

    private String sendFileTemp() {
        return "Hello, Workd!";
    }

    @SneakyThrows
    private byte[] sendFile() {
        return Files.readAllBytes(Paths.get(getClass().getResource("/text.txt").toURI()));
    }

    private Map<String, String> sendDataThroughQueue(String str) {
        return Map.ofEntries(
            entry("Key1", "value1 %s".formatted(str)),
            entry("Key2", "value2 %s".formatted(str)),
            entry("Key3", "value3 %s".formatted(str))
        );
    }
}
