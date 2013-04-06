/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2012, 2013, Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 */
package eu.earthobservatory.org.StrabonEndpoint.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.resultio.stSPARQLQueryResultFormat;
import org.openrdf.query.resultio.sparqlkml.stSPARQLResultsKMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * SpatialEndpoint is a SPARQLEndpoint which can store and 
 * query for spatial data. It also supports KML format for 
 * this kind of data.
 * 
 * @author Charalampos Nikolaou <charnik@di.uoa.gr>
 * @author Kallirroi Dogani <kallirroi@di.uoa.gr>
 */
public class SpatialEndpoint extends SPARQLEndpoint {
	
	public SpatialEndpoint(String host, int port) {
		super(host, port);
	}
	
	public SpatialEndpoint(String host, int port, String endpointName) {
		super(host, port, endpointName);
	}
	
	public EndpointResult queryForKML(String sparqlQuery) throws IOException, TupleQueryResultHandlerException{
		
		EndpointResult xmlResult = query(sparqlQuery, stSPARQLQueryResultFormat.XML);
		
		if (xmlResult.getStatusCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + xmlResult.getStatusCode() + " " + xmlResult.getStatusText());
		}
		
		String xml = xmlResult.getResponse();
		
		Vector<BindingSet> bindingSets = xmlToBindingSet(xml);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		stSPARQLResultsKMLWriter kmlWriter = new stSPARQLResultsKMLWriter(outputStream);
			
		kmlWriter.startQueryResult(new Vector<String>());
					
		for(int i=0; i<bindingSets.size(); i++){
		
				kmlWriter.handleSolution(bindingSets.get(i));
		}
					
		kmlWriter.endQueryResult();
		
		
		EndpointResult kmlResult = new EndpointResult(xmlResult.getStatusCode(), xmlResult.getStatusText(), outputStream.toString());
		return kmlResult;
	}
	
	
private Vector<BindingSet> xmlToBindingSet(String xml){
		
		Vector<BindingSet> bindingSetList = new Vector<BindingSet>();
		
		try { 
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
			
			doc.getDocumentElement().normalize();
			 		 
			Node resultsNode = doc.getElementsByTagName("results").item(0);
			Element resultsElement = (Element) resultsNode;
			NodeList resultsList = resultsElement.getElementsByTagName("result");
		 		 
			for (int i = 0; i < resultsList.getLength(); i++) {
		 				
				Node resultItem = resultsList.item(i); 
				Element resultElement = (Element) resultItem;
				NodeList bindingNamesList = resultElement.getElementsByTagName("binding");
					
				QueryBindingSet bindingSet = new QueryBindingSet();
				ValueFactoryImpl valueFactImpl = new ValueFactoryImpl();
				
				for (int j=0; j<bindingNamesList.getLength(); j++){
					
					Node bindingNameItem = bindingNamesList.item(j);
					Element bindingNameElement = (Element) bindingNameItem;
					
					String bindingName = bindingNameElement.getAttribute("name");
					Node child = bindingNameItem.getFirstChild();
					Element childElement = (Element) child;
					String childName = child.getNodeName();
					
					if(childName.equals("uri")){
						URI uri = valueFactImpl.createURI(bindingNameElement.getElementsByTagName("uri").item(0).getTextContent());
						bindingSet.addBinding(bindingName, uri);
					}
					else if (childName.equals("literal")){
						URI datatype = valueFactImpl.createURI(childElement.getAttribute("datatype"));
						String value = bindingNameElement.getElementsByTagName("literal").item(0).getTextContent();
						LiteralImpl literal= new LiteralImpl(value, datatype);
						bindingSet.addBinding(bindingName, literal);
						
					}
					else{
						System.out.println("Parse error: Unkown xml");
						return null;
					}
									
				}
				
				bindingSetList.add(bindingSet);
				
			}
			
			
		}
	    catch (Exception e) {
	    	e.printStackTrace();
	    }
		
		return bindingSetList;
	}
}
