/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.iteration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.sail.generaldb.GeneralDBSpatialFuncInfo;
import org.openrdf.sail.generaldb.GeneralDBValueFactory;
import org.openrdf.sail.generaldb.algebra.GeneralDBColumnVar;
import org.openrdf.sail.generaldb.schema.IdSequence;
import org.openrdf.sail.generaldb.schema.ValueTable;
import org.openrdf.sail.rdbms.exceptions.RdbmsQueryEvaluationException;
import org.openrdf.sail.rdbms.iteration.base.RdbmIterationBase;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsValue;

/**
 * Converts a {@link ResultSet} into a {@link BindingSet} in an iteration.
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 * 
 */
public abstract class GeneralDBBindingIteration extends RdbmIterationBase<BindingSet, QueryEvaluationException> {

	protected BindingSet bindings;

	protected Collection<GeneralDBColumnVar> projections;

	protected GeneralDBValueFactory vf;

	protected IdSequence ids;

	//XXX addition- manolee
	protected HashMap<Integer,String> geoNames = new HashMap<Integer, String>();
	
	//XXX addition - constant
	protected HashMap<Integer,String> temporalVars = new HashMap<Integer, String>();

	//protected HashMap<String, Integer> sp_ConstructIndexesAndNames = new HashMap<String, Integer>();
	protected HashMap<GeneralDBSpatialFuncInfo, Integer> sp_ConstructIndexesAndNames = new HashMap<GeneralDBSpatialFuncInfo, Integer>();

	//	protected HashMap<String, Integer> sp_MetricIndexesAndNames = new HashMap<String, Integer>();
	//	
	//	protected HashMap<String, Integer> sp_IntPropertiesIndexesAndNames = new HashMap<String, Integer>();
	//	
	//	protected HashMap<String, Integer> sp_BoolPropertiesIndexesAndNames = new HashMap<String, Integer>();
	//	
	//	protected HashMap<String, Integer> sp_StringPropertiesIndexesAndNames = new HashMap<String, Integer>();

	//

	public GeneralDBBindingIteration(PreparedStatement stmt)
	throws SQLException
	{
		super(stmt);
	}

	////

	public HashMap<GeneralDBSpatialFuncInfo, Integer> getConstructIndexesAndNames() {
		return sp_ConstructIndexesAndNames;
	}

	public void setConstructIndexesAndNames(HashMap<GeneralDBSpatialFuncInfo, Integer> indexesAndNames) {
		this.sp_ConstructIndexesAndNames = indexesAndNames;
	}

	public HashMap<Integer,String> getGeoNames() {
		return geoNames;
	}
	public HashMap<Integer,String> getTemporalVars() {
		return temporalVars;
	}

	public void setGeoNames(HashMap<Integer,String> geoNames) {
		this.geoNames = geoNames;
	}
	public void setTemporalVars(HashMap<Integer,String> temporalVars) {
		this.temporalVars = temporalVars;
	}

	public void setBindings(BindingSet bindings) {
		this.bindings = bindings;
	}

	public void setProjections(Collection<GeneralDBColumnVar> proj) {
		this.projections = proj;
	}

	public void setValueFactory(GeneralDBValueFactory vf) {
		this.vf = vf;
	}

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	//XXX Numerous additions here!
	@Override
	protected BindingSet convert(ResultSet rs)
	throws SQLException
	{
		
		/// debug
		/*for(int i=1; i<12;i++) {
			Object o = rs.getObject(i);
			if (o instanceof byte[] ) {
				byte[] label = rs.getBytes(i);
				int srid = rs.getInt(i + 1);
				GeneralDBPolyhedron g = vf.getRdbmsPolyhedron(114, StrabonPolyhedron.ogcGeometry, label, srid);
				System.out.println(i+": "+g.getPolyhedronStringRep());
			} else if (o instanceof Blob ) {
				Blob labelBlob = rs.getBlob(i); 
				byte[] label = labelBlob.getBytes((long)1, (int)labelBlob.length());
				int srid = rs.getInt(i + 1);
				GeneralDBPolyhedron g = vf.getRdbmsPolyhedron(114, StrabonPolyhedron.ogcGeometry, label, srid);
				System.out.println(i+": "+g.getPolyhedronStringRep());
			}  
			else 
				System.out.println(i+": "+rs.getObject(i));
		}*/
		///

		QueryBindingSet result = new QueryBindingSet(bindings);
		for (GeneralDBColumnVar var : projections) {
			String name = var.getName();
			if (var != null && !result.hasBinding(name)) {
				Value value = var.getValue();
				if (value == null) {
					
					if(var.isSpatial())
					{
						value = createGeoValue(rs, var.getIndex() + 1);
					}
					else if(var.isTemporal())
					{
						value = createTemporalValue(rs,var.getIndex()+1);
					}
					else
					{
						//default action
						value = createValue(rs, var.getIndex() + 1);
					}
				}
				if (value != null) {
					result.addBinding(var.getName(), value);
				}
			}
		}

		for(GeneralDBSpatialFuncInfo construct : sp_ConstructIndexesAndNames.keySet())
		{
			Value value = null;
			switch(construct.getType())
			{
			case BOOLEAN: 
				value = createBooleanGeoValueForSelectConstructs(rs, sp_ConstructIndexesAndNames.get(construct));
				break;
			case DOUBLE: 
				value = createDoubleGeoValueForSelectConstructs(rs, sp_ConstructIndexesAndNames.get(construct));
				break;
			case INTEGER: 
				value = createIntegerGeoValueForSelectConstructs(rs, sp_ConstructIndexesAndNames.get(construct));
				break;
			case STRING: 
				value = createStringGeoValueForSelectConstructs(rs, sp_ConstructIndexesAndNames.get(construct));
				break;
			case WKB: 
				value = createBinaryGeoValueForSelectConstructs(rs, sp_ConstructIndexesAndNames.get(construct));
				break;

			}
			//Value value = createGeoValueForSelectConstructs(rs, sp_ConstructIndexesAndNames.get(construct));
			result.addBinding(construct.getFieldName(), value);
		}

		return result;
	}

