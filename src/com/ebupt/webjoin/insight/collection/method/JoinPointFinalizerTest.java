package com.ebupt.webjoin.insight.collection.method;


import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationFields;
import com.ebupt.webjoin.insight.intercept.operation.OperationList;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.gemstone.bp.edu.emory.mathcs.backport.java.util.Arrays;

import junit.framework.TestCase;

public class JoinPointFinalizerTest extends TestCase {
	JoinPointFinalizer finalizer = new JoinPointFinalizer();
	Operation op = new Operation();
 	protected void setUp() throws Exception {
		super.setUp();
		op.label("test");
		op.type(OperationType.valueOf("test"));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testStringifyArguments() {
		Object[] args ={"123",12,"op"};
		OperationList list = op.createList(OperationFields.ARGUMENTS);
		list.addAll(Arrays.asList(args));
		
		finalizer.stringifyArguments(op, args);
		list = op.get(OperationFields.ARGUMENTS,OperationList.class);
		for(int i = 0 ;i < args.length;i++)
		{
			assertEquals(list.get(i),String.valueOf(args[i]));
		}
	}

}
