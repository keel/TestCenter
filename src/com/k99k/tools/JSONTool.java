/**
 * 
 */
package com.k99k.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.stringtree.json.JSONErrorListener;
import org.stringtree.json.JSONValidator;

/**
 * @author keel
 *
 */
public final class JSONTool {

	/**
	 * 不可实例化
	 */
	private JSONTool() {
	}
	
	
	/**
	 * 使用JSONValidatingReader读取json的String
	 * @param in String
	 * @return HashMap or ArrayList or else json object
	 */
	@SuppressWarnings("unchecked")
	public static final HashMap<String,Object> readJsonString(String in){
		Object o = JSON.read(in);
		if (o != null && (o instanceof HashMap)) {
			return (HashMap<String,Object>)o;
		}
		return null;
	}
	
	/**
	 * 使用JSON将HashMap转成String形式的json
	 * @param Object
	 * @return json String
	 */
	public static final String writeJsonString(Object obj){
		return JSON.write(obj);
	}
	
	/**
	 * 使用JSON将List转成String形式的json
	 * @param list
	 * @return
	 */
	public static final String writeJsonString(List<Object> list){
		return JSON.write(list);
	}
	
	/**
	 * 使用JSON将HashMap转成String形式的json,并进行format
	 * @param map
	 * @return json String
	 */
	public static final String writeFormatedJsonString(HashMap<String,Object> map){
		return JSON.writeFormat(map,6);
	}
	
	public static final String writeFormatedJsonString(Object obj){
		return JSON.writeFormat(obj,6);
	}
	
	/**
	 * 使用JSON将HashMap转成String形式的json,并进行format
	 * @param map
	 * @param formatDeep 
	 * @return json String
	 */
	public static final String writeFormatedJsonString(HashMap<String,Object> map,int formatDeep){
		return JSON.writeFormat(map,formatDeep);
	}
	
	/**
	 * 验证json
	 * @param in String
	 * @return boolean 是否通过验证
	 */
	public static final boolean validateJsonString(String in){
		JSONValidator vali = new JSONValidator(new JSONErrorListener() {
			
			@Override
			public void start(String text) {
				
			}
			
			@Override
			public void error(String message, int column) {
				
			}
			
			@Override
			public void end() {
				
			}
		});
		return vali.validate(in);
	}
	
	
	/**
	 * 检查Map是否存在指定的多个String key
	 * @param m Map
	 * @param keys String[]
	 * @return 少任意一个key则返回false
	 */
	@SuppressWarnings("unchecked")
	public static final boolean checkMapKeys(Map m,String[] keys){
		if (m == null || keys == null) {
			return false;
		}
		for (int i = 0; i < keys.length; i++) {
			if (!m.containsKey(keys[i])) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 检查Map是否存在指定的多个String key，且类型分别为valueClass(instanceof)
	 * @param m Map
	 * @param keys String[] 
	 * @param valueClass Class[] 当为Object.class时可为任意类型
	 * @return 少任意一个key则返回false
	 */
	@SuppressWarnings("unchecked")
	public static final boolean checkMapTypes(Map m,String[] keys,Class[] valueClass){
		if (m == null || keys == null || valueClass == null || keys.length!=valueClass.length) {
			return false;
		}
		for (int i = 0; i < keys.length; i++) {
			//Object可为任意类型
			if (valueClass[i].equals(Object.class)) {
				continue;
			}
			if (!m.containsKey(keys[i]) || (!m.get(keys[i]).getClass().equals(valueClass[i]))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 定位到Json的某一个节点
	 * @param root HashMap<String,Object> 
	 * @param jsonPath  String[]
	 * @return 节点对应对象,若路径不存在则返回null
	 */
	@SuppressWarnings("unchecked")
	public static final Object findJsonNode(HashMap<String,Object> root,String[] jsonPath){
		if (jsonPath==null || jsonPath.length<1) {
			return null;
		}
		HashMap<String,Object> target =  root;
		//定位到需要更新的节点
		for (int i = 0; i < jsonPath.length; i++) {
			if (target == null) {
				return null;
			}
			target = (HashMap<String,Object>)(target.get(jsonPath[i]));
		}
		return target;
	}
	
	//TODO 验证json格式,与指定的样例比较
	
}
