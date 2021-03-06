/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.strabon.endpoint.capabilities;

import java.util.List;


public class StoreBeanCapabilities implements RequestCapabilities {

  private static StoreBeanCapabilities caps;

  protected StoreBeanCapabilities() {

  }

  public static synchronized StoreBeanCapabilities getInstance() {
    if (caps == null) {
      caps = new StoreBeanCapabilities();
    }

    return caps;
  }

  @Override
  public Parameters getParametersObject() {
    return null;
  }

  @Override
  public List<String> getAcceptedValues(String param) {
    return null;
  }

  @Override
  public boolean isOptional(String param) {
    return false;
  }

}
