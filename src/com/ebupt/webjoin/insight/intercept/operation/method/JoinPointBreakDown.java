package com.ebupt.webjoin.insight.intercept.operation.method;

import com.ebupt.webjoin.insight.intercept.operation.SourceCodeLocation;

public class JoinPointBreakDown {
	private String declaringTypeName;
	private Object[] args;
	private Class<?>[] argTypes;
	private SourceCodeLocation sourceLocation;

	public JoinPointBreakDown(String declaringTypeName, Object[] args,
			Class<?>[] argTypes, SourceCodeLocation sourceLocation) {
		this.declaringTypeName = declaringTypeName;
		this.args = args;
		this.argTypes = argTypes;
		this.sourceLocation = sourceLocation;
	}

	public String getMethodString() {
		return getMethodStringFromArgs(this.sourceLocation, this.argTypes);
	}

	public static String getMethodStringFromArgs(
			SourceCodeLocation sourceLocation, Class<?>[] argTypes) {
		StringBuilder methodString = new StringBuilder(
				sourceLocation.getMethodName());
		methodString.append("(");
		int size = argTypes.length;
		for (int i = 0; i < size; i++) {
			methodString.append(argTypes[i].getSimpleName());
			if (i < size - 1) {
				methodString.append(",");
			}
		}
		methodString.append(")");
		return methodString.toString();
	}

	public String getMethodName() {
		return this.sourceLocation.getMethodName();
	}

	public String getDeclaringTypeName() {
		return this.declaringTypeName;
	}

	public Object[] getArgs() {
		return this.args;
	}

	public SourceCodeLocation getSourceLocation() {
		return this.sourceLocation;
	}
}