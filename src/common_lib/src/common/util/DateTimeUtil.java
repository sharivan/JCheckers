package common.util;

import java.util.Calendar;

public class DateTimeUtil {

	public static Calendar currentTimeStamp() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		return calendar;
	}

	public static String dateHourToStr(Calendar date) {
		return dateHourToStr(date, true);
	}

	public static String dateHourToStr(Calendar date, boolean seconds) {
		String result = FormatterUtil.formatDigits(date.get(Calendar.HOUR_OF_DAY), 2) + ":" + FormatterUtil.formatDigits(date.get(Calendar.MINUTE), 2);

		if (seconds)
			result += ":" + FormatterUtil.formatDigits(date.get(Calendar.SECOND), 2);

		return result;
	}

	public static String dateHourToStr_(Calendar date) {
		return FormatterUtil.formatDigits(date.get(Calendar.HOUR_OF_DAY), 2) + "-" + FormatterUtil.formatDigits(date.get(Calendar.MINUTE), 2) + "-"
				+ FormatterUtil.formatDigits(date.get(Calendar.SECOND), 2);
	}

	public static String dateToStr(Calendar date) {
		return dateToStr(date, true, true);
	}

	public static String dateToStr(Calendar date, boolean hoursAndMinutes, boolean seconds) {
		String result = FormatterUtil.formatDigits(date.get(Calendar.DAY_OF_MONTH), 2) + "/" + FormatterUtil.formatDigits(date.get(Calendar.MONTH) + 1, 2) + "/"
				+ FormatterUtil.formatDigits(date.get(Calendar.YEAR), 4);

		if (hoursAndMinutes)
			result += " " + dateHourToStr(date);

		return result;
	}

	public static String dateToStr_(Calendar date) {
		return FormatterUtil.formatDigits(date.get(Calendar.DAY_OF_MONTH), 2) + "-" + FormatterUtil.formatDigits(date.get(Calendar.MONTH) + 1, 2) + "-"
				+ FormatterUtil.formatDigits(date.get(Calendar.YEAR), 4) + " " + dateHourToStr_(date);
	}

	public static String daysTimeToStr(long time) {
		if (time < 60)
			return time + " minutos";

		time /= 60;
		if (time < 24)
			return time + " horas";

		time /= 24;

		return time + " dias";
	}

}
