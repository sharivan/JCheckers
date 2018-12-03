package common.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListConfigEntry extends ConfigEntry {

	private ArrayList<String> items;

	public ListConfigEntry(String name, Collection<String> items) {
		super(name);

		this.items = new ArrayList<>(items);
	}

	public ListConfigEntry(String name, String[] items) {
		super(name);

		this.items = new ArrayList<>();
		for (int i = 0; i < items.length; i++)
			this.items.add(items[i]);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!super.equals(obj))
			return false;

		if (!(obj instanceof ListConfigEntry))
			return false;

		ListConfigEntry other = (ListConfigEntry) obj;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!items.equals(other.items))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = super.hashCode();

		result = prime * result + (items == null ? 0 : items.hashCode());

		return result;
	}

	public List<String> items() {
		return items;
	}

	@Override
	public String toString() {
		return "<" + getName() + " " + writeItems() + "/>";
	}

	private String writeItems() {
		String result = "";
		for (String item : items)
			result += "'" + item + "' ";

		return result;
	}

}
