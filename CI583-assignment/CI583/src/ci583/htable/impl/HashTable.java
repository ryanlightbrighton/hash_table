package ci583.htable.impl;
import java.util.ArrayList;
import java.util.Collection;



/**
* <font size='3'><h1>Hash Table</h1></font>
* <h2>Description</h2>
 * A HashTable with no deletions allowed. 
 * Duplicates overwrite the existing value.
 * 
 * The underlying data is stored in the array `tableArray', and the actual values stored are pairs of 
 * (key, value). This is so that we can detect collisions in the hash function and look for the next 
 * location when necessary.
 * 
 * <h2>Modifications</h2>
 * 
 * 1 - Keys have been parameterised so that now the hash table will accept types like string, integer
 * 	   and object
 * 
 * 2 - 'Tail Calls' library has been implemented to avoid stack overflow when dealing with large 
 * 	   numbers of keys - Not my work! External library - credits in TailCalls.java
 * 
 * 3 - Hash function has been replaced to provide better distribution and less collisions
 * 
 * 4 - Double Hash function has been rewritten for same reason
 * 
 * 5 - New probe method based on Golden Ratio has been added for better distribution 
 * 	   following a collision
 * 
 * 
 * @author  Ryan Light (original framework by Jim Burton)
 */
public class HashTable<T,V> {
	/**
	 * Create a new Hashtable with a given initial capacity and using a given probe type
	 * @param initialCapacity
	 * @param pt
	 */
	public HashTable(int initialCapacity, PROBE_TYPE pt) {
		this.probeType = pt;
		constructorCommon(initialCapacity);
	}
	
	/**
	 * Create a new Hashtable with a given initial capacity and using the default probe type
	 * @param initialCapacity
	 */
	public HashTable(int initialCapacity) {
		this.probeType = PROBE_TYPE.LINEAR_PROBE;
		constructorCommon(initialCapacity);
	}
	/**
	 * Contains a list of commands and functions common to each constructor
	 * @param initialCapacity
	 */
	private void constructorCommon(int initialCapacity) {
		this.primes = new boolean[Math.max(initialCapacity * 10, 100000000)];
		makeSieve(primes.length);
		setCapacity(nextPrime(initialCapacity));
		Object[] newArr = new Object[getCapacity()];
		setTableArray(newArr);
		setCapacity(getCapacity());
		setItemCount(0);
	}
	
