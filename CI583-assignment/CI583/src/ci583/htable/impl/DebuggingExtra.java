package ci583.htable.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import ci583.htable.impl.HashTableDebugger.Pair;

public class DebuggingExtra {
	
	public DebuggingExtra() {
		setDictionaryPath(System.getProperty("user.dir") + "\\words_1000000.txt");
		setOutputDistPath(System.getProperty("user.dir") + "\\results\\distribution.csv");
		setOutputCollPath(System.getProperty("user.dir") + "\\results\\collision_vs_load.csv");
		setOutputLoadPath(System.getProperty("user.dir") + "\\results\\load_at_insert.csv");
	}
	
	//collision counting code
	
	private int divisions = 100;
	private int bucketsPerDiv;
	private int distribution[] = new int[divisions];
	private ArrayList<Double> collisionList = new ArrayList<>();
	private ArrayList<Double> loadVsHashList = new ArrayList<>();
	private int collCount = 0;
	private int resizeCount = 0;
	private boolean listMem = false;
	
	public int getDivisions() {
		return this.divisions;
	}
	
	public void setBucketsPerDiv(int buckets) {
		this.bucketsPerDiv = buckets / getDivisions();
	}
	
	public void incrementCollCount() {
		this.collCount++;
	}
	
	public int getCollCount() {
		return this.collCount;
	}
	
	public void incrementResizeCount() {
		this.resizeCount++;
	}
	
	public int getResizeCount() {
		return this.resizeCount;
	}
	
	public void pushCollisionList(double loadFactor) {
		this.collisionList.add(loadFactor);
	}
	public void pushLoadVsHashList(double loadFactor) {
		this.loadVsHashList.add(loadFactor);
	}
	
	public void addToDistribution(int value) {
		int tmpnum = distribution[value % getDivisions()];
		distribution[value % getDivisions()] = tmpnum + 1;	
	}
	
