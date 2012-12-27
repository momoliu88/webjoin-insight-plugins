/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *//*

package com.ebupt.webjoin.insight.intercept.metrics;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Test;

import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.resource.ResourceKey;
import com.springsource.insight.intercept.resource.ResourceNames;
import com.springsource.insight.intercept.test.AbstractInterceptTestSupport;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.SimpleFrame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.IDataPoint;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.time.TimeRange;

public abstract class AbstractMetricsGeneratorTest extends AbstractInterceptTestSupport {
	protected final TimeRange timeRange = new TimeRange(1304387418963003000L, 1304387419123224000L);
	protected final MetricsGenerator	gen;

	protected AbstractMetricsGeneratorTest (MetricsGenerator generator) {
		if ((gen=generator) == null) {
			throw new IllegalStateException("No generator provided");
		}
	}    

    @Test
    public void generateMetrics() {
    	OperationType	opType=gen.getOperationType();
    	if (OperationType.UNKNOWN.equals(opType)) {
    		return;
    	}

        Trace trace = mock(Trace.class);
        when(trace.getLastFramesOfType(opType)).thenReturn(makeFrame());
        when(trace.getAllFramesOfType(opType)).thenReturn(makeFrame());        
        when(trace.getRange()).thenReturn(timeRange);
        
        ResourceKey endPointName = ResourceKey.valueOf("EndPoint", "epName");

        List<MetricsBag> mbs = gen.generateMetrics(trace, endPointName);
        validateMetricsBags(mbs); 
    }
    
    protected void validateMetricsBags(List<MetricsBag> mbs) {
		assertEquals(2, mbs.size());
        
        MetricsBag mb = mbs.get(0);

        List<String> keys = mb.getMetricKeys();
        assertEquals(2, keys.size());
        
        assertEquals("opExtKey", mb.getResourceKey().getName());

        List<IDataPoint> points = mb.getPoints(AbstractMetricsGenerator.EXECUTION_TIME);
        assertEquals(1, points.size());
        assertEquals(160.0 , points.get(0).getValue(), .0001);

        points = mb.getPoints(AbstractMetricsGenerator.INVOCATION_COUNT);
        assertEquals(1, points.size());
        assertEquals(1.0, points.get(0).getValue(), .0001);
	}

    @Test
    public void noFrameGeneratesNoMetrics() {
    	OperationType	opType=gen.getOperationType();
    	if (OperationType.UNKNOWN.equals(opType)) {
    		return;
    	}
        Trace trace = mock(Trace.class);        
        when(trace.getFirstFrameOfType(opType)).thenReturn(null);
        
        ResourceKey endPointName = mock(ResourceKey.class);
        assertEquals(0, gen.generateMetrics(trace, endPointName).size());
    }
    
    protected List<Frame> makeFrame() {
        Operation op = new Operation().type(gen.getOperationType());
        op.put(OperationFields.OPERATION_KEY, "insight:name=\"opExtKey\",type=" + ResourceNames.ApplicationServerExternalResource);
        List<Frame> res = new ArrayList<Frame>();
        res.add(new SimpleFrame(FrameId.valueOf(1), null, op, timeRange, Collections.<Frame>emptyList()));
        res.add(new SimpleFrame(FrameId.valueOf(1), null, op, timeRange, Collections.<Frame>emptyList()));
        return res;
    }

	protected void assertDefaultExternalResourceMetricsFound (MetricsBag mb) {
		assertNotNull("No bag instance", mb);

		ResourceKey	resKey=mb.getResourceKey();
		String		typeName=resKey.getType();
		assertEquals("Mismatched resource type for " + resKey, ResourceNames.ApplicationServerExternalResource, typeName);

		Set<String>	expectedNames=new TreeSet<String>(
				Arrays.asList(AbstractMetricsGenerator.EXECUTION_TIME, AbstractMetricsGenerator.INVOCATION_COUNT));
		List<String>	metricKeys=mb.getMetricKeys();
		for (String	name : metricKeys) {
			if (!expectedNames.remove(name)) {
				continue;
			}
		}

		assertTrue("Missing metrics: " + expectedNames, expectedNames.isEmpty());
	}
	
