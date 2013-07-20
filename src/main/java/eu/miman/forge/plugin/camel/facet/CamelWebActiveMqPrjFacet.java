/**
 * 
 */
package eu.miman.forge.plugin.camel.facet;

import java.io.IOException;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

import eu.miman.forge.plugin.util.MimanBaseFacet;
import eu.miman.forge.plugin.util.helpers.DomFileHelper;
import eu.miman.forge.plugin.util.helpers.DomFileHelperImpl;

/**
 * This is the Facet class for Camel war projects with ActiveMQ support.
 * It makes sure the project has the correct dependencies for being a Camel web project with routes using ActiveMQ.
 * @author Mikael Thorman
 */
@Alias("camel-web-activemq-facet")
@RequiresFacet({ MavenCoreFacet.class, JavaSourceFacet.class,
	DependencyFacet.class, CamelWebPrjFacet.class })
public class CamelWebActiveMqPrjFacet extends MimanBaseFacet {

	// TODO there must be a better way to transfer data from the plugin to the Facet, but I don'y know it now.
	public static String brokerURL = "";
	
	DomFileHelper domFileHelper;

	public CamelWebActiveMqPrjFacet() {
		super();
		domFileHelper = new DomFileHelperImpl();
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.forge.project.Facet#install()
	 */
	@Override
	public boolean install() {
		super.install();
		configureProject();
		return true;
	}

	/* (non-Javadoc)
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
						&& dependency.getArtifactId().compareToIgnoreCase("activemq-pool") == 0) {
					return true;
				}
			}
			return false;
		}
		return reply;
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
		this.mergePomFileWithTemplate(pom);
		
		mvnFacet.setPOM(pom);	// Store the changed POM

		createApplicationContext(pom.getProjectDirectory().getAbsolutePath());
	}

	/**
	 * Updates the applicationContext.xml file with the definitions needed for an ActiveMQ supported project.
	 * 
	 * This is added:
	 * 	- A JMS Active MQ component with a connection factory
	 */
	private void createApplicationContext(String prjAbsolutePath) {
		String sourceUri = "/template-files/web/main/webapp/WEB-INF/applicationContext-activemq-fragment.xml";
		String targetUri = "src/main/webapp/WEB-INF/applicationContext.xml";
		
		try {
			Xpp3Dom dom = domFileHelper.readXmlResourceFile(sourceUri);
			String targetPath = prjAbsolutePath + "/" + targetUri;
			Xpp3Dom targetDom = domFileHelper.readXmlFile(targetPath);
			
			Xpp3Dom resultingDom = Xpp3DomUtils.mergeXpp3Dom(targetDom, dom, false);
			domFileHelper.writeXmlFile(targetPath, resultingDom);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected String getTargetPomFilePath() {
		return "/template-files/web/pom_activemq_fragment.xml";
	}
}
