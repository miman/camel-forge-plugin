package eu.miman.forge.plugin.camel;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.parser.java.Method;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.resources.java.JavaResource;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.SetupCommand;

import eu.miman.forge.plugin.camel.completer.ApacheCamelRouteCompleter;
import eu.miman.forge.plugin.camel.completer.ApacheCamelRouteType;
import eu.miman.forge.plugin.camel.completer.BooleanCompleter;
import eu.miman.forge.plugin.camel.facet.CamelActiveMqRoutePrjFacet;
import eu.miman.forge.plugin.camel.facet.CamelRoutePrjFacet;
import eu.miman.forge.plugin.util.NazgulPrjUtil;
import eu.miman.forge.plugin.util.VelocityUtil;

/**
 * Modifies a project to be a Spring based WAR project that can run Apache Camel
 * routes.
 * 
 * @author Mikael Thorman
 */
@Alias("camel-route")
@Help("A plugin that helps to build Apache Camel Route artifacts")
@RequiresProject
public class CamelRoutePlugin implements Plugin {
	@Inject
	private Event<InstallFacets> event;

	@Inject
	private Project project;

	@Inject
	ProjectFactory prjFactory;
	
	/**
	 * The velocity engine used to replace data in the supplied templates with the correct info.
	 */
	private final VelocityEngine velocityEngine;
	private VelocityUtil velocityUtil;

	private NazgulPrjUtil nazgulPrjUtil;

