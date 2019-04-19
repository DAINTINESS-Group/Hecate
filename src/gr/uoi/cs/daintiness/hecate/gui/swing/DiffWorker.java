/**
 * 
 */
package gr.uoi.cs.daintiness.hecate.gui.swing;

import gr.uoi.cs.daintiness.hecate.hecatemanager.ApiExecutioner;

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
	private File folder = null;
	
	private File directory;
	private boolean folderDiffOn;
	
	
	private ApiExecutioner apiExecutioner;
	
	
	public DiffWorker(MainPanel mp,
			          File oldFile, File newFile) {
		this.mainPanel = mp;
		this.oldFile = oldFile;
		this.newFile = newFile;
		folderDiffOn = false;
		
		apiExecutioner = new ApiExecutioner(null);
	}
	
	public DiffWorker(MainPanel mp, File folder) {
		this.mainPanel = mp;
		this.folder = folder;
		
		System.out.println(this.folder.getAbsolutePath());
	
		apiExecutioner = new ApiExecutioner(this.folder.getAbsolutePath());

	}

	@Override
	protected Void doInBackground() throws Exception {
		
		progressMonitor = new ProgressMonitor(mainPanel.getRootPane(), "Working...", null, 0, 100);
	

		if (oldFile != null && newFile != null) {
			apiExecutioner.handleSchemaPairs(oldFile,newFile);
		} else if (folder != null){
			processFolderInBackground(progressMonitor);
		}
		return null;
	}

	/**
	 * This method processes an entire folder with the history of a schema to compute metrics for all this history 
	 */
	public void processFolderInBackground(ProgressMonitor progressMonitor) {

		if (progressMonitor == null)
			progressMonitor = new ProgressMonitor(null, "Working...", null, 0, 100);
		folderDiffOn = true;
		
		String[] list = folder.list();
		progressMonitor.setMaximum(list.length);
		String path = folder.getAbsolutePath();			
		java.util.Arrays.sort(list);
		
		for (int i = 0; i < list.length-1; i++) {
			progressMonitor.setNote("Parsing " + list[i]);
			
			
			apiExecutioner.setOldSchema(path + File.separator + list[i], i);
			
			progressMonitor.setNote("Parsing " + list[i+1]);
			
			apiExecutioner.setNewSchema(path + File.separator + list[i+1], i, list);
			
			progressMonitor.setNote(list[i] + "-" + list[i+1]);
			
			apiExecutioner.getDifference();
			apiExecutioner.exportMetrics();
			progressMonitor.setProgress(i+1);	
		}
		
		apiExecutioner.exportFiles(path, list);
		

		String parent = (new File(path)).getParent();
		directory = new File(parent + File.separator + "results");
		
		folder = null;
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
	                "Metrics were exported to:" +directory.getPath() ,
	                "Metrics were saved", JOptionPane.DEFAULT_OPTION);
		}
		
		super.done();
	}
}
