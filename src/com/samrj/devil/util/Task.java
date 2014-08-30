package com.samrj.devil.util;

import java.util.LinkedList;
import java.util.List;

public class Task
{
    private Thread runningThread = null;
    private final List<Object> blockers = new LinkedList<>();
    private final Runnable task;
    private boolean running = false, complete = false;
    
    public Task(Runnable task)
    {
        this.task = task;
    }
    
    /**
     * Runs this task on the calling thread.
     */
    public final void run()
    {
        if (running) throw new IllegalStateException("Task already running.");
        if (complete) throw new IllegalStateException("Task already complete.");
        
        runningThread = Thread.currentThread();
        running = true;
        task.run();
        complete = true;
        running = false;
        runningThread = null;
        
        for (Object blocker : blockers) synchronized (blocker) {blocker.notify();}
        blockers.clear();
    }
    
    public final boolean isRunning()
    {
        return running;
    }
    
    public final boolean complete()
    {
        return complete;
    }
    
    /**
     * Blocks the calling thread until the task is complete. Throws an exception
     * if called on the currently executing thread.
     */
    public final void block() throws InterruptedException
    {
        if (complete()) return;
        if (Thread.currentThread() == runningThread)
            throw new IllegalThreadStateException("Cannot block for task on own thread.");
        
        Object blocker = new Object();
        blockers.add(blocker);
        synchronized (blocker) {blocker.wait();}
    }
}