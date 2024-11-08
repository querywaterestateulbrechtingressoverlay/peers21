package com.example.demo.schoolapiscraper;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class ApiScraperService {
    @Bean
    public ApiScraperService apiScraper() {
        return new ApiScraperService();
    }

}
