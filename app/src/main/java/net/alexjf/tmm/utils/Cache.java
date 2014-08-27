/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.utils;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class implements a generic key-value cache based on soft references and
 * periodic cleanup of nulled entries.
 */
public class Cache<K,V> {
    static final int CLEANUP_DELAY = 10000;
    static final int CLEANUP_PERIOD = 10000;

    private Map<K, SoftReference<V>> map;
    private Timer cleanupTimer;

    public Cache() {
        map = new HashMap<K, SoftReference<V>>();
        cleanupTimer = new Timer(true);
        cleanupTimer.schedule(new TimerTask() {
            public void run() {
                Iterator<Map.Entry<K, SoftReference<V>>> it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<K, SoftReference<V>> pair = it.next();
                    if (pair.getValue() == null) {
                        it.remove();
                    }
                }
            }
        }, CLEANUP_DELAY, CLEANUP_PERIOD);
    }

    public V get(K key) {
        SoftReference<V> ref = map.get(key);

        if (ref != null) {
            return ref.get();
        }

        return null;
    }

    public void put(K key, V value) {
        map.put(key, new SoftReference<V>(value));
    }

    public void remove(K key) {
        map.remove(key);
    }

    public void clear() {
    	map.clear();
    }
}
