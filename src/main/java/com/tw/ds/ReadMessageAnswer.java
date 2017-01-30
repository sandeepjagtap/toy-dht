package com.tw.ds;

import java.io.Serializable;

public class ReadMessageAnswer implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int key;
    private final int value;

    public ReadMessageAnswer(int key, int value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public int getValue() {
        return value;
    }
}
