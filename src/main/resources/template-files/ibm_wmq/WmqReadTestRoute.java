package ${package}.routes;

import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * This test class receives messages from a IBM Websphere MQ queue and writes the content to the log.
 */
@Component
public class WmqReadTestRoute extends SpringRouteBuilder
{

   @Override
   public void configure() throws Exception
   {
      from("wmq:MY.QUEUE.NAME")
            .log(">>> IBM WMQ JMS msg received: ${body}");
   }
}
