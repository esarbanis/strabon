package org.openrdf.sail.h2;

import org.openrdf.sail.generaldb.GeneralDBSqlValueTableFactory;


public class H2SqlValueTableFactory extends GeneralDBSqlValueTableFactory {

  public H2SqlValueTableFactory() {
    super(new H2SqlTableFactory());
  }
}
