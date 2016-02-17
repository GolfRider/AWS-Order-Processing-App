package com.order.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
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
import com.order.commons.OrderUtils;
import com.order.model.OrderUpdateEvent;

@Service
public class OrderEntryServiceImpl implements OrderEntryService{
	
	@Value("${aws_access_key_id}")
	private String awsAccessKey;
	
	@Value("${aws_secret_access_key}")
	private String awsSecretKey;
	
	public static final Map<ORDER_QUEUE,String> orderQueueMap= new HashMap<>();
	private final ObjectMapper mapper = new ObjectMapper();
	private AmazonSQS sqs=null;
	
	private final Map<Integer,OrderUpdateEvent> ORDER_SENT_MAP= new ConcurrentHashMap<>(100);
	private final Map<String,OrderUpdateEvent> ORDER_UPDATE_EVENT_MAP= new ConcurrentHashMap<>(100);
	private AtomicInteger counter=new AtomicInteger(100); 
	
	
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
	
	@Async
	public void recieveOrder(OrderUpdateEvent orderUpdateEvent){
		try{
			//1. Prepare Order Entry
			orderUpdateEvent.setOrderNo(counter.getAndIncrement());
			orderUpdateEvent.setServerId(OrderUtils.appServerId);
			orderUpdateEvent.setOrderUpdateEvent(ORDER_STAGE.ORDER_ENTRY);
			orderUpdateEvent.setOrderUpdateTimestamp(System.currentTimeMillis());
			//2. Send it to Queue
			sendMessage(ORDER_QUEUE.ORDER_ENTRY_QUEUE, orderUpdateEvent);	
		}catch(Exception ex){
			ex.printStackTrace();
		}		
	}
	
	public List<OrderUpdateEvent> getOrderUpdateEvents(){
		return Collections.unmodifiableList(new ArrayList<OrderUpdateEvent>(ORDER_UPDATE_EVENT_MAP.values()));
	}
	
	public void sendMessage(ORDER_QUEUE orderQueue,OrderUpdateEvent orderUpdateEvent) throws JsonProcessingException {
		 	  String message=mapper.writeValueAsString(orderUpdateEvent);
			  sqs.sendMessage(new SendMessageRequest(orderQueueMap.get(orderQueue), message));
			  ORDER_SENT_MAP.putIfAbsent(orderUpdateEvent.getOrderNo(), orderUpdateEvent);
	}
	  
	private List<Message> _pollQueue(ORDER_QUEUE orderQueue){
		  ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(orderQueueMap.get(orderQueue));
          List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
          return messages;
	}
	
	 @Scheduled(initialDelay=2000,fixedDelay=2000) //trigger every 2 sec
	 public void _readQueueMessage(){
		  //NOTE:Don't poll SQS unnecessary (as it involves cost)
		  //if the no. of total received update event messages is twice the sent messages
		 try{
		    if(ORDER_SENT_MAP.size()*2!=ORDER_UPDATE_EVENT_MAP.size()){
		    	List<Message> receivedMessages=_pollQueue(ORDER_QUEUE.ORDER_UPDATE_EVENT_QUEUE);
			    for(Message queueMessage:receivedMessages){
			    	OrderUpdateEvent orderUpdateEvent=mapper.readValue(queueMessage.getBody(), OrderUpdateEvent.class);
			    	//Check if the message was sent by this instance
			    	if(orderUpdateEvent.getServerId().equalsIgnoreCase(OrderUtils.appServerId)){
			    		//Process only if this message was not processed before,else delete...
			    		 ORDER_UPDATE_EVENT_MAP.putIfAbsent(queueMessage.getMessageId(), orderUpdateEvent);
			    		
			    		//Delete the message from the ORDER_UPDATE_EVENT queue
			    		 _deleteMessage(ORDER_QUEUE.ORDER_UPDATE_EVENT_QUEUE, queueMessage);
			    	}
			    }	
		    }
		 }catch(Exception ex){
			 ex.printStackTrace();
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
