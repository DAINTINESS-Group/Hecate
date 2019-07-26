package gr.uoi.cs.daintiness.hecate.output.heartbeatExports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.Duration;

import gr.uoi.cs.daintiness.hecate.output.TimeConverter;

public class HeartbeatExporter {

	private String directory;


	public HeartbeatExporter (String aDirectory) {
		this.directory = aDirectory;
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
			
			HeartbeatRecord schemaFirstTransition = new HeartbeatRecord(lineParts, epochTimeForSchemaBirth, _DELIMETER);
			outPrintWritter.println(schemaFirstTransition.toString());
			
//			String oldVer 			= lineParts[2];
//			//			String [] epochString 	= oldVer.split("."); //simply fails to produce a new array?
//			String epochString = oldVer.substring(0, oldVer.length() - 4);
//
//			Boolean hasTimeInfoInEpoch = true;
//			try {
//				epochTimeForSchemaBirth 						= Long.parseLong(epochString);
//			} catch (NumberFormatException e) {
//				epochTimeForSchemaBirth = 0L;
//				hasTimeInfoInEpoch = false;
//				//e.printStackTrace();
//			}
//
//			if(hasTimeInfoInEpoch) {
//				TimeConverter timeConverter = new TimeConverter();
//
//				humanTimeOfSchemaBirth = timeConverter.convertEpochToHumanString(epochTimeForSchemaBirth);
//			}
//			Integer numOldTables 	= Integer.parseInt(lineParts[4]);
//			Integer numOldAttributes = Integer.parseInt(lineParts[6]);
//			outPrintWritter.println(
//					"0" + _DELIMETER +
//					epochString + _DELIMETER +
//					"-" + _DELIMETER +
//					oldVer + _DELIMETER +
//					"-" + _DELIMETER +
//					numOldTables + _DELIMETER +
//					"-" + _DELIMETER +
//					numOldAttributes + _DELIMETER +
//					numOldTables + _DELIMETER +
//					"0" + _DELIMETER +
//					numOldAttributes + _DELIMETER +
//					"0" + _DELIMETER +
//					"0" + _DELIMETER +
//					"0" + _DELIMETER +
//					"0" + _DELIMETER +
//					"0" + _DELIMETER +	//last from metrics, PKUPD
//					humanTimeOfSchemaBirth + _DELIMETER +	//start DateInfo, HumanTime
//					"0" + _DELIMETER +
//					"0" + _DELIMETER + //lastDate
//					numOldTables + _DELIMETER +		//startdelta
//					numOldAttributes + _DELIMETER +//end delta
//					numOldAttributes + _DELIMETER +		//startactivity
//					"0" + _DELIMETER +
//					"0" + _DELIMETER +
//					numOldAttributes + _DELIMETER +		//expansion
//					"0" + _DELIMETER +//mntnc
//					numOldAttributes + _DELIMETER //total
//					);
//			//			String outputSummaryString = attrBirths + _DELIMETER + attrDeaths + _DELIMETER + attrUpds 
//			//			+ _DELIMETER + attrBirths + _DELIMETER + attrMaintenance + _DELIMETER + totalAttrActivity + _DELIMETER;
//			//now write the first transition's line
//			constructAndExportOutputLine(_DELIMETER, outPrintWritter, inputFirstLine, epochTimeForSchemaBirth);
		} catch (IOException e1) {
			System.out.println("ExportManager.exportSchemaHeartbeatFile(): extra line for v0 at heartbeat file cannot not created");
			e1.printStackTrace();
		}

		// ******************** FOR EACH NEXT LINE, GENERATE THE RESPECTIVE .TSV ONE ************************
		String nextLine = "";
		try {
			while ((nextLine = inputBuffReader.readLine()) != null) {
				String[] lineParts = convertMetricsLineToHeartBeatLine(nextLine.split(";"));
				HeartbeatRecord nextRecord = new HeartbeatRecord(lineParts, epochTimeForSchemaBirth, _DELIMETER);
				outPrintWritter.println(nextRecord.toString());
				//constructAndExportOutputLine(_DELIMETER, outPrintWritter, nextLine, epochTimeForSchemaBirth);
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
//				"trID" + _DELIMETER +
//				"epochTime" + _DELIMETER +
//				"oldVer" + _DELIMETER +
//				"newVer" + _DELIMETER +
//				"#numOldTables" + _DELIMETER +
//				"#numNewTables" + _DELIMETER +
//				"#numOldAttrs" + _DELIMETER +
//				"#numNewAttrs" + _DELIMETER +
//				"tablesIns" + _DELIMETER +
//				"tablesDel" + _DELIMETER +
//				"attrsInsWithTableIns" + _DELIMETER +
//				"attrsbDelWithTableDel" + _DELIMETER +
//				"attrsInjected" + _DELIMETER +
//				"attrsEjected" + _DELIMETER +
//				"attrsWithTypeUpd" + _DELIMETER +
//				"attrsInPKUpd" + _DELIMETER +
//				"humanTime" + _DELIMETER +
//				"distFromV0InDays" + _DELIMETER +		//ADD RUNNING MONTH FROM V0
//				"runningYearFromV0" + _DELIMETER +
//				"tableDelta" + _DELIMETER +
//				"attrDelta" + _DELIMETER +
//				"attrBirthsSum" + _DELIMETER +
//				"attrDeathsSum" + _DELIMETER +
//				"attrUpdsSum" + _DELIMETER +
//				"Expansion" + _DELIMETER +
//				"Maintenance" + _DELIMETER +
//				"TotalAttrActivity" + _DELIMETER ;
		
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
			System.out.println("ExportManager.exportSchemaHeartbeatFile(): header line for heartbeat file cannot not created");
			e1.printStackTrace();
		}
		return outputHeaderLine;
	}

