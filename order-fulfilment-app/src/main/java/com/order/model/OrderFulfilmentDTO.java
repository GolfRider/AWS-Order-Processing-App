package com.order.model;

import java.util.ArrayList;
import java.util.List;

public class OrderFulfilmentDTO {
	
	private final List<OrderUpdateEvent> pendingFulfilmentList=new ArrayList<OrderUpdateEvent>();
	private final List<OrderUpdateEvent> completedFulfilmentList=new ArrayList<OrderUpdateEvent>();
	private final OrderUpdateEvent[] recentArrivedOrderItem=new OrderUpdateEvent[1]; //holds only 1 item
	
	public OrderFulfilmentDTO(OrderUpdateEvent recentArrivedOrderItem){
	   	this.recentArrivedOrderItem[0]=recentArrivedOrderItem;
	}
	
	public OrderUpdateEvent[] getRecentArrivedOrderItem() {
		return recentArrivedOrderItem;
	}

	public List<OrderUpdateEvent> getPendingFulfilmentList() {
		return pendingFulfilmentList;
	}

	public List<OrderUpdateEvent> getCompletedFulfilmentList() {
		return completedFulfilmentList;
	}
}
