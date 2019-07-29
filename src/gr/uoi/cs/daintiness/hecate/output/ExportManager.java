package gr.uoi.cs.daintiness.hecate.output;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import gr.uoi.cs.daintiness.hecate.diff.DiffResult;
import gr.uoi.cs.daintiness.hecate.metrics.tables.MetricsOverVersion;
import gr.uoi.cs.daintiness.hecate.metrics.tables.TablesInfo;
import gr.uoi.cs.daintiness.hecate.output.heartbeatExports.HeartbeatExporter;
import gr.uoi.cs.daintiness.hecate.output.heartbeatExports.HeartbeatRecord;
import gr.uoi.cs.daintiness.hecate.output.heartbeatExports.MetricsExporter;
import gr.uoi.cs.daintiness.hecate.output.heartbeatExports.TransitionChangesExporteFactory;
import gr.uoi.cs.daintiness.hecate.output.heartbeatExports.TransitionChangesExporter;
import gr.uoi.cs.daintiness.hecate.output.tableExports.TableMetricsExporter;
import gr.uoi.cs.daintiness.hecate.output.tableExports.TableMetricsExporterFactory;
import gr.uoi.cs.daintiness.hecate.transitions.Transitions;

public class ExportManager implements FileExporter{

	private ArrayList<TableMetricsExporter> tableMetricsExporters;
	private MetricsExporter metricsExporter;
	private HeartbeatExporter heartbeatExporter;
	private HashMap<Integer, HeartbeatRecord> heartbeatRecords;
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
			heartbeatRecords = new HashMap<Integer, HeartbeatRecord>();
			//System.out.println("[ExportManager constructor] Working dir is: " + this.path + "\n");
		}

	}



	/**
	 * Used to create the metrics.csv file
	 */
	public void exportMetrics(DiffResult res){
		if(!isPathNull()){
			try {
				metricsExporter.exportMetrics(res, path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Exports all the different metrics files concerning TABLES (not schema life)
	 * 
	 * Basically creates all the TableMetricsExporter instances of the arraylist tableMetricsExporters, and,
	 *     for each of these table metrics Exporters, 
	 *          for each table (i.e., row in the file), 
	 *              for each version (i.e., column in the row), 
	 *                 it writes the respective metric.
	 *          Be careful on the original sorting of table names, so that tables are exported alphabetically
	 */
	public void exportTableMetrics(int versions, TablesInfo ti){
		if(!isPathNull()){
			TableMetricsExporterFactory exportFactory = new TableMetricsExporterFactory();
			tableMetricsExporters = exportFactory.createExporters(path,versions, this.heartbeatRecords);

			//TODO MUST find a way to clear the TablesInfo each time we change a data set, without closing and re-running Hecate from scratch!
			//2019-06-13: probably did it by calling diff.clear at HacateBackEndEngine#handleFolderWithSchemaHistory
			//System.out.println("exportManager#explortTableMEtrics(): tablesInfo #tables " + ti.getTables().size());
			//System.out.println("exportManager#explortTableMEtrics(): tablesInfo #versions " + ti.getNumVersions());
			ArrayList<String> tableNamesSorted = new ArrayList<String>();
			tableNamesSorted.addAll(ti.getTables());
			Collections.sort(tableNamesSorted);	

			for(TableMetricsExporter tableMetricsExporter: tableMetricsExporters){
				tableMetricsExporter.writeHeader(versions);
				//for (String t : ti.getTables()){
				for (String t : tableNamesSorted){
					tableMetricsExporter.writeText(t + tableMetricsExporter.getDelimiter());

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


	/**
	 * Invokes the HeartbeatExporter to export the SchemaHeartbeat.tsv
	 */
	public String exportSchemaHeartbeatFile() {
		this.heartbeatExporter = new HeartbeatExporter(this.getDirectory(), this.heartbeatRecords);
		return this.heartbeatExporter.exportSchemaHeartbeatFile();
	}
	
	/**
	 * Used to create all the transitions.* files -- e.g., .xml and .csv 
	 */
	public void exportTransitionChanges(Transitions transitions,DiffResult res){
		if(!isPathNull()){
			TransitionChangesExporteFactory transitionsFactory = new TransitionChangesExporteFactory();
			transitionChangesExporters	= transitionsFactory.createExporters(transitions, path);

			for(TransitionChangesExporter transitionsExporter: transitionChangesExporters){
				transitionsExporter.exportTransitions();
			}
		}
	}

	/**
	 * Returns the string with the path of the results directory
	 * If the results directory does not exist, it creates it
	 * 
	 * @return a string with the path of the results directory
	 */
	public String getDirectory() {
		String parent = (new File(path)).getParent();
		File directory = new File(parent + File.separator + "results");
		if (!directory.exists()) {
			directory.mkdir();
		}
		return directory.getPath();
	}

}
