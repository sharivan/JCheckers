package common.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapConfigEntry extends ConfigEntry {

	private HashMap<String, String> map;

	public MapConfigEntry(String name, Map<String, String> map) {
		super(name);

		this.map = new HashMap<>(map);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!super.equals(obj))
			return false;

		if (!(obj instanceof MapConfigEntry))
			return false;

		MapConfigEntry other = (MapConfigEntry) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = super.hashCode();

		result = prime * result + (map == null ? 0 : map.hashCode());

		return result;
	}

	public Map<String, String> map() {
		return map;
	}

	@Override
	public String toString() {
		return "<" + getName() + " " + writeMap() + "/>";
	}

	private String writeMap() {
		String result = "";
		Set<String> keys = map.keySet();
		for (String key : keys)
			result += key + "='" + map.get(key) + "' ";

		return result;
	}

}
