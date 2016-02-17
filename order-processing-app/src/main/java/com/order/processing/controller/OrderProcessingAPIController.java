package com.order.processing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.order.model.OrderProcessingDTO;
import com.order.service.OrderProcessingService;

@RestController
@RequestMapping("/order")
public class OrderProcessingAPIController {

	@Autowired
	private OrderProcessingService orderProcessingService;
	
	@RequestMapping(method=RequestMethod.GET,path="/processing",produces="application/json")
	public OrderProcessingDTO getOrderProcessingUpdates(){
		return orderProcessingService.getOrderProcessingSnaphsot();
	}
	
}
