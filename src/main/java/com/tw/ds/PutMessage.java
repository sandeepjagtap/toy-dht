package com.tw.ds;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PutMessage  implements Serializable {
    private static final long serialVersionUID = 1L;

    private int key;
    private int value;

//    {
//        actorA : 1
//        actorB : 2
//    }

    private Map<String, Integer> versionInfoMap = new HashMap<>();

    public PutMessage(int key, int value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }


    public int getValue() {
        return value;
    }

    public Map<String, Integer> getVersionInfoMap() {
        return versionInfoMap;
    }

    public void addVersionInfo(String actorPath) {
        Integer version = versionInfoMap.get(actorPath);
        versionInfoMap.put(actorPath, version == null?  1 : version + 1);
    }

    @Override
    public String toString() {
        return "PutMessage{" +
                "key=" + key +
                ", value=" + value +
                ", versionInfoMap=" + versionInfoMap +
                '}';
    }
}

