/**
 * 
 */
package gr.uoi.cs.daintiness.hecate.clients;

import java.io.File;

import gr.uoi.cs.daintiness.hecate.hecatemanager.HecateBackEndEngineFactory;
import gr.uoi.cs.daintiness.hecate.hecatemanager.IHecateBackEndEngine;


/**
 * A simple client to process a folder.
 * 
 * @author pvassil
 * @since 2019-06-10
 * @version v.0.4
 */
public class SingleFolderProcessingClient {

	/**
	 * A simple client to process a folder.
	 * Assume you have the project <code>PRJ</code> its should contain a folder <code>PRJ/schemata</code> with the schema history.
	 * You should pass the <code>PRJ/schemata</code> as the parameter needed at args[0]
	 *
	 * @param args the first parameter given should be the path of the schema history. 
	 * 
	 */
	public static void main(String[] args) {
		String folderOfSchemaHistory = args[0];
		File folderToProcess = new File(folderOfSchemaHistory);
		
		HecateBackEndEngineFactory factory = new HecateBackEndEngineFactory();
		IHecateBackEndEngine engine = factory.createApiExecutioner(folderOfSchemaHistory);
		
		System.out.println("Working with " + folderOfSchemaHistory + "\n");
		
		engine.handleFolderWithSchemaHistory(folderToProcess);

		System.out.println("\nSUCCESSFUL END: Done with " + folderOfSchemaHistory + "\n" +
			"********************************************************************************* \n\n");
		

	}

}
