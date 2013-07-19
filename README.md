camel-forge-plugin
==================

JBoss Forge plugins for Apache Camel

Apache Camel WAR plugin
-----------------------

This is a plugin for creating a sub-project which is a web project that will run any camel routes in its path.

The purpose is to create one project like this and then a number of camel routes projects with the archetype described below which contains the actual routes.


Apache Camel route plugin
-------------------------

This is a plugin for creating a sub-project which contains one or more camel routes and any classes needed by these routes (transformations...). This is then run by the web project created with camel-war plugin.


More info around this plugin can be found at the Wiki [here] (https://github.com/miman/camel-forge-plugin/wiki/Camel-JBoss-Forge-plugin-description)
