/*
 * Scott Austin
 * Comp 2000-03
 * App 6 - Huffman Coding
 * Due: Fr 12/15/2017
 */
package edu.wit.dcsn.ds.treeapp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An extension of DataOutputStream that allows writing bits
 * and bit strings in binary form. The bits are written to bytes 
 * in little endian form. An application can retrieve the 
 * bit data by using BitInputStream
 * @author Scott Austin
 *
 */
public class BitOutputStream extends DataOutputStream {

	private int currentByteValue;
	private int nextBit;
	
	/**
	 * Create a new bit output stream to write bits to the specified
	 * underlying output stream.
	 * @param out the underlying output stream
	 */
	public BitOutputStream(OutputStream out) {
		super(out);
		currentByteValue = 0;
		nextBit = 0;
	}
	
	/**
	 * Write a bit value to the output stream.
	 * @param bitValue the bit value to be written. Valid inputs are 0 or 1.
	 * Supplying invalid inputs produce undefined results.
	 * @throws IOException if the write function fails.
	 */
	public void writeBit(int bitValue) throws IOException {
		currentByteValue += bitValue << nextBit++;
		if (nextBit == 8) {
			super.writeByte(currentByteValue);
			currentByteValue = 0;
			nextBit = 0;
		}
	}
	
	/**
	 * Write a bit string to the output stream.
	 * @param bitString the bit string to be written. Valid characters in
	 * the string are '0' or '1'. Supplying invalid inputs produce undefined results.
	 * @throws IOException if the write function fails.
	 */
	public void writeBitString(String bitString) throws IOException {
		for (int i = 0; i < bitString.length(); ++i) {
			writeBit(bitString.charAt(i) - '0');
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.io.FilterOutputStream#close()
	 */
	public void close() throws IOException {
		if (nextBit != 0) {
			super.writeByte(currentByteValue);
			currentByteValue = 0;
			nextBit = 0;
		}
		super.close();
	}
}
