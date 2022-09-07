package com.datasite.timestamps.model;

public class TimestampRequest {

    private RequestContext requestContext;
    private ContextType contextType;

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public TimestampRequest setRequestContext(RequestContext requestContext) {
        this.requestContext = requestContext;
        return this;
    }

    public ContextType getContextType() {
        return contextType;
    }

    public TimestampRequest setContextType(ContextType contextType) {
        this.contextType = contextType;
        return this;
    }
}
