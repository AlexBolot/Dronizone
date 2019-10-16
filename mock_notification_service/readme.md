#Mock Notification Server
_This is a mock notification server, with a web API, it is intented to be used with the rest of the Dronizone Project_
##API

The following routes are available to emulate a notification send:
- [POST]`/notifications/customer/{target_id}` to send a "customer notification" the expected json format is as follow
        
         
         {
           "target_id": 1,
           "customer_name": "George",
           "payload": "How are you today"
         }
- [POST]`/notifications/customer/{target_id}/order` to send a "customer notification" the expected json format is as follow 
       
        
         {
           "target_id": 1,
           "customer_name": "George",
           "payload": "Your order is on it's way",
           "orderName": "Hamilton the musical- Full album collection"
         }
- [POST]`/notifications/alert` to send a behaviour alert to Haley. The expected json format is as follow  
         
         
         {
           "target_id": 1,
           "order_id": 15,
           "payload": "I detected a strange customer behavior !!"
         }     
         
##Usage
To boot this mock as a stand alone you can
 - launch the spring boot app with your favorite tools
 - Build it with `mvn clean install` and launch the produces jar in target
 - Docker image, through the supplied Dockerfile: 
    
    `mvn clean install`
     
     `docker build --build-arg JAR_FILE="target/mock_notification_service-0.0.1-SNAPSHOT.jar --tag dronizone-mock-notification`
     
     `docker run -p 44444:8080 -d dronizone-mock-notification`

It is also spun up by default by the project docker compose file

##Monitoring
The incoming notifications are displayed in the console, 
for develpement convinience, a web interface is available at the server root
