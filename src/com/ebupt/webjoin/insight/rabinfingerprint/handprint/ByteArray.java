package com.ebupt.webjoin.insight.rabinfingerprint.handprint;
public class ByteArray {

	final byte[] arr;

	public ByteArray(byte[] b) {
		arr = (byte[]) b.clone();
	}

	public ByteArray() {
		arr = new byte[0];
	}

	public ByteArray(byte b) {
		arr = new byte[] { b };
	}

	public boolean equals(Object o) {
		ByteArray ba = (ByteArray) o;
		return java.util.Arrays.equals(arr, ba.arr);
	}
	public ByteArray subBytes(int start,int index){
		byte[] newarr = new byte[index-start];
		for(int i = start;i< index;i++)
			newarr[i-start]= getAt(i);
		return new ByteArray(newarr);
	}
	public ByteArray subBytes(int index){
		byte[] newarr = new byte[size()-index];
		for(int i = index;i< size();i++)
			newarr[i-index]= getAt(i);
		return new ByteArray(newarr);
	}
	public int hashCode() {
		int code = 0;
		for (int i = 0; i < arr.length; ++i)
			code = code * 2 + arr[i];
		return code;
	}

	public int size() {
		return arr.length;
	}

	public byte getAt(int i) {
		return arr[i];
	}

	public ByteArray conc(ByteArray b2) {
		int sz = size() + b2.size();
		byte[] b = new byte[sz];
		for (int i = 0; i < size(); ++i)
			b[i] = getAt(i);
		for (int i = 0; i < b2.size(); ++i)
			b[i + size()] = b2.getAt(i);
		return new ByteArray(b);
	}

	public ByteArray conc(byte b2) {
		return conc(new ByteArray(b2));
	}

	public byte[] getBytes() {
		return (byte[]) arr.clone();
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public byte getLast() {
		return arr[size() - 1];
	}

	public ByteArray dropLast() {
		byte[] newarr = new byte[size() - 1];
		for (int i = 0; i < newarr.length; ++i)
			newarr[i] = arr[i];
		return new ByteArray(newarr);
	}
	public ByteArray dropFirst() {
		byte[] newarr = new byte[size() - 1];
		for (int i = 1; i < arr.length; ++i)
			newarr[i-1] = arr[i];
		return new ByteArray(newarr);
	}
	public String toString() {
		return new String(arr);
	}
}