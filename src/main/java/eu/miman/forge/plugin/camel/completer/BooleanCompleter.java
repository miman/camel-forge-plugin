package eu.miman.forge.plugin.camel.completer;

import java.util.Arrays;

import org.jboss.forge.shell.completer.SimpleTokenCompleter;

/**
 * @author Mikael
 *
 */
public class BooleanCompleter extends SimpleTokenCompleter
{

   /* (non-Javadoc)
    * @see org.jboss.forge.shell.completer.SimpleTokenCompleter#getCompletionTokens()
    */
   @Override
   public Iterable<?> getCompletionTokens()
   {
      return Arrays.asList(BooleanType.TRUE, BooleanType.FALSE);
   }

}
