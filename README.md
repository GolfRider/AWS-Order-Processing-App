### AWS-Order-Processing-App
  3 Apps with UI inter-connected via AWS SQS + Spring Boot
 
	The system consists of 3 apps with UI:
	1. Order Entry App 
	2. Order Processing App
	3. Order Fulfilment App
    
	The applications are inter-connected via 3 AWS-SQS Queues: 
	a. ORDER_ENTRY_QUEUE,
	b. ORDER_PROCESSED_QUEUE,
	c. ORDER_UPDATE_EVENT_QUEUE
  
	OrderEntryApp ==> [ORDER_ENTRY_QUEUE (SQS)]==> OrderProcessingApp ==> [ORDER_PROCESSED_QUEUE (SQS)]==> OrderFulfilmentApp
	/\                                                 ||                                                      ||
	||                                                 ||                                                      ||  
	||                                                 \/                                                      ||
	||=================================== [ORDER_UPDATE_EVENT_QUEUE (SQS)] <===================================||
                                                  
                                                  
                                 
                                    
  
