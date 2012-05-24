/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.generaldb.schema;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.openrdf.query.algebra.evaluation.function.spatial.StrabonPolyhedron;
import org.openrdf.sail.rdbms.schema.RdbmsTable;
import org.openrdf.sail.rdbms.schema.ValueTypes;
import org.openrdf.sail.rdbms.schema.ValueType;

/**
 * Manages the life-cycle of the rows in a single predicate table.
 * 
 * @author James Leigh
 * 
 */
public class TripleTable {

	public static int tables_created;

	public static int total_st;

	public static final boolean UNIQUE_INDEX_TRIPLES = true;

	private static final String[] PKEY = { "obj", "subj", "ctx", "expl" };

	private static final String[] SUBJ_INDEX = { "subj" };

	private static final String[] CTX_INDEX = { "ctx" };

	private static final String[] PRED_PKEY = { "obj", "subj", "pred", "ctx", "expl" };

	private static final String[] PRED_INDEX = { "pred" };

	private static final String[] EXPL_INDEX = { "expl" };

	private RdbmsTable table;

	private ValueTypes objTypes = new ValueTypes();

	private ValueTypes subjTypes = new ValueTypes();

	private boolean initialize;

	private boolean predColumnPresent;

	private boolean indexed;

	private IdSequence ids;

	/**
	 * My additions in order to avoid the expensive initialization queries
	 */
	private int[] subjAggregates;
	private int[] objAggregates;

	public TripleTable(RdbmsTable table) {
		this.table = table;
	}

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	public boolean isPredColumnPresent() {
		return predColumnPresent;
	}

	public void setPredColumnPresent(boolean present) {
		predColumnPresent = present;
	}

	public void setIndexed(boolean indexingTriples) {
		indexed = true;
	}

	public synchronized void initTable()
			throws SQLException
			{
		if (initialize)
			return;
		table.createTransactionalTable(buildTableColumns());
		tables_created++;
		total_st++;
		if (UNIQUE_INDEX_TRIPLES) {
			if (isPredColumnPresent()) {
				table.primaryIndex(PRED_PKEY);
				total_st++;
			}
			else {
				table.primaryIndex(PKEY);
				total_st++;
			}
		}
		if (indexed) {
			createIndex();
		}
		initialize = true;
			}


	public void reload()
			throws SQLException
			{
		//File existing = new File(StrabonPolyhedron.CACHEPATH+"initialized.bin");
		File file = new File(StrabonPolyhedron.CACHEPATH+"tableProperties/"+this.getName());
		//if(!existing.exists())
		if(!file.exists())
		{			
			long start = System.nanoTime();
			table.count();
			if (table.size() > 0) {

				ValueType[] values = ValueType.values();
				String[] OBJ_CONTAINS = new String[values.length];
				String[] SUBJ_CONTAINS = new String[values.length];
				StringBuilder sb = new StringBuilder();
				for (int i = 0, n = values.length; i < n; i++) {
					sb.delete(0, sb.length());
					ValueType code = values[i];
					sb.append("MAX(CASE WHEN obj BETWEEN ").append(ids.minId(code));
					sb.append(" AND ").append(ids.maxId(code));
					sb.append(" THEN 1 ELSE 0 END)");
					OBJ_CONTAINS[i] = sb.toString();
					sb.delete(0, sb.length());
					sb.append("MAX(CASE WHEN subj BETWEEN ").append(ids.minId(code));
					sb.append(" AND ").append(ids.maxId(code));
					sb.append(" THEN 1 ELSE 0 END)");
					SUBJ_CONTAINS[i] = sb.toString();
				}
				int[] aggregate = table.aggregate(OBJ_CONTAINS);

				objAggregates = aggregate;

				for (int i = 0; i < aggregate.length; i++) {
					if (aggregate[i] == 1) {
						objTypes.add(values[i]);
					}
				}
				aggregate = table.aggregate(SUBJ_CONTAINS);

				subjAggregates = aggregate;

				for (int i = 0; i < aggregate.length; i++) {
					if (aggregate[i] == 1) {
						subjTypes.add(values[i]);
					}
				}
			}
			initialize = true;
			//System.out.println("["+this.getName()+"] Cache TRIPLETABLE file not found. Initialization took "+(System.nanoTime()-start)+" nanoseconds.");
		}
		else
		{
			long start = System.nanoTime();
			//System.out.println("Everything is cached");
			table.count();
			if (table.size() > 0) {


				try {
					ValueType[] values = ValueType.values();
					//KKFileInputStream fstream = new FileInputStream(new File(StrabonPolyhedron.TABLE_SUBJ_OBJ_TYPES));
					FileInputStream fstream = new FileInputStream(file);
					DataInputStream dis = new DataInputStream(fstream);
					//KKboolean foundName = false;
					//KKwhile(!foundName)
					//KK{
					//KKString potentialName = dis.readUTF();
					//KKif(table.getName().equals(potentialName))//found table name
					//KK{
					//KK	foundName = true;
					int[] aggregate = new int[16];
					for(int i=0;i<16;i++)
					{
						aggregate[i] = dis.readInt();
						if (aggregate[i] == 1) {
							subjTypes.add(values[i]);

						}
					}

					for(int i=0;i<16;i++)
					{
						aggregate[i] = dis.readInt();
						if (aggregate[i] == 1) {
							objTypes.add(values[i]);

						}
					}

					dis.close();
					//KK}
					//KKelse //skip line
					//KK{
					//KKwhile(dis.readChar()!='\n')
					//KK{
					//KK//just loop till you find next line
					//KK}
					//KK}}
					//KK}
				} catch (FileNotFoundException e) {

					e.printStackTrace();
				} catch (IOException e) {

					e.printStackTrace();
				}

			}
			initialize = true;
			//System.out.println("["+this.getName()+"] Cache TRIPLETABLE file found. Initialization took "+(System.nanoTime()-start)+" nanoseconds.");
		}
			}

