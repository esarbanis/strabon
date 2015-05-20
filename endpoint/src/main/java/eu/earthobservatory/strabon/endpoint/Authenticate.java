/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Copyright (C) 2010, 2011, 2012, Pyravlos Team
 * 
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.strabon.endpoint;

import org.apache.commons.codec.binary.Base64;

import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Keeps common variables shared by beans and .jsp pages.
 */
public class Authenticate {

  static final String CREDENTIALS_PROPERTIES_FILE = "CREDENTIALS_PROPERTIES_FILE";

  private final String credentialsFile;

  Authenticate(String credentialsFile) {
    this.credentialsFile = credentialsFile;
  }

  /**
   * Authenticate user
   * 
   * @throws IOException
   * */
  public boolean authenticateUser(String authorization, ServletContext context) throws IOException {
    Properties properties = new Properties();
    if (authorization == null)
      return false; // no authorization

    if (!authorization.toUpperCase().startsWith("BASIC "))
      return false; // only BASIC authentication

    // get encoded user and password, comes after "BASIC "
    String userpassEncoded = authorization.substring(6);
    // decode
    String userpassDecoded = new String(Base64.decodeBase64(userpassEncoded));

    Pattern pattern = Pattern.compile(":");
    String[] credentials = pattern.split(userpassDecoded);
    // get credentials.properties as input stream
    InputStream input = new FileInputStream(context.getRealPath(credentialsFile));

    // load the properties
    properties.load(input);

    // close the stream
    input.close();

    // check if the given credentials are allowed
    return !userpassDecoded.equals(":") && credentials[0].equals(properties.get("username"))
        && credentials[1].equals(properties.get("password"));
  }
}
