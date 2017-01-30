package com.tw.ds;

import java.io.Serializable;

public class ReadMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private int key;

    public ReadMessage(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
