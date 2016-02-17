package com.order.model;

import java.io.Serializable;
import java.util.List;
import com.order.commons.ORDER_STAGE;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderUpdateEvent implements Serializable{

	 private static final long serialVersionUID = 1L;
	 private String serverId;
	 private int orderNo;
	 private List<String> orderItems;
	 private ORDER_STAGE orderUpdateEvent;
	 private long orderUpdateTimestamp;
	 
	 public String getServerId() {
		return serverId;
	 }
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	public int getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(int orderNo) {
		this.orderNo = orderNo;
	}
	public List<String> getOrderItems() {
		return orderItems;
	}
	public void setOrderItems(List<String> orderItems) {
		this.orderItems = orderItems;
	}
	public String getOrderUpdateEvent() {
		return orderUpdateEvent.toString();
	}
	public void setOrderUpdateEvent(ORDER_STAGE orderStage) {
		this.orderUpdateEvent = orderStage;
	}
	public long getOrderUpdateTimestamp() {
		return orderUpdateTimestamp;
	}
	public void setOrderUpdateTimestamp(long orderUpdateTimestamp) {
		this.orderUpdateTimestamp = orderUpdateTimestamp;
	}
	 
}
