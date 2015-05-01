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


/**
 * Interface that exposes the capabilities of a Strabon Endpoint.
 */
public interface Capabilities {

  /**
   * Return the version of Stabon Endpoint.
   * 
   * @return
   */
  String getVersion();

  /**
   * True when the endpoint supports limiting of results.
   * 
   * @return
   */
  boolean supportsLimit();

  /**
   * True when the endpoint supports authentication for update operations
   * (Store/Delete/Update/Insert).
   * 
   * @return
   */
  boolean supportsAuthentication();

  /**
   * True when the endpoint supports querying using SPARQL.
   * 
   * @return
   */
  boolean supportsQuerying();

  /**
   * True when the endpoint supports update queries using SPARQL.
   * 
   * @return
   */
  boolean supportsUpdating();

  /**
   * True when the endpoint supports storing of RDF triples.
   * 
   * @return
   */
  boolean supportsStoring();

  /**
   * True when the endpoint supports describe queries using SPARQL.
   * 
   * @return
   */
  boolean supportsDescribing();

  /**
   * True when the endpoint supports browsing of RDF resources.
   * 
   * @return
   */
  boolean supportsBrowsing();

  /**
   * True when the endpoint supports modification of the connection details used for the database
   * connection.
   * 
   * @return
   */
  boolean supportsConnectionModification();

  /**
   * Return a list of URIs corresponding to the spatial extension functions that stSPARQL supports.
   * 
   * @return
   */
  List<String> getstSPARQLSpatialExtensionFunctions();

  // TODO
  // public List<String> getstSPARQLTemporalExtensionFunctions();

  /**
   * Return a list of URIs corresponding to the spatial extension functions that GeoSPARQL supports.
   * 
   * @return
   */
  List<String> getGeoSPARQLSpatialExtensionFunctions();

  /**
   * Return a list of URIs corresponding to the units of measure that can be used in an extension
   * function requiring such an argument.
   * 
   * @return
   */
  List<String> getUnitsOfMeasure();

  /**
   * Returns a {@link RequestCapabilities} instance containing the details for how one can query the
   * Query service of the endpoint.
   * 
   * @return
   */
  RequestCapabilities getQueryCapabilities();

  /**
   * Returns a {@link RequestCapabilities} instance containing the details for how one can query the
   * Update service of the endpoint.
   * 
   * @return
   */
  RequestCapabilities getUpdateCapabilities();

  /**
   * Returns a {@link RequestCapabilities} instance containing the details for how one can query the
   * Store service of the endpoint.
   * 
   * @return
   */
  RequestCapabilities getStoreCapabilities();

  /**
   * Returns a {@link RequestCapabilities} instance containing the details for how one can query the
   * Browse service of the endpoint.
   * 
   * @return
   */
  RequestCapabilities getBrowseCapabilities();

  /**
   * Returns a {@link RequestCapabilities} instance containing the details for how one can query the
   * Connection service of the endpoint.
   * 
   * @return
   */
  RequestCapabilities getConnectionCapabilities();
}
