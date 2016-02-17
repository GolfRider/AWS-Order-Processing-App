package com.order.service;

import java.util.List;

import com.order.model.OrderUpdateEvent;

public interface OrderEntryService {
	public void recieveOrder(OrderUpdateEvent orderUpdateEvent);
	public List<OrderUpdateEvent> getOrderUpdateEvents();
	public void _readQueueMessage();
}

