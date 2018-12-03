package common.config;

public class SimpleConfigEntry extends ConfigEntry {

	private String value;

	public SimpleConfigEntry(String name, String value) {
		super(name);

		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!super.equals(obj))
			return false;

		if (!(obj instanceof SimpleConfigEntry))
			return false;

		SimpleConfigEntry other = (SimpleConfigEntry) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;

		return true;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		int prime = 31;

		int result = super.hashCode();
		result = prime * result + (value == null ? 0 : value.hashCode());

		return result;
	}

	@Override
	public String toString() {
		return "<" + getName() + " '" + value + "' />";
	}

}
