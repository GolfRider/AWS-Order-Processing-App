### AWS-Order-Processing-App
  3 Apps with UI inter-connected via AWS SQS + Spring Boot
 
	The system consists of 3 apps with UI:
	1. Order Entry App 
	2. Order Processing App
	3. Order Fulfilment App
    
	The applications are inter-connected via 3 pre-created *AWS-SQS Queues: 
	a. ORDER_ENTRY_QUEUE,
	b. ORDER_PROCESSED_QUEUE,
	c. ORDER_UPDATE_EVENT_QUEUE
  
	*Data Flow:      
	OrderEntryApp ==>[ORDER_ENTRY_QUEUE]==>OrderProcessingApp ==> [ORDER_PROCESSED_QUEUE]==>OrderFulfilmentApp
	/\                                                 ||                                           ||
	||                                                 ||                                           ||  
	||                                                 \/                                           ||
	||=================================== [ORDER_UPDATE_EVENT_QUEUE] <==============================||
                                                  
                                                  
                                 
                                    
###  Screen Shots
#### 1. Order Entry App
<img src="https://github.com/GolfRider/AWS-Order-Processing-App/blob/master/order-entry-app-screen.PNG" width="600"    
         height="400" />

#### 2. Order Processing App
<img src="https://github.com/GolfRider/AWS-Order-Processing-App/blob/master/order-processing-app-screen.PNG" width="600"  
           height="400" />
           
#### 3. Order Fulfilment App
<img src="https://github.com/GolfRider/AWS-Order-Processing-App/blob/master/order-fulfilment-app-screen.PNG" width="600"    
          height="400" />