	public CamelRoutePlugin() {
		super();
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
	
	/**
	 * The setup command converts a project to a jar project that can run Apache
	 * Camel routes.
	 * 
	 * @param camelBasePackage
	 *            The package where the camel routes can be found (used to know
	 *            where to scan for Camel annotated classes)
	 * @param prjDescription
	 *            The description of the project (used in the project pom file.
	 * @param warProjectName
	 *            The maven artifactId of the web project that should run the
	 *            routes in this project (will add the dependency to this
	 *            project to that project).
	 * @param out
	 *            Used to write info back to the user.
	 */
	@SetupCommand
	@Command(value = "setup", help = "Convert the project to a Spring based JAR project that can contain Apache Camel routes")
	public void setup(
			@Option(name = "prjDescription", shortName = "pd") String prjDescription,
			@Option(name = "warProjectArtifactId", shortName = "wpa") String warProjectArtifactId,
			@Option(name = "addTestRoute", shortName = "atr", completer = BooleanCompleter.class) boolean addTestRoute,
			PipeOut out) {

		if (!project.hasFacet(CamelRoutePrjFacet.class)) {
			event.fire(new InstallFacets(CamelRoutePrjFacet.class));
			project.getFacet(CamelRoutePrjFacet.class).setPrjDescription(prjDescription);
			if (addTestRoute) {
				createTestRoute();
			}
			if (warProjectArtifactId != null) {
				updateWarProjectDependencies(warProjectArtifactId);
			}
		} else {
			ShellMessages.info(out, "Project already an Camel routes project.");
		}
	}

	/**
	 * Command to create a test camel route that only regularly writes something to stdout
	 * @param out
	 */
	@Command(value = "add-test-route", help = "Adds a Test Apache Camel route to this project")
	public void addTestRoute(PipeOut out) {
		createTestRoute();
	}

	/**
	 * Command to create a test camel route that only regularly writes something to stdout
	 * @param name	The wanted name of the route
	 * @param path	Where the route should be created (under java/src)
	 * @param type	What type of route we should create (auto-complete type)
	 * @param out	Error info statements are written to this pipe to be displayed to the user
	 */
	@Command(value = "add-route", help = "Adds an Apache Camel route to this project")
	public void addRoute(@Option(name = "name", required=true) String name,
			@Option(name = "path", required=false) String path,
			@Option(name = "type", completer=ApacheCamelRouteCompleter.class, required=true) String type,
			PipeOut out) {
		
		if (type.compareToIgnoreCase(ApacheCamelRouteType.JMS.getType()) == 0) {
			// Make sure 
			if (!project.hasFacet(CamelActiveMqRoutePrjFacet.class)) {
				event.fire(new InstallFacets(CamelActiveMqRoutePrjFacet.class));
			}
			// TODO create actual route
		} else {
			System.out.println("Not implemented yet !");	
		}
	}

	/**
	 * Command to create a camel JMS route and add the needed dependencies
	 * @param out
	 */
	@Command(value = "add-jms-route", help = "Adds an Apache Camel ActiveMQ JMS route to this project")
	public void addJmsRoute(@Option(name = "name", required=true) String name,
			@Option(name = "path", required=false) String path,
			@Option(name = "queueName", shortName = "q", required=false) String qName,
			PipeOut out) {
		
		if (!project.hasFacet(CamelActiveMqRoutePrjFacet.class)) {
			event.fire(new InstallFacets(CamelActiveMqRoutePrjFacet.class));
		}
		createJmsRouteClass(name, path, qName, out);
	}

	// ############################# Helper functions ##############################

	/**
	 * Creates a JMS Camel route example class with the given name, Queue name & at the wanted location.
	 * 
	 * The defaul 
	 * @param name	The wanted name of the class
	 * @param path	The path where the class should be created
	 * @param qName	The wanted queue name for this JMS route 
	 * @param out	An out pipe to write error info on
	 */
	private void createJmsRouteClass(String name, String path, String qName,
			PipeOut out) {
//		MavenJavaSourceFacet javaMavenFacet = project.getFacet(MavenJavaSourceFacet.class);
		JavaSourceFacet javaFacet = project.getFacet(JavaSourceFacet.class);
		
		String usedPath = path;
		if (usedPath == null) {
			usedPath = javaFacet.getBasePackage();
		} else {
			// Replace ~ with base package if it starts the given path
			if (usedPath.startsWith("~")) {
				usedPath = javaFacet.getBasePackage() + usedPath.replaceAll("~", "");
			}
		}
		String usedQname = qName;
		if (usedQname == null) {
			usedQname = "TEST.QUEUE.NAME";
		}
		
		String fullPath = (usedPath + "." + name).replaceAll("\\.", "/") + ".java";
		
		JavaResource javaSrcResource = javaFacet.getSourceFolder().getChildOfType(JavaResource.class, fullPath);

        if (!javaSrcResource.exists()) {
            if (javaSrcResource.createNewFile()) {
                JavaClass javaTestClass = JavaParser.create(JavaClass.class);
                javaTestClass.setName(javaFacet.calculateName(javaSrcResource));
                javaTestClass.setPackage(javaFacet.calculatePackage(javaSrcResource));

                javaTestClass.addImport("org.apache.camel.spring.SpringRouteBuilder");
                javaTestClass.addImport("org.springframework.stereotype.Component");
                javaTestClass.addAnnotation("Component");
                
                javaTestClass.setSuperType("SpringRouteBuilder");

                String m1 = new StringBuilder("public void configure() throws Exception {")
                .append("\r\n")
                .append("from(\"jms:").append(usedQname + ".REMOVE_ME").append("\")")
                .append("\r\n")
                .append(".log(\"Received msg: ${body}\")")
                .append("\r\n")
                .append(".to(\"jms:").append(usedQname + ".REMOVE_ME").append("?jmsMessageType=Text\");")
                .append("\r\n")
                .append("}").toString();
                
                @SuppressWarnings("rawtypes")
				Method m = javaTestClass.addMethod(m1);
                m.addAnnotation("Override");
                
                javaSrcResource.setContents(javaTestClass);
            } else {
                ShellMessages.error(out, "Cannot create class [" + javaSrcResource.getFullyQualifiedName() + "]");
            }
        } else {
            ShellMessages.error(out, "Class already exists [" + javaSrcResource.getFullyQualifiedName() + "]");
        }
	}

	/**
	 * Creates a TestRoute.java file based on the template located 
	 * at src/main/resources/template-files/camel/route/src/main/java/TestRoute.java
	 */
	private void createTestRoute() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();
		String parentPomUri = "/template-files/route/src/main/java/TestRoute.java";
		
		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		velocityPlaceholderMap.put("package", pom.getGroupId() + ".routes");
		
	    // Replace the current pom with the copied/merged
		VelocityContext velocityContext = velocityUtil.createVelocityContext(velocityPlaceholderMap);
		velocityUtil.createJavaSource(parentPomUri, velocityContext, project, velocityEngine);
	}
	
	/**
	 * The Maven project with the given artifactId will be updated with dependencies to this project.
	 * @param warProjectArtifactId
	 */
	private void updateWarProjectDependencies(String warProjectArtifactId) {
		if (warProjectArtifactId == null) {
			// No project supplied -> do nothing
			return;
		}

		Project prj = nazgulPrjUtil.findProjectWithArtifactId(project, warProjectArtifactId, prjFactory);
		if (prj != null) {
			final MavenCoreFacet mvnFacet = prj.getFacet(MavenCoreFacet.class);
			Model pom = mvnFacet.getPOM();
			final MavenCoreFacet thisPrjMvnFacet = project.getFacet(MavenCoreFacet.class);
			Model thisPrjPom = thisPrjMvnFacet.getPOM();
			
			Dependency dependency = new Dependency();
			dependency.setGroupId(thisPrjPom.getGroupId());
			dependency.setArtifactId(thisPrjPom.getArtifactId());
			dependency.setVersion(thisPrjPom.getVersion());
			
			pom.addDependency(dependency);
			mvnFacet.setPOM(pom);
		}
	}

}
