package gr.uoi.cs.daintiness.hecate.hecatemanager;

import gr.uoi.cs.daintiness.hecate.diff.DiffResult;
import gr.uoi.cs.daintiness.hecate.metrics.Metrics;
import gr.uoi.cs.daintiness.hecate.sql.Schema;
import gr.uoi.cs.daintiness.hecate.sql.Table;
import gr.uoi.cs.daintiness.hecate.transitions.Transitions;

import java.io.File;
import java.util.Map.Entry;


public class HecateBackEndEngine implements IHecateBackEndEngine {

	private Schema oldSchema;
	private Schema newSchema;
	private Transitions transitions;
	private HecateAPI hecateApi;
	private DiffResult res;
	private HecateApiFactory hecateApiFactory ;

	public HecateBackEndEngine(String absPathOfSchemaHistoryFolder){
		oldSchema = null;
		newSchema = null;
		transitions = new Transitions();
		res = new DiffResult();
		hecateApiFactory = new HecateApiFactory();
		hecateApi = hecateApiFactory.createHecateManager(absPathOfSchemaHistoryFolder);
	}
	
	@Override
	public int handleSchemaPair(File oldFile,File newFile){
		hecateApi = hecateApiFactory.createHecateManager(null);
		oldSchema = hecateApi.parse(oldFile.getAbsolutePath());
		newSchema = hecateApi.parse(newFile.getAbsolutePath());	
		res = hecateApi.getDifference(oldSchema, newSchema);
		
		if(res == null)
			 return -1;
		return 0;
	}
	
	@Override
	public int handleFolderWithSchemaHistory(File folderOfSchemaHistory) {
		if (folderOfSchemaHistory == null)
			return -1;
		if (!folderOfSchemaHistory.isDirectory())
			return -1;

		//Essential to be able to work with multiple projects, one after the other.
		res.clear();
		res = new DiffResult();
//		System.out.println("HecateBckEngine#handleFolder(): tablesInfo #tables " + res.getTableInfo().getTables().size());
//		System.out.println("HecateBckEngine#handleFolder(): tablesInfo #versions " + res.getTableInfo().getNumVersions());

		
		String path = folderOfSchemaHistory.getAbsolutePath();			
		String[] list = folderOfSchemaHistory.list();
		java.util.Arrays.sort(list);
		
		for (int i = 0; i < list.length-1; i++) {
			System.out.println("Parsing " + list[i] + " vs " + list[i+1]);
			
			this.setOldSchema(path + File.separator + list[i], i);
			this.setNewSchema(path + File.separator + list[i+1], i, list);
			this.performPairwiseComparison();	
		}
		
		this.exportOutputFilesAndCleanUp(path, list);

		//String parent = (new File(path)).getParent();
		//File folderOfOutputResults = new File(parent + File.separator + "results");
		//folderOfSchemaHistory = null;
		return list.length;
	}
	
	@Override
	public Schema getOldSchema(){
		return this.oldSchema;
	}
	
	@Override
	public Schema getNewSchema(){
		return this.newSchema;
	}
	
	@Override
	public Metrics getMetrics(){
		return this.res.getMetrics();
	}
	
	
	@Override
	public void setOldSchema(String path,int i){
		oldSchema = hecateApi.parse(path);
		addTables(i, oldSchema);
	}
	
	@Override
	public void setNewSchema(String path,int i, String[] list){
		newSchema = hecateApi.parse(path);
		if (i == list.length-2) {
			addTables(i+1, newSchema);
		}
	}
	
	@Override
	/**
	 * Compares Two Schema Versions, old and new, and exports the respective metrics, after the comparison is over 
	 */
	public void performPairwiseComparison() {
		this.compareTwoSchemaVersions();
		this.exportMetrics();
	}
	
	
	
	
	@Override
	/**
	 * Exports tables and transitions for a folder with the history of a schema, after its transition computation has taken place 
	 * and resets the first and last schemata and computes their diff
	 * 
	 * This prepares the ground for the visualization after the batch processing of a folder
	 * @param path the absolute path of the folder where the schema history resides
	 * @param list the sorted list of file names of the folders (sort order should be isomorphic to date order)
	 */
	public void exportOutputFilesAndCleanUp(String path,String[] list){
		try {
			//System.out.println("HecateBckEngine#exportOutputNCleanUp(): tablesInfo #tables " + res.getTableInfo().getTables().size());
			//System.out.println("HecateBckEngine#exportOutputNCleanUp(): tablesInfo #versions " + res.getTableInfo().getNumVersions());
			
			hecateApi.export(res,transitions, "tables");
		} catch (Exception e) {
			e.printStackTrace();
		}
		hecateApi.export(res,transitions, "transitions");
		hecateApi.export(res,transitions, "heartbeat");
		
		resetNewNOldSchemasPostFolderExport(path, list);
		
	}

	
	private void compareTwoSchemaVersions(){
		res = hecateApi.getDifference(oldSchema, newSchema);
		transitions.add(res.getTransitionList());			
	}
	
	
	/**
	 * Exports metrics for a comparison, after the computation of the diff has taken place
	 */
	private void exportMetrics(){
		//System.out.println("HecateBckEngine#export(): tablesInfo #tables " + res.getTableInfo().getTables().size());
		//System.out.println("HecateBckEngine#export(): tablesInfo #versions " + res.getTableInfo().getNumVersions());

		
		hecateApi.export(res,transitions,"metrics");
	}

	
	/**
	 * After exporting tables and transitions for a folder, resets the first and last schemata and computes their diff
	 * 
	 * This prepares the ground for the visualization after the batch processing of a folder
	 * @param path the absolute path of the folder where the schema history resides
	 * @param list the sorted list of file names of the folders (sort order should be isomorphic to date order)
	 */
	private void resetNewNOldSchemasPostFolderExport(String path, String[] list) {
		oldSchema = hecateApi.parse(path + File.separator + list[0]);
		newSchema = hecateApi.parse(path + File.separator + list[list.length-1]);
		res = hecateApi.getDifference(oldSchema, newSchema);
	}
		

	
	private void addTables(int i, Schema schema){
		for (Entry<String, Table> e : schema.getTables().entrySet()) {
			String tableName = e.getKey();
			int attributes = e.getValue().getSize();
			res.getTableInfo().addTable(tableName, i, attributes);
		}
	}

}//end class
