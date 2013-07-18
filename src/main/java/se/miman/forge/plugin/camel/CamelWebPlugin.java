package se.miman.forge.plugin.camel;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.SetupCommand;

import se.miman.forge.plugin.camel.facet.CamelWebActiveMqPrjFacet;
import se.miman.forge.plugin.camel.facet.CamelWebPrjFacet;
import se.miman.forge.plugin.camel.facet.CamelWebRestletPrjFacet;

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
//			@Option(name = "brokerURL", shortName = "url") String brokerURL, 
								PipeOut out) {

		if (!project.hasFacet(CamelWebActiveMqPrjFacet.class)) {
			event.fire(new InstallFacets(CamelWebActiveMqPrjFacet.class));
		}
	}
}
