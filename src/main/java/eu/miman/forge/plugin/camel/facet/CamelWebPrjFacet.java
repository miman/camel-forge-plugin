/**
 * 
 */
package eu.miman.forge.plugin.camel.facet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.DependencyInstaller;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

import eu.miman.forge.plugin.util.NazgulPrjUtil;
import eu.miman.forge.plugin.util.VelocityUtil;
import eu.miman.forge.plugin.util.helpers.MavenPomHelper;
import eu.miman.forge.plugin.util.helpers.MavenPomHelperImpl;

/**
 * This is the Facet class for Camel war projects & artifacts 
 * @author Mikael Thorman
 */
@Alias("camel-web-facet")
@RequiresFacet({ MavenCoreFacet.class, JavaSourceFacet.class,
	DependencyFacet.class })
public class CamelWebPrjFacet extends BaseFacet {

	@Inject
	ProjectFactory prjFactory;

   @Inject
   private ShellPrintWriter writer;
   
   @Inject
   DependencyInstaller installer;
   
   MavenPomHelper pomHelper;
   
   // Dependencies
   List<Dependency> dependencies = null;
   private static final String CAMEL_CORE = "org.apache.camel:camel-core:${camel.version}";
   private static final String CAMEL_SPRING = "org.apache.camel:camel-spring:${camel.version}";
   private static final String CAMEL_STREAM = "org.apache.camel:camel-stream:${camel.version}";
   private static final String CAMEL_SERVLET = "org.apache.camel:camel-servlet:${camel.version}";
   private static final String CAMEL_HTTP = "org.apache.camel:camel-http:${camel.version}";
   private static final String CAMEL_JAXB = "org.apache.camel:camel-jaxb:${camel.version}";
   private static final String CAMEL_TEST_SPRING = "org.apache.camel:camel-test-spring:${camel.version}";
   
   private static final String SPRING_WEB = "org.springframework:spring-web:${spring.version}";
   private static final String SPRING_TX = "org.springframework:spring-tx:${spring.version}";
   private static final String SPRING_CONTEXT = "org.springframework:spring-context:${spring.version}";
   private static final String SPRING_AOP = "org.springframework:spring-aop:${spring.version}";
   
   private static final String JAXB_API = "javax.xml.bind:jaxb-api:${jaxb.version}";
   private static final String COMMONS_LANG = "commons-lang:commons-lang:${commons-lang.version}";

   // Dependency exclusions
   private static final String GERONIMO_SERVLET = "org.apache.geronimo.specs:geronimo-servlet_2.5_spec";
   
   List<Plugin> plugins = null;
   Properties properties = null;
   
	/**
	 * The velocity engine used to replace data in the supplied templates with the correct info.
	 */
	private final VelocityEngine velocityEngine;
	
	// TODO there must be a better way to transfer data from the plugin to the Facet, but I don'y know it now.
	public static String camelBasePackage = "";
	public static String prjDescription = "Web project for Camel routes";
	
	private NazgulPrjUtil nazgulPrjUtil;
	private VelocityUtil velocityUtil;

