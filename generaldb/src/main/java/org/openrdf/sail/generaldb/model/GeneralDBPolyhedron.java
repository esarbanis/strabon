package org.openrdf.sail.generaldb.model;

import java.io.IOException;

import org.openrdf.model.URI;
import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.sail.rdbms.model.RdbmsValue;

import com.vividsolutions.jts.io.ParseException;

import eu.earthobservatory.constants.GeoConstants;

/**
 * 
 * @author Manos Karpathiotakis <mk@di.uoa.gr>
 *
 */
public class GeneralDBPolyhedron extends RdbmsValue {

	private static final long serialVersionUID = -7751266742783048766L;
	
	/**
	 * The string representation of this value. The representation
	 * may be one of the Constraint-based, WKT, or GML encodings.
	 * 
	 * @see #setPolyhedronStringRep(StrabonPolyhedron)
	 */
	private String polyhedronStringRep;
	
	/**
	 * The underlying strabon polyhedron
	 */
	private StrabonPolyhedron polyhedron;
	
	/**
	 * The datatype of the polyhedron
	 */
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

	public void setPolyhedronStringRep(StrabonPolyhedron polyhedron) throws IOException, ClassNotFoundException {
		//TODO kkyzir prepares this method
		// TODO add GML
		
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
		return new String(this.polyhedronStringRep)+";http://www.opengis.net/def/crs/EPSG/0/"+this.getPolyhedron().getGeometry().getSRID();
	}

	@Override
	public String toString() {
		return new String("\""+this.polyhedronStringRep+";http://www.opengis.net/def/crs/EPSG/0/"
				+this.getPolyhedron().getGeometry().getSRID()+"\"" + "^^<" + 
				((StrabonPolyhedron.EnableConstraintRepresentation)  ? 
						GeoConstants.stRDFSemiLinearPointset : GeoConstants.WKT)
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