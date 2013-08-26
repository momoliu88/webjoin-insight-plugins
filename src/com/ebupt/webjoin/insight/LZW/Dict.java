package com.ebupt.webjoin.insight.LZW;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ebupt.webjoin.insight.rabinfingerprint.handprint.ByteArray;

public class Dict {
	private final Integer STEP = 200000;
	public Map<ByteArray,Code> mp = new HashMap<ByteArray,Code>();
	public Map<ByteArray,Code> wordMp = new HashMap<ByteArray,Code>();

	public List<ByteArray> ls = new ArrayList<ByteArray>();
	public List<ByteArray> words = new ArrayList<ByteArray>();;
	public void add(ByteArray str) {
		add(str,Boolean.FALSE);
	}
	public void add(ByteArray str,Boolean flag) {
//		int code =  new Integer(ls.size());
		if(numFromStr(str)!=-1) return;
		
		if(flag == Boolean.FALSE){
			mp.put(str,new Code(new Integer(ls.size()),flag));
			System.out.println("add to mp:[key]"+str+"[code]"+(ls.size()));

			ls.add(str);

		}
		else{
			wordMp.put(str,new Code(new Integer(words.size()+STEP),flag));
			System.out.println("add to words:[key]"+str+"[code]"+wordMp.get(str).getCode());

			words.add(str);
		}
	}
	public final int numFromMap(ByteArray str){
		return (mp.containsKey(str) ? ( mp.get(str).getCode().intValue()): -1);
	}
	public final int numFromStr(ByteArray str) {
		if(mp.containsKey(str))
			return mp.get(str).getCode().intValue();
		else if(wordMp.containsKey(str))
			return wordMp.get(str).getCode().intValue();
		else 
			return -1;
	//s	return (mp.containsKey(str) ? ( mp.get(str).getCode().intValue()): -1);
	}
	public final Boolean getCodeFlag(ByteArray str){
		
		if(mp.containsKey(str))
			return Boolean.FALSE;
		else if(wordMp.containsKey(str))
			return Boolean.TRUE;
		else 
			return Boolean.FALSE;
//		return (mp.containsKey(str) ? ( mp.get(str).getFlag()):Boolean.FALSE);
	}
	public final ByteArray strFromNum(int i) {
//		System.out.println("strFromNum: "+i);
		if(i<STEP)
			return (i < ls.size() ? (ByteArray) ls.get(i) : null);
		else{
			i -= STEP;
			return (i< words.size() ? (ByteArray) words.get(i) : null);
		}
	}

	public final int size() {
		return ls.size()+words.size();
	}
}