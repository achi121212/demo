package tw.com.demo.basic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MyApplicationContext {

	private static volatile MyApplicationContext instance;
	private final Map<String, Object> beanMap = new HashMap<>();
	private Set<String> transactionalClass;
	private Set<String> transactionalMethod;
	
	private MyApplicationContext() {
		transactionalClass = new HashSet<>();
		transactionalMethod = new HashSet<>();
	}

	public static MyApplicationContext getInstance() {
		if (instance == null) {
			synchronized (MyApplicationContext.class) {
				if (instance == null) {
					instance = new MyApplicationContext();
				}
			}
		}
		return instance;
	}
	
	public Object getBean(String beanName) {
		if (beanMap.containsKey(beanName))
			return beanMap.get(beanName);
		return null;
	}
	
	public boolean addBean(String name, Object bean) {
		if (!beanMap.containsKey(name)) {
			beanMap.put(name, bean);
			return true;
		}
		return false;
	}
	
	public Set<String> getTransactionalClass() {
		return transactionalClass;
	}

	public Set<String> getTransactionalMethod() {
		return transactionalMethod;
	}
}
