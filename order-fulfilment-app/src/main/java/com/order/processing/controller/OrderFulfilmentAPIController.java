package com.order.processing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.order.model.OrderFulfilmentDTO;
import com.order.service.OrderFulfilmentService;

@RestController
@RequestMapping("/order")
public class OrderFulfilmentAPIController {

	@Autowired
	private OrderFulfilmentService orderFulfilmentService;
	
	@RequestMapping(method=RequestMethod.GET,path="/fulfilment",produces="application/json")
	public OrderFulfilmentDTO getOrderFulfilmentUpdates(){
		return orderFulfilmentService.getOrderFulfilmentSnaphsot();
	}
	
}
