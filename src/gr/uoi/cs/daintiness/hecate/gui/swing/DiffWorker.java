/**
 * 
 */
package gr.uoi.cs.daintiness.hecate.gui.swing;

import gr.uoi.cs.daintiness.hecate.hecatemanager.HecateBackEndEngineFactory;
import gr.uoi.cs.daintiness.hecate.hecatemanager.IHecateBackEndEngine;
import gr.uoi.cs.daintiness.hecate.metrics.Metrics;


import java.io.File;


import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

/**
 * @author iskoulis
 *
 */
public class DiffWorker extends SwingWorker<Void, Void> {
	
	private MainPanel mainPanel;
	private ProgressMonitor progressMonitor;
	private File oldFile = null;
	private File newFile = null;
	private File folderOfSchemaHistory = null;
	private File folderOfOutputResults;
	private boolean folderDiffOn;
	
	
	private IHecateBackEndEngine apiExecutioner;
	
	
	public DiffWorker(MainPanel mp,
			          File oldFile, File newFile) {
		this.mainPanel = mp;
		this.oldFile = oldFile;
		this.newFile = newFile;
		folderDiffOn = false;
		
		HecateBackEndEngineFactory engineFactory = new HecateBackEndEngineFactory(); 
		apiExecutioner = engineFactory.createApiExecutioner(null);
		//apiExecutioner = new ApiExecutioner(null);
	}
	
	public DiffWorker(MainPanel mp, File folder) {
		this.mainPanel = mp;
		this.folderOfSchemaHistory = folder;
		
		System.out.println(this.folderOfSchemaHistory.getAbsolutePath());
	
		//apiExecutioner = new ApiExecutioner(this.folderOfSchemaHistory.getAbsolutePath());
		HecateBackEndEngineFactory engineFactory = new HecateBackEndEngineFactory(); 
		apiExecutioner = engineFactory.createApiExecutioner(this.folderOfSchemaHistory.getAbsolutePath());
	}

	@Override
	protected Void doInBackground() throws Exception {
		
		progressMonitor = new ProgressMonitor(mainPanel.getRootPane(), "Working...", null, 0, 100);
	

		if (oldFile != null && newFile != null) {
			apiExecutioner.handleSchemaPair(oldFile,newFile);
		} else if (folderOfSchemaHistory != null){
			processFolderInBackground(progressMonitor);
		}
		return null;
	}

	/**
	 * This method processes an entire folderOfSchemaHistory with the history of a schema to compute metrics for all this history 
	 */
	public void processFolderInBackground(ProgressMonitor progressMonitor) {

		if (progressMonitor == null)
			progressMonitor = new ProgressMonitor(null, "Working...", null, 0, 100);
		folderDiffOn = true;
		
		String[] list = folderOfSchemaHistory.list();
		progressMonitor.setMaximum(list.length);
		String path = folderOfSchemaHistory.getAbsolutePath();			
		java.util.Arrays.sort(list);
		
		for (int i = 0; i < list.length-1; i++) {
			progressMonitor.setNote("Parsing " + list[i]);
			
			apiExecutioner.setOldSchema(path + File.separator + list[i], i);
			
			progressMonitor.setNote("Parsing " + list[i+1]);
			
			apiExecutioner.setNewSchema(path + File.separator + list[i+1], i, list);
			
			progressMonitor.setNote(list[i] + "-" + list[i+1]);
			
			apiExecutioner.performPairwiseComparison();
			
			progressMonitor.setProgress(i+1);	
		}
		
		apiExecutioner.exportOutputFilesAndCleanUp(path, list);
		

		String parent = (new File(path)).getParent();
		folderOfOutputResults = new File(parent + File.separator + "results");
		
		folderOfSchemaHistory = null;
	}
	
	public Metrics getMetrics() {
		return apiExecutioner.getMetrics();
	}
	
	@Override
	protected void done() {
		
		mainPanel.drawSchema(apiExecutioner.getOldSchema(), "old");
		mainPanel.drawSchema(apiExecutioner.getNewSchema(), "new");
		progressMonitor.setProgress(progressMonitor.getMaximum());
		
		if(folderDiffOn){
			JOptionPane.showConfirmDialog(null,
	                "Metrics were exported to:" +folderOfOutputResults.getPath() ,
	                "Metrics were saved", JOptionPane.DEFAULT_OPTION);
		}
		
		super.done();
	}
}
