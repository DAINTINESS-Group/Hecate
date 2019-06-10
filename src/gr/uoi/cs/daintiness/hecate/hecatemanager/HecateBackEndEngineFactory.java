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
	 * Creates a materialization of IHecateBackEndEngine, materialized as an HecateBackEndEngine
	 * 
	 * Keep in mind:
	 * <ul>
	 * <li> Assuming you have the project <code>PRJ</code> its should contain a folder <code>PRJ/schemata</code> with the schema history.
	 *      You should pass the <code>PRJ/schemata</code> as the parameter needed
	 * <li> The absPathOfSchemaHistoryFolder can be null without a problem (@see the case of a simple comparison of two schemata in gr.uoi.cs.daintiness.hecate.gui.swing.DiffWorker)     
	 * </ul>
	 * @param absPathOfSchemaHistoryFolder the abs path
	 * @return an IHecateBackEndEngine materialized as an HecateBackEndEngine
	 */
	public IHecateBackEndEngine createApiExecutioner(String absPathOfSchemaHistoryFolder) {
		return new HecateBackEndEngine(absPathOfSchemaHistoryFolder);
	}
}
