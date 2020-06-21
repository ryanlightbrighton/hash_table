package ci583.htable.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import ci583.htable.impl.HashTable;

class TestHashX<T, V> extends HashTable<T, V>{
	public TestHashX(int initialCapacity, PROBE_TYPE pt) {
        super(initialCapacity, pt);
    }

    public TestHashX(int initialCapacity) {
        super(initialCapacity);
    }
}

public class TestHT {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testEmpty() {
		TestHashX<String, Boolean> h = new TestHashX<String, Boolean>(100);
		assertNull(h.get("foo"));
	}
	
	@Test
	public void testNotFound() {
		TestHashX<String, Boolean> h = new TestHashX<String, Boolean>(100);
		h.put("yes", true);
		assertNull(h.get("no"));
	}
	
	@Test
	public void testInsert() {
		TestHashX<String, Boolean> h = new TestHashX<String, Boolean>(1000, TestHashX.PROBE_TYPE.DOUBLE_HASH);
		for(int i=0;i<2000;i++) {
			for(int j=2000;j>0;j--) {
				h.put(i+":"+j, true);
			}
		}
		
		for(int i=0;i<2000;i++) {
			for(int j=2000;j>0;j--) {
				assertTrue(h.hasKey(i+":"+j));
			}
		}
	}
	
	@Test
	public void testGet() {
		TestHashX<String, String> h = new TestHashX<String, String>(9);
		for(int i=0;i<10;i++) {
			for(int j=10;j>0;j--) {
				h.put(i+":"+j, j+":"+i);
			}
		}
				
		for(int i=0;i<10;i++) {
			for(int j=10;j>0;j--) {
				assertEquals(h.get(i+":"+j), j+":"+i);
			}
		}
	}
	
	@Test
	public void testNull() {
		TestHashX<String, Integer> h = new TestHashX<String, Integer>(20);
		for(int i=0;i<10;i++) h.put(Integer.valueOf(i).toString(), Integer.valueOf(i));
		assertNull(h.get(11+""));
	}

	@Test
	public void testCapacity() {
		TestHashX<String, Integer> h = new TestHashX<String, Integer>(20, TestHashX.PROBE_TYPE.LINEAR_PROBE);
		assertEquals(h.getCapacity(), 23);//23 is smallest prime > 20
		for(int i=0;i<20;i++) h.put(Integer.valueOf(i).toString(), Integer.valueOf(i));
		assertFalse(h.getCapacity() == 23);//should have resized
		assertFalse(h.getLoadFactor() > 0.6);
	}
	
	@Test
	public void testKeys() {
		TestHashX<String, Integer> h = new TestHashX<String, Integer>(20, TestHashX.PROBE_TYPE.LINEAR_PROBE);
		h.put("bananas", 1);
		h.put("pyjamas", 99);
		h.put("kedgeree", 1);
		for(String k: h.getKeys()) {
			assertTrue(k.equals("bananas") || k.equals("pyjamas") || k.equals("kedgeree"));
		}
	}
	@Test
	public void NEW_testInsertEightMil_DblHashProbe() {
		TestHashX<String, Boolean> h = new TestHashX<String, Boolean>(1000, TestHashX.PROBE_TYPE.DOUBLE_HASH);
		for(int i=0;i<2829;i++) {
			for(int j=2829;j>0;j--) {
				h.put(i+":"+j, true);
			}
		}
		
		for(int i=0;i<2829;i++) {
			for(int j=2829;j>0;j--) {
				assertTrue(h.hasKey(i+":"+j));
			}
		}
	}
	@Test
	public void NEW_testInsertEightMil_GoldenRatioProbe() {
		TestHashX<String, Boolean> h = new TestHashX<String, Boolean>(1000, TestHashX.PROBE_TYPE.GR_PROBE);
		for(int i=0;i<2829;i++) {
			for(int j=2829;j>0;j--) {
				h.put(i+":"+j, true);
			}
		}
		
		for(int i=0;i<2829;i++) {
			for(int j=2829;j>0;j--) {
				assertTrue(h.hasKey(i+":"+j));
			}
		}
	}
	@Test
	public void NEW_testInsertEightMil_QuadraticProbe() {
		TestHashX<String, Boolean> h = new TestHashX<String, Boolean>(1000, TestHashX.PROBE_TYPE.QUADRATIC_PROBE);
		for(int i=0;i<2829;i++) {
			for(int j=2829;j>0;j--) {
				h.put(i+":"+j, true);
			}
		}
		
		for(int i=0;i<2829;i++) {
			for(int j=2829;j>0;j--) {
				assertTrue(h.hasKey(i+":"+j));
			}
		}
	}
	@Test
	public void NEW_testInsertRandomIntegerKeys() {
		// need to save these objects to an array list for retrieval
		ArrayList<Integer> coll = new ArrayList<>();
		Random randomObj = new Random();
		TestHashX<Integer, Boolean> h = new TestHashX<Integer, Boolean>(1000, TestHashX.PROBE_TYPE.GR_PROBE);
		for(int i = 0; i < 4000000; i++) {
			int rand = randomObj.nextInt(4000000 - 0 + 1) + 0;
			coll.add(rand);
			h.put(rand, true);
		}
		
		for(int i = 0; i < 4000000; i++) {
			int retrieveNumb = coll.get(i);
			assertTrue(h.hasKey(retrieveNumb));
		}
	}
	@Test
	public void NEW_testInsertObjectKeys() {
		// need to save these objects to an array list for retrieval
		ArrayList<Object> coll = new ArrayList<>();
		TestHashX<Object, Boolean> h = new TestHashX<Object, Boolean>(1000, TestHashX.PROBE_TYPE.GR_PROBE);
		for(int i = 0; i < 4000000; i++) {
			Object insertObj = new Object();
			coll.add(insertObj);
			h.put(insertObj, true);
		}
		
		for(int i = 0; i < 4000000; i++) {
			Object retrieveObj = coll.get(i);
			assertTrue(h.hasKey(retrieveObj));
		}
	}
}