/**
 * 
 */
package se.miman.forge.plugin.camel.facet;

import javax.inject.Inject;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

import se.miman.forge.plugin.util.MimanBaseFacet;

/**
 * This is the Facet class for a projects with ActiveMQ support. It makes sure
 * the project has the correct dependencies for being a Camel project containing
 * routes with ActiveMQ support.
 * 
 * @author Mikael Thorman
 */
@Alias("camel-activemq-route-facet")
@RequiresFacet({ MavenCoreFacet.class, JavaSourceFacet.class,
		DependencyFacet.class, CamelRoutePrjFacet.class })
public class CamelActiveMqRoutePrjFacet extends MimanBaseFacet {

	@Inject
	ProjectFactory prjFactory;

	@Inject
	private ShellPrintWriter writer;

	public CamelActiveMqRoutePrjFacet() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.forge.project.Facet#install()
	 */
	@Override
	public boolean install() {
		super.install();
		configureProject();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.forge.project.Facet#isInstalled()
	 */
	@Override
	public boolean isInstalled() {
		boolean reply = super.isInstalled();
		if (reply) {
			final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
			Model pom = mvnFacet.getPOM();

			java.util.List<Dependency> deps = pom.getDependencies();
			if (deps == null) {
				return false;
			}
			for (Dependency dependency : deps) {
				if (dependency.getGroupId().compareToIgnoreCase("org.apache.activemq") == 0 
						&& dependency.getArtifactId().compareToIgnoreCase("activemq-camel") == 0) {
					return true;
				}
			}
			return false;
		}
		return reply;
	}

	// Helper functions ****************************************
	/**
	 * Set the project parent to the main parent project. Changes the project to
	 * a pom project. Add the poms module
	 */
	private void configureProject() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();

		// Change the POM parent to parent project
		this.mergePomFileWithTemplate(pom);

		mvnFacet.setPOM(pom); // Store the changed POM
	}

	@Override
	protected String getTargetPomFilePath() {
		return "/template-files/route/activemq_pom_fragment.xml";
	}
}
