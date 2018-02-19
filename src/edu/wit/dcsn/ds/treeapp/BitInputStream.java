/*
 * Scott Austin
 * Comp 2000-03
 * App 6 - Huffman Coding
 * Due: Fr 12/15/2017
 */
package edu.wit.dcsn.ds.treeapp;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An extension of DataInputStream that allows reading bits of data.
 * The bits are read little-endian first. An application uses a bit 
 * output stream to write data that can later be read by a bit input stream.
 * @author Scott Austin
 *
 */
public class BitInputStream extends DataInputStream {

	private int currentByteValue;
	private int nextBit;
	
	/**
	 * Create a BitInputStream that uses the specified underlying InputStream
	 * @param in the specified input stream
	 */
	public BitInputStream(InputStream in) {
		super(in);
		currentByteValue = 0;
		nextBit = 8;
	}
	
	/**
	 * Read a bit from the contained input stream and returns the value
	 * of the bit.
	 * @return 0 or 1 for the value of the bit. Returns -1 if the end of the stream
	 * has been reached
	 * @throws IOException if the read operation fails.
	 */
	public int readBit() throws IOException {
		if (nextBit == 8) {
			currentByteValue = super.read();
			nextBit = 0;
		}
		return (currentByteValue == -1) ? -1 : (currentByteValue >> nextBit++) & 1;
	}
}