	/**
	 * Takes as input a line from the metrics.csv file and outputs into SchemaHeartbeat.tsv an output line
	 * 
	 * @param _DELIMETER	A string to delimit the columns of the tsv. Expected to be "\t"
	 * @param outPrintWritter	A PrintWritter that has previously been opened to receive the contents that are printed 
	 * @param inputLine	A String containing yet another line coming from metrics.csv
	 * @param epochTimeForSchemaBirth A long value that represents the unix time of the birth of the first schema version. 
	 */
	private void constructAndExportOutputLine(String _DELIMETER, PrintWriter outPrintWritter, String inputLine, long epochTimeForSchemaBirth) {
		String[] lineParts = convertMetricsLineToHeartBeatLine(inputLine.split(";"));

		Integer trID 						= Integer.parseInt(lineParts[0]);
		long epochTime = 0;
		Boolean hasTimeInfoInEpoch = true;
		try {
			epochTime 						= Long.parseLong(lineParts[1]);
		} catch (NumberFormatException e) {
			epochTime = 0L;
			hasTimeInfoInEpoch = false;
			//e.printStackTrace();
		}
		//		String oldVer 						= lineParts[2];
		//		String newVer 						= lineParts[3];
		Integer numOldTables 				= Integer.parseInt(lineParts[4]);
		Integer numNewTables 				= Integer.parseInt(lineParts[5]);
		Integer numOldAttributes 			= Integer.parseInt(lineParts[6]);
		Integer numNewAttributes 			= Integer.parseInt(lineParts[7]);
		//		Integer tablesAdded		 			= Integer.parseInt(lineParts[8]);
		//		Integer tablesDeleted				= Integer.parseInt(lineParts[9]);
		Integer attrBornWTableBirth			= Integer.parseInt(lineParts[10]);
		Integer attrDeletedWTableDeath		= Integer.parseInt(lineParts[11]);
		Integer attributesInjected			= Integer.parseInt(lineParts[12]);
		Integer attributesEjected			= Integer.parseInt(lineParts[13]);
		Integer attributesWithTypeUpd		= Integer.parseInt(lineParts[14]);
		Integer attributesAtPrimaryKeyUpd 	= Integer.parseInt(lineParts[15]);


		//at output, can also put it next to TBL added and deleted.
		//They are not the same thing, this is just the growth delta, not how it happened
		Integer tableDelta = numNewTables - numOldTables;
		Integer attrDelta = numNewAttributes - numOldAttributes;

		String outputDeltaString = tableDelta + _DELIMETER + attrDelta + _DELIMETER;


		//GROWTH, equiv., EXPANSION
		Integer attrBirths = attributesInjected + attrBornWTableBirth;
		//MAINTENANCE
		Integer attrDeaths = attributesEjected + attrDeletedWTableDeath;
		Integer attrUpds = attributesWithTypeUpd + attributesAtPrimaryKeyUpd;
		Integer attrMaintenance = attrDeaths + attrUpds;
		Integer totalAttrActivity = attrBirths + attrMaintenance;

		String outputSummaryString = attrBirths + _DELIMETER + attrDeaths + _DELIMETER + attrUpds 
				+ _DELIMETER + attrBirths + _DELIMETER + attrMaintenance + _DELIMETER + totalAttrActivity + _DELIMETER;


		//DATE conversions
		String outputDateString = "" + _DELIMETER + "" + _DELIMETER + "" + _DELIMETER;
		if(hasTimeInfoInEpoch) {
			TimeConverter timeConverter = new TimeConverter();

			String humanTime = timeConverter.convertEpochToHumanString(epochTime);
			Duration distFromV0Duration = timeConverter.computeDurationBetweenEpochTimes(epochTimeForSchemaBirth, epochTime);
			int distFromV0Days = (int)distFromV0Duration.toDays();
			int runnnigYearFromV0 = (int)(distFromV0Days/365.0) + 1;  //if you are in month 14 from V0, you are inside the 2nd year

			outputDateString = 	humanTime + _DELIMETER  +
					distFromV0Days + _DELIMETER +
					runnnigYearFromV0 + _DELIMETER;
		}//endif has timeInfo

		
		//*** There is a time to live and a time to println ***
		for (int i=0; i< 16; i++)
			outPrintWritter.print(
					lineParts[i] + _DELIMETER 
					);

		outPrintWritter.print(outputDateString);
		outPrintWritter.print(outputDeltaString);
		outPrintWritter.print(outputSummaryString);

		//Write last newline
		outPrintWritter.println();
	}// end constructAndExportOutputLine
	
	

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
