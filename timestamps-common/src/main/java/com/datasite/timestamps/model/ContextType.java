package com.datasite.timestamps.model;

public enum ContextType {

    PROJECT(60*60*6), // 6 hours
    TASK(60*60*3), // 3 hours
    TASK_LIST(60*60*4) // 4 hours
    ;

    /**
     * How long is the delay until the timestamp is updated upon request
     */
    public final long timestampDelay;

    ContextType(long seconds) {
        this.timestampDelay = seconds;
    }
}
