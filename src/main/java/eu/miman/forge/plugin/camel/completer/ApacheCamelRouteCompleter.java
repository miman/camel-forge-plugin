/**
 * 
 */
package eu.miman.forge.plugin.camel.completer;

import java.util.Arrays;

import org.jboss.forge.shell.completer.SimpleTokenCompleter;

/**
 * @author Mikael
 *
 */
public class ApacheCamelRouteCompleter extends SimpleTokenCompleter {

	/* (non-Javadoc)
	 * @see org.jboss.forge.shell.completer.SimpleTokenCompleter#getCompletionTokens()
	 */
	@Override
	public Iterable<?> getCompletionTokens() {
		return Arrays.asList(ApacheCamelRouteType.JMS, ApacheCamelRouteType.DIRECT, ApacheCamelRouteType.FILE, ApacheCamelRouteType.RESTLET, ApacheCamelRouteType.TIMER);
	}

}
