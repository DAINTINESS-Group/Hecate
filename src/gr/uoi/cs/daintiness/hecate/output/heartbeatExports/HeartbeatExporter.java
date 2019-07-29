package gr.uoi.cs.daintiness.hecate.output.heartbeatExports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;


public class HeartbeatExporter {

	private String directory;
	private HashMap<Integer, HeartbeatRecord> heartbeatRecords;

	/**
	 * Constructor for the heaertbeat exporter, of the SchemaHeartbeat.tsv file
	 * 
	 * @param aDirectory  a string with the 'results' folder, for the results of the history analysis, 
	 * @param hashmapToFill a HashMap of HeartbeatRecords, which must have been created at the ExportManager (depend. injection style) to be filled with objects pertaining to the transitions of the schema history, key-ed via their trId
	 */
	public HeartbeatExporter (String aDirectory, HashMap<Integer, HeartbeatRecord> hashmapToFill) {
		this.directory = aDirectory;
		if (hashmapToFill == null)
			this.heartbeatRecords = new HashMap<Integer, HeartbeatRecord>();
		else 
			this.heartbeatRecords = hashmapToFill;
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
		long epochTimeForSchemaBirth = 0;

		File metricsFile = new File(metricsFilePlace);
		if(!metricsFile.exists()) {
			System.err.println("MEtrics file does not exist; exportSchemaHeartbeatFile() returns null");
			return null;
		}

		// input
		FileInputStream fis=null;
		try {
			fis = new FileInputStream(metricsFile);
		} catch (FileNotFoundException e) {
			System.err.println("ExportManager.exportSchemaHeartbeatFile(): Metrics file does not open");			
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
			System.err.println("ExportManager.exportSchemaHeartbeatFile(): heartbeat file cannot not created");
			e.printStackTrace();
		}
		PrintWriter outPrintWritter = new PrintWriter(fos);

		
		// *** HEADER LINE ****
		String headerReturn = null;
		headerReturn = readAndWriteHeaderLine(_DELIMETER, inputBuffReader, outPrintWritter);
		if (headerReturn == null) {		//the input file did not have the appropriate header
			return null;
		}
		
		//********* introduce a new line, for v0, the BIRTH VERSION OF THE SCHEMA ****************//
		String inputFirstLine;
		try {
			inputFirstLine = inputBuffReader.readLine();
			String[] lineParts = convertMetricsLineToHeartBeatLine(inputFirstLine.split(";"));

			HeartbeatRecordForSchemaBirth schemaBirthObject = new HeartbeatRecordForSchemaBirth(lineParts, -1, _DELIMETER); 
			String humanTimeOfSchemaBirth = schemaBirthObject.getSchemaBirthString(); 
			epochTimeForSchemaBirth =  schemaBirthObject.getSchemaBirthEpoch();
			outPrintWritter.println(schemaBirthObject.toString());
			this.heartbeatRecords.put(0, schemaBirthObject);
			
			HeartbeatRecord schemaFirstTransition = new HeartbeatRecord(lineParts, epochTimeForSchemaBirth, _DELIMETER);
			outPrintWritter.println(schemaFirstTransition.toString());
			this.heartbeatRecords.put(1, schemaFirstTransition);
			
		} catch (IOException e1) {
			System.err.println("ExportManager.exportSchemaHeartbeatFile(): extra line for v0 at heartbeat file cannot not created");
			e1.printStackTrace();
		}

		// ******************** FOR EACH NEXT LINE, GENERATE THE RESPECTIVE .TSV ONE ************************
		String nextLine = "";
		try {
			while ((nextLine = inputBuffReader.readLine()) != null) {
				String[] lineParts = convertMetricsLineToHeartBeatLine(nextLine.split(";"));
				HeartbeatRecord nextRecord = new HeartbeatRecord(lineParts, epochTimeForSchemaBirth, _DELIMETER);
				outPrintWritter.println(nextRecord.toString());
				this.heartbeatRecords.put(nextRecord.getTrID(), nextRecord);

				//constructAndExportOutputLine(_DELIMETER, outPrintWritter, nextLine, epochTimeForSchemaBirth);
			}
		} catch (IOException e1) {
			System.err.println("ExportManager.exportSchemaHeartbeatFile(): unable to write in targetFile");
			e1.printStackTrace();
		}
		outPrintWritter.flush();

		try {
			outPrintWritter.close();
			inputBuffReader.close();
		} catch (IOException e) {
			System.err.println("ExportManager.exportSchemaHeartbeatFile(): file closing cannot not done");
			e.printStackTrace();
		}

		//System.out.println("########## Exported " + heartbeatRecords.size() + " records for " + heartbeatFilePlace);
		return heartbeatFilePlace;
	}// end exportSchemaHeartbeatFile

	/**
	 * Reads the first header line from input metrics.csv and produces the header line of the output .tsv file
	 * 
	 * @param _DELIMETER
	 * @param inputBuffReader
	 * @param outPrintWritter
	 * @return a String with the header to be outputed
	 */
	private String readAndWriteHeaderLine(String _DELIMETER, BufferedReader inputBuffReader,
			PrintWriter outPrintWritter) {
		String inputHeaderLine = "";
		String outputHeaderLine = 	HeartbeatRecord.getHeaderForHeartBeat(_DELIMETER);
		
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
			outPrintWritter.println(outputHeaderLine);

		} catch (IOException e1) {
			System.err.println("ExportManager.exportSchemaHeartbeatFile(): header line for heartbeat file cannot not created");
			e1.printStackTrace();
		}
		return outputHeaderLine;
	}

	/**
	 * Reshuffles the input from metrics.csv s.t. the different columns have the correct order before being exported.
	 * 
	 * @param inputMetricsArray An array of strings with the different values per row of metrics.csv, via string.split()
	 * @return a correctly reshuffled array of strings for the line of ShcemaBiographies.tsv
	 */
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


	private String getDirectory() {
		return directory;
	}


}//end HeartbeatExporter
