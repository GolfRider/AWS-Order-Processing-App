package com.order.model;

import java.util.ArrayList;
import java.util.List;

public class OrderProcessingDTO {
	
	private final List<OrderUpdateEvent> orderProcessingList=new ArrayList<OrderUpdateEvent>();
	private final List<OrderUpdateEvent> orderProcessedList=new ArrayList<OrderUpdateEvent>();
	private final OrderUpdateEvent[] recentArrivedOrderItem=new OrderUpdateEvent[1]; //holds only 1 item
	
	public OrderProcessingDTO(OrderUpdateEvent recentArrivedOrderItem){
	   	this.recentArrivedOrderItem[0]=recentArrivedOrderItem;
	}
	
	public List<OrderUpdateEvent> getOrderProcessingList() {
		return orderProcessingList;
	}
	public List<OrderUpdateEvent> getOrderProcessedList() {
		return orderProcessedList;
	}
	public OrderUpdateEvent[] getRecentArrivedOrderEvent() {
		return recentArrivedOrderItem;
	}
}
