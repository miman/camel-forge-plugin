/**
 * 
 */
package se.miman.forge.plugin.camel.facet;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

import se.miman.forge.plugin.util.MimanBaseFacet;
import se.miman.forge.plugin.util.helpers.DomFileHelper;
import se.miman.forge.plugin.util.helpers.DomFileHelperImpl;

/**
 * This is the Facet class for Camel war projects with Restlet support.
 * It makes sure the project has the correct dependencies for being a Camel web project with Restlet support.
 * @author Mikael Thorman
 */
@Alias("camel-web-restlet-facet")
@RequiresFacet({ MavenCoreFacet.class, JavaSourceFacet.class,
	DependencyFacet.class, CamelWebPrjFacet.class })
public class CamelWebRestletPrjFacet extends MimanBaseFacet {

	@Inject
	ProjectFactory prjFactory;

   @Inject
   private ShellPrintWriter writer;
   
	// TODO there must be a better way to transfer data from the plugin to the Facet, but I don'y know it now.
	public static String camelBasePackage = "";
	public static String prjDescription = "Web project for Camel routes";
	
	DomFileHelper domFileHelper;

	public CamelWebRestletPrjFacet() {
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

			// We verify that we have a parent and that it ends with the default parent suffixes
			if (pom.getParent() == null) {
				return false;
			}
			return true;
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
		createWebXml(pom.getProjectDirectory().getAbsolutePath());
	}

	/**
	 * Updates the applicationContext.xml file with the definitions needed for a restlet project.
	 * 
	 * This is added:
	 * 	<bean id="RestletComponent" class="org.restlet.Component" />
	 * 
	 * 	<bean id="RestletComponentService" class="org.apache.camel.component.restlet.RestletComponent">
	 * 		<constructor-arg index="0">
	 * 			<ref bean="RestletComponent" />
	 * 		</constructor-arg>
	 * 	</bean>
	 * 
	 */
	private void createApplicationContext(String prjAbsolutePath) {
		String sourceUri = "/template-files/web/main/webapp/WEB-INF/applicationContext-restlet-fragment.xml";
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

	/**
	 * Creates a web.xml file based on the template located 
	 * at src/main/resources/template-files/web/main/webapp/WEB-INF/web.xml
	 */
	private void createWebXml(String prjAbsolutePath) {
		String sourceUri = "/template-files/web/main/webapp/WEB-INF/web-restlet-fragment.xml";
		String targetUri = "src/main/webapp/WEB-INF/web.xml";
		
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
		return "/template-files/web/restlet_pom.xml";
	}
}
