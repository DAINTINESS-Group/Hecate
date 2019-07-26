package gr.uoi.cs.daintiness.hecate.output.heartbeatExports;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import gr.uoi.cs.daintiness.hecate.output.TimeConverter;

public class HeartbeatRecord {

	public HeartbeatRecord() {
		;
	}
	public HeartbeatRecord(String [] lineParts, long epochTimeForSchemaBirth, String aDelimeter) {
		
		this._DELIMETER = aDelimeter;
		
		this.trID 						= Integer.parseInt(lineParts[0]);
		this.epochTime = 0;
		this.hasTimeInfoInEpoch = true;
		this.epochString = lineParts[1];
		try {
			this.epochTime 	= Long.parseLong(lineParts[1]);
		} catch (NumberFormatException e) {
			this.epochTime = 0L;
			this.hasTimeInfoInEpoch = false;
			//e.printStackTrace();
		}
		this.oldVer 					= lineParts[2];
		this.newVer 					= lineParts[3];
		this.numOldTables 				= Integer.parseInt(lineParts[4]);
		this.numNewTables 				= Integer.parseInt(lineParts[5]);
		this.numOldAttributes 			= Integer.parseInt(lineParts[6]);
		this.numNewAttributes 			= Integer.parseInt(lineParts[7]);
		this.tablesAdded		 		= Integer.parseInt(lineParts[8]);
		this.tablesDeleted				= Integer.parseInt(lineParts[9]);
		this.attrBornWTableBirth		= Integer.parseInt(lineParts[10]);
		this.attrDeletedWTableDeath		= Integer.parseInt(lineParts[11]);
		this.attributesInjected			= Integer.parseInt(lineParts[12]);
		this.attributesEjected			= Integer.parseInt(lineParts[13]);
		this.attributesWithTypeUpd		= Integer.parseInt(lineParts[14]);
		this.attributesAtPrimaryKeyUpd 	= Integer.parseInt(lineParts[15]);

		//at output, can also put it next to TBL added and deleted.
		//They are not the same thing, this is just the growth delta, not how it happened
		this.tableDelta = numNewTables - numOldTables;
		this.attrDelta = numNewAttributes - numOldAttributes;

		//GROWTH, equiv., EXPANSION
		this.attrBirths = attributesInjected + attrBornWTableBirth;
		this.attrExpansion = attrBirths;
		//MAINTENANCE
		this.attrDeaths = attributesEjected + attrDeletedWTableDeath;
		this.attrUpds = attributesWithTypeUpd + attributesAtPrimaryKeyUpd;
		this.attrMaintenance = attrDeaths + attrUpds;
		this.totalAttrActivity = attrBirths + attrMaintenance;

		//DATE conversions
		if(hasTimeInfoInEpoch) {
			TimeConverter timeConverter = new TimeConverter();
			this.humanTime = timeConverter.convertEpochToHumanString(epochTime);
			if (epochTimeForSchemaBirth > 0) {
				Duration distFromV0Duration = timeConverter.computeDurationBetweenEpochTimes(epochTimeForSchemaBirth, epochTime);
				this.distFromV0Days = (int)distFromV0Duration.toDays();
				
				LocalDateTime ldtCurrentV = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochTime), ZoneId.of("UTC"));
				LocalDateTime ldtSchBirth = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochTimeForSchemaBirth), ZoneId.of("UTC"));
			   // long diffInDays = ChronoUnit.DAYS.between(ldtSchBirth, ldtCurrentV);
			    this.runnningMonthFromV0 = (int)ChronoUnit.MONTHS.between(ldtSchBirth, ldtCurrentV) + 1; //if you are in month 14 from V0, you are inside the 2nd year
			    this.runnningYearFromV0 = (int)ChronoUnit.YEARS.between(ldtSchBirth, ldtCurrentV) + 1;

				//this.runnnigYearFromV0 = (int)(distFromV0Days/365.2425) + 1;  //if you are in month 14 from V0, you are inside the 2nd year
				//this.runnnigMonthFromV0 = (int)(distFromV0Days/365.2425/12) + 1;
			}//we know time of schema birth
		}//endif has timeInfo

	}//end constructor

	public String toString() {
		//String epochString = oldVer.substring(0, oldVer.length() - 4);
		String outputIdString = this.trID + _DELIMETER  + this.epochString + _DELIMETER  + this.oldVer + _DELIMETER + this.newVer  + _DELIMETER;
		String outputDateString = "" + _DELIMETER + "" + _DELIMETER + "" + _DELIMETER + "" + _DELIMETER;
		if(hasTimeInfoInEpoch) {
			outputDateString = 	humanTime + _DELIMETER  +
					distFromV0Days + _DELIMETER +				
					runnningYearFromV0 + _DELIMETER +
					runnningMonthFromV0 + _DELIMETER;
		}
		
		String outputMetrics = numOldTables + _DELIMETER + 
				numNewTables + _DELIMETER + 
				numOldAttributes + _DELIMETER + 
				numNewAttributes + _DELIMETER + 
				tablesAdded + _DELIMETER + 
				tablesDeleted + _DELIMETER + 
				attrBornWTableBirth + _DELIMETER + 
				attrDeletedWTableDeath + _DELIMETER + 
				attributesInjected + _DELIMETER + 
				attributesEjected + _DELIMETER + 
				attributesWithTypeUpd + _DELIMETER + 
				attributesAtPrimaryKeyUpd + _DELIMETER; 

		String outputDeltaString = tableDelta + _DELIMETER + attrDelta + _DELIMETER;
		String outputSummaryString = attrBirths + _DELIMETER + attrDeaths + _DELIMETER + attrUpds 
				+ _DELIMETER + attrExpansion + _DELIMETER + attrMaintenance + _DELIMETER + totalAttrActivity + _DELIMETER;
				
		return outputIdString + outputDateString + outputMetrics + outputDeltaString + outputSummaryString;
	}
	
	public static String getHeaderForHeartBeat(String _DELIMETER) {
		final String outputHeaderLine = 	"trID" + _DELIMETER +
				"epochTime" + _DELIMETER +
				"oldVer" + _DELIMETER +
				"newVer" + _DELIMETER +
				"humanTime" + _DELIMETER +
				"distFromV0InDays" + _DELIMETER +		//ADD RUNNING MONTH FROM V0
				"runningYearFromV0" + _DELIMETER +
				"runningMonthFromV0" + _DELIMETER +			
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
				"attrsInPKUpd" + _DELIMETER +
				"tableDelta" + _DELIMETER +
				"attrDelta" + _DELIMETER +
				"attrBirthsSum" + _DELIMETER +
				"attrDeathsSum" + _DELIMETER +
				"attrUpdsSum" + _DELIMETER +
				"Expansion" + _DELIMETER +
				"Maintenance" + _DELIMETER +
				"TotalAttrActivity" + _DELIMETER ;
		
		return outputHeaderLine;
	}
	
	protected Integer trID    = null;
	protected long epochTime  = 0;
	protected Boolean hasTimeInfoInEpoch = true;
	protected String oldVer       	= "";
	protected String newVer       	= "";
	protected Integer numOldTables     	= 0;
	protected Integer numNewTables     	= 0;
	protected Integer numOldAttributes  = 0;
	protected Integer numNewAttributes  = 0;
	protected Integer tablesAdded      	= 0;
	protected Integer tablesDeleted    	= 0;
	protected Integer attrBornWTableBirth   = 0;
	protected Integer attrDeletedWTableDeath= 0;
	protected Integer attributesInjected   	= 0;
	protected Integer attributesEjected   	= 0;
	protected Integer attributesWithTypeUpd  	= 0;
	protected Integer attributesAtPrimaryKeyUpd	= 0;
	protected Integer tableDelta 	= 0;
	protected Integer attrDelta 	= 0;
	protected Integer attrBirths 	= 0;		
	protected Integer attrDeaths 	= 0;
	protected Integer attrUpds 		= 0;
	protected Integer attrExpansion 	= 0;
	protected Integer attrMaintenance 	= 0;
	protected Integer totalAttrActivity = 0;
	protected String humanTime 		= "";
	protected Integer distFromV0Days 	= null;
	protected Integer runnningMonthFromV0 	= null;
	protected Integer runnningYearFromV0	= null;

	protected String _DELIMETER = "\t";
	protected String epochString;
}