	public void results(Pair[] table,int max,double load) throws IOException  {
		int counter = 0;
		for (int i = 0; i < table.length; i++) {
			if (table[i] instanceof Pair) {
				counter++;
			}
		}
		System.out.println("Resize count: " + getResizeCount());
		System.out.println("Collision count: " + getCollCount());
		System.out.println("Full Buckets: " + counter);
		System.out.println("Total Operations: " + (counter + getCollCount()));
		double success =  Math.round(((double) counter / (counter + getCollCount())) * 100);
		System.out.println("Success rate: " + success + "%");
		System.out.println("Buckets (max): " + max);
		System.out.println("Array length: " + (table.length));
		outputDistFile(distribution);
		outputCollFile(collisionList);
		outputLoadVsHashFile(loadVsHashList);
		System.out.println("Load factor: " + load);
		countFrequencies(table);
		
		if (listMem) {
			// https://stackoverflow.com/questions/74674/how-do-i-check-cpu-and-memory-usage-in-java
			Runtime runtime = Runtime.getRuntime();
			NumberFormat format = NumberFormat.getInstance();
			long maxMemory = runtime.maxMemory();
			long allocatedMemory = runtime.totalMemory();
			long freeMemory = runtime.freeMemory();
	
			System.out.println("free memory: " + format.format(freeMemory / 1024));
			System.out.println("allocated memory: " + format.format(allocatedMemory / 1024));
			System.out.println("max memory: " + format.format(maxMemory / 1024));
			System.out.println("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));
			//checkQuad(0,7);
			//checkQuad(0,11);
		}		
	}
	//////////////
	
	private String dictionary_path;
	private String output_path_dist;
	private String output_path_coll;
	private String output_path_load;
	
	public String getDictionaryPath() {
		return this.dictionary_path;
	}
	public void setDictionaryPath(String filePath) {
		this.dictionary_path = filePath;
	}
	public String getOutputDistPath() {
		return this.output_path_dist;
	}
	public void setOutputDistPath(String filePath) {
		this.output_path_dist = filePath;
	}
	public String getOutputCollPath() {
		return this.output_path_coll;
	}
	public void setOutputCollPath(String filePath) {
		this.output_path_coll = filePath;
	}
	public String getOutputLoadPath() {
		return this.output_path_load;
	}
	public void setOutputLoadPath(String filePath) {
		this.output_path_load = filePath;
	}
	
	public void outputDistFile(int[] contents) throws IOException {
		FileWriter fw = new FileWriter(getOutputDistPath());
		PrintWriter out = new PrintWriter(fw);
		
		//out.print("Divisions");
		//out.print(",");
		out.println("Attempts");
		for (int i = 0; i < contents.length; i++) {
			//out.print("" + i);
			//out.print(",");
			out.println("" + contents[i]);
		}
		//out.println("" + bucketsPerDiv);
		out.close();
		fw.close();
	}
	
	public void outputCollFile(ArrayList<Double> contents) throws IOException {
		FileWriter fw = new FileWriter(getOutputCollPath());
		PrintWriter out = new PrintWriter(fw);
		out.println("Load at collision");
		for (int i = 0; i < contents.size(); i++) {
			out.println("" + contents.get(i));
		}
		out.close();
		fw.close();
	}
	
	public void outputLoadVsHashFile(ArrayList<Double> contents) throws IOException {
		FileWriter fw = new FileWriter(getOutputLoadPath());
		PrintWriter out = new PrintWriter(fw);
		out.println("Load at insert");
		for (int i = 0; i < contents.size(); i++) {
			out.println("" + contents.get(i));
		}
		out.close();
		fw.close();
	}
	
	public String[] returnFile(String path) throws IOException {
		FileReader reader = new FileReader(path);
		BufferedReader textReader = new BufferedReader(reader);
		int numberLines = countLines(path);
		String[] contents = new String[numberLines];
		for (int i=0; i< numberLines; i++) {
			contents[i] = textReader.readLine();
		}
		textReader.close();
		return contents;
	}
	public int countLines(String path) throws IOException {
		FileReader reader = new FileReader(path);
		BufferedReader textReader = new BufferedReader(reader);
		int count = 0;
		while (textReader.readLine() != null) {
			count++;
		}
		textReader.close();
		return count;
	}
	
	// shuffle array
	public void shuffle(String[] array) {
		// https://www.journaldev.com/32661/shuffle-array-java
		Random rand = new Random();
		for (int i = 0; i < array.length; i++) {
			int randomIndexToSwap = rand.nextInt(array.length);
			String temp = array[randomIndexToSwap];
			array[randomIndexToSwap] = array[i];
			array[i] = temp;
		}
	}
	
	// reverse array
	
	public void reverse(int a[], int n) {
		// https://www.geeksforgeeks.org/reverse-an-array-in-java/
        int i, k, t; 
        for (i = 0; i < n / 2; i++) { 
            t = a[i]; 
            a[i] = a[n - i - 1]; 
            a[n - i - 1] = t; 
        } 
    } 
	
	// map to check results
	
	public static void countFrequencies(ArrayList<Integer> list) {
		// code lifted from here: https://www.geeksforgeeks.org/count-occurrences-elements-list-java/
        // hash map to store the frequency of element 
        Map<Integer, Integer> hm = new HashMap<Integer, Integer>(); 
        for (int i : list) { 
            Integer j = hm.get(i); 
            hm.put(i, (j == null) ? 1 : j + 1); 
        }
        // displaying the occurrence of elements in the array list 
        for (Map.Entry<Integer, Integer> val : hm.entrySet()) { 
            System.out.println(
	    		"Element " + val.getKey() + " "
	            + "occurs"
	            + ": " + val.getValue() + " times"
            ); 
        } 
    }
	
	public static void countFrequencies(Pair[] list) {
		// code lifted from here: https://www.geeksforgeeks.org/count-occurrences-elements-list-java/
        // hash map to store the frequency of element 
        Map<Integer, Integer> hm = new HashMap<Integer, Integer>(); 
        for (int p = 0; p < list.length; p++) {
        	if (list[p] instanceof Pair) {
	        	int i = list[p].getPairBounceCount();
	            Integer j = hm.get(i); 
	            hm.put(i, (j == null) ? 1 : j + 1);
        	}
        }
        // displaying the occurrence of elements in the array list 
        for (Map.Entry<Integer, Integer> val : hm.entrySet()) { 
            System.out.println(
	    		"Bounce total " + val.getKey() + " "
	            + "occurs"
	            + ": " + val.getValue() + " times"
            ); 
        } 
    }
	
	// check golden ratio probe indices
	
	public static void checkGoldenRatioProbe(int index, int tableSize) {
		ArrayList<Integer> checkList = new ArrayList<>();
		for (int i = 0; i < 10000; i++) {
			index = HashTableDebugger.goldenRatioProbe(index,tableSize);
			checkList.add(index);
		}
		System.out.println("GOLDEN RATIO PROBE RESULTS");
		countFrequencies(checkList);
	}
		
	// check quadratic probing indexes
	
	public static void checkQuadraticProbe(int index, int tableSize) {
		ArrayList<Integer> checkList = new ArrayList<>();
		for (int i = 0; i < 10000; i++) {
			int newIndex = (index + (i * i)) % tableSize;
			checkList.add(newIndex);
		}
		System.out.println("QUADRATIC PROBE RESULTS");
		countFrequencies(checkList);
	}
}
