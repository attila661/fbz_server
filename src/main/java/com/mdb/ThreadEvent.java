package com.mdb;

public class ThreadEvent {

    private final Object lock = new Object();
    private boolean signalPending = false;

    public void signal() {
        synchronized (lock) {
            signalPending = true;
            lock.notify();
        }
    }

    public void await() throws InterruptedException {
        synchronized (lock) {
            if (!signalPending) {
                lock.wait();
            }
            signalPending = false;
        }
    }

    public void await(int msec) throws InterruptedException {
        synchronized (lock) {
            if (!signalPending) {
                lock.wait(msec);
            }
            signalPending = false;
        }
    }
}
