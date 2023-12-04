/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.List;

/**
 * HardenedObjectInputStream restricts the set of classes that can be
 * deserialized to a set of explicitly whitelisted classes. This prevents
 * certain type of attacks from being successful.
 * 
 * <p>
 * It is assumed that classes in the "java.lang" and "java.util" packages are
 * always authorized.
 * </p>
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.2.0
 */
public class HardenedObjectInputStream extends ObjectInputStream {

    final private List<String> whitelistedClassNames;
    final private static String[] JAVA_PACKAGES = new String[] { "java.lang", "java.util" };
    final private static int DEPTH_LIMIT = 16;
    final private static int ARRAY_LIMIT = 10000;

    public HardenedObjectInputStream(InputStream in, String[] whitelist) throws IOException {
        super(in);
        this.initObjectFilter();
        this.whitelistedClassNames = new ArrayList<String>();
        if (whitelist != null) {
            for (int i = 0; i < whitelist.length; i++) {
                this.whitelistedClassNames.add(whitelist[i]);
            }
        }
    }

    private void initObjectFilter() {
        this.setObjectInputFilter(ObjectInputFilter.Config.createFilter(
                "maxarray=" + ARRAY_LIMIT + ";maxdepth=" + DEPTH_LIMIT + ";"
        ));
    }
    public HardenedObjectInputStream(InputStream in, List<String> whitelist) throws IOException {
        super(in);
        this.initObjectFilter();
        this.whitelistedClassNames = new ArrayList<String>();
        this.whitelistedClassNames.addAll(whitelist);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass anObjectStreamClass) throws IOException, ClassNotFoundException {

        String incomingClassName = anObjectStreamClass.getName();

        if (!isWhitelisted(incomingClassName)) {
            throw new InvalidClassException("Unauthorized deserialization attempt", anObjectStreamClass.getName());
        }

        return super.resolveClass(anObjectStreamClass);
    }

    private boolean isWhitelisted(String incomingClassName) {
        for (int i = 0; i < JAVA_PACKAGES.length; i++) {
            if (incomingClassName.startsWith(JAVA_PACKAGES[i]))
                return true;
        }
        for (String whiteListed : whitelistedClassNames) {
            if (incomingClassName.equals(whiteListed))
                return true;
        }
        return false;
    }

    protected void addToWhitelist(List<String> additionalAuthorizedClasses) {
        whitelistedClassNames.addAll(additionalAuthorizedClasses);
    }
}
