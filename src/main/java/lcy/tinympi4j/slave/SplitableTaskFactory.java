package lcy.tinympi4j.slave;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lcy.tinympi4j.common.SplitableTask;

public class SplitableTaskFactory {
	
	private SplitableTaskFactory(){}
	
	private static final Map<String, SplitableTask> splitabletaskobjmap = new ConcurrentHashMap<String, SplitableTask>();
	
	public static SplitableTask instance(Class<SplitableTask> clazz){
		
		synchronized (clazz) {
			SplitableTask obj = splitabletaskobjmap.get(clazz.getName());
			if(obj == null){
				try {
					splitabletaskobjmap.put(clazz.getName(), obj = clazz.newInstance());
				} catch (InstantiationException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
			return obj;
		}
	}

}
