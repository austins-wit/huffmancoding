/*
 * Scott Austin
 * Comp 2000-03
 * App 6 - Huffman Coding
 * Due: Fr 12/15/2017
 */
package edu.wit.dcsn.ds.treeapp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * This is a Huffman Tree used to compress a data file using
 * the Huffman Coding algorithm
 * @author Scott Austin
 *
 */
public class HuffmanTree {

	private HuffmanNode root;
	
	/**
	 * Create an empty HuffmanTree. 
	 */
	public HuffmanTree() {
		root = null;
	}
	
	/**
	 * Use Huffman Coding algorithm to compress a text file and write the encoded
	 * file to another file. After this method is called, this object will contain
	 * the huffman tree used to map codes to each symbol.
	 * @param textFile the text file to be compressed.
	 * @param encodedFile the encoded file after huffman coding has been applied. Note
	 * that the symbol code is not stored in this file; to decode this file, you must
	 * use this object! 
	 * @throws IOException if either textFile or encodedFile cannot be opened
	 */
	public void encode(File textFile, File encodedFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(textFile));
		ArrayList<Entry> characterFrequencies = new ArrayList<Entry>();
		int next;
		
		// finds frequency of each symbol in the input file
		while ( (next = in.read()) != -1 ) {
			Entry entry = new Entry((char)next);
			int index = characterFrequencies.indexOf(entry);
			if (index < 0) {
				characterFrequencies.add(entry);
			}
			else {
				entry = characterFrequencies.get(index);
				entry.setFrequency( entry.getFrequency() + 1 );
			}
		}
		in.close();
		
		System.out.println("Character Frequencies of file " + textFile.getName());
		System.out.println(characterFrequencies.toString().replaceAll(", ", "\n"));
		System.out.println("\n");

		// builds the huffman tree using algorithm specified in the book 
		PriorityQueue<HuffmanNode> nodeQueue = new PriorityQueue<HuffmanNode>();
		for (Entry entry : characterFrequencies) {
			nodeQueue.add(new HuffmanNode(entry));
		}
		while (nodeQueue.size() > 1) {
			HuffmanNode firstNode = nodeQueue.poll();
			HuffmanNode secondNode = nodeQueue.poll();
			HuffmanNode newNode = new HuffmanNode(
					new Entry(null, firstNode.getEntry().getFrequency() + firstNode.getEntry().getFrequency()),
					firstNode,
					secondNode);
			nodeQueue.add(newNode);
		}
		root = nodeQueue.poll();
		
		HashMap<Character, String> codeTable = new HashMap<Character, String>();
		StringBuilder codeBuilder = new StringBuilder();
		buildCodeTableInOrder(root, codeTable, codeBuilder);
		
		System.out.println("Character encoded bit-strings:");
		System.out.println(codeTable.toString().replaceAll(", ", "\n"));
		
		// write the compressed file using the code table
		in = new BufferedReader(new FileReader(textFile));
		BitOutputStream out = new BitOutputStream(new FileOutputStream(encodedFile));
		while ( (next = in.read()) != -1 ) {
			String code = codeTable.get((char)next);
			out.writeBitString(code);
		}
		
