package bundle_2;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import java.util.Hashtable;

public class Activator implements BundleActivator, EventHandler {

    public void start(BundleContext bc) throws Exception {
        Hashtable props = new Hashtable();
        props.put(EventConstants.EVENT_TOPIC, "org/osgi/framework/ServiceEvent/*");
        bc.registerService(EventHandler.class, this, props);

        System.out.println("Bundle 2 started");
    }

    public void stop(BundleContext bc) throws Exception {
        System.out.println("Bundle 2 stopped");
    }

    public void handleEvent(Event event) {
        System.out.println("Event received: " + eventDetails(event));
    }

    private String eventDetails(Event event) {
        StringBuilder builder = new StringBuilder();
        builder.append("Topic: ").append(event.getTopic()).append("\n");
        String[] names = event.getPropertyNames();
        for (String name : names) {
            builder.append(name).append("=").append(event.getProperty(name)).append("\n");
        }
        return builder.toString();
    }
}