package pa.iscde.metrics.demo;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import pa.iscde.metrics.services.MetricsServices;
import pt.iscte.pidesco.extensibility.PidescoServices;
import pt.iscte.pidesco.javaeditor.service.JavaEditorServices;
import pt.iscte.pidesco.projectbrowser.service.ProjectBrowserServices;

public class Activator implements BundleActivator {

	private static BundleContext context;
	
	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		ServiceReference<PidescoServices> ref = context.getServiceReference(PidescoServices.class);
		PidescoServices pidescoServices = context.getService(ref);
		
		context.registerService(MetricsServices.class, new Testview(), null);
		
		ServiceReference<JavaEditorServices> editorReference = context.getServiceReference(JavaEditorServices.class);
		JavaEditorServices javaEditorServices = context.getService(editorReference);
		
		ServiceReference<ProjectBrowserServices> browserReference = context.getServiceReference(ProjectBrowserServices.class);
		ProjectBrowserServices projectBrowserServices = context.getService(browserReference);
		
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		
		
	}

}