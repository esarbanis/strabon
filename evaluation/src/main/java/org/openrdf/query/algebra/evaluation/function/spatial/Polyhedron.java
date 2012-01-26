package org.openrdf.query.algebra.evaluation.function.spatial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * @author kkyzir
 *
 */
public class Polyhedron implements Serializable {
	public static final int OPTIMAL_CONVEX_PARTITION = 0; //Optimal number of pieces, O(n^4) time and O(n^3) space
	public static final int APPROXIMATE_CONVEX_PARTITION = 1; //Approximate optimal number of pieces, uses approximation algorithm of Hertel and Mehlhorn (triangulation), O(n) time and space.
	public static final int GREEN_CONVEX_PARTITION = 2; //Approximate optimal number of pieces, uses sweep-line approximation algorithm of Greene, O(n log(n)) time and O(n) space.
	public static final int Y_MONOTONE_PARTITION = 3; //Same complexity as Hertel and Mehlhorn, but can sometimes produce better results (i.e., convex partitions with fewer pieces).

	public static final String stRDFSemiLinearPointset="http://strdf.di.uoa.gr/ontology#SemiLinearPointSet";
	public static final String ogcGeometry="http://strdf.di.uoa.gr/ontology#WKT";

}
