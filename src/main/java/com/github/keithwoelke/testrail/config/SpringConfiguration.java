package com.github.keithwoelke.testrail.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SuppressWarnings("unused")
@ComponentScan(basePackages = "com.github.keithwoelke.testrail")
@Configuration
public class SpringConfiguration {
}
