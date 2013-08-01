package eu.miman.forge.plugin.camel.facet;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.maven.model.Dependency;
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

import eu.miman.forge.plugin.util.MimanBaseFacet;
import eu.miman.forge.plugin.util.helpers.DomFileHelper;
import eu.miman.forge.plugin.util.helpers.DomFileHelperImpl;

/**
 * This is the Facet class for Camel war projects with Websphere MQ support.
 * It makes sure the project has the correct dependencies for being a Camel web project with Websphere MQ support.
 * @author Mikael Thorman
 */
@Alias("camel-web-webspheremq-facet")
@RequiresFacet({ MavenCoreFacet.class, JavaSourceFacet.class,
	DependencyFacet.class, CamelWebPrjFacet.class })
public class CamelWebWebsphereMqPrjFacet extends MimanBaseFacet {

	@Inject
	ProjectFactory prjFactory;

   @Inject
   private ShellPrintWriter writer;
   
	// TODO there must be a better way to transfer data from the plugin to the Facet, but I don'y know it now.
	public static String wmqHostname = "";
	public static String wmqPort = "";
	public static String wmqQueueManager = "";
	public static String wmqChannel = "";
	
	DomFileHelper domFileHelper;

	public CamelWebWebsphereMqPrjFacet() {
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

		      List<Dependency> deps = pom.getDependencies();
		      boolean dependenciesOk = false;
		      for (Dependency dependency : deps)
		      {
		         if (dependency.getGroupId().equals("com.ibm") && dependency.getArtifactId().equals("com.ibm.mqjms"))
		         {
		            dependenciesOk = true;
		         }
		         // TODO more checks should be added here
		      }
		      return dependenciesOk;
		}
		return reply;
	}

	// Helper functions ****************************************
	/**
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
	 * Updates the applicationContext.xml file with the definitions needed for a Websphere MQ project.
	 */
	private void createApplicationContext(String prjAbsolutePath) {
		String sourceUri = "/template-files/ibm_wmq/ibm_wmq_applicationContext_fragment.xml";
		String targetUri = "src/main/webapp/WEB-INF/applicationContext.xml";
		
		try {
			Xpp3Dom dom = domFileHelper.readXmlResourceFile(sourceUri);
			String targetPath = prjAbsolutePath + "/" + targetUri;
			Xpp3Dom targetDom = domFileHelper.readXmlFile(targetPath);
			
			Xpp3Dom bean = dom.getChild("bean");
			Xpp3Dom property = bean.getChild("property");
			Xpp3Dom propertyBean = property.getChild("bean");
			
			Xpp3Dom hostname = new Xpp3Dom("property");
			hostname.setAttribute("name", "hostName");
			hostname.setAttribute("value", wmqHostname);
			propertyBean.addChild(hostname);

			Xpp3Dom port = new Xpp3Dom("property");
			port.setAttribute("name", "port");
			port.setAttribute("value", wmqPort);
			propertyBean.addChild(port);

			Xpp3Dom queueManager = new Xpp3Dom("property");
			queueManager.setAttribute("name", "queueManager");
			queueManager.setAttribute("value", wmqQueueManager);
			propertyBean.addChild(queueManager);

			Xpp3Dom channel = new Xpp3Dom("property");
			channel.setAttribute("name", "channel");
			channel.setAttribute("value", wmqChannel);
			propertyBean.addChild(channel);
			
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
		return "/template-files/ibm_wmq/wmq_pom_fragment.xml";
	}
}
