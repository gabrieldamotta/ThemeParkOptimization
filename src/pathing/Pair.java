package pathing;

/**
 * Auxiliary class to store pairs.
 * 
 * @author Gabriel da Motta
 * @version 10/06/2019
 *
 */
public class Pair<K extends Comparable<K>,T> implements Comparable<Pair<K,T>> {
	// stored values
	K x;
	T y;
	
	/**
	 * Constructs the pair with
	 * values x and y.
	 * 
	 * @param x the first value
	 * @param y the second value
	 */
	public Pair(K x, T y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Gets the y.
	 * 
	 * @return the object y
	 */
	public K getX() {
		return x;
	}
	
	/**
	 * Gets the x.
	 * 
	 * @return the object x
	 */
	public T getY() {
		return y;
	}
	
	/**
	 * Converts this pair into a
	 * more legible string. Used
	 * for debugging.
	 */
	@Override
	public String toString() {
		String s = "(x: " + getX() + ", y: " + getY() + ")";
		return s;
	}

	/**
	 * Compare this to pair
	 * other using x.
	 * 
	 * @param other the other pair
	 * @return comparison
	 */
	@Override
	public int compareTo(Pair<K,T> other) {
		return getX().compareTo(other.getX());
	}
	
	@SuppressWarnings("unchecked")
	@Override
    public boolean equals(Object other) {
		// works in accordance with K
		if (other instanceof Pair<?,?>) {
			return getX().equals(((Pair<K, T>) other).getX());
		}
		return false;
    }
	
	@Override
    public int hashCode(){
        return getX().hashCode();
    }
}