package com.ebupt.webjoin.insight.LZW;

import com.ebupt.webjoin.insight.rabinfingerprint.handprint.ByteArray;

public class LimitedDict extends Dict {
	int maxSize;

	LimitedDict(int maxSize) {
		this.maxSize = maxSize;
	}

	public void add(ByteArray str) {
		if (size() < maxSize)
			super.add(str);
	}
}