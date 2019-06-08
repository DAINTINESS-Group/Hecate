/**
 * 
 */
package gr.uoi.cs.daintiness.hecate.hecatemanager;

/**
 * A factory for the IHecateBackEndEngineFactory
 * 
 * @author pvassil
 * @version 1.0
 * @since   2019-06-08  
 */
public class HecateBackEndEngineFactory {
	
	/**
	 * Creates a materialization of IHecateBackEndEngine, materialized as an ApiExecutioner
	 * 
	 * Keep in mind:
	 * <ul>
	 * <li> Assuming you have the project <code>PRJ</code> its should contain a folder <code>PRJ/schemata</code> with the schema history.
	 *      You should pass the <code>PRJ/schemata</code> as the folder needed
	 * <li> The absPathOfSchemaHistoryFolder can be null without a problem (@see the case of a simple comparison of two schemata in gr.uoi.cs.daintiness.hecate.gui.swing.DiffWorker)     
	 * </ul>
	 * @param absPathOfSchemaHistoryFolder the abs path
	 * @return an IHecateBackEndEngine materialized as an ApiExecutioner
	 */
	public IHecateBackEndEngine createApiExecutioner(String absPathOfSchemaHistoryFolder) {
		return new ApiExecutioner(absPathOfSchemaHistoryFolder);
	}
}
