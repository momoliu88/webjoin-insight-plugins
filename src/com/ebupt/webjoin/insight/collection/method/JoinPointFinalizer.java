package com.ebupt.webjoin.insight.collection.method;

import java.util.Map;

import org.aspectj.lang.JoinPoint;

import com.ebupt.webjoin.insight.intercept.operation.*;
import com.ebupt.webjoin.insight.util.*;

/**
 * Populates an Operation based on data stored within a JoinPoint.
 * 
 * If an Operation is uninitialized, it will be set to: type=METHOD
 * label=MyClass#myMethod
 * 
 * The Operation's properties are as follows:
 * 
 * methodName: Short name of the method (myMethod) methodSignature:
 * "myMethod(int, long)" className: Full name of the class shortClassName: Short
 * class name arguments: ["java.lang.Object", "3", "myLogin"] (OperationList of
 * arguments)
 * 
 * This class employs a cache to reduce the amount of object creation and string
 * construction. Since data from the JoinPoint rarely changes, we cache nearly
 * everything in a template and fill in operations from the template.
 */
public class JoinPointFinalizer implements OperationFinalizer {
	private static final String KEY = JoinPointFinalizer.class.getName()
			+ "#KEY";
	private static final JoinPointFinalizer INSTANCE = new JoinPointFinalizer();

	// package protected for testing
	OperationCache cache = new OperationCache();

	/**
	 * Register the finalizer for the operation and breakDown.
	 */
	public static final void register(Operation operation, JoinPoint jp) {
		registerWithFinalizer(operation, jp, INSTANCE);
	}

	protected static final void registerWithFinalizer(Operation op,
			JoinPoint jp, JoinPointFinalizer finalizer) {
		op.addFinalizerObject(KEY, jp).addFinalizer(finalizer);
	}

	public void finalize(Operation operation, Map<String, Object> richObjects) {
		JoinPoint jp = (JoinPoint) richObjects.get(KEY);
		if (jp != null) {
			populateOperation(operation, jp);
		}
	}

	// Methods used in tests
	protected JoinPointFinalizer() {
		super();
	}

	protected void registerWithSelf(Operation op, JoinPoint jp) {
		registerWithFinalizer(op, jp, this);
	}

	protected void populateOperation(Operation op, JoinPoint jp) {
		Operation template = cache.getOperationTemplate(jp.getStaticPart());
		if (StringUtil.isEmpty(op.getLabel())) {
			op.label(template.getLabel());
		}

		if (op.getType().equals(OperationType.SIMPLE)) {
			op.type(template.getType());
		}

		if (op.getSourceCodeLocation() == null) {
			op.sourceCodeLocation(template.getSourceCodeLocation());
		}

		op.copyPropertiesFrom(template);
		stringifyArguments(op, jp.getArgs());
	}

	/**
	 * If any of the given arguments can be converted to a nicer string
	 * representation, we make a copy of the operation's argument list and
	 * modify it with the stringified version of the appropriate arguments.
	 * 
	 * @param op
	 *            The {@link Operation} to which to add the argument
	 * @param args
	 */
	protected void stringifyArguments(Operation op, Object... args) {
		if (ArrayUtil.length(args) <= 0) {
			return;
		}

		OperationList orgList = op.get(OperationFields.ARGUMENTS,OperationList.class), 
					argList = null;
		if(orgList == null || orgList.size() == 0) return;

		argList = orgList.shallowCopy();
		
		for (int i = 0; i < args.length; i++) {
			Object argVal = args[i];
			if (!StringFormatterUtils.isToStringable(argVal)) {
				continue;
			}
			argList.update(i, StringFormatterUtils.formatObject(argVal));
		}

		if (argList != null) { // check if any argument changed
			op.put(OperationFields.ARGUMENTS, argList);
		}
	}
}
