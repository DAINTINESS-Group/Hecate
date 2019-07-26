package gr.uoi.cs.daintiness.hecate.output.heartbeatExports;

import gr.uoi.cs.daintiness.hecate.output.TimeConverter;

public class HeartbeatRecordForSchemaBirth extends HeartbeatRecord {

	public HeartbeatRecordForSchemaBirth(String [] lineParts, long epochTimeForSchemaBirth, String aDelimeter) {
		
		
		this._DELIMETER = aDelimeter;
		
		this.trID = 0;
		this.epochTime = 0;
		this.hasTimeInfoInEpoch = true;
		this.epochString = lineParts[2].substring(0, lineParts[2].length() - 4);
		try {
			this.epochTime 	= Long.parseLong(epochString);
		} catch (NumberFormatException e) {
			this.epochTime = 0L;
			this.hasTimeInfoInEpoch = false;
			//e.printStackTrace();
		}
		this.oldVer 					= null;
		this.newVer 					= lineParts[2];

		if(hasTimeInfoInEpoch) {
			TimeConverter timeConverter = new TimeConverter();
			this.humanTime = timeConverter.convertEpochToHumanString(epochTime);
			this.distFromV0Days = 0;				//ADD MONTH
			this.runnningYearFromV0 = 0;
			this.runnningMonthFromV0 = 0;
		}//endif has timeInfo
		
				
		this.numOldTables 				= null;
		this.numNewTables 				= Integer.parseInt(lineParts[4]);
		this.numOldAttributes 			= null;
		this.numNewAttributes 			= Integer.parseInt(lineParts[6]);
		this.tablesAdded		 		= this.numNewTables;
		this.tablesDeleted				= 0;
		this.attrBornWTableBirth		= this.numNewAttributes;
		this.attrDeletedWTableDeath		= 0;
		this.attributesInjected			= 0;
		this.attributesEjected			= 0;
		this.attributesWithTypeUpd		= 0;
		this.attributesAtPrimaryKeyUpd 	= 0;
	
		//at output, can also put it next to TBL added and deleted.
		//They are not the same thing, this is just the growth delta, not how it happened
		this.tableDelta = 0;
		this.attrDelta = 0;

		//GROWTH, equiv., EXPANSION
		this.attrBirths = attributesInjected + attrBornWTableBirth;
		this.attrExpansion = attrBirths;
		//MAINTENANCE
		this.attrDeaths = attributesEjected + attrDeletedWTableDeath;
		this.attrUpds = attributesWithTypeUpd + attributesAtPrimaryKeyUpd;
		this.attrMaintenance = attrDeaths + attrUpds;
		this.totalAttrActivity = attrBirths + attrMaintenance;


	}
	/**
	 * Returns the epoch time for this v., which is also the birth version of the schema
	 * 
	 * @return the epoch time if there is an epoch, 0 otherwise
	 */
	public long getSchemaBirthEpoch() {
		return this.epochTime;
	}
	
	
	/**
	 * Returns the human readable string for the birth for this v., which is also the birth version of the schema
	 * 
	 * @return a human readable string for the birth for this v. if there is time info, null otherwise
	 */
	public String getSchemaBirthString() {
		return this.humanTime;
	}
	
	
	public String toString() {
		String outputIdString = this.trID + _DELIMETER  + this.epochString + _DELIMETER  + "" + _DELIMETER + this.newVer  + _DELIMETER;
		String outputDateString = "" + _DELIMETER + "" + _DELIMETER + "" + _DELIMETER + "" + _DELIMETER;
		if(hasTimeInfoInEpoch) {
			outputDateString = 	humanTime + _DELIMETER  +
					distFromV0Days + _DELIMETER +				
					runnningYearFromV0 + _DELIMETER +
					runnningMonthFromV0 + _DELIMETER;
		}
		
		String outputMetrics = "" + _DELIMETER + 
				numNewTables + _DELIMETER + 
				"" + _DELIMETER + 
				numNewAttributes + _DELIMETER + 
				tablesAdded + _DELIMETER + 
				tablesDeleted + _DELIMETER + 
				attrBornWTableBirth + _DELIMETER + 
				attrDeletedWTableDeath + _DELIMETER + 
				attributesInjected + _DELIMETER + 
				attributesEjected + _DELIMETER + 
				attributesWithTypeUpd + _DELIMETER + 
				attributesAtPrimaryKeyUpd + _DELIMETER; 

		String outputDeltaString = "" + _DELIMETER + "" + _DELIMETER;
		String outputSummaryString = attrBirths + _DELIMETER + attrDeaths + _DELIMETER + attrUpds 
				+ _DELIMETER + attrExpansion + _DELIMETER + attrMaintenance + _DELIMETER + totalAttrActivity + _DELIMETER;
				
		return outputIdString + outputDateString + outputMetrics + outputDeltaString + outputSummaryString;
	}

	
}//end class
