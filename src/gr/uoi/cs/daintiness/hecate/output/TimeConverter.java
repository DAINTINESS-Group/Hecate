package gr.uoi.cs.daintiness.hecate.output;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeConverter {

	/**
	 * Returns a local date time (not local date!) for unix time
	 * 
	 * @param longValue the long value with the epoch unix time
	 * @return the LocalDateTime that corresponds to the Unix epoch time given
	 */
	public LocalDateTime convertUnixTimeToLocalDateTime(long unixTime) {
		//ofEpochMilli
		return LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTime), ZoneId.of("UTC"));
	}


	/**
	 * Converts unix time to java date and a human readable representation
	 * https://stackoverflow.com/questions/17432735/convert-unix-time-stamp-to-date-in-java
	 * 
	 * @param unixTime a long value with the unix timestamp
	 * @return a string in human readable format for the input string
	 */
	public String convertEpochToHumanString(long unixTime) {
		final DateTimeFormatter formatter = 
			    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String _TIMEZONE = "UTC";
		final String formattedDtm = Instant.ofEpochSecond(unixTime)
			        .atZone(ZoneId.of(_TIMEZONE))
			        .format(formatter);
		return (formattedDtm); 
	}
	
	
	/**
	 * Returns the difference of two epoch Unix timestamps as a Duration
	 * which can allow calling toHours(), to Days(), toNano(), ...
	 * These calls round() the distance. E.g., if in Excel you see a distance of 3.67 days, toDays() returns 4.
	 * For example:
	 * 	Duration duration = computeDurationBetweenEpochTimes(unixSeconds1, unixSeconds2);
	 *	long diffMinutes = duration.toMinutes();
	 *	long diffHours = duration.toHours();
	 *	long diffDays = duration.toDays();
	 * 
	 * @param unixEpochTime1 a long for the first epoch time to be compared
	 * @param unixEpochTime2 a long for the second epoch time to be compared
	 * @return a Duration with the difference
	 */
	public  Duration computeDurationBetweenEpochTimes(long unixEpochTime1, long unixEpochTime2) {
		LocalDateTime dt1 = convertUnixTimeToLocalDateTime(unixEpochTime1);
		LocalDateTime dt2 = convertUnixTimeToLocalDateTime(unixEpochTime2);
		Duration duration = Duration.between(dt1, dt2);
		return duration;
	}

//	//DIFF IN MONTHS
//  LocalDateTime has Month getMonth() and int getYear
//	so you can have (year1-year2) + (month1 - month2)
//also
//https://stackoverflow.com/questions/1086396/java-date-month-difference/34811261#34811261
//	public void monthBetween() {
//
//	    LocalDate d1 = LocalDate.of(2013, Month.APRIL, 1);
//	    LocalDate d2 = LocalDate.of(2014, Month.APRIL, 1);
//
//	    long monthBetween = ChronoUnit.MONTHS.between(d1, d2);
//
//	    assertEquals(12, monthBetween);
//
//	}
	
//	/**
//	 * Converts unix time to java date and a human readable representation
//	 * https://stackoverflow.com/questions/17432735/convert-unix-time-stamp-to-date-in-java
//	 * 
//	 * @param unixSeconds a long value with the unix timestamp
//	 * @return a string in human-readable format for the input string
//	 */
//	private static String convertUnixTimeToHumanString(long unixSeconds) {
//		// convert seconds to milliseconds
//		Date date = new java.util.Date(unixSeconds*1000L); 
//		// the format of your date
//		SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); 
//		// give a timezone reference for formatting (see comment at the bottom)
//		sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC")); 
//		String formattedDate = sdf.format(date);
//		return formattedDate;
//	}


} //end TimeConverter
