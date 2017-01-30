package com.tw.ds;

public class Shard {

    private final int startKey;
    private final int endKey;

    public Shard(int startKey, int endKey) {
        this.startKey = startKey;
        this.endKey = endKey;
    }

    public int getStartKey() {
        return startKey;
    }

    public int getEndKey() {
        return endKey;
    }

    public boolean wraps(int key) {
        return key >= startKey && key <= endKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Shard shard = (Shard) o;

        if (startKey != shard.startKey) return false;
        return endKey == shard.endKey;
    }

    @Override
    public int hashCode() {
        int result = startKey;
        result = 31 * result + endKey;
        return result;
    }

    @Override
    public String toString() {
        return "Shard{" +
                "startKey=" + startKey +
                ", endKey=" + endKey +
                '}';
    }
}
