# Reference for the interface IHecateBackEndEngine 

## public int handleSchemaPair(File oldFile, File newFile);
	/**
	 * This method compares two arbitrary versions of a schema, located in the respective files provided as parameters
	 *  
	 * @param oldFile	a File object for the schema of the old version 
	 * @param newFile	a File object for the schema of the new version
	 * @return			returns 0 if the comparison produces a DiffResult, -1 if not
	 */
	

## public int handleFolderWithSchemaHistory(File folderOfSchemaHistory);
	/**
	 * This method processes a folder with <i>only</i> SQL DDL files, i.e., the history of the schema
	 * 
	 * The method takes the folder as input, compares pairwise the differences between adjacent versions.
	 * This returns the transitions from one v. to another. 
	 * Also the method computes stats and saves them in the 'results' folder
	 *  
	 * @param folderOfSchemaHistory a File object for the folder containing the history of the schema 
	 * @return -1 if the File object is null or not a folder; the number of files of the folder, otherwise
	 */
	
## public Schema getOldSchema();	
	/**
	 * Returns the old schema of a comparison
	 * 
	 * @return The old {@link gr.uoi.cs.daintiness.hecate.sql.Schema} of the a comparison.
	 */
	

##	public Schema getNewSchema();
	/**
	 * Returns the new schema of a comparison
	 * 
	 * @return The new {@link gr.uoi.cs.daintiness.hecate.sql.Schema} of the a comparison.
	 */
	
	
## public Metrics getMetrics();
	/**
	 * Returns a Metrics object with the Metrics for the last comparison
	 * 
	 * @return a Metrics object with the Metrics for the last comparison
	 */
	