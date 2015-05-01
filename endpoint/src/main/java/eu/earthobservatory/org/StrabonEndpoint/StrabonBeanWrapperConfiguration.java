/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint;

import java.util.Iterator;
import java.util.List;

public class StrabonBeanWrapperConfiguration {
  private String label;
  private String bean;
  private String statement;
  private String format;
  private String title;
  private String handle;
  private boolean isHeader;
  private boolean isBean;

  public StrabonBeanWrapperConfiguration(String label, String bean, String statement,
      String format, String title, String handle) {
    this.label = label;
    this.bean = bean;
    this.statement = statement;
    this.format = format;
    this.title = title;
    this.handle = handle;
    this.isHeader = false;
    this.isBean = false;
  }

  public StrabonBeanWrapperConfiguration(String label) {
    this.label = label;
    this.bean = null;
    this.isHeader = true;
    this.isBean = false;
  }

  public StrabonBeanWrapperConfiguration(String label, String bean) {
    this.label = label;
    this.bean = bean;
    this.isHeader = false;
    this.isBean = true;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getBean() {
    return bean;
  }

  public void setBean(String bean) {
    this.bean = bean;
  }


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getStatement() {
    return statement;
  }

  public void setStatement(String statement) {
    this.statement = statement;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getHandle() {
    return this.handle;
  }

  public void setHandle(String handle) {
    this.handle = handle;
  }

  public boolean isHeader() {
    return isHeader;
  }

  public void setHeader(boolean isHeader) {
    this.isHeader = isHeader;
  }

  public boolean isBean() {
    return isBean;
  }

  public void setBean(boolean isBean) {
    this.isBean = isBean;
  }


  public static StrabonBeanWrapperConfiguration create(List<String> list) {
      Iterator<String> it = list.iterator();

    StrabonBeanWrapperConfiguration entry = null;
    while (it.hasNext()) {
        int items = 0;
        // Header:label
        // Bean :label bean
        // Entry :label bean statement format title handle
        String param1 = "", param2 = "", param3 = "", param4 = "", param5 = "", param6 = "";

        if (it.hasNext()) {
          param1 = it.next();
          items++;
        }
        if (it.hasNext()) {
          param2 = it.next();
          items++;
        }
        if (it.hasNext()) {
          param3 = it.next();
          items++;
        }
        if (it.hasNext()) {
          param4 = it.next();
          items++;
        }
        if (it.hasNext()) {
          param5 = it.next();
          items++;
        }
        if (it.hasNext()) {
          param6 = it.next();
          items++;
        }

        if (items == 1) {
          // the first element corresponds to the label
          entry = new StrabonBeanWrapperConfiguration(param1);
        } else if (items == 2) {
          // the first element corresponds to the label
          entry = new StrabonBeanWrapperConfiguration(param1, param2);
        } else if (items == 6) {
          entry =
              new StrabonBeanWrapperConfiguration(param3, param1, param4, param2, param5, param6);
        }
    }
    return entry;
  }
}
