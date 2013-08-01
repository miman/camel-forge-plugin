package eu.miman.forge.plugin.camel;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.maven.model.Model;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.SetupCommand;

import eu.miman.forge.plugin.camel.facet.CamelWebActiveMqPrjFacet;
import eu.miman.forge.plugin.camel.facet.CamelWebPrjFacet;
import eu.miman.forge.plugin.camel.facet.CamelWebRestletPrjFacet;
import eu.miman.forge.plugin.camel.facet.CamelWebWebsphereMqPrjFacet;
import eu.miman.forge.plugin.util.VelocityUtil;


/**
 * Modifies a project to be a Spring based WAR project that can run Apache Camel
 * routes.
 * 
 * @author Mikael Thorman
 */
@Alias("camel-web")
@Help("A plugin that helps to build Apache Camel Web artifacts")
@RequiresProject
public class CamelWebPlugin implements Plugin {
	@Inject
	private Event<InstallFacets> event;

	@Inject
	private Project project;

	@Inject
	private ShellPrintWriter writer;
	
	/**
	 * The velocity engine used to replace data in the supplied templates with the correct info.
	 */
	private final VelocityEngine velocityEngine;
	private VelocityUtil velocityUtil;
	
	public CamelWebPlugin() {
		super();
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

	/**
	 * The setup command converts a project to a war project that can run Apache
	 * Camel routes.
	 * 
	 * @param camelBasePackage
	 *            The package where the camel routes can be found (used to know
	 *            where to scan for Camel annotated classes)
	 * @param prjDescription
	 *            The description of the project (used in the project pom file.
	 * @param out
	 *            Used to write info back to the user.
	 */
	@SetupCommand
	@Command(value = "setup", help = "Convert the project to a Spring based WAR project that can run Apache Camel routes")
	public void setup(
			@Option(name = "camelBasePackage", shortName = "cbp") String camelBasePackage,
			@Option(name = "prjDescription", shortName = "pd") String prjDescription,
			@Option(name = "restletSupport", shortName = "rs") String restletSupport,
			PipeOut out) {

		// if (camelBasePackage == null) {
		// ShellMessages.info(out, "You must supply a camelBasePackage!");
		// return;
		// }
		CamelWebPrjFacet.camelBasePackage = camelBasePackage;
		CamelWebPrjFacet.prjDescription = prjDescription;
		if (!project.hasFacet(CamelWebPrjFacet.class)) {
			event.fire(new InstallFacets(CamelWebPrjFacet.class));
		} else {
			ShellMessages.info(out, "Project already an Camel project.");
		}

		if (restletSupport != null && "true".equalsIgnoreCase(restletSupport)) {
			if (!project.hasFacet(CamelWebRestletPrjFacet.class)) {
				event.fire(new InstallFacets(CamelWebRestletPrjFacet.class));
			}
		}
	}

	@Command(value = "add-restlet-support", help = "Adds support for Restlets in a Spring based WAR project that can run Apache Camel routes")
	public void addRestleteSupport(PipeOut out) {

		if (!project.hasFacet(CamelWebRestletPrjFacet.class)) {
			event.fire(new InstallFacets(CamelWebRestletPrjFacet.class));
		}
	}

	@Command(value = "add-activemq-support", help = "Adds support for ActiveMQ in a Spring based WAR project that can run Apache Camel routes")
	public void addActiveMqSupport(
								PipeOut out) {

		if (!project.hasFacet(CamelWebActiveMqPrjFacet.class)) {
			event.fire(new InstallFacets(CamelWebActiveMqPrjFacet.class));
		}
	}

	@Command(value = "add-webspheremq-support", help = "Adds support for Websphere MQ in a Spring based WAR project that can run Apache Camel routes")
	public void addWebsphereMqSupport(
			@Option(name = "wmqHostname") String wmqHostname, 
			@Option(name = "wmqPort") String wmqPort,
			@Option(name = "wmqQueueManager") String wmqQueueManager,
			@Option(name = "wmqChannel") String wmqChannel,
			@Option(name = "addExampleRoutes") Boolean addExampleRoutes,
								PipeOut out) {

		if (!project.hasFacet(CamelWebPrjFacet.class)) {
			ShellMessages.warn(writer, "This is not Camel Web project, please run 'camel-web setup' before running this command");
			return;
		}
		
		if (!project.hasFacet(CamelWebWebsphereMqPrjFacet.class)) {
			CamelWebWebsphereMqPrjFacet.wmqChannel = wmqChannel;
			CamelWebWebsphereMqPrjFacet.wmqHostname = wmqHostname;
			CamelWebWebsphereMqPrjFacet.wmqPort = wmqPort;
			CamelWebWebsphereMqPrjFacet.wmqQueueManager = wmqQueueManager;
			
			event.fire(new InstallFacets(CamelWebWebsphereMqPrjFacet.class));
		}
		if (addExampleRoutes != null && addExampleRoutes) {
			createWmqTestRoute("WmqReadTestRoute.java");
			createWmqTestRoute("WmqWriteTestRoute.java");
		}
	}

	@Command(value = "add-webspheremq-example-routes", help = "Adds Example Camel routes for Websphere MQ")
	public void addWebsphereMqExampleFiles(PipeOut out) {

		if (!project.hasFacet(CamelWebPrjFacet.class)) {
			ShellMessages.warn(writer, "This is not Camel Web project, please run 'camel-web setup' before running this command");
			return;
		}
		
		if (!project.hasFacet(CamelWebWebsphereMqPrjFacet.class)) {
			ShellMessages.warn(writer, "This is not Camel Web project with websphere support, please run 'camel-web add-webspheremq-support ...' before running this command");
		} else {
			createWmqTestRoute("WmqReadTestRoute.java");
			createWmqTestRoute("WmqWriteTestRoute.java");
		}
	}
	
	/**
	 * Creates a TestRoute.java file based on the template located 
	 * at src/main/resources/template-files/camel/route/src/main/java/TestRoute.java
	 */
	private void createWmqTestRoute(String filename) {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();
		String parentPomUri = "/template-files/ibm_wmq/" + filename;
		
		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		velocityPlaceholderMap.put("package", pom.getGroupId());
		
	    // Replace the current pom with the copied/merged
		VelocityContext velocityContext = velocityUtil.createVelocityContext(velocityPlaceholderMap);
		velocityUtil.createJavaSource(parentPomUri, velocityContext, project, velocityEngine);
	}
}
