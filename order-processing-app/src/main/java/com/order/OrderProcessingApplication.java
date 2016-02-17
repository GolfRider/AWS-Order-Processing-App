package com.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class OrderProcessingApplication extends SpringBootServletInitializer{
   
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(OrderProcessingApplication.class);
    }
	
	public static void main(String args[]){
	   SpringApplication.run(OrderProcessingApplication.class,args);
	   System.out.println("Order Processing WebApp Started");
   }
}