package bundle_1;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    public void start(BundleContext bc) throws Exception {
        bc.registerService(TestService1.class, new TestService1() {}, null);
        System.out.println("Bundle 1 started");
    }

    public void stop(BundleContext bc) throws Exception {
        System.out.println("Bundle 1 stopped");
    }
}