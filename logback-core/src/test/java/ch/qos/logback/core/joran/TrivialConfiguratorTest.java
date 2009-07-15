/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.joran;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ext.IncAction;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.TrivialStatusListener;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;

public class TrivialConfiguratorTest {

  Context context = new ContextBase();
  HashMap<Pattern, Action> rulesMap = new HashMap<Pattern, Action>();

  public void doTest(String filename) throws Exception {

    rulesMap.put(new Pattern("x/inc"), new IncAction());

    TrivialConfigurator gc = new TrivialConfigurator(rulesMap);

    gc.setContext(context);
    gc.doConfigure(CoreTestConstants.TEST_DIR_PREFIX + "input/joran/"
        + filename);
  }

  @Test
  public void smoke() throws Exception {
    int oldBeginCount = IncAction.beginCount;
    int oldEndCount = IncAction.endCount;
    int oldErrorCount = IncAction.errorCount;
    doTest("inc.xml");
    assertEquals(oldErrorCount, IncAction.errorCount);
    assertEquals(oldBeginCount + 1, IncAction.beginCount);
    assertEquals(oldEndCount + 1, IncAction.endCount);
  }

  @Test
  public void inexistentFile() {
    TrivialStatusListener tsl = new TrivialStatusListener();
    String filename = "nothereBLAH.xml";
    context.getStatusManager().add(tsl);
    try {
      doTest(filename);
    } catch (Exception e) {
    }
    assertTrue(tsl.list.size() + " should be greater than or equal to 1",
        tsl.list.size() >= 1);
    Status s0 = tsl.list.get(0);
    assertTrue(s0.getMessage().startsWith("Could not open [" + filename + "]"));
  }

  @Test
  public void illFormedXML() {
    TrivialStatusListener tsl = new TrivialStatusListener();
    String filename = "illformed.xml";
    context.getStatusManager().add(tsl);
    try {
      doTest(filename);
    } catch (Exception e) {
    }
    assertEquals(2, tsl.list.size());
    Status s0 = tsl.list.get(0);
    assertTrue(s0.getMessage().startsWith(
        "Parsing fatal error on line 5 and column 3"));
    Status s1 = tsl.list.get(1);
    assertTrue(s1
        .getMessage()
        .startsWith(
            "Problem parsing XML document. See previously reported errors. Abandoning all further processing."));
  }

  @Test
  public void lbcore105() throws IOException, JoranException {
    String jarEntry = "buzz.xml";
    File jarFile = makeJarFile();
    fillInJarFile(jarFile, jarEntry);
    URL url = asURL(jarFile, jarEntry);
    TrivialConfigurator tc = new TrivialConfigurator(rulesMap);
    tc.setContext(context);
    tc.doConfigure(url);
    // deleting an open file fails
    assertTrue(jarFile.delete());
    assertFalse(jarFile.exists());
  }

  File makeJarFile() {
    File outputDir = new File(CoreTestConstants.OUTPUT_DIR_PREFIX);
    outputDir.mkdirs();
    int randomInt = RandomUtil.getPositiveInt();
    return new File(CoreTestConstants.OUTPUT_DIR_PREFIX + "foo-" + randomInt
        + ".jar");
  }

  private void fillInJarFile(File jarFile, String jarEntryName)
      throws IOException {
    JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile));
    jos.putNextEntry(new ZipEntry(jarEntryName));
    jos.write("<x/>".getBytes());
    jos.closeEntry();
    jos.close();
  }

  URL asURL(File jarFile, String jarEntryName) throws IOException {
    URL innerURL = jarFile.toURI().toURL();
    return new URL("jar:" + innerURL + "!/" + jarEntryName);
  }

}