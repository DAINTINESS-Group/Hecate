package gr.uoi.cs.daintiness.hecate.output;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;


import gr.uoi.cs.daintiness.hecate.diff.DiffResult;
import gr.uoi.cs.daintiness.hecate.metrics.tables.MetricsOverVersion;
import gr.uoi.cs.daintiness.hecate.metrics.tables.TablesInfo;
import gr.uoi.cs.daintiness.hecate.output.heartbeatExports.MetricsExporter;
import gr.uoi.cs.daintiness.hecate.output.heartbeatExports.TransitionChangesExporteFactory;
import gr.uoi.cs.daintiness.hecate.output.heartbeatExports.TransitionChangesExporter;
import gr.uoi.cs.daintiness.hecate.output.tableExports.TableMetricsExporter;
import gr.uoi.cs.daintiness.hecate.output.tableExports.TableMetricsExporterFactory;
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
			tableMetricsExporters = exportFactory.createExporters(path,versions);

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
	 * Takes the metrics.csv as input and produces a detailed report of the Schema Heartbeat in the SchemaHeartbeat.tsv file
	 *  
	 * @return A String with the path of the produced file if metrics.csv exists and no problems exist; null otherwise
	 */
	public String exportSchemaHeartbeatFile() {
		String _DELIMETER = "\t";
		String resultsDirectory = this.getDirectory();
		String metricsFilePlace = resultsDirectory +  File.separator + "metrics.csv";
		String heartbeatFilePlace = resultsDirectory +  File.separator + "SchemaHeartbeat.tsv";
		
		File metricsFile = new File(metricsFilePlace);
		if(!metricsFile.exists()) {
			System.out.println("MEtrics file does not exist; exportSchemaHeartbeatFile() returns null");
			return null;
		}
		
	     // input
	     FileInputStream fis=null;
		try {
			fis = new FileInputStream(metricsFile);
		} catch (FileNotFoundException e) {
			System.out.println("ExportManager.exportSchemaHeartbeatFile(): Metrics file does not open");			
			e.printStackTrace();
		}
	     BufferedReader inputBuffReader = new BufferedReader
	         (new InputStreamReader(fis));

	     // output         
	     File heartbeatFile = new File(heartbeatFilePlace);
	     FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(heartbeatFile);
		} catch (FileNotFoundException e) {
			System.out.println("ExportManager.exportSchemaHeartbeatFile(): heartbeat file cannot not created");
			e.printStackTrace();
		}
	     PrintWriter outPrintWritter = new PrintWriter(fos);

	     String inputHeaderLine = "";
	     String inputFirstLine = "";
	     try {
			inputHeaderLine = inputBuffReader.readLine();//just read it, so that we can move on to the next; we print user-friendly column names instead
			if (inputHeaderLine == null)
				return null;
		    if (inputHeaderLine.trim().compareTo("trID;time;oldVer;newVer;#oldT;#newT;#oldA;#newA;tIns;tDel;aIns;aDel;aTypeAlt;keyAlt;aTabIns;aTabDel") != 0) {
		    	System.err.println("Did not find the correct header at metrics.csv which is:");
		    	System.err.println("trID;time;oldVer;newVer;#oldT;#newT;#oldA;#newA;tIns;tDel;aIns;aDel;aTypeAlt;keyAlt;aTabIns;aTabDel");
		    	System.err.println(inputHeaderLine);
		    	System.err.println("... is what I found");
		    	return null;
		    }
		    		
			//ATTN: the order of columns is NOT the same
			outPrintWritter.println(
					"trID" + _DELIMETER +
					"epochTime" + _DELIMETER +
					"oldVer" + _DELIMETER +
					"newVer" + _DELIMETER +
					"#numOldTables" + _DELIMETER +
					"#numNewTables" + _DELIMETER +
					"#numOldAttrs" + _DELIMETER +
					"#numNewAttrs" + _DELIMETER +
					"tablesIns" + _DELIMETER +
					"tablesDel" + _DELIMETER +
					"attrsInsWithTableIns" + _DELIMETER +
					"attrsbDelWithTableDel" + _DELIMETER +
					"attrsInjected" + _DELIMETER +
					"attrsEjected" + _DELIMETER +
					"attrsWithTypeUpd" + _DELIMETER +
					"attrsInPKUpd" + _DELIMETER 
				);
			
			inputFirstLine = inputBuffReader.readLine();
			String[] lineParts = convertMetricsLineToHeartBeatLine(inputFirstLine.split(";"));

			//introduce a new line, for v0, the birth version of the schema
			String oldVer 			= lineParts[2];
//			String [] epochString 	= oldVer.split("."); //simply fails to produce a new array?
			String epochString = oldVer.substring(0, oldVer.length() - 4);
			Integer numOldTables 	= Integer.parseInt(lineParts[4]);
			Integer numOldAttributes = Integer.parseInt(lineParts[6]);
			outPrintWritter.println(
					"0" + _DELIMETER +
					epochString + _DELIMETER +
					"-" + _DELIMETER +
					oldVer + _DELIMETER +
					"-" + _DELIMETER +
					numOldTables + _DELIMETER +
					"-" + _DELIMETER +
					numOldAttributes + _DELIMETER +
					numOldTables + _DELIMETER +
					"0" + _DELIMETER +
					numOldAttributes + _DELIMETER +
					"0" + _DELIMETER +
					"0" + _DELIMETER +
					"0" + _DELIMETER +
					"0" + _DELIMETER +
					"0" + _DELIMETER 
				);
			//now write the first transition's line
			constructAndExportOutputLine(_DELIMETER, outPrintWritter, inputFirstLine);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	         
	     String nextLine = "";
	     int numLines = 2;
	     try {
			while ((nextLine = inputBuffReader.readLine()) != null) {
				constructAndExportOutputLine(_DELIMETER, outPrintWritter, nextLine);
			   numLines++;
			   }
		} catch (IOException e1) {
			System.out.println("ExportManager.exportSchemaHeartbeatFile(): unable to write in targetFile");
			e1.printStackTrace();
		}
	    outPrintWritter.flush();
	    
	    try {
	    	outPrintWritter.close();
	    	inputBuffReader.close();
		} catch (IOException e) {
			System.out.println("ExportManager.exportSchemaHeartbeatFile(): file closing cannot not done");
			e.printStackTrace();
		}
		
		//System.out.println("################################################ " + heartbeatFilePlace);
		return heartbeatFilePlace;
	}// end exportSchemaHeartbeatFile

	/**
	 * Takes as input a line from the metrics.csv file and outputs into SchemaHeartbeat.tsv an output line
	 * 
	 * @param _DELIMETER
	 * @param outPrintWritter
	 * @param inputLine
	 */
	private void constructAndExportOutputLine(String _DELIMETER, PrintWriter outPrintWritter, String inputLine) {
		String[] lineParts = convertMetricsLineToHeartBeatLine(inputLine.split(";"));
		for (int i=0; i< 16; i++)
		outPrintWritter.print(
				lineParts[i] + _DELIMETER 
				);
		Integer trID 						= Integer.parseInt(lineParts[0]);
		Long epochTime = 0L;
		Boolean hasTimeInfoInEpoch = true;
		try {
			epochTime 						= Long.parseLong(lineParts[1]);
		} catch (NumberFormatException e) {
			epochTime = 0L;
			hasTimeInfoInEpoch = false;
			//e.printStackTrace();
		}
		String oldVer 						= lineParts[2];
		String newVer 						= lineParts[3];
		Integer numOldTables 				= Integer.parseInt(lineParts[4]);
		Integer numNewTables 				= Integer.parseInt(lineParts[5]);
		Integer numOldAttributes 			= Integer.parseInt(lineParts[6]);
		Integer numNewAttributes 			= Integer.parseInt(lineParts[7]);
		Integer tablesAdded		 			= Integer.parseInt(lineParts[8]);
		Integer tablesDeleted				= Integer.parseInt(lineParts[9]);
		Integer attributesInjected			= Integer.parseInt(lineParts[10]);
		Integer attributesEjected			= Integer.parseInt(lineParts[11]);
		Integer attributesWithTypeUpd		= Integer.parseInt(lineParts[12]);
		Integer attributesAtPrimaryKeyUpd 	= Integer.parseInt(lineParts[13]);
		Integer attrBornWTableBirth			= Integer.parseInt(lineParts[14]);
		Integer attrDeletedWTableDeath		= Integer.parseInt(lineParts[15]);

		
		//DATE conversions
		
		outPrintWritter.println();
	}

	private String[] convertMetricsLineToHeartBeatLine(String[] inputMetricsArray) {
		//"trID;time;oldVer;newVer;#oldT;#newT;#oldA;#newA"
		//+ ";tIns;tDel;aIns;aDel;aTypeAlt;keyAlt;aTabIns;aTabDel\n"
		int _NUM_ATTRIBUTES_IN = 16;
		if(inputMetricsArray.length != _NUM_ATTRIBUTES_IN)
			return null;
		
		int _NUM_ATTRIBUTES_OUT = 16;
		String[] outputArray = new String[ _NUM_ATTRIBUTES_OUT ];
		for (int i = 0; i < 10; i++)
			outputArray[i] = inputMetricsArray[i];
		outputArray[10] = inputMetricsArray[14];
		outputArray[11] = inputMetricsArray[15];
		outputArray[12] = inputMetricsArray[10];
		outputArray[13] = inputMetricsArray[11];
		outputArray[14] = inputMetricsArray[12];
		outputArray[15] = inputMetricsArray[13];
		return outputArray;
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
