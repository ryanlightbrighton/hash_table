package ci583.htable.impl;
import java.math.BigInteger;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

public class HashTableDebugger<V> {
	public HashTableDebugger(int initialCapacity, PROBE_TYPE pt) {
		this.probeType = pt;
		this.primes = new boolean[Math.max(initialCapacity * 10, 10000000)];
		makeSieve(primes.length);
		setMax(nextPrime(initialCapacity));
		resize(getMax());
		setDebugObj(new DebuggingExtra());
	}
	public HashTableDebugger(int initialCapacity) {
		this.probeType = PROBE_TYPE.LINEAR_PROBE;
		this.primes = new boolean[Math.max(initialCapacity * 10, 10000000)];
		makeSieve(primes.length);
		setMax(nextPrime(initialCapacity));
		resize(getMax());
		setDebugObj(new DebuggingExtra());
	}
	private static DebuggingExtra DEBUG_CLASS; // only works for static, not instance declarations
	private DebuggingExtra debug;
	public DebuggingExtra getDebugObj() {
		return this.debug;
	}
	public void setDebugObj(DebuggingExtra obj) {
		this.debug = obj;
	}
	private Pair<V>[] tableArray; //an array of Pair objects, where each pair contains the key and value stored in the hashtable
	private int max; //the size of tableArray. This should be a prime number
	private int itemCount; //the number of items stored in tableArray
	private static final double maxLoad = 0.6; //the maximum load factor
	private boolean[] primes;
	private static final double ratio = (1 + Math.sqrt(5))/2;
	private static final int chenPrimes[] = {3691, 3709, 3719, 3761, 3767, 3779, 3797, 3803, 3821, 3847, 3851, 3863, 3881, 3889, 3907, 3917, 3919, 3929, 3947, 3989, 4001, 4007, 4019, 4049, 4091, 4099, 4127, 4133, 4139, 4157, 4211, 4217, 4229, 4241, 4259, 4271, 4283, 4289, 4297, 4337, 4339, 4349, 4357, 4391, 4397, 4409, 4421, 4441, 4447, 4451, 4481, 4517, 4547, 4567, 4591, 4637, 4643, 4649, 4657, 4679, 4703, 4721, 4733, 4787, 4789, 4799, 4801, 4817, 4861, 4871, 4889, 4909, 4931, 4937, 4967, 4969, 4987, 4999, 5009, 5021, 5039, 5051, 5077, 5087, 5099, 5147, 5153, 5167, 5171, 5189, 5197, 5231, 5261, 5279, 5297, 5303, 5309, 5347, 5351, 5381, 5387, 5399, 5417, 5431, 5441, 5471, 5477, 5483, 5501, 5507, 5519, 5531, 5581, 5639, 5651, 5657, 5669, 5701, 5711};
	private int bounces = 0;
	public static enum PROBE_TYPE {
		LINEAR_PROBE, QUADRATIC_PROBE, DOUBLE_HASH, GR_PROBE;
	}

