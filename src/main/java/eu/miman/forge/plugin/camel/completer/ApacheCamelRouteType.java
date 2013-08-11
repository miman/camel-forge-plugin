package eu.miman.forge.plugin.camel.completer;


public enum ApacheCamelRouteType {
	   NONE("", "None"),
	   JMS("jms", "JMS triggered Route"),
	   RESTLET("restlet", "Restlet triggered Route"),
	   FILE("file", "File triggered Route"),
	   DIRECT("direct", "Direct triggered Route"),
	   TIMER("timer", "Timer triggered Route");

	   private String type;
	   private String description;

	   private ApacheCamelRouteType(final String type, final String description)
	   {
	      setType(type);
	      setDescription(description);
	   }

	   public String getType()
	   {
	      return type;
	   }

	   private void setType(String type)
	   {
	      if (type != null)
	      {
	         type = type.trim().toLowerCase();
	      }
	      this.type = type;
	   }

	   @Override
	   public String toString()
	   {
	      return type;
	   }

	   public String getDescription()
	   {
	      return description;
	   }

	   private void setDescription(final String description)
	   {
	      this.description = description;
	   }

	   public static ApacheCamelRouteType from(String type)
	   {
		   ApacheCamelRouteType result = NONE;
	      if ((type != null) && !type.trim().isEmpty())
	      {
	         type = type.trim();
	         for (ApacheCamelRouteType p : values())
	         {
	            if (p.getType().equals(type) || p.name().equalsIgnoreCase(type))
	            {
	               result = p;
	               break;
	            }
	         }
	      }
	      return result;
	   }
}
