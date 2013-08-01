package ${package}.routes;

import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * This test class sends one message to a IBM Websphere MQ queue once.
 */
@Component
public class WmqWriteTestRoute extends SpringRouteBuilder
{

   @Override
   public void configure() throws Exception
   {
      from("timer://ibm.wmq.test?repeatCount=1")
      		.setBody(simple("New body"))
            .log(">>> sending IBM WMQ JMS msg: ${body}")
            .to("wmq:MY.QUEUE.NAME");
   }
}
