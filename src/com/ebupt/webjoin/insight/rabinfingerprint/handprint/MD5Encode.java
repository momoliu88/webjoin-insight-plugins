package com.ebupt.webjoin.insight.rabinfingerprint.handprint;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MD5Encode {
	public static byte[] md5String(String s){
		Long t1 = System.currentTimeMillis();
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(s.getBytes());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println("md5 time "+(System.currentTimeMillis()-t1));

		return md.digest();
	}
}
