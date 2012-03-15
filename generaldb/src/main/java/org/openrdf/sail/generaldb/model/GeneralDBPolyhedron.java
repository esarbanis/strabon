package org.openrdf.sail.generaldb.model;

import java.io.IOException;

import org.openrdf.model.URI;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.sail.rdbms.model.RdbmsValue;

import com.vividsolutions.jts.io.ParseException;


public class GeneralDBPolyhedron extends RdbmsValue{

	private String polyhedronStringRep;
	private StrabonPolyhedron polyhedron;
	private URI datatype;
	/**
	 * CONSTRUCTOR
	 */
	public GeneralDBPolyhedron(Number id, Integer version, URI datatype, byte[] polyhedron, int srid) throws IOException, ClassNotFoundException {
		super(id, version);

		try {
			this.polyhedron = new StrabonPolyhedron(polyhedron, srid);
		} catch (ParseException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
		setPolyhedronStringRep(this.polyhedron);
		this.datatype = datatype;
	}

	//	public GeneralDBPolyhedron(Number id, Integer version, URI datatype, String polyhedron) throws IOException, ClassNotFoundException {
	//		super(id, version);
	//
	//		try {
	//			this.polyhedron = new StrabonPolyhedron(polyhedron);
	//		} catch (ParseException e) {
	//
	//			e.printStackTrace();
	//		} catch (Exception e) {
	//
	//			e.printStackTrace();
	//		}
	//		setPolyhedronStringRep(this.polyhedron);
	//		this.datatype = datatype;
	//	}
	/**
	 * METHODS
	 */

	public String getPolyhedronStringRep() {
		return polyhedronStringRep;
	}

	public void setPolyhedronStringRep(StrabonPolyhedron polyhedron) throws  IOException, ClassNotFoundException {
		//TODO kkyzir prepares this method

		if (StrabonPolyhedron.EnableConstraintRepresentation) {
			this.polyhedronStringRep = polyhedron.toConstraints();	
		} else {
			this.polyhedronStringRep = polyhedron.toWKT();
		}		
	}

	public URI getDatatype() {
		return datatype;
	}

	public void setDatatype(URI datatype) {
		this.datatype = datatype;
	}

	public StrabonPolyhedron getPolyhedron() {
		return polyhedron;
	}


	public void setPolyhedron(StrabonPolyhedron polyhedron) {
		this.polyhedron = polyhedron;
	}


	public String stringValue() {
		return new String(this.polyhedronStringRep);
	}

	@Override
	public String toString() {
		return new String("\""+this.polyhedronStringRep+";http://www.opengis.net/def/crs/EPSG/0/"
				+this.getPolyhedron().getGeometry().getSRID()+"\"" + "^^<" + 
				((StrabonPolyhedron.EnableConstraintRepresentation)  ? 
						StrabonPolyhedron.stRDFSemiLinearPointset : StrabonPolyhedron.ogcGeometry)
						+">");
	}

	@Override
	public int hashCode() {
		return polyhedronStringRep.hashCode();
	}

	@Override
	public boolean equals(Object other) {

		if(other instanceof GeneralDBPolyhedron)
		{
			if (((GeneralDBPolyhedron) other).getPolyhedron().equals(this.getPolyhedron()))
			{
				return true;
			}

		}
		return false;
	}

}