	PROBE_TYPE probeType; //the type of probe to use when dealing with collisions
	private final BigInteger DBL_HASH_K = BigInteger.valueOf(8);
	public int getBounceCount() {
		return this.bounces;
	}
	public void incrementBounceCount() {
		this.bounces++;
	}
	public void resetBounceCount() {
		//this.bounces = 0;
	}
	public double getMaxLoad() {
		return maxLoad;
	}
	public Pair<V>[] getTableArray() {
		return this.tableArray;
	}
	public void setTableArray(Pair<V>[] x) {
		this.tableArray = x;
	}
	public int getMax() {
		return this.max;
	}
	public void setMax(int x) {
		this.max = x;
	}
	public int getItemCount() {
		return this.itemCount;
	}
	public void setItemCount(int x) {
		this.itemCount = x;
	}
	public void incrementItemCount() {
		this.itemCount++;
	}
	public boolean[] getPrimes() {
		return this.primes;
	}
	public void setPrimes(boolean[] x) {
		this.primes = x;
	}
	public Collection<String> getKeys() {
		throw new UnsupportedOperationException("Method not implemented");
		// try it RECURSIVE
	}
	public double getLoadFactor() {
		return ((double) getItemCount() / getMax()); 
	}
	public int getCapacity() {
		return getMax();
	}
	public boolean hasKey(String key) {
		boolean hasKey = false;
		if (null != get(key)) {
			hasKey = true;
		}
		return hasKey;
	}
	public V get(String key) {
		int startPos = akrowneHash(key);
		V someVar = find(startPos, key, 0);
		return someVar;
	}
	private V find(int startPos, String key, int stepNum) {
		Pair<V> element = getTableArray()[startPos];
		if (element == null) {
			return null;
		} else if (element.getKey().equals(key)) {
			return element.getValue();
		} else {
			return find(getNextLocation(startPos, ++stepNum, key), key, stepNum);
		}

	}
	public void put(String key, V value) {
		if (getLoadFactor() > getMaxLoad()) {
			resize(-1); // O(n) n=length of table (unless we are calling the double hash O(n*s) where s=length of string)
		}
		int index = akrowneHash(key); // O(n) n=length of key
		if (getTableArray()[index] instanceof Pair) {
			// we have a collision
			index = findEmpty(index, 0, key); // O(n) n=length of table  (unless we are calling the double hash O(n*s) where s=length of string)
			getDebugObj().addToDistribution(index);
			getTableArray()[index] = createNode(key, value);
			getDebugObj().pushLoadVsHashList(getBounceCount());
			incrementItemCount();
		} else {
			// put it in
			getDebugObj().addToDistribution(index);
			getTableArray()[index] = createNode(key, value); // constructor sets key & value
			getDebugObj().pushLoadVsHashList(getBounceCount());
			incrementItemCount();
		}
	}
	private int findEmpty(int index, int stepNum, String key) {
		if (getTableArray()[index] instanceof Pair) {
			getDebugObj().incrementCollCount();
			getDebugObj().pushCollisionList(getLoadFactor());
			index = getNextLocation(index, ++stepNum, key);
			return findEmpty(index, stepNum, key);
		} else {
			return index;
		}
	}
	private int getNextLocation(int index, int stepNum, String key) {
		incrementBounceCount();
		int step = index; // why reassign?  Why not just use index?
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
			step += (int) (getMax() / ratio);
			break;
		default:
			break;
		}
		//System.out.println("step: " + step  % getMax());
		return step % getMax();
	}
	private void resize(int primeNumb) {
		Pair<V>[] oldArr = getTableArray();
		
		if (oldArr == null) {
			@SuppressWarnings("unchecked")
			Pair<V>[] newArr = (HashTableDebugger<V>.Pair<V>[]) new Pair[primeNumb];
			setTableArray(newArr);
			setMax(primeNumb);
			setItemCount(0);
		} else {
			// now copy!
			getDebugObj().incrementResizeCount();
			primeNumb = nextPrime(2 * oldArr.length);
			@SuppressWarnings("unchecked")
			Pair<V>[] newArr = (HashTableDebugger<V>.Pair<V>[]) new Pair[primeNumb];
			setMax(primeNumb);
			setTableArray(newArr);
			// note: I used LOOP because of issue with Stack Overflow in recursive statements and big arrays
			setItemCount(0);  // need to do this because put() increments it
			for (int i = 0; i < oldArr.length; i++) {
				Pair<V> pair = oldArr[i];
				if (pair instanceof Pair) {
					String key = pair.getKey();
					V value = pair.getValue();
					put(key,value);
				}
			}
		}
	}
	private int doubleHash(String key) {
		BigInteger hashVal = BigInteger.valueOf(key.charAt(0) - 96);
		for (int i = 0; i < key.length(); i++) {
			BigInteger c = BigInteger.valueOf(key.charAt(i) - 96);
			hashVal = hashVal.multiply(BigInteger.valueOf(27)).add(c);
		}
		return DBL_HASH_K.subtract(hashVal.mod(DBL_HASH_K)).intValue();
	}
	public int binaryHash(String str) {
		// wip binary implementation
        /*String hashS = "";
        for (int i = 0; i < str.length(); i++) {
            hashS += Integer.toBinaryString(str.charAt(i));
        }
        int hash = (int) Long.parseLong(hashS,2) % getMax();
        return hash;*/
		
		// this is a binary component sum hash
		// it also uses polynomial accumulation
		// idea taken from here: https://www.cs.vu.nl/~tcs/ds/lecture6.pdf
		
		// gaussian, chen, irregular primes, isolated primes
		
		int hash = 307;
		int l = str.length();
        for (int i = 0; i < str.length(); i++) {
            String hashS = Integer.toBinaryString(str.charAt(i));
            //hash += (int) Integer.parseInt(hashS,2) * (5381 ^ --l);
            hash = (hash + (int) Integer.parseInt(hashS,2) * (409/*5381*/ ^ --l)) % getMax(); // 491 // 409 //311 // 503 307 373
        }
        
        return hash;
    }
	
	public int javaHash(String str) {
		// java implementation
		// https://www.tutorialspoint.com/java/java_string_hashcode.htm
        int hash = 0;
        int l = str.length();
        for (int i = 0; i < str.length(); i++) {
        	hash = (hash + str.charAt(i) * (31 ^ --l)) % getMax();
        	//hash += str.charAt(i) * (31 ^ --l);
        	// https://stackoverflow.com/questions/299304/why-does-javas-hashcode-in-string-use-31-as-a-multiplier
        	//hash = (hash + str.charAt(i) + (hash << 5)) % getMax();
        }
        return hash;
    }
	
	public int chenHash(String str) {
		//https://prime-numbers.info/list/chen-primes-up-to-10000
		//int isolatedPrimes[] = {701, 709, 719, 727, 733, 739, 743, 751, 757, 761, 769, 773, 787, 797, 839, 853, 863, 877, 887, 907, 911, 919, 929, 937, 941, 947, 953, 967, 971, 977, 983, 991, 997, 1009, 1013, 1039, 1069, 1087, 1097, 1103, 1109, 1117, 1123, 1129, 1163, 1171, 1181, 1187, 1193, 1201, 1213, 1217, 1223, 1237, 1249, 1259, 1283, 1297, 1307, 1327, 1361, 1367, 1373, 1381, 1399, 1409, 1423, 1433, 1439, 1447, 1459, 1471, 1493, 1499, 1511, 1523, 1531, 1543, 1549, 1553, 1559, 1567, 1571, 1579, 1583, 1597};
		//int chenPrimeslo[] = {701, 719, 743, 751, 761, 769, 787, 797, 809, 811, 821, 827, 829, 839, 857, 863, 877, 881, 887, 911, 919, 937, 941, 947, 953, 971, 977, 983, 991, 1009, 1019, 1031, 1039, 1049, 1061, 1091, 1097, 1109, 1117, 1151, 1163, 1187, 1193, 1201, 1217, 1229, 1259, 1277, 1283, 1289, 1291, 1297, 1301, 1319, 1327, 1361, 1367, 1381, 1399, 1409, 1427, 1439, 1451, 1459, 1471, 1481, 1487, 1499, 1511, 1553, 1559, 1567, 1583, 1601, 1607, 1619, 1621, 1637, 1667, 1669, 1697, 1709, 1721, 1733, 1759, 1777, 1787, 1801, 1847, 1871, 1877, 1889, 1901, 1907};
		// ==> this one int chenPrimes[] = {3691, 3709, 3719, 3761, 3767, 3779, 3797, 3803, 3821, 3847, 3851, 3863, 3881, 3889, 3907, 3917, 3919, 3929, 3947, 3989, 4001, 4007, 4019, 4049, 4091, 4099, 4127, 4133, 4139, 4157, 4211, 4217, 4229, 4241, 4259, 4271, 4283, 4289, 4297, 4337, 4339, 4349, 4357, 4391, 4397, 4409, 4421, 4441, 4447, 4451, 4481, 4517, 4547, 4567, 4591, 4637, 4643, 4649, 4657, 4679, 4703, 4721, 4733, 4787, 4789, 4799, 4801, 4817, 4861, 4871, 4889, 4909, 4931, 4937, 4967, 4969, 4987, 4999, 5009, 5021, 5039, 5051, 5077, 5087, 5099, 5147, 5153, 5167, 5171, 5189, 5197, 5231, 5261, 5279, 5297, 5303, 5309, 5347, 5351, 5381, 5387, 5399, 5417, 5431, 5441, 5471, 5477, 5483, 5501, 5507, 5519, 5531, 5581, 5639, 5651, 5657, 5669, 5701, 5711};
		//int chenPrimes[] = {8663, 8681, 8689, 8707, 8741, 8747, 8779, 8807, 8819, 8837, 8849, 8861, 8887, 8933, 8951, 8969, 8999, 9001, 9011, 9029, 9041, 9067, 9109, 9181, 9199, 9209, 9221, 9227, 9239, 9257, 9281, 9311, 9341, 9377, 9419, 9431, 9437, 9461, 9467, 9479, 9491, 9521, 9533, 9551, 9587, 9629, 9649, 9661, 9677, 9689, 9719, 9743, 9767, 9769, 9791, 9811, 9839, 9851, 9857, 9901, 9929, 9941, 9967};
		//int somePrimes[] = {53,97,193,389,769,1543,3079,6151,12289,24593,49157,98317,196613,393241,786433,1572869,3145739,6291469,12582917,25165843,50331653,100663319,201326611,402653189,805306457,1610612741};
		// https://www.planetmath.org/goodhashtableprimes
		int hash = 5737;
		int prime = 683; // 6291469;
		for(int i = 0; i < str.length(); i++) {
			hash = (hash + (str.charAt(i) * prime)) % getMax();
			prime = chenPrimes[i];

		}
		return hash;
	}
	
	public int oldAkrowneHash(String str) {
		// https://www.planetmath.org/goodhashtableprimes
		int somePrimes[] = {53,97,193,389,769,1543,3079,6151,12289,24593,49157,98317,196613,393241,786433,1572869,3145739,6291469,12582917,25165843,50331653,100663319,201326611,402653189,805306457,1610612741};
		//int somePrimes[] = {805306457,402653189,201326611,100663319,50331653,25165843,12582917,6291469,805306457,402653189,201326611,100663319,50331653,25165843,12582917,6291469,805306457,402653189,201326611,100663319,50331653,25165843,12582917,6291469,805306457,402653189,201326611,100663319,50331653,25165843,12582917,6291469,805306457,402653189,201326611,100663319,50331653,25165843,12582917,6291469};
		getDebugObj().reverse(somePrimes,somePrimes.length);
		int hash = 5737;
		int prime = 1610612741; // 6291469;
		for(int i = 0; i < str.length(); i++) {
			//hash = (Math.abs(hash + (str.charAt(i) * prime))) % getMax();
			hash = (Math.abs(hash + (str.charAt(i) * prime))) % getMax();
			prime = somePrimes[i];

		}
		return hash;
	}
	static int akrownePrimesX[] = {1610612741,805306457,402653189,201326611,100663319,50331653,25165843,12582917,6291469,3145739,1572869,786433,393241,196613,98317,49157,24593,12289,6151,3079,1543,769,389,193,97,53};
	
	private int akrowneHash(String key) {
		long hash = 5381;
		int prime = 6291469;
		for(int i = 0; i < key.length(); i++) {
			hash = ((hash << 5) + hash + (prime << 4) + (key.charAt(i)));
			prime = akrownePrimesX[i % akrownePrimesX.length];
		}
		hash &= 0x7fffffff;
		return (int)hash % getCapacity();
	}
	
	public int FNVHash(String str) {
		// https://github.com/ArashPartow/hash/blob/master/GeneralHashFunctions_-_Java/GeneralHashFunctionLibrary.java
		long fnv_prime = 0x811C9DC5;
		fnv_prime &= 0x7fffffff;
		long hash = 0;
	
		for(int i = 0; i < str.length(); i++) {
			hash *= fnv_prime; 
			hash ^= str.charAt(i); // xor operation
		}
		
		hash &= 0x7fffffff;

		return (int) hash % getMax();
	}
	
	public int djb2Hash(String str) {
		// http://www.cse.yorku.ca/~oz/hash.html
		long hash = 5381;
		for(int i = 0; i < str.length(); i++) {
			hash = ((hash << 5) + hash) + str.charAt(i);
		}
		hash &= 0x7fffffff;  // this is a bitmask - if most sig bit is 1, this reverses it //1111111111111111111111111111111 'and' operation  
		return (int) hash % getMax();
	}
	
	public int bHash(String str) {
		// wip binary implementation
		long hash = 805306457;
		BigInteger prime = new BigInteger("805306457");
		for(int i = 0; i < str.length(); i++) {
			hash = ((hash << 5) + hash + (str.charAt(i) * (i+1) * prime.intValue()));
			prime = prime.nextProbablePrime();

		}
		hash &= 0x7fffffff;
		return (int) hash % getMax();
    }
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
	private boolean isPrime(int n) {
		return getPrimes()[n];
	}
	private int nextPrime(int n) {
		int nextPrime = -1;
		if (n >= getPrimes().length -1) { // we start our check at n+1 (n++) so check against primes length -1 (so not out of range);
			//System.out.println("nextPrime() error: nextPrime = -1 ");
			return nextPrime; // error!  No number in the prime array larger than n
		}
		for (n++; n < getPrimes().length; n++) {
			if (isPrime(n)) {
				nextPrime = n;
				break;
			}
		}
		return nextPrime;
	}
	public Pair<V> createNode(String key, V value) {
		return (HashTableDebugger<V>.Pair<V>) new Pair<>(key, value);
	}
	@SuppressWarnings("hiding")
	public class Pair<V> {
		private String key;
		private V value;
		private int bounces;
		public Pair(String key, V value) {
			this.key = key;
			this.value = value;
			this.bounces = getBounceCount();
			resetBounceCount();
		}
		public String getKey() {
			return this.key;
		}
		public V getValue() {
			return this.value;
		}
		public void setValue(V x) {
			this.value = x;
		}
		public int getPairBounceCount() {
			return this.bounces;
		}
	}
	
	public static int goldenRatioProbe(int index,int maxArraySize) {
		// idea from here: https://news.ycombinator.com/item?id=17331568
		// resource: https://demonstrations.wolfram.com/SunflowerSeedArrangements/
		index = ( index + (int) (maxArraySize / ratio)) % maxArraySize;
		return index;
	}

	/*-----------------------------------------------------------------------------------*/
	/*--------------------------------------MAIN()---------------------------------------*/
	/*-----------------------------------------------------------------------------------*/
	
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException {
	
		DEBUG_CLASS.checkGoldenRatioProbe(0,13); // also works for powers of 2
		DEBUG_CLASS.checkQuadraticProbe(0,13);
		
		System.out.println("Starting main()");
		/*---------------------------------------------*/
		/*-------------------PARAMS--------------------*/
		/*---------------------------------------------*/
		boolean shuffle = true;
		int startSize = 4000;
		int words = 255; // max is ~450000
		/*---------------------------------------------*/
		/*---------------------------------------------*/
		/*---------------------------------------------*/
		HashTableDebugger<Integer> myTable = new HashTableDebugger<Integer>(startSize,PROBE_TYPE.GR_PROBE);
		
		// set up file import
		
		String dictionaryPath = myTable.getDebugObj().getDictionaryPath();
		
		String[] dictionaryContents = new String[myTable.getDebugObj().countLines(dictionaryPath)];
		try {
			dictionaryContents = myTable.getDebugObj().returnFile(dictionaryPath);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		if (shuffle) {
			myTable.getDebugObj().shuffle(dictionaryContents);
		}
		
		myTable.getDebugObj().setBucketsPerDiv(myTable.getMax());
		
		// generate some Pairs
		for (int i = 0; i < words; i++) {
			String key = dictionaryContents[i];
			myTable.put(key,i^2);
		}
		long startTime = System.nanoTime();
		
//		for(int i=0;i<2000;i++) {
//			for(int j=2000;j>0;j--) {
//				myTable.put(i+":"+j,i*j);
//			}
//		}
//		
//		for(int i=0;i<2000;i++) {
//			for(int j=2000;j>0;j--) {
//				myTable.get(i+":"+j);
//			}
//		}
		
		long endTime = System.nanoTime();
		long durationInNano = (endTime - startTime);
		long durationInMillis = TimeUnit.NANOSECONDS.toMillis(durationInNano);
		
		myTable.getDebugObj().results(myTable.getTableArray(),myTable.getMax(),myTable.getLoadFactor());
		
		System.out.println("Duration (nanosecs):  " + durationInNano);
	    System.out.println("Duration (millisecs): " + durationInMillis);
	}
}