	public static final List<AbstractExternalResourceMetricsGenerator> createExternalResourceMetricsGenerators (
			boolean generatesEndpoints, Collection<OperationType> ops) {
		if (ListUtil.size(ops) <= 0) {
			return Collections.emptyList();
		}
		
		List<AbstractExternalResourceMetricsGenerator>	genList=new ArrayList<AbstractExternalResourceMetricsGenerator>(ops.size());
		for (OperationType opType : ops) {
			genList.add(new AbstractExternalResourceMetricsGenerator(opType, generatesEndpoints) {  nothing extra  });
		}

		return genList;
	}

	public static final <G extends MetricsGenerator> Map<OperationType,G> toGeneratorsMap (Collection<? extends G> genList) {
		if (ListUtil.size(genList) <= 0) {
			return Collections.emptyMap();
		}
		
		Map<OperationType,G>	genMap=new TreeMap<OperationType, G>(OperationType.BY_NAME_COMPARATOR);
		for (G gen : genList) {
			OperationType	opType=gen.getOperationType();
			Object			prev=genMap.put(opType, gen);
			assertNull("Multiple generators for op type=" + opType, prev);
		}

		return genMap;
	}

	public static final Map<OperationType,List<MetricsBag>> analyzeTrace (String seed, Trace trace, Collection<? extends MetricsGenerator> genList) {
		if (ListUtil.size(genList) <= 0) {
			return Collections.emptyMap();
		}

		EndPointAnalysis					analysis=trace.getEndpoint();
		EndPointName						epName=(analysis == null) ? EndPointName.valueOf("seed-ep") : analysis.getEndPointName();
		ResourceKey							epKey=epName.makeKey();
		Map<OperationType,List<MetricsBag>>	result=new TreeMap<OperationType,List<MetricsBag>>(OperationType.BY_NAME_COMPARATOR);
		for (MetricsGenerator gen : genList) {
			OperationType		opType=gen.getOperationType();
			List<MetricsBag>	mbs=gen.generateMetrics(trace, epKey);
			if (ListUtil.size(mbs) <= 0) {
				continue;
			}
			
			List<MetricsBag>	prev=result.get(opType);
			if (prev != null) {
				prev.addAll(mbs);
			} else {
				result.put(opType, mbs);
			}
		}

		return result;
	}

	public static final IDataPoint assertInvocationCountValue (String testName, MetricsBag mb, long expected) {
		return assertCounterMetricValue(testName, mb, AbstractMetricsGenerator.INVOCATION_COUNT, expected);
	}

	public static final IDataPoint assertCounterMetricValue (String testName, MetricsBag mb, String keyName, long expected) {
		assertNotNull(testName + ": no metrics", mb);
		assertEquals(testName + "[" + keyName + "]: not a counter", MetricsBag.PointType.COUNTER, mb.getMetricType(keyName));

		List<? extends IDataPoint>	values=mb.getPoints(keyName);
		assertEquals(testName + "[" + keyName + "]: bad num of values - " + values, 1, ListUtil.size(values));

		IDataPoint	point=values.get(0);
		long		actual=Math.round(point.getValue());
		assertEquals(testName + "[" + keyName + "]: mismatched result", expected, actual);
		return point;
	}

	protected Trace loadTrace (String traceName) throws IOException, ClassNotFoundException {
		InputStream	inStream=getLoaderResourceAsStream(traceName + ".ser");
		assertNotNull(traceName + ": Not found", inStream);

		try {
			ObjectInputStream	objIn=new ObjectInputStream(inStream);
			try {
				return (Trace) objIn.readObject();
			} finally {
				objIn.close();
			}
		} finally {
			inStream.close();
		}
	}
}
*/