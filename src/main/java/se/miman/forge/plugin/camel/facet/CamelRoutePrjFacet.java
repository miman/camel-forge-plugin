/**
 * 
 */
package se.miman.forge.plugin.camel.facet;

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

import se.miman.forge.plugin.util.MimanBaseFacet;
import se.miman.forge.plugin.util.NazgulPrjUtil;
import se.miman.forge.plugin.util.dto.MavenProjectId;

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
	
	private NazgulPrjUtil nazgulPrjUtil;

	public CamelRoutePrjFacet() {
		super();
		nazgulPrjUtil = new NazgulPrjUtil();
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

		// We verify that we have a parent and that it ends with the default parent suffixes
		if (pom.getParent() == null) {
			return false;
		}
		if (!pom.getParent().getGroupId().endsWith("poms.parent")) {
			return false;
		}
		if (!pom.getParent().getArtifactId().endsWith("-parent")) {
			return false;
		}
		
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

		// Change the POM parent to parent project
		MavenProjectId prjId = nazgulPrjUtil.getParentProjectId(project, prjFactory);
		mergePomFileWithTemplate(pom);
		getMavenPomHelper().addOrUpdateParentProject(prjId, pom);
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
