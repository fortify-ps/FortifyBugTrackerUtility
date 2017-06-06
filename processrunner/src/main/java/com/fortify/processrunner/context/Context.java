package com.fortify.processrunner.context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>This class defines the process runner context. It is basically a 
 * {@link HashMap} that can contain arbitrary String keys together with 
 * arbitrary Object values.</p>
 * 
 * <p>To allow type-safe access to this {@link Map}, callers can call the
 * {@link #as(Class)} method to 'cast' the {@link Map} to an arbitrary
 * interface. This will generate a Java proxy that provides access to 
 * the {@link Map} via regular bean methods like getX() and setX(value).</p>
 * 
 * <p>Note that interfaces used with the {@link #as(Class)} method should
 * use unique method names (for example based on the interface name) to avoid
 * naming conflicts in the map between different types of functionality.</p>
 * 
 * <p>Currently the following method declarations are supported in interfaces
 * passed to the {@link #as(Class)} method:</p>
 * <ul>
 *  <li><code>get[SomeProperty]()</code>Get the value for the [SomeProperty] key from the context</li>
 *  <li><code>set[SomeProperty](value)</code>Set the value for the [SomeProperty] key on the context</li>
 * </ul>
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class Context extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	private final Map<Class, Object> proxies = new HashMap<Class, Object>();
	
	public Context() {}
	
	public Context(Context context) {
		super(context);
	}
	
	public final void addContextPropertyDefinitions(ContextPropertyDefinitions contextPropertyDefinitions) {
		for ( Object obj : values() ) {
			if ( obj instanceof IContextPropertyDefinitionProvider ) {
				((IContextPropertyDefinitionProvider)obj).addContextPropertyDefinitions(contextPropertyDefinitions, this);
			}
		}
	}
	
	public final <T> T as(Class<T> iface) {
		T result = (T)proxies.get(iface);
		if ( result == null ) {
			result = (T) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{iface},
                new MapBasedInvocationHandler(this));
		}
		return result;
	}
	
	/**
	 * This {@link InvocationHandler} implementation can be used to provide
	 * method implementations based on a backing map, for example to get and 
	 * set specific properties in the backing map. 
	 */
	private static final class MapBasedInvocationHandler implements InvocationHandler {
		private final Map<String, Object> map;
		private final List<MethodHandler> methodHandlers = Arrays.asList(new MethodHandler[]{
			new GetMethodHandler(), new SetMethodHandler()
		});
		public MapBasedInvocationHandler(Map<String, Object> map) {
			this.map = map;
		}
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			for ( MethodHandler mh : methodHandlers ) {
		    	if ( mh.canHandleMethod(map, method, args) ) {
		    		return mh.handleMethod(map, method, args);
		    	}
		    }
		    throw new RuntimeException("No handler found for method "+method);
		}
		
		private static interface MethodHandler {
			public Object handleMethod(Map<String, Object> map, Method method, Object[] args);
			public boolean canHandleMethod(Map<String, Object> map, Method method, Object[] args);
		}
		
		private static abstract class AbstractMethodHandler implements MethodHandler{
			protected abstract String getSupportedPrefix();
			protected abstract boolean canHandleArgs(Object[] args);
			protected abstract Object handleKey(Map<String, Object> map, String key, Object[] args);
			
			public boolean canHandleMethod(Map<String, Object> map, Method method, Object[] args) {
				return method.getName().startsWith(getSupportedPrefix());
			}
			
			public final Object handleMethod(Map<String, Object> map, Method method, Object[] args) {
				return handleKey(map, method.getName().substring(getSupportedPrefix().length()), args);
			}
		}
		
		/**
		 * Given an interface method named get[SomeProperty](), this 
		 * {@link AbstractMethodHandler} implementation will get the
		 * value for the [SomeProperty] key from the backing map. 
		 */
		private static final class GetMethodHandler extends AbstractMethodHandler {
			@Override
			protected String getSupportedPrefix() {
				return "get";
			}
			@Override
			protected boolean canHandleArgs(Object[] args) {
				return args.length==0;
			}
			@Override
			protected Object handleKey(Map<String, Object> map, String key, Object[] args) {
				return map.get(key);
			}
		}
		
		/**
		 * Given an interface method named set[SomeProperty](), this 
		 * {@link AbstractMethodHandler} implementation will set the
		 * value for the [SomeProperty] key on the backing map. 
		 */
		private static final class SetMethodHandler extends AbstractMethodHandler {
			@Override
			protected String getSupportedPrefix() {
				return "set";
			}
			@Override
			protected boolean canHandleArgs(Object[] args) {
				return args.length==1;
			}
			@Override
			protected Object handleKey(Map<String, Object> map, String key, Object[] args) {
				return map.put(key, args[0]);
			}
		}
	}
}
