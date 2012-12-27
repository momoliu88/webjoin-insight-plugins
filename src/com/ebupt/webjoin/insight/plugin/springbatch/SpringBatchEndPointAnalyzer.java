package com.ebupt.webjoin.insight.plugin.springbatch;
import com.ebupt.webjoin.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.ebupt.webjoin.insight.intercept.endpoint.EndPointAnalysis;
import com.ebupt.webjoin.insight.intercept.endpoint.EndPointName;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.trace.Frame;

public class SpringBatchEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {
    /**
     * The <U>static</U> score value assigned to endpoints - <B>Note:</B>
     * we return a score of {@link EndPointAnalysis#CEILING_LAYER_SCORE} so as
     * to let other endpoints &quot;beat&quot; this one
     */
	public static final int	DEFAULT_SCORE=EndPointAnalysis.CEILING_LAYER_SCORE;

    public SpringBatchEndPointAnalyzer() {
        super(SpringBatchDefinitions.BATCH_TYPE);
    }

    @Override
    protected int getDefaultScore(int depth) {
    	return DEFAULT_SCORE;
    }

    @Override
    protected EndPointAnalysis makeEndPoint(Frame frame, int depth) {
        Operation   op=frame.getOperation();
        return new EndPointAnalysis(EndPointName.valueOf(op), op.getLabel(), op.getLabel(), getOperationScore(op, depth), op);
    }
}