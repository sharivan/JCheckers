package common.tuples;

public class Triplet<U, V, W> {
	private U u;
	private V v;
	private W w;

	public Triplet(U u, V v, W w) {
		this.u = u;
		this.v = v;
		this.w = w;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Triplet))
			return false;
		Triplet<?, ?, ?> other = (Triplet<?, ?, ?>) obj;
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
		if (w == null) {
			if (other.w != null)
				return false;
		} else if (!w.equals(other.w))
			return false;
		return true;
	}

	public U getU() {
		return u;
	}

	public V getV() {
		return v;
	}

	public W getW() {
		return w;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (u == null ? 0 : u.hashCode());
		result = prime * result + (v == null ? 0 : v.hashCode());
		result = prime * result + (w == null ? 0 : w.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "(" + u + ", " + v + ", " + w + ")";
	}
}