	public CamelWebPrjFacet() {
		super();
		pomHelper = new MavenPomHelperImpl();
		nazgulPrjUtil = new NazgulPrjUtil();
		velocityUtil = new VelocityUtil();
		
		velocityEngine = new VelocityEngine();
		velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER,
				"classpath");
		velocityEngine.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		velocityEngine.setProperty(
				RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
				"org.apache.velocity.runtime.log.JdkLogChute");

	}

	/* (non-Javadoc)
	 * @see org.jboss.forge.project.Facet#install()
	 */
	@Override
	public boolean install() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();
		Properties propsInPom = pom.getProperties();
		for (Entry<Object, Object> req : getRequiredProperties().entrySet()) {
			if (!propsInPom.containsKey(req.getKey())) {
				propsInPom.setProperty((String)req.getKey(), (String)req.getValue());
			}
		}

		pom.setDescription(prjDescription);
		pom.setPackaging("war");
		mvnFacet.setPOM(pom);
		
		DependencyFacet deps = project.getFacet(DependencyFacet.class);
		for (Dependency requirement : getRequiredDependencies()) {
			if (!deps.hasEffectiveDependency(requirement)) {
				deps.addDirectDependency(requirement);
			}
		}
		
		final MavenCoreFacet mvnFacet2 = project.getFacet(MavenCoreFacet.class);
		pom = mvnFacet2.getPOM();
		Map<String, Plugin> pluginMap = pom.getBuild().getPluginsAsMap();
		for (Plugin req : getRequiredPlugins()) {
			if (!pluginMap.containsKey(req.getGroupId() + ":" + req.getArtifactId())) {
				pom.getBuild().getPlugins().add(req);
			}
		}

		mvnFacet.setPOM(pom);

		configureProject();
		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.jboss.forge.project.Facet#isInstalled()
	 */
	@Override
	public boolean isInstalled() {
		DependencyFacet deps = project.getFacet(DependencyFacet.class);

        for (Dependency requirement : getRequiredDependencies())
        {
            if(!deps.hasEffectiveDependency(requirement))
            {
                return false;
            }
        }		
		return true;
	}

	// Helper functions ****************************************
	protected List<Dependency> getRequiredDependencies()
    {
		if (dependencies == null) {
			dependencies = new ArrayList<Dependency>();
			
			dependencies.add(DependencyBuilder.create(CAMEL_CORE));
			dependencies.add(DependencyBuilder.create(CAMEL_SPRING));
			dependencies.add(DependencyBuilder.create(CAMEL_STREAM));
			dependencies.add(DependencyBuilder.create(CAMEL_SERVLET));
			
			dependencies.add(DependencyBuilder.create(CAMEL_JAXB));
			dependencies.add(DependencyBuilder.create(CAMEL_TEST_SPRING));
			
			Dependency dep = DependencyBuilder.create(CAMEL_HTTP);
			dep.getExcludedDependencies().add(DependencyBuilder.create(GERONIMO_SERVLET));
			dependencies.add(dep);
			
			dependencies.add(DependencyBuilder.create(SPRING_WEB));
			dependencies.add(DependencyBuilder.create(SPRING_TX));
			dependencies.add(DependencyBuilder.create(SPRING_CONTEXT));
			dependencies.add(DependencyBuilder.create(SPRING_AOP));
			
			dependencies.add(DependencyBuilder.create(JAXB_API));
			dependencies.add(DependencyBuilder.create(COMMONS_LANG));
		}
		return dependencies;
    }

	protected Properties getRequiredProperties() {
		if (properties == null) {
			properties = new Properties();
			properties.setProperty("camel.version", "2.10.4");
			properties.setProperty("spring.version", "3.2.4.RELEASE");
			properties.setProperty("maven-compiler.version", "2.5.1");
			properties.setProperty("maven-jetty.version", "7.5.4.v20111024");
			properties.setProperty("maven-eclipse.version", "2.9");
			properties.setProperty("jaxb.version", "2.1");
			properties.setProperty("commons-lang.version", "2.5");
		}
		return properties;
    }
	
	protected List<Plugin> getRequiredPlugins()
    {
		if (plugins == null) {
			plugins = new ArrayList<Plugin>();
			Plugin p;
			Xpp3Dom config;
			Xpp3Dom childElem;
			
			p = new Plugin();
			p.setGroupId("org.apache.maven.plugins");
			p.setArtifactId("maven-compiler-plugin");
			p.setVersion("${maven-compiler.version}");
			
			if (p.getConfiguration() == null) {
				p.setConfiguration(new Xpp3Dom("configuration"));
			}
			config = (Xpp3Dom) p.getConfiguration();
			childElem = new Xpp3Dom("source");
			childElem.setValue("1.6");
			config.addChild(childElem);
			
			childElem = new Xpp3Dom("target");
			childElem.setValue("1.6");
			config.addChild(childElem);
			
			plugins.add(p);
			
			// Add jetty-maven-plugin
			p = new Plugin();
			p.setGroupId("org.mortbay.jetty");
			p.setArtifactId("jetty-maven-plugin");
			p.setVersion("${maven-jetty.version}");
			
			if (p.getConfiguration() == null) {
				p.setConfiguration(new Xpp3Dom("configuration"));
			}
			config = (Xpp3Dom) p.getConfiguration();
			childElem = new Xpp3Dom("webAppConfig");
			Xpp3Dom subChildElem = new Xpp3Dom("contextPath");
			subChildElem.setValue("/");
			childElem.addChild(subChildElem);
			config.addChild(childElem);
			
			childElem = new Xpp3Dom("systemProperties");
			subChildElem = new Xpp3Dom("systemProperty");
			Xpp3Dom subSubChildElem = new Xpp3Dom("name");
			subSubChildElem.setValue("com.sun.management.jmxremote");
			subChildElem.addChild(subSubChildElem);
			
			subSubChildElem = new Xpp3Dom("value");
			subChildElem.addChild(subSubChildElem);
			
			childElem.addChild(subChildElem);
			config.addChild(childElem);

			childElem = new Xpp3Dom("systemProperties");
			childElem.setValue("10");
			config.addChild(childElem);

			plugins.add(p);
			
			// Add eclipse-plugin
			p = new Plugin();
			p.setGroupId("org.apache.maven.plugins");
			p.setArtifactId("maven-eclipse-plugin");
			p.setVersion("${maven-eclipse.version}");
			
			if (p.getConfiguration() == null) {
				p.setConfiguration(new Xpp3Dom("configuration"));
			}
			config = (Xpp3Dom) p.getConfiguration();
			childElem = new Xpp3Dom("wtpmanifest");
			childElem.setValue("true");
			config.addChild(childElem);
			
			childElem = new Xpp3Dom("wtpapplicationxml");
			childElem.setValue("true");
			config.addChild(childElem);
			
			childElem = new Xpp3Dom("wtpversion");
			childElem.setValue("2.0");
			config.addChild(childElem);
			
			plugins.add(p);

		}
		return plugins;
    }
	
	
	/**
	 * Set the project parent to the main parent project.
	 * Changes the project to a pom project.
	 * Add the poms module
	 */
	private void configureProject() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();
		
		if (camelBasePackage == null) {
			// If no camel base package is set, we use the group-id of the parent pom as the default path
			Project rootPrj = nazgulPrjUtil.findRootProject(project, prjFactory);
			if (rootPrj == null) {
				// The root project cannot be found -> use this projects group-id
				camelBasePackage = pom.getGroupId().trim().replace("-", "").replace("_", "");
			} else {
				final MavenCoreFacet rootPrjMvnFacet = rootPrj.getFacet(MavenCoreFacet.class);
				Model rootPrjPom = rootPrjMvnFacet.getPOM();

				camelBasePackage = rootPrjPom.getGroupId().trim().replace("-", "").replace("_", "");
			}
		}
		createApplicationContext(camelBasePackage);
		createWebXml();
		createLog4jProperties();
		createReadMeTxt();
	}


	/**
	 * Creates a applicationContext.xml file based on the template located 
	 * at src/main/resources/template-files/web/main/webapp/WEB-INF/applicationContext.xml
	 * @param parentPrjName	The name of the parent folder
	 */
	private void createApplicationContext(String camelBasePackage) {
		String parentPomUri = "/template-files/web/main/webapp/WEB-INF/applicationContext.xml";
		
		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		velocityPlaceholderMap.put("camelBasePackage", camelBasePackage);
		
	    // Replace the current pom with the copied/merged
		String targetUri = "WEB-INF/applicationContext.xml";
		VelocityContext velocityContext = velocityUtil.createVelocityContext(velocityPlaceholderMap);
		velocityUtil.createWebResourceAbsolute(parentPomUri, velocityContext, targetUri, project, velocityEngine);
	}

	/**
	 * Creates a web.xml file based on the template located 
	 * at src/main/resources/template-files/web/main/webapp/WEB-INF/web.xml
	 */
	private void createWebXml() {
		String parentPomUri = "/template-files/web/main/webapp/WEB-INF/web.xml";
		
		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		
	    // Replace the current pom with the copied/merged
		String targetUri = "WEB-INF/web.xml";
		VelocityContext velocityContext = velocityUtil.createVelocityContext(velocityPlaceholderMap);
		velocityUtil.createWebResourceAbsolute(parentPomUri, velocityContext, targetUri, project, velocityEngine);
	}

	/**
	 * Creates a log4j config file based on the template located 
	 * at src/main/resources/template-files/web/main/resources/log4j.properties
	 */
	private void createLog4jProperties() {
		String parentPomUri = "/template-files/web/main/resources/log4j.properties";
		
		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		
	    // Replace the current pom with the copied/merged
		String targetUri = "log4j.properties";
		VelocityContext velocityContext = velocityUtil.createVelocityContext(velocityPlaceholderMap);
		velocityUtil.createResourceAbsolute(parentPomUri, velocityContext, targetUri, project, velocityEngine);
	}

	/**
	 * Creates a read-me.txt file based on the template located 
	 * at src/main/resources/template-files/web/read-me.txt
	 */
	private void createReadMeTxt() {
		String parentPomUri = "/template-files/web/read-me.txt";
		
		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		
	    // Replace the current pom with the copied/merged
		String targetUri = "../../../read-me.txt";
		VelocityContext velocityContext = velocityUtil.createVelocityContext(velocityPlaceholderMap);
		velocityUtil.createResourceAbsolute(parentPomUri, velocityContext, targetUri, project, velocityEngine);
	}
}
