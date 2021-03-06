package edu.ohio.minuku.Utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import edu.ohio.minuku.config.Constants;

public class ScheduleAndSampleManager {

	public static int bedStartTime = 0;
	public static int bedEndTime = 5;
	public static int bedMiddleTime = 2;
	public static long time_base = 0;

	/**convert long to timestring**/
	public static String getTimeString(long time){

		SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_SLASH);
		String currentTimeString = sdf_now.format(time);

		return currentTimeString;
	}


	public static String getTimeString(long time, SimpleDateFormat sdf){

		String currentTimeString = sdf.format(time);

		return currentTimeString;
	}

	public static String getCurrentTimeString() {

		return getTimeString(getCurrentTimeInMillis());
	}

	public static String getCurrentDate() {

		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_DAY);

		return getTimeString(getCurrentTimeInMillis(), sdf);
	}

	public static boolean isCurrentDate(long time) {

		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_DAY);

		String timeDate = getTimeString(time, sdf);

		String currentDate = getTimeString(getCurrentTimeInMillis(), sdf);

		return timeDate.equals(currentDate);
	}

	public static String getCurrentMidNightTimeString() {

		return getTimeString(getCurrentMidNightTimeInMillis());
	}
	/**get the current time in milliseconds**/
	public static long getCurrentTimeInMillis(){
		//get timzone
		TimeZone tz = TimeZone.getDefault();
		Calendar cal = Calendar.getInstance(tz);
		long t = cal.getTimeInMillis();
		return t;
	}

	public static long getCurrentMidNightTimeInMillis(){
		long currentTime = getCurrentTimeInMillis();
		SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_DAY);
		String currentTimeString = sdf_now.format(currentTime);

		long currentMidNightTime = getTimeInMillis(currentTimeString, sdf_now);

		return currentMidNightTime;
	}

	public static long getTimeInMillis(String givenDateFormat, SimpleDateFormat sdf){
		long timeInMilliseconds = 0;
		try {
			Date mDate = sdf.parse(givenDateFormat);
			timeInMilliseconds = mDate.getTime();

		} catch (ParseException e) {
//			e.printStackTrace();
		}
		return timeInMilliseconds;
	}

	public static long changeTimetoCurrentDate(long givenTime){

		long currentDateInGivenTime = Constants.INVALID_TIME_VALUE;

		SimpleDateFormat sdf_date = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_DAY);
		String currentDate = getTimeString(getCurrentTimeInMillis(), sdf_date);

		SimpleDateFormat sdf_HHmmss = new SimpleDateFormat(Constants.DATE_FORMAT_HOUR_MIN_SECOND);
		String givenHHmmss = getTimeString(givenTime, sdf_HHmmss);

//		DATE_FORMAT_NOW_NO_ZONE
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_NO_ZONE);
		String currentDateInGivenTimeString = currentDate + " " + givenHHmmss;
		currentDateInGivenTime = getTimeInMillis(currentDateInGivenTimeString, sdf);

		return currentDateInGivenTime;
	}

	public static int getHourOfTimeOfDay (String TimeOfDay){

		return Integer.parseInt(TimeOfDay.split(":")[0] ) ;
	}

	public static int getMinuteOfTimeOfDay (String TimeOfDay){

		return Integer.parseInt(TimeOfDay.split(":")[1] );
	}

}
