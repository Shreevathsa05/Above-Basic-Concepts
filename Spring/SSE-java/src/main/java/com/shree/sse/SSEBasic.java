package com.shree.sse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1")
public class SSEBasic {
    @GetMapping("/stream")
    public Flux<String> stream(){

        return Flux
                .interval(Duration.ofSeconds(2))
                .map(i -> "Hello - " + i);
    }
}
