package gr.uoi.cs.daintiness.hecate.hecatemanager;

import java.io.File;

import gr.uoi.cs.daintiness.hecate.metrics.Metrics;
import gr.uoi.cs.daintiness.hecate.sql.Schema;

/**
 * The IHecateBackEndEngine is an interface that wraps the functionality of the entire back-end system.
 * 
 * The main method calls are
 * <ul> 
 * <li> handleSchemaPairs to compare a couple of schemata
 * <li>TODO ADD a processFolder method
 * <li> getMetrics to return the computed metrics
 * <li> getOldSchema and getNewSchema that return the old and schemata of a comparison (first and last schemata of a folder, if applicable)
 * </ul>
 * 
 * TODO: consider splitting this iface. 
 * <b>Probably make a simple interface with a couple of core calls, and specialize it with the details</b>
 * Too many methods are basically related to the ApiExecutioner implementation, 
 * and practically they populate the private members of the ApiExecutioner class. This is why they return void.
 * Cannot remove them now, because they are called at the diff worker with the "informed" ProgressMonitor wrt internal steps.
 * 
 * @author	pvassil
 * @version 1.1
 * @since   2019-06-08 
 */
public interface IHecateBackEndEngine {

	/**
	 * This method compares two arbitrary versions of a schema, located in the respective files provided as parameters
	 *  
	 * @param oldFile	a File object for the schema of the old version 
	 * @param newFile	a File object for the schema of the new version
	 * @return			returns 0 if the comparison produces a DiffResult, -1 if not
	 */
	int handleSchemaPair(File oldFile, File newFile);


	/**
	 * Returns the old schema of a comparison
	 * 
	 * @return The old {@link gr.uoi.cs.daintiness.hecate.sql.Schema} of the a comparison.
	 */
	Schema getOldSchema();

	
	/**
	 * Returns the new schema of a comparison
	 * 
	 * @return The new {@link gr.uoi.cs.daintiness.hecate.sql.Schema} of the a comparison.
	 */
	Schema getNewSchema();
	

	/**
	 * Returns a Metrics object with the Metrics for the last comparison
	 * 
	 * @return a Metrics object with the Metrics for the last comparison
	 */
	Metrics getMetrics();

	

	/**
	 * In the context of a pairwise comparison, it determines that the oldSchema of the comparison is in the i-th position in the list
	 * Also makes sure that the very final position, in the context of a folder comparison is the last version
	 * 
	 * @param path	a String with the absolute path for the folder containing the schema history
	 * @param i		the position of the oldSchema file,
	 */
	void setOldSchema(String path, int i);

	
	/**
	 * In the context of a pairwise comparison, it determines that the newSchema of the comparison is in the i+1 position in the list
	 * Also makes sure that the very final position, in the context of a folder comparison is the last version
	 * 
	 * @param path	a String with the absolute path for the folder containing the schema history
	 * @param i		the position of the oldSchema file, i.e., i+1 is the position of the newSchema file
	 * @param list	the sorted list of file names of the folders (sort order should be isomorphic to date order)
	 */
	void setNewSchema(String path, int i, String[] list);

	
	/**
	 * Compares Two Schema Versions, old and new and exports the respective metrics, after the comparison is over 
	 */
	void performPairwiseComparison();

	

	/**
	 * Exports tables and transitions for a folder with the history of a schema, after its transition computation has taken place 
	 * and resets the first and last schemata and computes their diff
	 * 
	 * This prepares the ground for the visualization after the batch processing of a folder
	 * @param path the absolute path of the folder where the schema history resides
	 * @param list the sorted list of file names of the folders (sort order should be isomorphic to date order)
	 */
	void exportOutputFilesAndCleanUp(String path, String[] list);

	


}//end iFace