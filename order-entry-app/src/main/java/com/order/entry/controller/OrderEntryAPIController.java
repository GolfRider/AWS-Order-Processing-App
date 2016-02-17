package com.order.entry.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.order.service.OrderEntryService;
import com.order.model.OrderUpdateEvent;

@RestController
@RequestMapping("/order")
public class OrderEntryAPIController {

	@Autowired
	private OrderEntryService orderEntryService;
	
	@RequestMapping(method=RequestMethod.POST,path="/process",consumes="application/json")
	public String processOrderItems(@RequestBody OrderUpdateEvent orderUpdateEvent){
		orderEntryService.recieveOrder(orderUpdateEvent);
		return "success";
	}
	
	@RequestMapping(method=RequestMethod.GET,path="/show",produces="application/json")
	public List<OrderUpdateEvent> getOrderUpdates(){
		return orderEntryService.getOrderUpdateEvents();
	}
	
}
