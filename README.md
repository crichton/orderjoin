# orderjoiner


      This Project demonstrates a Kafka Streams App that joins messages from 2 topics and merges the data landing in a 3 topic. 



## Configuration and Quickstart

See `src/main/resources/application.yaml` for config settings

#### Build 
   
execute `./gradlew bootJar`


#### Run 

execute `java -jar build/libs/orderjoin-1.0.0-latest.jar `



## Data Governance 
  
The project uses Json Schema to define and validate the message models handled by the stream processing app.  Message models can be found in  src/main/resources/json. The project will generate these Pojo models during compilation. 

#### Topic Naming Conventions 

 **`<region>.<messageType>.<qualifier>.<format>.<error>`**

region      |=  `us` or `eu` or `ca`  
messageType |=  `order` or `product`     
qualifier   |=   `filtered` or `joined` or `<any-descriptive-word>`  
format      |=  `raw` or `canonical` or `formed`     
error       |=   `error` or `<nothing>`  


#### Data Error Topic   
 **us.order.raw.error**

## Testing 

There are TopologyTestDriver JUnit tests as well as EmbeddedKafka JUnit Integrations Tests. 






