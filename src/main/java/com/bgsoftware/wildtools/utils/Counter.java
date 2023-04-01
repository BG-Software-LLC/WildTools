package com.bgsoftware.wildtools.utils;

public class Counter {

    private int counter = 0;

    public void increase() {
        increase(1);
    }

    public void increase(int delta) {
        this.counter += delta;
    }

    public int get() {
        return counter;
    }

}