	@Override
	protected QueryEvaluationException convertSQLException(SQLException e) {
		return new RdbmsQueryEvaluationException(e);
	}

	protected RdbmsResource createResource(ResultSet rs, int index)
	throws SQLException
	{
		Number id = ids.idOf(rs.getLong(index));
		if (id.longValue() == ValueTable.NIL_ID)
			return null;
		return vf.getRdbmsResource(id, rs.getString(index + 1));
	}

	protected RdbmsValue createValue(ResultSet rs, int index)
	throws SQLException
	{
		System.out.println("Object in index "+index+" is: "+ rs.getObject(index).toString());
		System.out.println("Object in index "+ --index+" is: "+ rs.getObject(index).toString());

		Number id = ids.idOf(rs.getLong(index));
		if (ids.isLiteral(id)) {
			String label = rs.getString(index + 1);
			String language = rs.getString(index + 2);
			String datatype = rs.getString(index + 3);
			return vf.getRdbmsLiteral(id, label, language, datatype);
		}
		return createResource(rs, index);
	}

	/**
	 * XXX additions
	 */
	/**
	 * 
	 * my addition
	 * 
	 */
	protected abstract RdbmsValue createGeoValue(ResultSet rs, int index)
	throws SQLException;

	protected abstract RdbmsValue createTemporalValue(ResultSet rs, int index)
	throws SQLException;


	protected abstract RdbmsValue createBinaryGeoValueForSelectConstructs(ResultSet rs, int index)
	throws SQLException;

	protected RdbmsValue createDoubleGeoValueForSelectConstructs(ResultSet rs, int index)
	throws SQLException
	{
		double potentialMetric;
		//case of metrics
		potentialMetric = rs.getFloat(index + 1);

		return vf.asRdbmsLiteral(vf.createLiteral(potentialMetric));

	}

	protected RdbmsValue createIntegerGeoValueForSelectConstructs(ResultSet rs, int index)
	throws SQLException
	{
		//case of integer spatial properties
		int potentialMetric = rs.getInt(index + 1);

		return vf.asRdbmsLiteral(vf.createLiteral(potentialMetric));
	}

	protected RdbmsValue createBooleanGeoValueForSelectConstructs(ResultSet rs, int index)
	throws SQLException
	{
		boolean spProperty = rs.getBoolean(index + 1);

		return vf.asRdbmsLiteral(vf.createLiteral(spProperty));
	}

	protected RdbmsValue createStringGeoValueForSelectConstructs(ResultSet rs, int index)
	throws SQLException
	{
		String spProperty = rs.getString(index + 1);

		return vf.asRdbmsLiteral(vf.createLiteral(spProperty));

	}

	//	protected RdbmsValue createGeoValueForSelectConstructs(ResultSet rs, int index)
	//	throws SQLException
	//	{
	//		double potentialMetric;
	//		try
	//		{
	//			//case of metrics
	//			potentialMetric = rs.getFloat(index + 1);
	//
	//			return vf.asRdbmsLiteral(vf.createLiteral(potentialMetric));
	//
	//		}
	//		catch(SQLException e)
	//		{
	//			//Case of spatial constructs
	//			byte[] label = rs.getBytes(index + 1);
	//			return vf.getRdbmsPolyhedron(114, StrabonPolyhedron.ogcGeometry, label);
	//		}
	//
	//	}

}
