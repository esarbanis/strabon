/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (C) 2013, Pyravlos Team
 *
 * http://www.strabon.di.uoa.gr/
 * 
 * 
 *  @author Panayiotis Smeros <psmeros@di.uoa.gr>
 *  @author Konstantina Bereta <Konstantina.Bereta@di.uoa.gr>
 */

package eu.earthobservatory.runtime.generaldb;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.earthobservatory.constants.TemporalConstants;

public class utils {
	
	private static Logger logger = LoggerFactory.getLogger(eu.earthobservatory.runtime.generaldb.Strabon.class);
	
	public static String queryRewriting(String queryString) 
	{
		String newQueryString="";
		int numOfQuadruples=0;
		int startIndex=0;
		
		Hashtable< String, String> periodsAndGraphs = new Hashtable<String, String>();
		
		StringBuffer whereClauses = new StringBuffer(2048);
		NQuadsParser parser = new NQuadsParser();
		
		try
		{
			String graphVariable="?g"+(int)(Math.random()*10000);
			while (queryString.contains(graphVariable))
			{
				graphVariable+="1";
			}
			graphVariable+="_";
			
			//remove comments from query
			queryString=queryString.replaceAll("\\.#", ".\n#");
		    String REGEX = "((^(\\s)*#)|((\\s)+#)).*$";
			Pattern pattern = Pattern.compile(REGEX, Pattern.MULTILINE);							
			Matcher matcher = pattern.matcher(queryString);
			String oldQueryString=matcher.replaceAll("");
			
			String updateREGEX= "((where([\\s])*\\{(.)*\\})|(WHERE([\\s])*\\{(.)*\\}))";
	
			//check whether the query contains quadruples	
			String Word="((\\w)|(\\p{InGreek})|-)+";
			String URI="(<([\\S-])*>)|("+Word+":"+Word+")";
			String Literal="\".*\"(\\^\\^"+URI+")?";
			String Variable="\\?"+Word;
			
			String SPOT="(("+URI+")|("+Literal+")|("+Variable+"))";
			REGEX="("+SPOT+"(\\s)+){3}"+SPOT+"(\\s)*[\\}\\.]";
			

			pattern = Pattern.compile(REGEX, Pattern.DOTALL);							
			matcher = pattern.matcher(oldQueryString);
			
			Pattern updatePattern = Pattern.compile(updateREGEX, Pattern.DOTALL);
			Matcher updateMatcher =  updatePattern.matcher(oldQueryString);
			
			while(updateMatcher.find())
			{
				String clause=oldQueryString.substring(updateMatcher.start(), updateMatcher.end()).trim();
				whereClauses.append(clause);
			}
			
			
			while(matcher.find())		
			{
				String quadruple=oldQueryString.substring(matcher.start(), matcher.end()).trim();
				
				boolean inWhere;
				if( whereClauses.indexOf(quadruple) <0 )
				{
					inWhere=false;
				}
				else
				{
					inWhere = true;
				}
				numOfQuadruples++;
				
				newQueryString+=oldQueryString.substring(startIndex, matcher.start());
				startIndex=matcher.end();
	
				//tokenize quadruples and convert them to triples:
				//s p o t  --becomes--> GRAPH ?g(numOfQuadruples) {s p o}
				//                      ?g(numOfQuadruples) strdf:hasValidTime t
				
				//About to use rewriting mechanism in updates too: 
				/*example: insert data { s p o t} is translated into: 
				 * 
				 * insert data {graph <g> {s p o} }
				 * 
				 */
				String[] token = quadruple.split("(\\s)+");
	
				
				int i=3;
				if(!isVar(token[3]) && inWhere==false) //the forth element is a literal representation of valid time
				{
					String tgraph =  "<"+parser.createValidTimeURI(token[3]).toString()+">";
					newQueryString+="\n GRAPH "+tgraph+" { " +token[0]+" "+token[1]+" "+token[2]+" .}\n";
					newQueryString+= tgraph+" "+ TemporalConstants.VALID_TIME_PROPERTY;
					i=3;
	
				}
				else
				{
					String tgraph=null;
					//String addedPattern = graphVariable+numOfQuadruples+ " TemporalConstants.VALID_TIME_PROPERTY"+ token[3];
					if(periodsAndGraphs.containsKey(token[3]))
					{
						tgraph = periodsAndGraphs.get(token[3]);

					}
					else
					{
						tgraph = graphVariable + numOfQuadruples;
						periodsAndGraphs.put(token[3], tgraph);
					}
					
					if(inWhere == false)
					{
						newQueryString+="\n GRAPH "+tgraph+ " { " +token[0]+" "+token[1]+" "+token[2]+" .}\n";
						i=4;
				
					}
					else
					{
				
						newQueryString+="\n GRAPH "+tgraph+" { " +token[0]+" "+token[1]+" "+token[2]+" .}\n";
						newQueryString+=tgraph+" "+ TemporalConstants.VALID_TIME_PROPERTY;
						i=3;
					
					}
				}
				
				//add the rest tokens
				while( i<token.length)
				{
					newQueryString+=" "+token[i];
					i++;
					
				}
			}
			if(numOfQuadruples==0)
			{
				newQueryString=queryString;
				logger.info("\n\nQuadruple not found\n\n");
			}
			else
			{
				newQueryString+=oldQueryString.substring(startIndex);
				logger.info("\n\nNew QueryString:\n {}\n\n", newQueryString);
			}
		
		}
		catch(Exception e)
		{ 
			logger.error("[Strabon.queryRewriting]", e);
		}

		return newQueryString;

}
	/*
	 * checks whether an element of a statement pattern is a variable or not
	 */
	public static boolean isVar (String element)
	{
		if(element.startsWith("?"))
			return true;
		else
			return false;
	}
}
