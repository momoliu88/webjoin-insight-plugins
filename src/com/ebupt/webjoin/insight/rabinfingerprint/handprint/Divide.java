package com.ebupt.webjoin.insight.rabinfingerprint.handprint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import  org.apache.commons.codec.binary.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import com.ebupt.webjoin.insight.Insight;
import com.ebupt.webjoin.insight.json.InsightJsonArray;
import com.ebupt.webjoin.insight.rabinfingerprint.fingerprint.RabinFingerprintLongWindowed;

public class Divide {
	private static Boolean checkCondition(Long num) {
//		System.out.println("magic value 255");
		return (num & (255)) == 0;
	}
	private static String encodeBase64(ByteArray bytes){
		return new String((new Base64()).encode(bytes.getBytes()));
		
	}
	public static Map<ByteArray,String> storage = new HashMap<ByteArray,String>();
	
	public static InsightJsonArray slideWindow(byte[] bytes,RabinFingerprintLongWindowed window ) throws IOException, JSONException {
	//	StringBuilder ret = new StringBuilder();
		InsightJsonArray ret = new InsightJsonArray();
		// Create new random irreducible polynomial
		// These can also be created from Longs or hex Strings
//		Polynomial polynomial = Polynomial.createIrreducible(53);

		// Create a windowed fingerprint object with a window size of 48 bytes.
//		Long t = System.currentTimeMillis();
		
//		System.out.println("window time "+(System.currentTimeMillis()-t));
		window.reset();
		int offset = 0 ;
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			// Push in one byte. Old bytes are automatically popped.
//			Long t1 = System.currentTimeMillis();
			window.pushByte(b);
//			System.out.println("pushbyte time "+(System.currentTimeMillis()-t1));

			sb.append((char)b);
			if (checkCondition(window.getFingerprintLong())) {
				//find and mark
				ByteArray byteArr = new ByteArray(MD5Encode.md5String(sb.toString()));
				JSONObject obj = new JSONObject();
			//	ret.append(offset).append('/').append(sb.length()).append('/');
				obj.put("o", offset);
				obj.put("l",sb.length());
				if(!storage.containsKey(byteArr))
				{
					obj.put("f", 0);
					obj.put("m",sb.toString());
					//ret.append(0).append('/').append(sb.toString()).append(';');
					storage.put(byteArr, new String(sb.toString()));
				}
				else 
				{
					obj.put("f", 1);
					obj.put("m",encodeBase64(byteArr));
//					ret.append(1).append('/').append(encodeBase64(byteArr)).append(';');
				}
				ret.put(obj);
				offset += sb.length();
//				System.out.println("offset "+offset);
				sb = new StringBuilder();
			}
		}
//		System.out.println("ret in divide "+ret);
		return ret;
	}
	
//		try {
//			PrintStream ps = new PrintStream(new FileOutputStream("log"));
//			System.setOut(ps);
//			Polynomial poly =  Polynomial.createIrreducible(53);
//			RabinFingerprintLongWindowed window = new RabinFingerprintLongWindowed(
//					poly, 48);
//			String ret = Divide.slideWindow(new FileInputStream("file.text"),window);
//			System.out.println(ret);
//			window.reset();
//			Long t1 = System.currentTimeMillis();
//			ret = Divide.slideWindow(new FileInputStream("file.text"),window);
//			System.out.println("pass time "+(System.currentTimeMillis()-t1));
//			System.out.println(ret);
//			System.out.println(Divide.storage.toString());
//		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (JSONException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

}
