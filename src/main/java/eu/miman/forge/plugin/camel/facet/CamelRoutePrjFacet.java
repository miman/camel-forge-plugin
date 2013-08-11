/**
 * 
 */
package eu.miman.forge.plugin.camel.facet;

import java.util.List;

import javax.inject.Inject;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

import eu.miman.forge.plugin.util.MimanBaseFacet;

/**
 * This is the Facet class for Camel jar projects & artifacts 
 * @author Mikael Thorman
 */
@Alias("camel-route-facet")
@RequiresFacet({ MavenCoreFacet.class, JavaSourceFacet.class,
	DependencyFacet.class })
public class CamelRoutePrjFacet extends MimanBaseFacet {

	@Inject
	ProjectFactory prjFactory;
	
	private String prjDescription = "JAR project for Camel routes";
	
	public CamelRoutePrjFacet() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.forge.project.Facet#install()
	 */
	@Override
	public boolean install() {
		configureProject();
		return true;
	}

	/* (non-Javadoc)
	 * @see org.jboss.forge.project.Facet#isInstalled()
	 */
	@Override
	public boolean isInstalled() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();

		List<Dependency> deps = pom.getDependencies();
		boolean dependenciesOk = false;
		for (Dependency dependency : deps) {
			if (dependency.getGroupId().equals("org.apache.camel") && dependency.getArtifactId().equals("camel-core")) {
				dependenciesOk = true;
			}
		}
		
		return dependenciesOk;
	}

	// Helper functions ****************************************
	/**
	 * Set the project parent to the main parent project.
	 * Changes the project to a pom project.
	 * Add the poms module
	 */
	private void configureProject() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();

		mergePomFileWithTemplate(pom);
		pom.setDescription(prjDescription);
		mvnFacet.setPOM(pom);
	}

	public void setPrjDescription(String prjDescription) {
		this.prjDescription = prjDescription;
	}

	@Override
	protected String getTargetPomFilePath() {
		return "/template-files/route/pom.xml";
	}
}
