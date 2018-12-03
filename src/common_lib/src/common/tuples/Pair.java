package common.tuples;

public class Pair<U, V> {
	private U u;
	private V v;

	public Pair(U u, V v) {
		this.u = u;
		this.v = v;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Pair))
			return false;
		Pair<?, ?> other = (Pair<?, ?>) obj;
		if (u == null) {
			if (other.u != null)
				return false;
		} else if (!u.equals(other.u))
			return false;
		if (v == null) {
			if (other.v != null)
				return false;
		} else if (!v.equals(other.v))
			return false;
		return true;
	}

	public U getU() {
		return u;
	}

	public V getV() {
		return v;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (u == null ? 0 : u.hashCode());
		result = prime * result + (v == null ? 0 : v.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "(" + u + ", " + v + ")";
	}
}
