package com.ebupt.webjoin.insight.collection.method;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;

import com.ebupt.webjoin.insight.collection.OperationCollectionUtil;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationFields;
import com.ebupt.webjoin.insight.intercept.operation.OperationList;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.operation.SourceCodeLocation;
import com.ebupt.webjoin.insight.intercept.operation.method.JoinPointBreakDown;
import com.ebupt.webjoin.insight.util.StringUtil;

public class OperationCache {
	private Map<OperationTemplateCacheKey, Operation> templateCache = new ConcurrentHashMap<OperationTemplateCacheKey, Operation>();

	public Operation getOperationTemplate(JoinPoint.StaticPart part) {
		OperationTemplateCacheKey key = new OperationTemplateCacheKey(part);
		Operation template = (Operation) this.templateCache.get(key);
		if (template != null) {
			return template;
		}
		template = makeOperationTemplate(part);
		this.templateCache.put(key, template);
		return template;
	}

	private Operation makeOperationTemplate(JoinPoint.StaticPart part) {
		SourceCodeLocation loc = OperationCollectionUtil.getSourceCodeLocation(part);
		MethodSignature sig = (MethodSignature) part.getSignature();

		String methodSignature = JoinPointBreakDown.getMethodStringFromArgs(
				loc, sig.getParameterTypes());
		String methodName = loc.getMethodName();
		String className = sig.getDeclaringTypeName();
		String shortClassName = StringUtil.subStringAfterLast(className,
				new char[] { '.', '$' });
		String label = StringUtil.subStringAfterLast(loc.getClassName(),
				new char[] { '.', '$' }) + "#" + methodName;

		Operation res = new Operation();
		res.sourceCodeLocation(loc).label(label).type(OperationType.METHOD)
				.put("methodName", methodName)
				.put("methodSignature", methodSignature)
				.put("className", className)
				.put("shortClassName", shortClassName);
		OperationList arguments = res.createList(OperationFields.ARGUMENTS);
		for (Class<?> arg : sig.getParameterTypes()) {
			arguments.add(arg.getName());
		}

		return res;
	}

	public int cacheSize() {
		return this.templateCache.size();
	}

	public void clear() {
		this.templateCache.clear();
	}

	private static class OperationTemplateCacheKey {
		private final String signatureName;
		private final String declaringTypeName;
		private final int line;
		private final String withinTypeName;
		private final String fileName;
		private int hashCode;

		public OperationTemplateCacheKey(JoinPoint.StaticPart part) {
			Signature sig = part.getSignature();
			this.signatureName = sig.getName();
			this.declaringTypeName = sig.getDeclaringTypeName();

			SourceLocation loc = part.getSourceLocation();
			this.line = loc.getLine();
			this.withinTypeName = loc.getWithinType().getName();
			this.fileName = loc.getFileName();

			this.hashCode = makeHashCode();
		}

		public boolean equals(Object o) {
			if (this == o)
				return true;
			if ((o == null) || (getClass() != o.getClass()))
				return false;

			OperationTemplateCacheKey that = (OperationTemplateCacheKey) o;

			return (this.line == that.line)
					&& (this.declaringTypeName.equals(that.declaringTypeName))
					&& (this.fileName.equals(that.fileName))
					&& (this.signatureName.equals(that.signatureName))
					&& (this.withinTypeName.equals(that.withinTypeName));
		}

		public int hashCode() {
			return this.hashCode;
		}

		private int makeHashCode() {
			int result = this.signatureName.hashCode();
			result = 31 * result + this.declaringTypeName.hashCode();
			result = 31 * result + this.line;
			result = 31 * result + this.withinTypeName.hashCode();
			result = 31 * result + this.fileName.hashCode();
			return result;
		}
	}
}