	public void close()
			throws SQLException
			{
		//XXX uncomment during 1st run in order to fill properties file
		//KKFile output = new File(StrabonPolyhedron.TABLE_SUBJ_OBJ_TYPES);
		File output = new File(StrabonPolyhedron.CACHEPATH+"tableProperties/"+this.getName());
		//File existing = new File(StrabonPolyhedron.CACHEPATH+"initialized.bin");
		if(!output.exists()&&subjAggregates!=null)
		{
			System.out.println("["+this.getName()+"] Cache TRIPLETABLE file not found. Storing cache details...");
			FileOutputStream fstream = null;
			DataOutputStream dos = null;
			try {
				//KKfstream = new FileOutputStream(output,true);
				fstream = new FileOutputStream(output,false);

				dos = new DataOutputStream(fstream);

				//KKdos.writeUTF(table.getName());
				//dos.writeChar(';');
				for(int ii=0;ii<subjAggregates.length;ii++)
				{
					dos.writeInt(subjAggregates[ii]);
				}

				//dos.writeChar(';');
				for(int ii=0;ii<objAggregates.length;ii++)
				{
					dos.writeInt(objAggregates[ii]);
				}

				//KKdos.writeChar('\n');
				dos.close();

			} catch (IOException e) {

				e.printStackTrace();
			}
		}



		table.close();
			}


	//	//Original close
	//	public void close()
	//		throws SQLException
	//	{
	//		table.close();
	//	}

	public boolean isIndexed()
			throws SQLException
			{
		return table.getIndexes().size() > 1;
			}

	public void createIndex()
			throws SQLException
			{
		if (isPredColumnPresent()) {
			table.index(PRED_INDEX);
			total_st++;
		}
		table.index(SUBJ_INDEX);
		total_st++;
		table.index(CTX_INDEX);
		total_st++;
		table.index(EXPL_INDEX);
		total_st++;
			}

	public void dropIndex()
			throws SQLException
			{
		for (Map.Entry<String, List<String>> e : table.getIndexes().entrySet()) {
			if (!e.getValue().contains("OBJ") && !e.getValue().contains("obj")) {
				table.dropIndex(e.getKey());
			}
		}
			}

	public boolean isReady() {
		return initialize;
	}

	public void blockUntilReady()
			throws SQLException
			{
		if (initialize)
			return;
		initTable();
			}

	public String getName()
			throws SQLException
			{
		return table.getName();
			}

	public String getNameWhenReady()
			throws SQLException
			{
		blockUntilReady();
		return table.getName();
			}

	public ValueTypes getObjTypes() {
		return objTypes;
	}

	public void setObjTypes(ValueTypes valueTypes) {
		this.objTypes.merge(valueTypes);
	}

	public ValueTypes getSubjTypes() {
		return subjTypes;
	}

	public void setSubjTypes(ValueTypes valueTypes) {
		this.subjTypes.merge(valueTypes);
	}

	public void modified(int addedCount, int removedCount)
			throws SQLException
			{
		blockUntilReady();
		table.modified(addedCount, removedCount);
		table.optimize();
		if (isEmpty()) {
			objTypes.reset();
			subjTypes.reset();
		}
			}

	public boolean isEmpty()
			throws SQLException
			{
		blockUntilReady();
		return table.size() == 0;
			}

	@Override
	public String toString() {
		return table.getName();
	}

	public void drop()
			throws SQLException
			{
		blockUntilReady();
		table.drop();
			}

	protected CharSequence buildTableColumns() {
		StringBuilder sb = new StringBuilder();
		sb.append("  ctx ").append(ids.getSqlType()).append(" NOT NULL,\n");
		sb.append("  subj ").append(ids.getSqlType()).append(" NOT NULL,\n");
		if (isPredColumnPresent()) {
			sb.append("  pred ").append(ids.getSqlType()).append(" NOT NULL,\n");
		}
		sb.append("  obj ").append(ids.getSqlType()).append(" NOT NULL,\n");
		sb.append("  expl ").append("BOOL").append(" NOT NULL,\n");
		//FIXME
		sb.append("  interval_start ").append("TIMESTAMP DEFAULT NULL").append(",\n");
		sb.append("  interval_end ").append("TIMESTAMP DEFAULT NULL").append("\n");
		return sb;
	}
}
