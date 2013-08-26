package com.ebupt.webjoin.insight.LZW;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BitOutputStream extends FilterOutputStream {
	class BitManager {
		int buf = 0;
		int cnt = 0;

		int writeOne(int next) {
			int ret = -1;
			buf = buf * 2 + next;
			cnt++;
			if (cnt == 7) {
				cnt = 0;
				ret = buf;
				buf = 0;
			} else {
				ret = -1;
			}
			return ret;
		}

		int writeLast() {
			int x = 0;
			for (int i = 0; i < 7 - cnt; ++i)
				x = x * 2 + 1;
			for (int i = 7 - cnt; i < 8; ++i)
				x = x * 2;
			return buf | x;
		}
	}

	BitManager bitManager = new BitManager();

	public BitOutputStream(OutputStream os) {
		super(os);
	}

	public void write(int i) throws IOException {
//		System.out.println("write "+i);
		int x = bitManager.writeOne(i >= 1 ? 1 : 0);
		if (x >= 0)
			out.write(x);
	}

	public void write(byte[] arr) throws IOException {
		write(arr, 0, arr.length);
	}

	public void write(byte[] arr, int off, int len) throws IOException {
		int clen = 0;
		for (int i = 0; i < len; ++i) {
			int x = bitManager.writeOne(arr[off + i]);
			if (x >= 0)
				arr[off + (clen++)] = (byte) x;
		}
		out.write(arr, off, clen);
	}

	public void close() throws IOException {
		out.write(bitManager.writeLast());
		out.close();
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Usage: java BitOutputStream FromFile ToFile");
			System.out
					.println("where 'FromFile' includes characters of '0' and '1'");
			System.out.println("and they are written as bits into 'ToFile'");
			System.exit(1);
		}

		try {
			InputStream is = new BufferedInputStream(new FileInputStream(
					args[0]));
			OutputStream os = new BitOutputStream(new BufferedOutputStream(
					new FileOutputStream(args[1])));
			int next;
			while ((next = is.read()) >= 0) {
				char ch = (char) next;
				if (ch == '0' || ch == '1')
					os.write((int) (ch - '0'));
			}
			is.close();
			os.close();
		} catch (FileNotFoundException fnfe) {
			System.out.println(args[0] + " file not found");
			System.exit(1);
		} catch (IOException ioe) {
			System.out.println("Error in reading file " + args[0]
					+ " or writing file " + args[1]);
			System.exit(1);
		}
	}
}
