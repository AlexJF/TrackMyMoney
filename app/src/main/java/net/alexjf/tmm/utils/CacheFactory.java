/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a generic key-value cache based on soft references and
 * periodic cleanup of nulled entries.
 */
public class CacheFactory {
	private static CacheFactory instance = null;
	private Map<String, Cache<?, ?>> caches;

	public static CacheFactory getInstance() {
		if (instance == null) {
			instance = new CacheFactory();
		}

		return instance;
	}

	private CacheFactory() {
		caches = new HashMap<String, Cache<?, ?>>();
	}

	public <K, V> Cache<K, V> getCache(String name) {
		Cache<K, V> cache = (Cache<K, V>) caches.get(name);

		if (cache == null) {
			cache = new Cache<K, V>();
			caches.put(name, cache);
		}

		return cache;
	}

	public void clearCaches() {
		for (Cache<?, ?> cache : caches.values()) {
			cache.clear();
		}
	}
}