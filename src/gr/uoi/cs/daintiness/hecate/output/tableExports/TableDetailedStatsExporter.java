package gr.uoi.cs.daintiness.hecate.output.tableExports;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeMap;

import gr.uoi.cs.daintiness.hecate.metrics.tables.MetricsOverVersion;
import gr.uoi.cs.daintiness.hecate.output.TimeConverter;
import gr.uoi.cs.daintiness.hecate.output.heartbeatExports.HeartbeatRecord;

/**
 * Exporter for tables_DetailedStats.tsv
 * 
 * TODO: refactor this and the simpler tables_stats.csv
 * What is the problem?
 * The abstract class TableMetricsExporter is designed to produce files that
 *  for every table (row)
 *    for every version (column)
 *      report on one (1) metric //e.g., sxhemaSize, #deletions, ...
 * This, however, collapses when you want to do summary stats, as we do in the aforementioned two files.
 * Why? Well, because these guys report on stuff, _after_ having processed the _entire_ behavior of a table.
 * 
 * How do we do it now?
 * At ExportManger.exportTableMetrics()
 * (1) we write table's name
 * (2) we pass _all_ versions where we call this.writeChanges() which does two things
 *     2.1 writes date info only once; we chose at table's birth (when the if is true)
 *     2.2 counts the numVersionsAlive s.t. we can compute the other metrics later, for all versions 
 * (3) call the this.writeLastColumn(), only once, after all versions have been iterated, to write the rest of the metrics
 * Of course, this is a hack that works, but we need to refactor it 
 * 
 * How must we do it?
 * process the entire life of the table and compute interim measures;
 * compute stats at the end and report just once without the crazy if(you are at birthV) writeText, else for the other 543 v.'s do nothing
 * But this probably means that must we depart from extending the (A)TableMetricsExporter and implement a new (A) class for the summaries...
 * Have to check.
 * 
 * @author giskou, pvassil
 *
 */
public class TableDetailedStatsExporter extends TableMetricsExporter{
	
	private int sumSize = 0;
	private int versionsAlive = 0;
	private MetricsOverVersion mov;
	private int versions;
	private String deathVersion = "";
	private final String _DELIMITER = "\t";
	private HashMap<Integer, HeartbeatRecord> transitionsHM;
	private TreeMap<Integer, HeartbeatRecord> transitionsSorted; 
	
	public TableDetailedStatsExporter(String path,int versions, HashMap<Integer, HeartbeatRecord> pTransitionsHM) {
		super(path, "tables_DetailedStats.tsv");
		this.versions = versions;
		if(pTransitionsHM != null) {
			this.transitionsHM = pTransitionsHM;
			this.transitionsSorted = new TreeMap<Integer, HeartbeatRecord>(this.transitionsHM);
		}
		else {
			this.transitionsHM = new HashMap<Integer, HeartbeatRecord>();
			this.transitionsSorted = new TreeMap<Integer, HeartbeatRecord>();
		}
	}

	@Override
	public void writeChanges(MetricsOverVersion metricsOverVersion, int i) {
		this.mov = metricsOverVersion;
		sumSize += metricsOverVersion.getSize(i);
		
		//ATTN: table_stats family is abusing the *tables* family strategy pattern
		//instead of writing a single metric for each version, here, several metrics are reported
		//This if() signifies that the birth/death/... stuff is written only once 
		if(i == metricsOverVersion.getBirth()){

			writeText(metricsOverVersion.getLife() + _DELIMITER);
			writeText(metricsOverVersion.getBirth() + _DELIMITER);

			if (metricsOverVersion.getLastKnownVersion()==versions-1 )
					deathVersion = "-" ;
				else 
					deathVersion = Integer.toString(metricsOverVersion.getLastKnownVersion());
			//(metricsOverVersion.getLastKnownVersion()==versions-1 ? "-" : metricsOverVersion.getLastKnownVersion()
			writeText(deathVersion	+ _DELIMITER);
			writeText(metricsOverVersion.getLastKnownVersion() + _DELIMITER);
			
			long schemaBirthEpoch = 0;
			TimeConverter timeconverter = new TimeConverter();
			long birthEpoch = transitionsSorted.get(metricsOverVersion.getBirth()).getEpochTime();
			long lkvEpoch = transitionsSorted.get(metricsOverVersion.getLastKnownVersion()).getEpochTime();
			schemaBirthEpoch = transitionsSorted.get(0).getEpochTime();
			
			if((birthEpoch == 0)||(lkvEpoch == 0)) {
				writeText(""	+ _DELIMITER);
				writeText(""	+ _DELIMITER);
				writeText(""	+ _DELIMITER);
				writeText(""	+ _DELIMITER);
				writeText(""	+ _DELIMITER);
//				writeText(""	+ _DELIMITER);
//				writeText(""	+ _DELIMITER);				
			}
			else {
				String birthStringHuman = timeconverter.convertEpochToHumanString(birthEpoch);
				String lkvStringHuman = timeconverter.convertEpochToHumanString(lkvEpoch);

				int yearBOffset = 0;
				if (birthEpoch != schemaBirthEpoch) {
					yearBOffset = 1;
				}
				int yearLOffset = 0;
				if (lkvEpoch != schemaBirthEpoch) {
					yearLOffset = 1;
				}
				long yearOfBirth = timeconverter.distInYearsCompleted(schemaBirthEpoch, birthEpoch) + yearBOffset;
				long yearOfLKV = timeconverter.distInYearsCompleted(schemaBirthEpoch, lkvEpoch) + yearLOffset;

				long durationInDays = timeconverter.distInDaysCompleted(birthEpoch, lkvEpoch);
				long durationInMonths = timeconverter.distInMonthsCompleted(birthEpoch, lkvEpoch);
				long durationInYears = timeconverter.distInYearsCompleted(birthEpoch, lkvEpoch);

				writeText(birthStringHuman	+ _DELIMITER);
				writeText(lkvStringHuman	+ _DELIMITER);
				writeText(yearOfBirth		+ _DELIMITER);
				writeText(yearOfLKV			+ _DELIMITER);
				writeText(durationInDays	+ _DELIMITER);
//				writeText(durationInMonths	+ _DELIMITER);
//				writeText(durationInYears	+ _DELIMITER);
			}
		}
		versionsAlive++;		//you NEED this. @ExportManger.exportTableMetrics(), you go through all versions, and you count how many they are
	}


