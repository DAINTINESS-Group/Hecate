package gr.uoi.cs.daintiness.hecate.output;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


import gr.uoi.cs.daintiness.hecate.diff.DiffResult;
import gr.uoi.cs.daintiness.hecate.metrics.tables.MetricsOverVersion;
import gr.uoi.cs.daintiness.hecate.metrics.tables.TablesInfo;
import gr.uoi.cs.daintiness.hecate.transitions.Transitions;

public class ExportManager implements FileExporter{
	
	private ArrayList<TableMetricsExporter> tableMetricsExporters;
	private MetricsExporter metricsExporter;

	private ArrayList<TransitionChangesExporter> transitionChangesExporters;
	private String path;
	
	
	private boolean isPathNull(){
		if(this.path == null)
			return true;
		return false;
	}
	
	public ExportManager(String aPath){
		this.path = aPath;
		if(!isPathNull()){
			tableMetricsExporters = new ArrayList<TableMetricsExporter>();
			this.path = this.getDirectory();
			metricsExporter = new MetricsExporter(this.path);
			transitionChangesExporters = new ArrayList<TransitionChangesExporter>();
			System.out.println("[ExportManager constructor] Working dir is: " + this.path + "\n");
		}
		
	}
	
	public void exportTableMetrics(int versions, TablesInfo ti){
		if(!isPathNull()){
			TableMetricsExporterFactory exportFactory = new TableMetricsExporterFactory();
			tableMetricsExporters = exportFactory.createExporters(path,versions);

			//TODO MUST find a way to clear the TablesInfo each time we change a data set, without closing and re-running Hecate from scratch!
			
			ArrayList<String> tableNamesSorted = new ArrayList<String>();
			tableNamesSorted.addAll(ti.getTables());
			Collections.sort(tableNamesSorted);	
			
			for(TableMetricsExporter tableMetricsExporter: tableMetricsExporters){
				tableMetricsExporter.writeHeader(versions);
				//for (String t : ti.getTables()){
				for (String t : tableNamesSorted){
					tableMetricsExporter.writeText(t + ";");
					
					MetricsOverVersion metricsOverVersion = ti.getTableMetrics(t);
					for (int i = 0; i < versions; i++) {
						if (metricsOverVersion != null && metricsOverVersion.containsKey(i)) {
							tableMetricsExporter.writeChanges(metricsOverVersion, i);
						} else {
							tableMetricsExporter.writeEmptyCells();
						}
					}
					tableMetricsExporter.writeLastColumn();				
				}
				tableMetricsExporter.closeFile();
			}//end for
			tableNamesSorted.clear();
		}

	}
	
	
	
	public void exportMetrics(DiffResult res){
		if(!isPathNull()){
			try {
				metricsExporter.exportMetrics(res, path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void exportTransitionChanges(Transitions transitions,DiffResult res){
		if(!isPathNull()){
			TransitionChangesExporteFactory transitionsFactory = new TransitionChangesExporteFactory();
			transitionChangesExporters	= transitionsFactory.createExporters(transitions, path);
			
			for(TransitionChangesExporter transitionsExporter: transitionChangesExporters){
				transitionsExporter.exportTransitions();
			}
		}
	}
	
	public String getDirectory() {
		String parent = (new File(path)).getParent();
		File directory = new File(parent + File.separator + "results");
		if (!directory.exists()) {
			directory.mkdir();
		}
		return directory.getPath();
	}

}
