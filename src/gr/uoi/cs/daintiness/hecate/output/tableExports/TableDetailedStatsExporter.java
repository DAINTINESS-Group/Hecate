package gr.uoi.cs.daintiness.hecate.output.tableExports;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import gr.uoi.cs.daintiness.hecate.metrics.tables.MetricsOverVersion;

public class TableDetailedStatsExporter extends TableMetricsExporter{
	
	private int sumSize = 0;
	private int versionsAlive = 0;
	private MetricsOverVersion mov;
	private int versions;
	private String deathVersion = "";
	private final String _DELIMITER = "\t";
	
	public TableDetailedStatsExporter(String path,int versions) {
		super(path, "tables_DetailedStats.tsv");
		this.versions = versions;
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
		}
		versionsAlive++;
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
				+ "SchemaSize@Birth" + _DELIMITER + "SchemaSize@LKV" + _DELIMITER + "SchemaSizeAvg" + _DELIMITER + "SchemaSizeResizeRatio" + _DELIMITER 
				+ "SumUpd" + _DELIMITER + "CountVwUpd" + _DELIMITER + "ATU" + _DELIMITER + "UpdRate" + _DELIMITER +	"AvgUpdVolume" + _DELIMITER	  
				+ "SurvivalClass" + _DELIMITER + "ActivityClass" + _DELIMITER + "LADClass"  
				+ "\n");
	}
//	YoB	YoD	birthDate	lastAppearance	durationInDays	durationInYears	durInYearRange

	@Override
	public String getDelimiter() {
		return _DELIMITER;
	}
}
