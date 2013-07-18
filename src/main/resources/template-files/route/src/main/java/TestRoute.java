/**
 * 
 */
package ${package};

import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * @author Mikael
 *
 */
@Component
public class TestRoute extends SpringRouteBuilder {

	/* (non-Javadoc)
	 * @see org.apache.camel.builder.RouteBuilder${symbol_pound}configure()
	 */
	@Override
	public void configure() throws Exception {
        from("timer://${package}.started?repeatCount=1")
    	.log(">>> TestRoute")
    	.to("stream:out");
	}

}
