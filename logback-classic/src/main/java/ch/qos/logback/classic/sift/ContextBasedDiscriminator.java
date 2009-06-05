/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.sift;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.Discriminator;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * This discriminator essentially returns the value mapped to an MDC key. If the
 * said value is null, then a default value is returned.
 * 
 * <p>Both Key and the DefaultValue are user specified properties.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class ContextBasedDiscriminator extends ContextAwareBase implements
    Discriminator<ILoggingEvent> {

  private static final String KEY = "contextName";
  private String defaultValue;
  private boolean started = false;

  public ContextBasedDiscriminator() {
  }

  /**
   * Return the name of the current context name as found in the logging event.
   */
  public String getDiscriminatingValue(ILoggingEvent event) {
    String contextName = event.getLoggerContextVO().getName();

    if (contextName == null) {
      return defaultValue;
    } else {
      return contextName;
    }
  }

  public boolean isStarted() {
    return started;
  }

  public void start() {
    started = true;
  }

  public void stop() {
    started = false;
  }

  public String getKey() {
    return KEY;
  }

  public void setKey(String key) {
    throw new UnsupportedOperationException("Key cannot be set. Using fixed key "+KEY);
  }

  /**
   * @see #setDefaultValue(String)
   * @return
   */
  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * The default context name in case the context name is not set for the
   * current logging event.
   * 
   * @param defaultValue
   */
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }
}