package com.ebupt.webjoin.insight.intercept.operation;

import java.util.Map;

public interface OperationFinalizer {
	public void finalize(Operation paramOperation,
			Map<String, Object> paramMap);
}