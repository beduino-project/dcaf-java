package de.unibremen.beduino.dcaf.utils;

public class Tuple<T1, T2> {
	public final T1 _1;
	public final T2 _2;

	public Tuple(T1 _1, T2 _2){
		this._1 = _1;
		this._2 = _2;
	}

	public static <T3, T4> Tuple<T3, T4> of(T3 _1, T4 _2) {
		return new Tuple<>(_1, _2);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Tuple<?, ?> tuple = (Tuple<?, ?>) o;

		return _1.equals(tuple._1) && _2.equals(tuple._2);
	}

	@Override
	public int hashCode() {
		int result = _1.hashCode();
		result = 31 * result + _2.hashCode();
		return result;
	}
}