package gogo.subsystem.command;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.osgi.framework.*;
import org.osgi.service.subsystem.Subsystem;
import org.osgi.service.subsystem.SubsystemConstants;
import org.osgi.service.subsystem.SubsystemException;

import java.net.URL;
import java.util.*;

public class SubsystemCommands {
    private final BundleContext m_bc;

    public SubsystemCommands(BundleContext bc) {
        m_bc = bc;
    }

    @Descriptor("display subsystem headers")
    public void headers(@Descriptor("target subsystem identifiers") long[] ids) {
        if ((ids != null) && (ids.length >= 1)) {
            List<ServiceReference> refs = new ArrayList<ServiceReference>();
            for (long id : ids) {
                try {
                    ServiceReference<Subsystem> serviceReference = Util.findSubsystem(id, m_bc);

                    if (serviceReference != null) {
                        refs.add(serviceReference);
                        printHeaders(m_bc.getService(serviceReference));
                    } else {
                        System.err.println("Subsystem ID " + id + " is invalid.");
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Unable to parse id '" + id + "'.");
                } catch (SubsystemException ex) {
                    ex.printStackTrace(System.err);
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            }
            Util.ungetServices(m_bc, refs);
        } else {
            System.err.println("Incorrect number of arguments");
        }
    }

    private void printHeaders(Subsystem subsystem) {
        Map<String, String> subsystemHeaders = subsystem.getSubsystemHeaders(null);
        for (String key : subsystemHeaders.keySet()) {
            System.out.println(String.format("%s=%s", key, subsystemHeaders.get(key)));
        }
    }

    @Descriptor("install subsystem using URLs")
    public void install(@Descriptor("target URLs") String[] urls)
            throws InvalidSyntaxException {
        install(getRootSubsystem().getSubsystemId(), urls);
    }


    @Descriptor("install subsystem using URLs")
    public void install(@Descriptor("Parent subsystem ID") long id,
                        @Descriptor("target URLs") String[] urls)
            throws InvalidSyntaxException {

        ServiceReference<Subsystem> serviceReference = Util.findSubsystem(id, m_bc);

        if (serviceReference == null) {
            System.out.println("No Subsystems are installed");
            return;
        }

        Subsystem root = m_bc.getService(serviceReference);

        StringBuilder sb = new StringBuilder();
        for (String url : urls) {
            String location = url.trim();
            Subsystem subsystem = null;
            try {
                subsystem = root.install(location, new URL(location).openStream());
            } catch (IllegalStateException ex) {
                ex.printStackTrace(System.err);
            } catch (SubsystemException ex) {
                ex.printStackTrace(System.err);
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
            }
            if (subsystem != null) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(subsystem.getSubsystemId());
            }
        }
        if (sb.toString().indexOf(',') > 0) {
            System.out.println("Subsystem IDs: " + sb.toString());
        } else if (sb.length() > 0) {
            System.out.println("Subsystem ID: " + sb.toString());
        }

        m_bc.ungetService(serviceReference);
    }

    @Descriptor("list all installed subsystems")
    public void ls() throws InvalidSyntaxException {
        Subsystem rootSubsystem = getRootSubsystem();
        if (rootSubsystem != null) {
            printSubsystem(rootSubsystem, 1);
        } else {
            System.out.println("No Subsystems are installed");
        }
    }

    private static void printSubsystem(Subsystem subsystem, int shift) {
        System.out.print(String.format("  %s", subsystem.getSubsystemId()));
        for (int i = 0; i < shift; i++) {
            System.out.print("  ");
        }
        System.out.println(String.format("%s  %s  %S", subsystem.getSymbolicName(), subsystem.getVersion(), subsystem.getState()));
        Collection<Subsystem> children = subsystem.getChildren();
        if (children != null) {
            for (Subsystem child : children) {
                printSubsystem(child, shift + 1);
            }
        }
    }

    private Subsystem getRootSubsystem() throws InvalidSyntaxException {
        Collection<ServiceReference<Subsystem>> serviceReferences = m_bc.getServiceReferences(Subsystem.class, String.format("(%s=%s)",
                SubsystemConstants.SUBSYSTEM_SYMBOLICNAME_PROPERTY, SubsystemConstants.ROOT_SUBSYSTEM_SYMBOLICNAME));
        if (serviceReferences != null && serviceReferences.size() == 1) {
            return m_bc.getService(serviceReferences.iterator().next());
        }
        return null; // something wrong
    }


    @Descriptor("start subsystems")
    public void start(@Descriptor("target subsystem identifiers") long[] ids) {
        if ((ids != null) && (ids.length >= 1)) {
            List<ServiceReference> refs = new ArrayList<ServiceReference>();

            for (long id : ids) {
                try {
                    ServiceReference<Subsystem> serviceReference = Util.findSubsystem(id, m_bc);

                    if (serviceReference != null) {
                        refs.add(serviceReference);
                        m_bc.getService(serviceReference).start();
                    } else {
                        System.err.println("Subsystem ID " + id + " is invalid.");
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Unable to parse id '" + id + "'.");
                } catch (SubsystemException ex) {
                    ex.printStackTrace(System.err);
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            }
            Util.ungetServices(m_bc, refs);
        } else {
            System.err.println("Incorrect number of arguments");
        }
    }

    @Descriptor("stop subsystems")
    public void stop(@Descriptor("target subsystem identifiers") long[] ids) {
        if ((ids != null) && (ids.length >= 1)) {
            List<ServiceReference> refs = new ArrayList<ServiceReference>();
            for (long id : ids) {
                try {
                    ServiceReference<Subsystem> serviceReference = Util.findSubsystem(id, m_bc);

                    if (serviceReference != null) {
                        refs.add(serviceReference);
                        m_bc.getService(serviceReference).stop();
                    } else {
                        System.err.println("Subsystem ID " + id + " is invalid.");
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Unable to parse id '" + id + "'.");
                } catch (SubsystemException ex) {
                    ex.printStackTrace(System.err);
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            }
            Util.ungetServices(m_bc, refs);

        } else {
            System.err.println("Incorrect number of arguments");
        }
    }

    @Descriptor("uninstall subsystems")
    public void uninstall(@Descriptor("target subsystem identifiers") long[] ids) {
        if ((ids != null) && (ids.length >= 1)) {
            List<ServiceReference> refs = new ArrayList<ServiceReference>();
            for (long id : ids) {
                try {
                    ServiceReference<Subsystem> serviceReference = Util.findSubsystem(id, m_bc);

                    if (serviceReference != null) {
                        refs.add(serviceReference);
                        m_bc.getService(serviceReference).uninstall();
                    } else {
                        System.err.println("Subsystem ID " + id + " is invalid.");
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Unable to parse id '" + id + "'.");
                } catch (SubsystemException ex) {
                    ex.printStackTrace(System.err);
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            }
            Util.ungetServices(m_bc, refs);
        } else {
            System.err.println("Incorrect number of arguments");
        }
    }

    @Descriptor("list all installed bundles")
    public void lb(
            @Descriptor("target subsystem identifier") long id) throws InvalidSyntaxException {
        lb(id, false, false, false, null);
    }

    @Descriptor("list installed bundles matching a substring")
    public void lb(
            @Descriptor("target subsystem identifier") long id,
            @Descriptor("show location") @Parameter(names = {"-l", "--location"}, presentValue = "true", absentValue = "false") boolean showLoc,
            @Descriptor("show symbolic name") @Parameter(names = {"-s", "--symbolicname"}, presentValue = "true", absentValue = "false") boolean showSymbolic,
            @Descriptor("show update location") @Parameter(names = {"-u", "--updatelocation"}, presentValue = "true", absentValue = "false") boolean showUpdate,
            @Descriptor("subtring matched against name or symbolic name") String pattern) throws InvalidSyntaxException {
        // Keep track of service references.
        List<ServiceReference> refs = new ArrayList();

        List<Bundle> found = new ArrayList();

        ServiceReference<Subsystem> serviceReference = Util.findSubsystem(id, m_bc);
        if (serviceReference == null) {
            System.out.println("Unknown Subsystem ID");
            return;
        }
        Subsystem subsystem = m_bc.getService(serviceReference);
        if (pattern == null) {
            found.addAll(Arrays.asList(subsystem.getBundleContext().getBundles()));
        } else {
            Bundle[] bundles = subsystem.getBundleContext().getBundles();

            for (Bundle bundle : bundles) {
                String name = bundle.getHeaders().get(Constants.BUNDLE_NAME);
                if (matchBundleName(bundle.getSymbolicName(), pattern)
                        || matchBundleName(name, pattern)) {
                    found.add(bundle);
                }
            }
        }

        if (found.size() > 0) {
            printBundleList(subsystem, (Bundle[]) found.toArray(new Bundle[found.size()]), showLoc, showSymbolic, showUpdate);
        } else {
            System.out.println("No matching bundles found");
        }

        m_bc.ungetService(serviceReference);
    }

    private static boolean matchBundleName(String name, String pattern) {
        return (name != null) && name.toLowerCase().contains(pattern.toLowerCase());
    }

    private static void printBundleList(Subsystem subsystem, Bundle[] bundles,
                                 boolean showLoc, boolean showSymbolic, boolean showUpdate) {
        // Display active start level.
        printSubsystem(subsystem, 0);

        // Determine last column.
        String lastColumn = "Name";
        if (showLoc) {
            lastColumn = "Location";
        } else if (showSymbolic) {
            lastColumn = "Symbolic name";
        } else if (showUpdate) {
            lastColumn = "Update location";
        }

        System.out.println(String.format("%5s|%-11s|%s", "ID", "State", lastColumn));
        for (Bundle bundle : bundles) {
            // Get the bundle name or location.
            String name = (String) bundle.getHeaders().get(Constants.BUNDLE_NAME);
            // If there is no name, then default to symbolic name.
            name = (name == null) ? bundle.getSymbolicName() : name;
            // If there is no symbolic name, resort to location.
            name = (name == null) ? bundle.getLocation() : name;

            // Overwrite the default value is the user specifically
            // requested to display one or the other.
            if (showLoc) {
                name = bundle.getLocation();
            } else if (showSymbolic) {
                name = bundle.getSymbolicName();
                name = (name == null) ? "<no symbolic name>" : name;
            } else if (showUpdate) {
                name = (String) bundle.getHeaders().get(Constants.BUNDLE_UPDATELOCATION);
                name = (name == null) ? bundle.getLocation() : name;
            }

            // Show bundle version if not showing location.
            name = (!showLoc && !showUpdate) ? name + " (" + bundle.getVersion() + ")"
                    : name;

            System.out.println(String.format("%5d|%-11s|%s",
                    bundle.getBundleId(), getStateString(bundle), name));
        }
    }

    private static String getStateString(Bundle bundle) {
        int state = bundle.getState();
        if (state == Bundle.ACTIVE) {
            return "Active     ";
        } else if (state == Bundle.INSTALLED) {
            return "Installed  ";
        } else if (state == Bundle.RESOLVED) {
            return "Resolved   ";
        } else if (state == Bundle.STARTING) {
            return "Starting   ";
        } else if (state == Bundle.STOPPING) {
            return "Stopping   ";
        } else {
            return "Unknown    ";
        }
    }
}