	private Object[] tableArray;			// an array of Pair objects
	private int max;						// the size of arr. This should be a prime number
	private int itemCount; 					// the number of items stored in arr
	private final double maxLoad = 0.6; 	// the maximum load factor
	private boolean[] primes;				// array of boolean used to find primes
	private static final double ratio = (1 + Math.sqrt(5))/2;	// golden ratio - used in probe
	public static enum PROBE_TYPE {
		LINEAR_PROBE, QUADRATIC_PROBE, DOUBLE_HASH, GR_PROBE;
	}
	// list of primes used by the hash function
	static int akrownePrimes[] = {1610612741,805306457,402653189,201326611,100663319,50331653,25165843,12582917,6291469,3145739,1572869,786433,393241,196613,98317,49157,24593,12289,6151,3079,1543,769,389,193,97,53};
	PROBE_TYPE probeType;
	/**
		<font size='5'><h1>{@code getMaxLoad()}</h1></font>
		<b><h2>Description</h2></b>
		returns the maximum allowed load of the hash table.
		<p>
		@param none
		@return {@code maxLoad}
	*/
	public double getMaxLoad() {
		return maxLoad;
	}
	/**
		<font size='5'><h1>{@code getTableArray()}</h1></font>
		<b><h2>Description</h2></b>
		Returns the current array that stores keys and their corresponding values.
		(Its size should always be a prime number).
		<p>
		@param none
		@return {@code tableArray}
	*/
	public Object[] getTableArray() {
		return this.tableArray;
	}
	/**
		<font size='5'><h1>{@code setTableArray(object array)}</h1></font>
		<b><h2>Description</h2></b>
		Sets the current array that stores keys and their corresponding values.</br>
		This should be calculated as 2 times the size of the current array.</br>
		(Its size should always be a prime number).
		<p>
		@param array this is an array to store {@code Pair} class objects
		@return {@code void}
	*/
	public void setTableArray(Object[] x) {
		this.tableArray = x;
	}
	/**
		<font size='5'><h1>{@code getCapacity()}</h1></font>
		<b><h2>Description</h2></b>
		Returns the current maximum size of the hashtable array
		<p>
		@param none
		@return {@code integer}
	*/
	public int getCapacity() {
		return this.max;
	}
	/**
		<font size='5'><h1>{@code setCapacity(int x)}</h1></font>
		<b><h2>Description</h2></b>
		Saves the current max size of the hashtable array
		<p>
		@param integer
		@return {@code void}
	*/
	public void setCapacity(int x) {
		this.max = x;
	}
	/**
		<font size='5'><h1>{@code getItemCount()}</h1></font>
		<b><h2>Description</h2></b>
		gets the current number of Pairs in the array
		<p>
		@param none
		@return {@code integer}
	*/
	public int getItemCount() {
		return this.itemCount;
	}
	/**
		<font size='5'><h1>{@code setItemCount(int x)}</h1></font>
		<b><h2>Description</h2></b>
		saves the current number of Pairs in the array
		<p>
		@param integer
		@return {@code none}
	*/
	public void setItemCount(int x) {
		this.itemCount = x;
	}
	/**
		<font size='5'><h1>{@code incrementItemCount()}</h1></font>
		<b><h2>Description</h2></b>
		Increments the count of current number of Pairs stored in the array by one
		<p>
		@param none
		@return {@code none}
	*/
	public void incrementItemCount() {
		this.itemCount++;
	}
	/**
		<font size='5'><h1>{@code getPrimes()}</h1></font>
		<b><h2>Description</h2></b>
		Returns the primes array
		<p>
		@param none
		@return {@code boolean[]}
	*/
	public boolean[] getPrimes() {
		return this.primes;
	}
	/**
		<font size='5'><h1>{@code setPrimes(boolean[] x)}</h1></font>
		<b><h2>Description</h2></b>
		Saves the primes array
		<p>
		@param boolean[]
		@return {@code none}
	*/
	public void setPrimes(boolean[] x) {
		this.primes = x;
	}
	/**
	 * <font size='5'><h1>{@code put(T key, V value)}</h1></font>
	 * <b><h2>Description</h2></b>
	 * Stores the value against the given key. If the loadFactor exceeds maxLoad, It calls the resize 
	 * method to resize the array. It calls the hash function, then finds an empty index and 
	 * creates a Pair Object with key and value. 
	 * @param T,
	 * @param V
	 * @return {@code none}
	 */
	public void put(T key, V value) {
		if (getLoadFactor() > getMaxLoad()) {
			resize();
		}
		int index = hash(key);
		if (getTableArray()[index] != null) {
			index = findEmpty(index, 0, key).get();
		}
		getTableArray()[index] = createNode(key, value);
		incrementItemCount();
	}

	/**
	 * <font size='5'><h1>{@code get(T key)}</h1></font>
	 * <b><h2>Description</h2></b>
	 * Get the value associated with key, or returns null if key does not exists.
	 * uses the hashed value of the key to start search
	 * @param key
	 * @return V
	 */
	public V get(T key) {
		int startPos = hash(key);
		return find(startPos, key, 0).get();
	}

	/**
	 * <font size='5'><h1>{@code hasKey(T key)}</h1></font>
	 * <b><h2>Description</h2></b>
	 * Return true if the Hash table contains this key, false otherwise 
	 * @param T
	 * @return boolean
	 */
	public boolean hasKey(T key) {
		boolean hasKey = false;
		if (null != get(key)) {
			hasKey = true;
		}
		return hasKey;
	}

	/**
	 * <font size='5'><h1>{@code getKeys()}</h1></font>
	 * <b><h2>Description</h2></b>
	 * Wrapper function - initialises array and calls Tail Calls getKeys
	 * Returns arraylist of keys 
	 * @param none
	 * @return ArrayList T
	 */
	public Collection<T> getKeys() {
		ArrayList<T> coll = new ArrayList<>();
		return getKeys(coll,0).get();
	}
	
	/**
	 * <font size='5'><h1>{@code getKeys(Collection<T> coll,int i)}</h1></font>
	 * <b><h2>Description</h2></b>
	 * Iterates through array and returns arraylist of keys  
	 * @param Arraylist T
	 * @param integer
	 * @return ArrayList T
	 */
	@SuppressWarnings("unchecked")
	public TailCall<Collection<T>> getKeys(Collection<T> coll,int i) {
		if (i == getCapacity()) {
			return TailCalls.done(coll);
		} else {
			if (getTableArray()[i] != null) {
				coll.add(((HashTable<T,V>.Pair) getTableArray()[i]).getKey());
			}
			return TailCalls.call (()-> getKeys(coll, i + 1));
		}
	}

