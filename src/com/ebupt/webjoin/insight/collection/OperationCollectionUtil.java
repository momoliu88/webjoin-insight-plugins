package com.ebupt.webjoin.insight.collection;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;

import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationList;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.operation.SourceCodeLocation;
import com.ebupt.webjoin.insight.intercept.operation.method.JoinPointBreakDown;

public class OperationCollectionUtil {
	public static JoinPointBreakDown breakDown(JoinPoint jp) {
		MethodSignature sig = (MethodSignature) jp.getSignature();
		String declaringTypeName = sig.getDeclaringTypeName();

		return new JoinPointBreakDown(declaringTypeName, jp.getArgs(),
				sig.getParameterTypes(),
				getSourceCodeLocation(jp.getStaticPart()));
	}

	public static JoinPointBreakDown breakDown(JoinPoint jp,
			SourceCodeLocation scl) {
		Signature sig = jp.getSignature();
		String declaringTypeName = sig.getDeclaringTypeName();

		MethodSignature mSig = (MethodSignature) jp.getSignature();
		return new JoinPointBreakDown(declaringTypeName, jp.getArgs(),
				mSig.getParameterTypes(), scl);
	}

	public static SourceCodeLocation getSourceCodeLocation(
			JoinPoint.StaticPart part) {
		Signature mSig = part.getSignature();
		SourceLocation sl = part.getSourceLocation();
		return new SourceCodeLocation(sl.getWithinType().getName(),
				mSig.getName(), sl.getLine());
	}

	public static Operation methodOperation(String className, String fileName,
			int lineNumber, String label, String methodName, String methodSig,
			String shortClassName, String[] args) {
		Operation o = new Operation();
		o.type(OperationType.METHOD)
				.sourceCodeLocation(
						new SourceCodeLocation(className, fileName, lineNumber))
				.label(label).put("methodName", methodName)
				.put("methodSignature", methodSig).put("className", className)
				.put("shortClassName", shortClassName);

		OperationList arguments = o.createList("arguments");
		for (String arg : args) {
			arguments.add(arg);
		}
		return o;
	}
}