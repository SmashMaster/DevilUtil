package com.samrj.devil.util;

import java.util.*;
import java.util.function.Consumer;

/**
 * Stores a list of callbacks. When this Notifier is activated, all subscribed callbacks are called.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Notifier<T> implements Consumer<T>, Runnable
{
    private final List<Consumer<T>> callbacks = new ArrayList<>();

    /**
     * Adds the given callback to this Notifier.
     */
    public <G extends Consumer<T>> G subscribe(G callback)
    {
        callbacks.add(callback);
        return callback;
    }

    /**
     * Adds the given callback to this notifier.
     */
    public <G extends Runnable> G subscribe(G callback)
    {
        callbacks.add(e -> callback.run());
        return callback;
    }

    /**
     * Activates this Notifier, sending the given argument to all callbacks.
     */
    @Override
    public void accept(T event)
    {
        callbacks.forEach(callback -> callback.accept(event));
    }

    /**
     * Activates this Notifier, sending null to all callbacks.
     */
    @Override
    public void run()
    {
        accept(null);
    }

    /**
     * Removes all callbacks from this Notifier.
     */
    public void clear()
    {
        callbacks.clear();
    }
}
