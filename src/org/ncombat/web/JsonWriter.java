package org.ncombat.web;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

@SuppressWarnings("unchecked")
public class JsonWriter
{
	public static final String DATE_FORMAT = "\"MM/dd/yyyy HH:mm:ss.SSS\"";
	
	// Used for escaping Unicode control characters.
	private DecimalFormat decFmt = new DecimalFormat("0000");
	
	// Used for formatting dates into strings in JSON output.
	private SimpleDateFormat dateFmt = new SimpleDateFormat(DATE_FORMAT);

	private StringBuilder buf = new StringBuilder();
	
	public JsonWriter() {
	}
	
	public void write(Object o) {
		if (o == null) {
			buf.append("null");
			return;
		}
		if (o instanceof String) {
			buf.append( encodeString((String)o));
		}
		else if (o instanceof Number) {
			buf.append( ((Number)o).toString());
		}
		else if (o instanceof Boolean) {
			buf.append( o.toString());
		}
		else if (o instanceof Date) {
			buf.append( dateFmt.format((Date)o));
		}
		else if (o instanceof Collection) {
			buf.append("[");
			boolean first = true;
			for (Iterator it = ((Collection)o).iterator() ; it.hasNext() ; ) {
				if (first) {
					first = false;
				}
				else {
					buf.append(',');
				}
				write(it.next());
			}
			buf.append("]");
		}
		else if (o.getClass().isArray()) {
			buf.append("[");
			int len = Array.getLength(o);
			boolean first = true;			
			for (int i = 0 ; i < len ; i++) {
				if (first) {
					first = false;
				}
				else {
					buf.append(',');
				}
				write(Array.get(o, i));
			}
			buf.append("]");
		}
		else if (o instanceof Map) {
			writeMap((Map)o);
		}
		else {
			Map objMap = new LinkedHashMap();
			BeanWrapper bean = new BeanWrapperImpl(o);
			PropertyDescriptor[] pda = bean.getPropertyDescriptors();
			TreeSet<String> properties = new TreeSet<String>();
			for (PropertyDescriptor pd : pda) {
				String property = pd.getName();
				if (property.equals("class")) continue;
				if (!bean.isReadableProperty(property)) continue;
				properties.add(property);
			}
			for (String property : properties) {
				Object value = bean.getPropertyValue(property);
				objMap.put(property, value);
			}
			writeMap(objMap);
		}
	}
	
	private void writeMap(Map map) {
		buf.append("{");
		boolean first = true;
		for (Iterator it = map.entrySet().iterator() ; it.hasNext() ; ) {
			Map.Entry entry = (Map.Entry) it.next();
			if (first) {
				first = false;
			}
			else {
				buf.append(',');
			}
			
			write(entry.getKey().toString());
			buf.append(':');
			write(entry.getValue());
		}
		buf.append("}");
	}
	
	public String encodeString(String in) {
		StringBuilder out = new StringBuilder();
		out.append('"');
		for (int i = 0 ; i < in.length() ; i++) {
			char ch = in.charAt(i);
			if (ch == '"') {
				out.append("\\\"");
			}
			else if (ch == '\\') {
				out.append("\\\\");
			}
//			else if (ch == '/') {
//				out.append("\\/");
//			}
			else if (ch == '\b') {
				out.append("\\b");
			}
			else if (ch == '\f') {
				out.append("\\f");
			}
			else if (ch == '\n') {
				out.append("\\n");
			}
			else if (ch == '\r') {
				out.append("\\r");
			}
			else if (Character.isISOControl(ch)) {
				out.append("\\u");
				out.append( decFmt.format(ch));
			}
			else {
				out.append(ch);
			}
		}
		out.append('"');
		return out.toString();
	}

	public String toString(Object o) {
		buf.setLength(0);
		write(o);
		return buf.toString();
	}
}