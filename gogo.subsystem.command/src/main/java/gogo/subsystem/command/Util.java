/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package gogo.subsystem.command;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.subsystem.Subsystem;
import org.osgi.service.subsystem.SubsystemConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Util {

    private final static StringBuffer m_sb = new StringBuffer();

    public static String getUnderlineString(int len) {
        synchronized (m_sb) {
            m_sb.delete(0, m_sb.length());
            for (int i = 0; i < len; i++) {
                m_sb.append('-');
            }
            return m_sb.toString();
        }
    }

    public static String getValueString(Object obj) {
        synchronized (m_sb) {
            if (obj instanceof String) {
                return (String) obj;
            } else if (obj instanceof String[]) {
                String[] array = (String[]) obj;
                m_sb.delete(0, m_sb.length());
                for (int i = 0; i < array.length; i++) {
                    if (i != 0) {
                        m_sb.append(", ");
                    }
                    m_sb.append(array[i].toString());
                }
                return m_sb.toString();
            } else if (obj instanceof Boolean) {
                return ((Boolean) obj).toString();
            } else if (obj instanceof Long) {
                return ((Long) obj).toString();
            } else if (obj instanceof Integer) {
                return ((Integer) obj).toString();
            } else if (obj instanceof Short) {
                return ((Short) obj).toString();
            } else if (obj instanceof Double) {
                return ((Double) obj).toString();
            } else if (obj instanceof Float) {
                return ((Float) obj).toString();
            } else if (obj == null) {
                return "null";
            } else {
                return obj.toString();
            }
        }
    }

    public static <T> T getService(
            BundleContext bc, Class<T> clazz, List<ServiceReference> refs) {
        ServiceReference ref = bc.getServiceReference(clazz.getName());
        if (ref == null) {
            return null;
        }
        T t = (T) bc.getService(ref);
        if (t != null) {
            refs.add(ref);
        }
        return t;
    }

    public static void ungetServices(BundleContext bc, List<ServiceReference> refs) {
        while (refs.size() > 0) {
            bc.ungetService(refs.remove(0));
        }
    }

    public static List<String> parseSubstring(String value) {
        List<String> pieces = new ArrayList<String>();
        StringBuffer ss = new StringBuffer();
        // int kind = SIMPLE; // assume until proven otherwise
        boolean wasStar = false; // indicates last piece was a star
        boolean leftstar = false; // track if the initial piece is a star
        boolean rightstar = false; // track if the final piece is a star

        int idx = 0;

        // We assume (sub)strings can contain leading and trailing blanks
        boolean escaped = false;
        loop:
        for (; ; ) {
            if (idx >= value.length()) {
                if (wasStar) {
                    // insert last piece as "" to handle trailing star
                    rightstar = true;
                } else {
                    pieces.add(ss.toString());
                    // accumulate the last piece
                    // note that in the case of
                    // (cn=); this might be
                    // the string "" (!=null)
                }
                ss.setLength(0);
                break loop;
            }

            // Read the next character and account for escapes.
            char c = value.charAt(idx++);
            if (!escaped && ((c == '(') || (c == ')'))) {
                throw new IllegalArgumentException(
                        "Illegal value: " + value);
            } else if (!escaped && (c == '*')) {
                if (wasStar) {
                    // encountered two successive stars;
                    // I assume this is illegal
                    throw new IllegalArgumentException("Invalid filter string: " + value);
                }
                if (ss.length() > 0) {
                    pieces.add(ss.toString()); // accumulate the pieces
                    // between '*' occurrences
                }
                ss.setLength(0);
                // if this is a leading star, then track it
                if (pieces.isEmpty()) {
                    leftstar = true;
                }
                wasStar = true;
            } else if (!escaped && (c == '\\')) {
                escaped = true;
            } else {
                escaped = false;
                wasStar = false;
                ss.append(c);
            }
        }
        if (leftstar || rightstar || pieces.size() > 1) {
            // insert leading and/or trailing "" to anchor ends
            if (rightstar) {
                pieces.add("");
            }
            if (leftstar) {
                pieces.add(0, "");
            }
        }
        return pieces;
    }

    public static String unparseSubstring(List<String> pieces) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < pieces.size(); i++) {
            if (i > 0) {
                sb.append("*");
            }
            sb.append(pieces.get(i));
        }
        return sb.toString();
    }

    public static boolean compareSubstring(List<String> pieces, String s) {
        // Walk the pieces to match the string
        // There are implicit stars between each piece,
        // and the first and last pieces might be "" to anchor the match.
        // assert (pieces.length > 1)
        // minimal case is <string>*<string>

        boolean result = true;
        int len = pieces.size();

        // Special case, if there is only one piece, then
        // we must perform an equality test.
        if (len == 1) {
            return s.equals(pieces.get(0));
        }

        // Otherwise, check whether the pieces match
        // the specified string.

        int index = 0;

        loop:
        for (int i = 0; i < len; i++) {
            String piece = pieces.get(i);

            // If this is the first piece, then make sure the
            // string starts with it.
            if (i == 0) {
                if (!s.startsWith(piece)) {
                    result = false;
                    break loop;
                }
            }

            // If this is the last piece, then make sure the
            // string ends with it.
            if (i == len - 1) {
                if (s.endsWith(piece)) {
                    result = true;
                } else {
                    result = false;
                }
                break loop;
            }

            // If this is neither the first or last piece, then
            // make sure the string contains it.
            if ((i > 0) && (i < (len - 1))) {
                index = s.indexOf(piece, index);
                if (index < 0) {
                    result = false;
                    break loop;
                }
            }

            // Move string index beyond the matching piece.
            index += piece.length();
        }

        return result;
    }

    static ServiceReference<Subsystem> findSubsystem(long id, BundleContext bundleContext) throws InvalidSyntaxException {
        Collection<ServiceReference<Subsystem>> serviceReferences =
                bundleContext.getServiceReferences(Subsystem.class, String.format("(%s=%s)",
                        SubsystemConstants.SUBSYSTEM_ID_PROPERTY, id));
        if (serviceReferences != null && serviceReferences.size() == 1) {
            return serviceReferences.iterator().next();
        }
        return null; // something wrong
    }
}