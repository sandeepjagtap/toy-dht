package com.tw.ds;

import java.io.Serializable;

public class ShardMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int startKey;
    private final int endKey;

    public ShardMessage(int startKey, int endKey) {
        this.startKey = startKey;
        this.endKey = endKey;
    }

    public int getStartKey() {
        return startKey;
    }

    public int getEndKey() {
        return endKey;
    }
}


