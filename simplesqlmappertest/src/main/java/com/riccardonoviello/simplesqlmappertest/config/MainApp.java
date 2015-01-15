package com.riccardonoviello.simplesqlmappertest.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 
 * @author novier
 */
@Configuration
@ComponentScan("com.riccardonoviello.simplesqlmappertest")
@PropertySource("classpath:application.properties")
@EnableAutoConfiguration
public class MainApp {
    
    public static void main(String args[]){
        SpringApplication.run(MainApp.class, args);
        
    }
}