		in.close();
		out.close();
	}
	
	/**
	 * Use this huffman tree to decode a file compressed by the huffman coding algorithm.
	 * @param encodedFile the file to be decoded. The file must have been encoded by this tree object
	 * or the resulting file will not be correct.
	 * @param decodedFile the output file, after the input file is decoded
	 * @throws IOException if either file cannot be read.
	 */
	public void decode (File encodedFile, File decodedFile) throws IOException {
		BitInputStream in = new BitInputStream(new BufferedInputStream(new FileInputStream(encodedFile)));
		BufferedWriter out = new BufferedWriter(new FileWriter(decodedFile));
		
		int next;
		HuffmanNode currentNode = root;
		while ( (next = in.readBit()) != -1 ) {
			if (next == 0) {
				currentNode = currentNode.getLeftChild();
			}
			else {
				currentNode = currentNode.getRightChild();
			}
			
			if (!hasChildren(currentNode)) {
				out.write(currentNode.getEntry().getSymbol());
				currentNode = root;
			}
		}
		
		in.close();
		out.close();
	}
	
	/**
	 * Recursively builds a hashmap of character symbols and their associated bit strings.
	 * @param currentRoot the root of the current subtree
	 * @param codeTable the symbol-code table being built
	 * @param codeBuilder the current bit string. The initial call must have an empty string.
	 */
	private void buildCodeTableInOrder(HuffmanNode currentRoot, HashMap<Character, String> codeTable, StringBuilder codeBuilder) {
		if (!hasChildren(currentRoot)) {
			codeTable.put(currentRoot.getEntry().getSymbol(), codeBuilder.toString());
		}
		else {
			if (currentRoot.getLeftChild() != null) {
				codeBuilder.append("0");
				buildCodeTableInOrder(currentRoot.getLeftChild(), codeTable, codeBuilder);
				codeBuilder.deleteCharAt(codeBuilder.length()-1);
			}
			if (currentRoot.getRightChild() != null) {
				codeBuilder.append("1");
				buildCodeTableInOrder(currentRoot.getRightChild(), codeTable, codeBuilder);
				codeBuilder.deleteCharAt(codeBuilder.length()-1);
			}
		}
	}
	
	/**
	 * Check if a node has children
	 * @param node the node to check
	 * @return true if the node has children, false otherwise.
	 */
	private boolean hasChildren(HuffmanNode node) {
		return node.getLeftChild() != null || node.getRightChild() != null;
	}
	
	/**
	 * A node in a huffman tree.
	 * @author Scott Austin
	 *
	 */
	private class HuffmanNode implements Comparable<HuffmanNode> {
		
		private final Entry entry;
		private final HuffmanNode leftChild;
		private final HuffmanNode rightChild;
		
		/**
		 * Create a HuffmanNode with default entry and no children
		 */
		private HuffmanNode() {
			this(new Entry(), null, null);
		}
		
		/**
		 * Create a HuffmanNode with an entry and no children
		 * @param entry an entry in a huffman tree
		 */
		private HuffmanNode(Entry entry) {
			this(entry, null, null);
		}
		
		/**
		 * Create a HuffmanNode with an entry and children
		 * @param entry an entry in a huffman tree
		 * @param leftChild the left child of this node
		 * @param rightChild the right child of this node
		 */
		private HuffmanNode(Entry entry, HuffmanNode leftChild, HuffmanNode rightChild) {
			this.entry = entry;
			this.leftChild = leftChild;
			this.rightChild = rightChild;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(HuffmanNode other) {
			return this.entry.compareTo(other.entry);
		}
		
		/**
		 * Get the entry of this node
		 * @return this node's entry
		 */
		private Entry getEntry() {
			return entry;
		}
		
		/**
		 * Get the left child of this node
		 * @return this node's left child
		 */
		private HuffmanNode getLeftChild() {
			return leftChild;
		}
		
		/**
		 * Get the right child of this node
		 * @return this node's right child
		 */
		private HuffmanNode getRightChild() {
			return rightChild;
		}
	}
	
	/**
	 * An entry in a huffman tree. Has a character symbol and a frequency
	 * denoting how often the symbol appears in the data.
	 * @author Scott Austin
	 *
	 */
	private class Entry implements Comparable<Entry> {
		
		private final Character symbol;
		private int frequency;
		
		/**
		 * Create an entry with a null symbol and frequency of 1
		 */
		private Entry() {
			this(null, 1);
		}
		
		/**
		 * Create an entry with a symbol and a frequency of 1
		 * @param symbol this entry's symbol
		 */
		private Entry(Character symbol) {
			this(symbol, 1);
		}
		
		/**
		 * Create an entry with a symbol and a frequency
		 * @param symbol this entry's symbol
		 * @param frequency this entry's frequency
		 */
		private Entry(Character symbol, int frequency) {
			this.symbol = symbol;
			this.frequency = frequency;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (other == null) {
				return false;
			}
			
			if ( other.getClass().equals( this.getClass() ) ) {
				Entry otherEntry = (Entry)other;
				return this.symbol.equals(otherEntry.symbol);
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Entry other) {
			return this.frequency - other.frequency;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return symbol.toString() + ": " + String.valueOf(frequency);
		}
		
		/**
		 * Gets the symbol of this entry
		 * @return this entry's symbol
		 */
		private char getSymbol() {
			return symbol;
		}
		
		/**
		 * Gets the frequency of this entry
		 * @return this entry's frequency
		 */
		private int getFrequency() {
			return frequency;
		}
		
		/**
		 * Sets the frequency of this entry
		 * @param frequency the new frequency
		 */
		private void setFrequency(int frequency) {
			this.frequency = frequency;
		}
	}
	
	/**
	 * Unit test driver
	 * @param args -unused-
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		HuffmanTree encoder = new HuffmanTree();
		File testInFile = new File("data/Liang-Intro_Java_11ed-01_01.txt");
		File testEncodeFile = new File("compressed.out");
		File testDecodeFile = new File("uncompressed.txt");
		
		encoder.encode(testInFile, testEncodeFile);
		encoder.decode(testEncodeFile, testDecodeFile);
	}
}
