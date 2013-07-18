package se.miman.forge.plugin.camel.facet.helper;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * A helper class for adding Camel dependencies to a Camel Web project
 * @author Mikael Thorman
 */
public class CamelWebPrjDependencyHelper {

//	public void addCamelDependencies(Project project, ProjectFactory prjFactory, Model pom) {
//		List<Dependency> deps = pom.getDependencies();
//		addOrReplaceDependency("org.apache.camel", "camel-core", DependencyVersions.CAMEL_VERSION_REF, deps);
//		addOrReplaceDependency("org.apache.camel", "camel-spring", DependencyVersions.CAMEL_VERSION_REF, deps);
//		addOrReplaceDependency("org.apache.camel", "camel-stream", DependencyVersions.CAMEL_VERSION_REF, deps);
//		addOrReplaceDependency("org.apache.camel", "camel-servlet", DependencyVersions.CAMEL_VERSION_REF, deps);
//		Dependency dep = addOrReplaceDependency("org.apache.camel", "camel-http", DependencyVersions.CAMEL_VERSION_REF, deps);
//		Exclusion exclusion = new Exclusion();
//		exclusion.setGroupId("org.apache.geronimo.specs");
//		exclusion.setArtifactId("geronimo-servlet_2.5_spec");
//		dep.addExclusion(exclusion);
//		addOrReplaceDependency("org.apache.camel", "camel-jaxb", DependencyVersions.CAMEL_VERSION_REF, deps);
//		addOrReplaceDependency("org.apache.camel", "camel-restlet", DependencyVersions.CAMEL_VERSION_REF, deps);
//		
//		// testing
//		dep = addOrReplaceDependency("org.apache.camel", "camel-test-spring", DependencyVersions.CAMEL_VERSION_REF, deps);
//		dep.setScope("test");
//	}
	
	/**
	 * Add plugin so you can run mvn jetty:run
	 * @param pom
	 */
	protected void addJettyRunPlugin(Model pom) {
		Build build = pom.getBuild();
		if (build != null) {
			build = new Build();
		}
		java.util.Map<String, Plugin> plugins = build.getPluginsAsMap();
		Plugin plugin = plugins.get("org.mortbay.jetty:jetty-maven-plugin");
		if (plugin == null) {
			plugin = new Plugin();
			plugin.setGroupId("org.mortbay.jetty");
			plugin.setArtifactId("jetty-maven-plugin");
		}
		plugin.setVersion("7.5.4.v20111024");
		Xpp3Dom a = (Xpp3Dom)plugin.getConfiguration();
		if (a == null) {
			a = new Xpp3Dom("configuration");
		}
		Xpp3Dom child = new Xpp3Dom("webAppConfig");
		a.addChild(child);
		child.addChild(new Xpp3Dom("contextPath"));
		
		child = new Xpp3Dom("systemProperties");
		a.addChild(child);
		Xpp3Dom c2 = new Xpp3Dom("systemProperty");
		child.addChild(c2);
		Xpp3Dom c3 = new Xpp3Dom("name");
		c3.setValue("com.sun.management.jmxremote");
		c2.addChild(c3);
		c2.addChild(new Xpp3Dom("value"));
		
		c2 = new Xpp3Dom("scanIntervalSeconds");
		c2.setValue("10");
		child.addChild(c2);
	}
}
