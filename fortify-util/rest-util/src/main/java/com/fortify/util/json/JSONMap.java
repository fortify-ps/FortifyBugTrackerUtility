package com.fortify.util.json;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.core.convert.support.DefaultConversionService;

public class JSONMap extends LinkedHashMap<String, Object> {
	private static final long serialVersionUID = 1L;

	public JSONMap() {
		super();
	}

	public JSONMap(int initialCapacity, float loadFactor, boolean accessOrder) {
		super(initialCapacity, loadFactor, accessOrder);
	}



	public JSONMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}



	public JSONMap(int initialCapacity) {
		super(initialCapacity);
	}

	public JSONMap(Map<? extends String, ? extends Object> m) {
		super(m);
	}

	public <T> T get(String key, Class<T> type) {
		return new DefaultConversionService().convert(super.get(key), type);
	}
	
	public JSONMap getOrCreateJSONMap(String key) {
		return (JSONMap)computeIfAbsent(key, new Function<String, JSONMap>() {
				public JSONMap apply(String key) { return new JSONMap(); };
		});
	}
	
	public JSONList getOrCreateJSONList(String key) {
		return (JSONList)computeIfAbsent(key, new Function<String, JSONList>() {
				public JSONList apply(String key) { return new JSONList(); };
		});
	}
	
	public void putPaths(Map<String, Object> map) {
		for ( Map.Entry<String, Object> entry : map.entrySet() ) {
			putPath(entry.getKey(), entry.getValue());
		}
	}
	
	public void putPath(String path, Object value) {
		putPath(Arrays.asList(path.split("\\.")), value);
	}
	
	private void putPath(List<String> path, Object value) {
		if ( path.size()==1 ) {
			put(path.get(0), value);
		} else if ( path.size()>1 ){
			String currentSegment = path.get(0);
			JSONMap intermediate;
			if ( currentSegment.endsWith("[]") ) {
				JSONList list = getOrCreateJSONList(currentSegment.substring(0, currentSegment.length()-2));
				intermediate = new JSONMap();
				list.add(intermediate);
			} else {
				intermediate = getOrCreateJSONMap(currentSegment);
			}
			intermediate.putPath(path.subList(1, path.size()), value);
		}
	}
	
	public static void main(String[] args) {
		JSONMap t = new JSONMap();
		t.putPath("update.comment[].add.body", "comment1");
		t.putPath("update.comment[].add.body", "comment2");
		System.out.println(t);
	}
}