	@Override
	public void writeLastColumn() {
		
		NumberFormat nf = DecimalFormat.getInstance(Locale.ENGLISH);
        DecimalFormat decimalFormat = (DecimalFormat) nf;
        decimalFormat.applyPattern("#.##");
        
		//DecimalFormat decimalFormat = new DecimalFormat("#.00");
        

		
		//schema size info:birth, LKV, avg, scaleUp
		super.writeText(mov.getBirthSize() + _DELIMITER);
		super.writeText(mov.getLastKnownVersionSize() + _DELIMITER);
		String avgSchemaSizeString = decimalFormat.format(sumSize/(float)versionsAlive);
		super.writeText(avgSchemaSizeString + _DELIMITER);
		String scaleUpString = decimalFormat.format(mov.getLastKnownVersionSize()/(float)mov.getBirthSize());
		super.writeText(scaleUpString + _DELIMITER);
		
		//change quantification: sum(updates), count(Versions with Updates), ATU, updRate, AvgUpdVolume
		int sumUpd = mov.getTotalChanges().getTotal();
		int countVwUpd = mov.getNumberOfVersionsWithChanges();
		float ATU = sumUpd / (float)mov.getLife();
		float updRate = countVwUpd / (float)mov.getLife();
		String avgUpdVolString = "";
		if (countVwUpd > 0.0) 
			avgUpdVolString = decimalFormat.format((sumUpd/(float)countVwUpd));
		
		super.writeText(sumUpd + _DELIMITER);
		super.writeText(countVwUpd + _DELIMITER);
		String ATUString = decimalFormat.format(ATU);
		super.writeText(ATUString + _DELIMITER);
		String updRateString = decimalFormat.format(updRate);
		super.writeText(updRateString + _DELIMITER); //Float.toString(updRate)
		super.writeText(avgUpdVolString + _DELIMITER);
		
		//now the characterizations: SurvivalClass, ActivityClass, LADClass
		int survClass = 0; 
		if(this.deathVersion.equals("-"))
			survClass = 20;
		else
			survClass = 10;

		int actClass = 0; 
		if((sumUpd > 5)&&(ATU > 0.1))
			actClass = 2;
		else if (sumUpd > 0)
			actClass = 1;
		else
			actClass = 0;
		
		int LAD = survClass + actClass;
		
		super.writeText(survClass + _DELIMITER);
		super.writeText(actClass + _DELIMITER);
		super.writeText(Integer.toString(LAD)); //no + _DELIMITER, last before \n
		
		
		//end of this table's line
		super.writeText("\n");
		
		//Clean them up for the next table
		sumSize= 0;
		versionsAlive = 0; deathVersion = "";
		mov = null; //versions = 0;
		
		sumUpd = 0; countVwUpd = 0; ATU = 0; updRate = 0; avgUpdVolString = "";
		survClass = 0; actClass = 0; LAD = 0;
		
	}

	@Override
	public void writeEmptyCells() {	}

	@Override
	public void writeHeader(int versions) {
		writeText("Table" + _DELIMITER + "Duration" + _DELIMITER + "Birth" + _DELIMITER + "Death" + _DELIMITER + "LastKnownVersion" + _DELIMITER  
				+ "BirthDate" + _DELIMITER + "LKVDate" + _DELIMITER + "YearOfBirth" + _DELIMITER + "YearOfLKV" + _DELIMITER 
				+ "DurationDays" + _DELIMITER //+ "DurationMonths" + _DELIMITER + "DurationYears" + _DELIMITER 
				+ "SchemaSize@Birth" + _DELIMITER + "SchemaSize@LKV" + _DELIMITER + "SchemaSizeAvg" + _DELIMITER + "SchemaSizeResizeRatio" + _DELIMITER 
				+ "SumUpd" + _DELIMITER + "CountVwUpd" + _DELIMITER + "ATU" + _DELIMITER + "UpdRate" + _DELIMITER +	"AvgUpdVolume" + _DELIMITER	  
				+ "SurvivalClass" + _DELIMITER + "ActivityClass" + _DELIMITER + "LADClass"  
				+ "\n");
	}

	@Override
	public String getDelimiter() {
		return _DELIMITER;
	}
}
