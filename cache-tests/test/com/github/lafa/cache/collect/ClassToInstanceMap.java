package com.github.lafa.cache.collect;

import java.util.HashMap;
import java.util.Map;

public class ClassToInstanceMap<B> {

	final Map<Class<? extends B>, ? super B> map = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <T extends B> T getInstance(Class<T> type) {
		return (T) map.get(type);
	}

	public <T extends B> void put(Class<T> type, T value) {
		map.put(type, value);
	}

	public <T extends B> boolean containsKey(Class<T> type) {
		return map.containsKey(type);
	}

}
