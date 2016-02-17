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
public class OrderEntryApplication extends SpringBootServletInitializer{
   
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(OrderEntryApplication.class);
    }
	
	public static void main(String args[]){
	   SpringApplication.run(OrderEntryApplication.class,args);
	   System.out.println("Order Entry WebApp Started");
   }
}