	/**
	 * <font size='5'><h1>{@code getLoadFactor()}</h1></font>
	 * <b><h2>Description</h2></b>
	 * Returns the load factor of the hash table  
	 * @param none
	 * @return double
	 */
	public double getLoadFactor() {
		return ((double) getItemCount() / getCapacity());
	}


	
	/**
	 * <font size='5'><h1>{@code find(int startPos, T key, int stepNum)}</h1></font>
	 * <b><h2>Description</h2></b>
	 * Finds the value stored for this key, starting the search at position startPos in the array. If
	 * the item at position startPos is null, the Hash table does not contain the value, so returns null. 
	 * If the key stored in the pair at position startPos matches the key we're looking for, then it return the associated 
	 * value. If the key stored in the pair at position startPos does not match the key we're looking for, this
	 * is a hash collision so it uses the getNextLocation method with an incremented value of stepNum to find 
	 * the next location to search (the way that this is calculated will differ depending on the probe type 
	 * being used). Then it uses the value of the next location in a recursive call to find.
	 * @param startPos
	 * @param key
	 * @param stepNum
	 * @return V
	 */
	private TailCall<V> find(int startPos, T key, int stepNum) {
		@SuppressWarnings("unchecked")
		Pair element = (HashTable<T,V>.Pair) getTableArray()[startPos];
		if (element == null) {
			return TailCalls.done(null);
		} else if (element.getKey().equals(key)) {
			return TailCalls.done(element.getValue());
		} else {
			return TailCalls.call (()-> find(getNextLocation(startPos, 1 + stepNum, key), key, stepNum));
		}
	}

	/**
	 * <font size='5'><h1>{@code findEmpty(int startPos, int stepNum, T key)}</h1></font>
	 * <b><h2>Description</h2></b>
	 * Finds the first unoccupied location where a value associated with key can be stored, starting the
	 * search at position startPos. If startPos is unoccupied, it returns startPos. Otherwise it uses the getNextLocation
	 * method with an incremented value of stepNum to find the appropriate next position to check 
	 * (which will differ depending on the probe type being used) and uses this in a recursive call to findEmpty.
	 * @param startPos
	 * @param stepNum
	 * @param key
	 * @return
	 */
	private TailCall<Integer> findEmpty(int startPos, int stepNum, T key) {
		if (getTableArray()[startPos] == null) {
			return TailCalls.done(startPos);
		} else {
			return TailCalls.call (()-> findEmpty(getNextLocation(startPos, 1 + stepNum, key), stepNum, key));
		}
	}

	/**
	 * <font size='5'><h1>{@code getNextLocation(int startPos, int stepNum, T key)}</h1></font>
	 * <b><h2>Description</h2></b>
	 * Finds the next position in the Hashtable array starting at position startPos. If the linear
	 * probe is being used, it increments startPos. If the double hash probe type is being used, 
	 * add the double hashed value of the key to startPos. If the quadratic probe is being used, it adds
	 * the square of the step number to startPos.  If Golden Ratio probe is used, it adds the current capacity
	 * of the array divided by the Golden Ratio
	 * @param integer
	 * @param stepNum
	 * @param T
	 * @return integer
	 */
	private int getNextLocation(int startPos, int stepNum, T key) {
		int step = startPos; // why reassign?  Why not just use startPos?
		switch (probeType) {
		case LINEAR_PROBE:
			step++;
			break;
		case DOUBLE_HASH:
			step += doubleHash(key);
			break;
		case QUADRATIC_PROBE:
			step += stepNum * stepNum;
			break;
		case GR_PROBE:
			step += (int) (getCapacity() / ratio);
			break;
		default:
			break;
		}
		return step % getCapacity();
	}

	/**
	 * <font size='5'><h1>{@code doubleHash(T input)}</h1></font>
	 * <b><h2>Description</h2></b>
	 * A secondary hash function which returns a small value
	 * to probe the next location if the double hash probe type is being used
	 * @param T
	 * @return integer
	 */
	private int doubleHash(T input) {
		String key = input.toString();
		int prime = 683;
		long hash = 0;
		for (int i = 0; i < key.length(); i++) {
			hash = hash + (prime - (key.charAt(i)));
		}
		hash &= 0x7fffffff;
		return (int) hash % prime;
	}

