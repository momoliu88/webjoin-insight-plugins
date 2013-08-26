package com.ebupt.webjoin.insight.LZW;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public 
interface Compression {

	void compress(InputStream inp, OutputStream out) throws IOException;

	void decompress(InputStream inp, OutputStream out) throws IOException;

}