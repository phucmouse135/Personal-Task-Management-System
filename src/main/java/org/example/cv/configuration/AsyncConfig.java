package org.example.cv.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync // Kích hoạt khả năng xử lý bất đồng bộ của Spring
public class AsyncConfig {}
