package common.db;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;

public class SQLFormater {

	public static String formatValue(Object value) {
		if (value == null)
			return "NULL";

		if (value instanceof SQLExpression)
			return ((SQLExpression) value).getValue();
		
		if (value instanceof Timestamp)
			return "\"" + value.toString() + "\"";
		
		if (value instanceof java.util.Date) {
			long t = ((java.util.Date) value).getTime();
			Date date = new Date(t);
			Time time = new Time(t);

			return "\"" + date + " " + time + "\"";
		}

		if (value instanceof String) {
			String result = (String) value;
			result = result.replace("\\", "\\\\");
			result = result.replace("\"", "\\\"");

			return "\"" + result + "\"";
		}

		if (value instanceof Enum)
			return "\"" + value + "\"";

		if (value instanceof Collection) {
			Collection<?> set = (Collection<?>) value;
			if (set.size() == 0)
				return "\"\"";

			Object[] elements = set.toArray();
			String result = elements[0].toString();
			for (int i = 1; i < elements.length; i++)
				result += "," + elements[i];

			return "\"" + result + "\"";
		}

		return value.toString();
	}

	public static String formatValues(Object... values) {
		String result = formatValue(values[0]);
		for (int i = 1; i < values.length; i++)
			result += ", " + formatValue(values[i]);

		return result;
	}

}
