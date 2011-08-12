package com.k99k.tools;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public class JSONFormatWriter {

    private StringBuffer buf = new StringBuffer();
    private Stack<Object> calls = new Stack<Object>();
    boolean emitClassName = true;
    
    
//    public static void main(String[] args) {
//		HashMap m = new HashMap();
//		m.put("node1", "string1");
//		HashMap node2 = new HashMap();
//		node2.put("int1", 5);
//		node2.put("boolean1", false);
//		node2.put("array1", new String[]{"arr1","arr2","arr3"});
//		m.put("node2", node2);
//		m.put("node3", new String[]{"arr4","arr5"});
//		ArrayList list = new ArrayList();
//		list.add("sfsdf");
//		list.add("listString2");
//		m.put("node4", list);
//		JSONFormatWriter jw = new JSONFormatWriter();
//		System.out.println(jw.write(m, 2));
//	}
    
    /**
     * 格式化深度，默认为5层,超过5层则不换行
     */
    private int formatDeep = 5;
    
//    /**
//     * 是否格式化输出
//     */
//    private boolean isFormat = false;
    
    /**
     * 当前深度
     */
    private int deep = 0;
    
    public JSONFormatWriter(boolean emitClassName) {
        this.emitClassName = emitClassName;
    }
    
    public JSONFormatWriter() {
        this(true);
    }

    public String write(Object object) {
        buf.setLength(0);
        value(object);
        return buf.toString();
    }
    
    public String write(Object object,int formatDeep) {
    	this.formatDeep = formatDeep;
//    	isFormat = true;
        buf.setLength(0);
        value(object);
        return buf.toString();
    }
    
    
    

    public String write(long n) {
        return String.valueOf(n);
    }

    public String write(double d) {
        return String.valueOf(d);
    }

    public String write(char c) {
        return "\"" + c + "\"";
    }
    
    public String write(boolean b) {
        return String.valueOf(b);
    }
    

    private void value(Object object) {
        if (object == null || cyclic(object)) {
            add("null");
        } else {
            calls.push(object);
            if (object instanceof Class) string(object);
            else if (object instanceof Boolean) bool(((Boolean) object).booleanValue());
            else if (object instanceof Number) add(object);
            else if (object instanceof String) string(object);
            else if (object instanceof Character) string(object);
            //Map对象处理
            else if (object instanceof Map) map((Map)object);
            else if (object.getClass().isArray()) array(object);
            else if (object instanceof Iterator) array((Iterator)object);
            else if (object instanceof Collection) array(((Collection)object).iterator());
            //java对象处理
            else bean(object);
            calls.pop();
        }
    }
    

    /**
     * 判断是否在此对象Stack中存在对此对象自我的引用
     * @param object
     * @return
     */
    private boolean cyclic(Object object) {
        Iterator it = calls.iterator();
        while (it.hasNext()) {
            Object called = it.next();
            if (object == called) return true;
        }
        return false;
    }
    
    private void bean(Object object) {
        add("{");
        BeanInfo info;
        boolean addedSomething = false;
        try {
            info = Introspector.getBeanInfo(object.getClass());
            PropertyDescriptor[] props = info.getPropertyDescriptors();
            for (int i = 0; i < props.length; ++i) {
                PropertyDescriptor prop = props[i];
                String name = prop.getName();
                Method accessor = prop.getReadMethod();
                if ((emitClassName==true || !"class".equals(name)) && accessor != null) {
                    if (!accessor.isAccessible()) accessor.setAccessible(true);
                    Object value = accessor.invoke(object, (Object[])null);
                    if (addedSomething) add(',');
                    add(name, value);
                    addedSomething = true;
                }
            }
            Field[] ff = object.getClass().getFields();
            for (int i = 0; i < ff.length; ++i) {
                Field field = ff[i];
                if (addedSomething) add(',');
                add(field.getName(), field.get(object));
                addedSomething = true;
            }
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        } catch (InvocationTargetException ite) {
            ite.getCause().printStackTrace();
            ite.printStackTrace();
        } catch (IntrospectionException ie) {
            ie.printStackTrace();
        } 
        add("}");
    }

    private void add(String name, Object value) {
        add('"');
        add(name);
        add("\":");
        value(value);
    }
    
    private void addFormat(int deeper){
//    	if (isFormat) {
			if (this.deep<this.formatDeep) {
				add("\r\n");
	    		if (deeper > 0) {
	    			this.deep++;
				}else if(deeper< 0){
					this.deep--;
				}
	    		addBlankFormat(this.deep);
			}else if(this.deep == this.formatDeep){
				if (deeper > 0) {
//					System.out.println(buf);
//					System.out.println("-------- deep:"+deep+" deeper:"+deeper);
	    			this.deep++;
				}else if (deeper < 0) {
//					System.out.println(buf);
//					System.out.println("-------- deep:"+deep+" deeper:"+deeper);
					add("\r\n");
					this.deep--;
					addBlankFormat(this.deep);
				}
			}else{
//				System.out.println(buf);
//				System.out.println(">>>>> deep:"+deep+" deeper:"+deeper);
    			if (deeper < 0) {
					this.deep--;
				}
			}
//		}
    }
    
    private void addBlankFormat(int blankCount){
    	for (int i = 0; i < blankCount; i++) {
    		add("\t");
		}
    }

    private void map(Map map) {
        add("{");
        addFormat(1);
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            value(e.getKey());
            add(":");
            value(e.getValue());
            if (it.hasNext()) {
            	add(',');
            	addFormat(0);
            }
            
        }
        addFormat(-1);
        add("}");
        
    }
    
    private void array(Iterator it) {
        add("[");
        addFormat(1);
        while (it.hasNext()) {
            value(it.next());
            if (it.hasNext()) {
            	add(",");
            	addFormat(0);
            }
            
        }
        addFormat(-1);
        add("]");
    }

    private void array(Object object) {
        add("[");
        addFormat(1);
        int length = Array.getLength(object);
        for (int i = 0; i < length; ++i) {
            value(Array.get(object, i));
            if (i < length - 1) {
            	add(',');
            	addFormat(0);
            }
        }
        addFormat(-1);
        add("]");
    }

    private void bool(boolean b) {
        add(b ? "true" : "false");
    }

    private void string(Object obj) {
        add('"');
        CharacterIterator it = new StringCharacterIterator(obj.toString());
        for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
            if (c == '"') {add("\\\"");continue;}
            else if (c == '\\') {add("\\\\");continue;}
            else if (c == '/') {add("\\/");continue;}
            else if (c == '\b') {add("\\b");continue;}
            else if (c == '\f') {add("\\f");continue;}
            else if (c == '\n') {add("\\n");continue;}
            else if (c == '\r') {add("\\r");continue;}
            else if (c == '\t') {add("\\t");continue;}
            else if (Character.isISOControl(c)) {
                unicode(c);continue;
            } else {
                add(c);
            }
        }
        add('"');
    }

    private void add(Object obj) {
        buf.append(obj);
    }

    private void add(char c) {
        buf.append(c);
    }

    static char[] hex = "0123456789ABCDEF".toCharArray();

    private void unicode(char c) {
        add("\\u");
        int n = c;
        for (int i = 0; i < 4; ++i) {
            int digit = (n & 0xf000) >> 12;
            add(hex[digit]);
            n <<= 4;
        }
    }
}