	/**
	 * <font size='5'><h1>{@code hash(T input)}</h1></font>
	 * <b><h2>Description</h2></b>
	 * Return an integer value calculated by hashing the key. The bitwise AND operator is applied to
	 * make sure the return value is not negative and modulus of the capacity to keep within bounds
	 * @param key
	 * @return integer
	 */
	private int hash(T input) {
		String key = input.toString();
		long hash = 5381;
		int prime = 6291469;
		for(int i = 0; i < key.length(); i++) {
			hash = ((hash << 5) + hash + (prime << 4) + (key.charAt(i)));
			prime = akrownePrimes[i % akrownePrimes.length];
		}
		hash &= 0x7fffffff;
		return (int)hash % getCapacity();
	}
	
	/**
	 * <font size='5'><h1>{@code makeSieve(int length)}</h1></font>
	 * <b><h2>Description</h2></b>
	 * Creates the primes sieve used by the resize function
	 * @param integer
	 * @return none
	 */
	public void makeSieve(int length) {
		for(int i = 0; i < length; i++) { 
			getPrimes()[i] = true;
		}
		getPrimes()[0] = getPrimes()[1] = false;  // 2 is smallest prime
		for (int i = 2; i < getPrimes().length; i++) {
			//if i is prime its multiples are not
			if (getPrimes()[i]) {
				for (int j = 2; i * j < getPrimes().length; j++) {
					getPrimes()[i * j] = false;
				}
			}
		}
	}

	/**
	 * <font size='5'><h1>{@code isPrime(int n)}</h1></font>
	 * <b><h2>Description</h2></b>
	 * Returns true if n is prime
	 * @param integer
	 * @return boolean
	 */
	private boolean isPrime(int n) {
		return getPrimes()[n];
	}

	/**
	 * <font size='5'><h1>{@code nextPrime(int n)}</h1></font>
	 * <b><h2>Description</h2></b>
	 * Gets the smallest prime number which is larger than n
	 * @param n
	 * @return integer
	 */
	private int nextPrime(int n) {
		int nextPrime = -1;
		if (n >= getPrimes().length -1) { // we start our check at n+1 (n++) so check against primes length -1 (so not out of range);
			throw new Error("Not enough primes - make your prime array bigger!");
		}
		for (n++; n < getPrimes().length; n++) {
			if (isPrime(n)) {
				nextPrime = n;
				break;
			}
		}
		return nextPrime;
	}

	/**
	 * <font size='5'><h1>{@code resize()}</h1></font>
	 * <b><h2>Description</h2></b>
	 * Resizes the hashtable, when the load factor exceeds maxLoad. The new size of
	 * the underlying array is the smallest prime number which is at least twice the size
	 * of the old array.
	 * @param none
	 * @return none
	 */
	private void resize() {
		Object[] oldArr = getTableArray();
		int primeNumb = nextPrime(2 * oldArr.length);
		Object[] newArr = new Object[primeNumb];
		setCapacity(primeNumb);
		setTableArray(newArr);
		setItemCount(0);
		for (int i = 0; i < oldArr.length; i++) {
			@SuppressWarnings("unchecked")
			Pair pair = (HashTable<T,V>.Pair) oldArr[i];
			if (pair != null) {
				T key = pair.getKey();
				V value = pair.getValue();
				put(key,value);
			}
		}
	}
	
	/**
	 * <font size='5'><h1>{@code createNode(T key, V value)}</h1></font>
	 * <b><h2>Description</h2></b>
	 * Creates a new Pair object and assigns key and value
	 * @param T
	 * @param V
	 * @return Pair
	 */
	public Pair createNode(T key, V value) {
		return new Pair(key, value);
	}

	
	/**
	 * <font size='5'><h1>{@code Pair}</h1></font>
	 * <b><h2>Description</h2></b>
	 * Class for Pair objects
	 */
	private class Pair {
		private T key;
		private V value;
		public Pair(T key, V value) {
			this.key = key;
			this.value = value;
		}
		public T getKey() {
			return this.key;
		}
		public V getValue() {
			return this.value;
		}
	}
}