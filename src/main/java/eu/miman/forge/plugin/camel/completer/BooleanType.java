package eu.miman.forge.plugin.camel.completer;

public enum BooleanType
{
   TRUE("true", "True"),
   FALSE("false", "False");

   private String type;
   private String description;

   private BooleanType(final String type, final String description)
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

   public static BooleanType from(String type)
   {
      BooleanType result = FALSE;
      if ((type != null) && !type.trim().isEmpty())
      {
         type = type.trim();
         for (BooleanType p : values())
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
