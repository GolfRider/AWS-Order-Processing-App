package com.order.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.commons.ORDER_QUEUE;
import com.order.commons.ORDER_STAGE;
import com.order.model.OrderProcessingDTO;
import com.order.model.OrderUpdateEvent;
import com.order.utils.OrderUtils;

@Service
public class OrderProcessingService {
	
	@Value("${aws_access_key_id}")
	private String awsAccessKey;
	
	@Value("${aws_secret_access_key}")
	private String awsSecretKey;
	
	public static final Map<ORDER_QUEUE,String> orderQueueMap= new HashMap<>();
	private final ObjectMapper mapper = new ObjectMapper();
	private AmazonSQS sqs=null;
	
	private final Map<String,OrderUpdateEvent> ORDER_PROCESSING_MSG_MAP= new ConcurrentHashMap<>(100);
	private final Map<String,OrderUpdateEvent> ORDER_PROCESSED_MSG_MAP= new ConcurrentHashMap<>(100);
	
	private final Queue<Message> orderProcessingQueue= new ConcurrentLinkedQueue<>();
	private final AtomicReference<OrderUpdateEvent> recentOrderItemAtomicReference=new AtomicReference<>();
	
	@PostConstruct
	private void init(){
		AWSCredentials credentials = null;
		Properties props = System.getProperties();
		props.setProperty("aws.accessKeyId", awsAccessKey.trim());
		props.setProperty("aws.secretKey", awsSecretKey.trim());
					
		try{
			credentials = new SystemPropertiesCredentialsProvider().getCredentials();
			sqs = new AmazonSQSClient(credentials);
			_getQueueDetails();
				
		}catch(Exception e){
			 throw new AmazonClientException(
	                    "Cannot load the credentials from the credential profiles file. " +
	                    "Please make sure that your credentials file is at the correct " +
	                    "location (~/.aws/credentials), and is in valid format.",
	                    e);
		}
	}
	
	public OrderProcessingDTO getOrderProcessingSnaphsot(){
		OrderUpdateEvent recentOrderItem=recentOrderItemAtomicReference.get();
		OrderProcessingDTO orderProcessingDTO= new OrderProcessingDTO(recentOrderItem);
		
		ArrayList<OrderUpdateEvent> processingOrderItemList=new ArrayList<>(ORDER_PROCESSING_MSG_MAP.values());
		ArrayList<OrderUpdateEvent> processedOrderItemList=new ArrayList<>(ORDER_PROCESSED_MSG_MAP.values());
		
		orderProcessingDTO.getOrderProcessingList().addAll(Collections.unmodifiableList(processingOrderItemList));
		orderProcessingDTO.getOrderProcessedList().addAll(Collections.unmodifiableList(processedOrderItemList));
		
		return orderProcessingDTO;
	}
	
	private void _sendMessage(ORDER_QUEUE orderQueue,Object objMessage) throws JsonProcessingException{
		  String message=mapper.writeValueAsString(objMessage);
		  sqs.sendMessage(new SendMessageRequest(orderQueueMap.get(orderQueue), message));
	}
	  
	private List<Message> _receiveMessage(ORDER_QUEUE orderQueue){
		  ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(orderQueueMap.get(orderQueue));
          List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
          return messages;
	}
	
	@Scheduled(initialDelay=2000,fixedDelay=2000) //trigger every 2 sec after last invocation
	private void pollAndProcessOrderItems(){
	    try{
	    	_pollOrderEntryQueue();
	    	_doOrderItemProcessing();
	    }catch(Exception ex){
	    	ex.printStackTrace();
	    }
	}
	
	  
	 private void _pollOrderEntryQueue(){
		 	 List<Message> receivedMessages=_receiveMessage(ORDER_QUEUE.ORDER_ENTRY_QUEUE);
			 orderProcessingQueue.addAll(receivedMessages);
	  }
	 
	 
	 private void _doOrderItemProcessing() throws JsonProcessingException,IOException{
		 	 Message orderMessage=orderProcessingQueue.poll();
			 if(orderMessage!=null){
				    //Is it already processed before,idempotent check
				 if(!ORDER_PROCESSED_MSG_MAP.containsKey(orderMessage.getMessageId())){
					 //set stage to processing mode
					 OrderUpdateEvent newOrderUpdateEvent=mapper.readValue(orderMessage.getBody(), OrderUpdateEvent.class);
					 newOrderUpdateEvent.setOrderUpdateEvent(ORDER_STAGE.ORDER_PROCESSING);
					 
					 //1.Update the recent arrived Message (atomically)
					 OrderUpdateEvent lastRecentOrderItem=recentOrderItemAtomicReference.get();
					 recentOrderItemAtomicReference.compareAndSet(lastRecentOrderItem, newOrderUpdateEvent);
					 
					 //2. Add to processing list
					 ORDER_PROCESSING_MSG_MAP.putIfAbsent(orderMessage.getMessageId(), newOrderUpdateEvent);
					 
					 //3. Do processing
					 OrderUtils.randomOrderProcessingTime();
					 //4. Add to processed list
					 newOrderUpdateEvent.setOrderUpdateEvent(ORDER_STAGE.ORDER_PROCESSED);
					 ORDER_PROCESSED_MSG_MAP.putIfAbsent(orderMessage.getMessageId(), newOrderUpdateEvent);
					 
					 //5. Remove from processing list
					 ORDER_PROCESSING_MSG_MAP.remove(orderMessage.getMessageId());
					 //6. Send processed order item/message to Queues: ORDER_PROCESSED_QUEUE,ORDER_UPDATE_EVENT_QUEUE
					 _sendMessage(ORDER_QUEUE.ORDER_PROCESSED_QUEUE, newOrderUpdateEvent);
					 _sendMessage(ORDER_QUEUE.ORDER_UPDATE_EVENT_QUEUE, newOrderUpdateEvent);
				 }
				 //7. Delete from the Order Entry Queue,even in case of duplicate message
				 _deleteMessage(ORDER_QUEUE.ORDER_ENTRY_QUEUE, orderMessage);
			 }	
	  }
		
	  private void _deleteMessage(ORDER_QUEUE orderQueue,Message message){
		  String messageReceiptHandle = message.getReceiptHandle();
          sqs.deleteMessage(new DeleteMessageRequest(orderQueueMap.get(orderQueue), messageReceiptHandle));
	  }
	  
	 private void _getQueueDetails(){
		Region usWest1 = Region.getRegion(Regions.US_WEST_1);
		sqs.setRegion(usWest1); 
        for (String queueUrl : sqs.listQueues().getQueueUrls()) {
        	  
        	   if(queueUrl.endsWith(ORDER_QUEUE.ORDER_ENTRY_QUEUE.toString())){
        		  orderQueueMap.put(ORDER_QUEUE.ORDER_ENTRY_QUEUE, queueUrl);
               }
        	   if(queueUrl.endsWith(ORDER_QUEUE.ORDER_PROCESSED_QUEUE.toString())){
         		  orderQueueMap.put(ORDER_QUEUE.ORDER_PROCESSED_QUEUE, queueUrl);
               }
        	   if(queueUrl.endsWith(ORDER_QUEUE.ORDER_UPDATE_EVENT_QUEUE.toString())){
         		  orderQueueMap.put(ORDER_QUEUE.ORDER_UPDATE_EVENT_QUEUE, queueUrl);
               }
        }
        
	  }
	  
}
