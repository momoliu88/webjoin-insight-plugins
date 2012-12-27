package com.ebupt.webjoin.insight.color;

import java.io.Serializable;

import com.ebupt.webjoin.insight.util.ObjectUtil;
import com.ebupt.webjoin.insight.util.StringUtil;

public class Color implements Serializable {
    private static final long serialVersionUID = -8060878096365332991L;
    
    public static final String TOKEN_NAME = "x-Insight-color";
    public static final String SOURCE_IP_TOKEN_NAME = "x-Insight-color-source-ip";
    private static final String SEPARATOR = "|";
    private static final String EMPTY_STRING = "";

    private String inId;
    private String outId;
    private String agentId;
    private String sourceId;

    public Color(String incomingId, String outgoingId, String coloringAgentId, String srcId) {
        this.inId = incomingId;
        this.outId = outgoingId;
        this.agentId = coloringAgentId;
        this.sourceId = srcId;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                        .append(convertNull(getInId()))
                        .append(SEPARATOR)
                        .append(convertNull(getOutId()))
                        .append(SEPARATOR)
                        .append(convertNull(getSourceId()))
                        .append(SEPARATOR)
                        .append(getAgentId())
                    .toString()
                    ;
    }

    public static Color valueOf(String color) {
        if (StringUtil.isEmpty(color)) {
            return null;
        }
        String[] parts = color.split("\\|");
        if (parts.length != 4) {
            return null;
        }
        try {
            String in = convertEmpty(parts[0]);
            String out = convertEmpty(parts[1]);
            String sourceId = convertEmpty(parts[2]);
            String agentId = parts[3];
            return new Color(in, out, agentId, sourceId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
    
    static String convertEmpty(String src) {
        return StringUtil.isEmpty(src) ? null : src;
    }
    
    static String convertNull(String src) {
        return src == null ? EMPTY_STRING : src;
    }
    
    public String getInId() {
        return inId;
    }

    public String getOutId() {
        return outId;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getSourceId() {
        return sourceId;
    }
    
    public boolean isEmpty() {
        return (outId == null) && (inId == null);
    }
    
    @Override
    public int hashCode() {
        return ObjectUtil.hashCode(getAgentId())
             + ObjectUtil.hashCode(getInId())
             + ObjectUtil.hashCode(getOutId())
             + ObjectUtil.hashCode(getSourceId())
             ;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Color other = (Color) obj;
        return ObjectUtil.typedEquals(getAgentId(), other.getAgentId())
            && ObjectUtil.typedEquals(getInId(), other.getInId())
            && ObjectUtil.typedEquals(getOutId(), other.getOutId())
            && ObjectUtil.typedEquals(getSourceId(), other.getSourceId())
            ;
   }
}