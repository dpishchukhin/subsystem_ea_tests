package gogo.subsystem.command;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

public class Activator implements BundleActivator {
    public void start(BundleContext bc) throws Exception {
        Hashtable props = new Hashtable();
        props.put("osgi.command.scope", "sub");
        props.put("osgi.command.function", new String[]{
                "install", "headers", "ls", "lb", "start", "stop", "uninstall"});
        bc.registerService(
                SubsystemCommands.class.getName(), new SubsystemCommands(bc), props);

        props = new Hashtable();
        props.put("osgi.command.scope", "sub");
        props.put("osgi.command.function", new String[]{
                "inspect"});
        bc.registerService(
                InspectCommands.class.getName(), new InspectCommands(bc), props);
    }

    public void stop(BundleContext bc) throws Exception {
    }
}