package jmar.json;

import java.lang.reflect.Field;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONToJavaAdapter<T> {
	public final Class<T> jsonClass;
	
	public JSONToJavaAdapter(Class<T> jsonClass) {
		this.jsonClass = jsonClass;
	}

	public JSONObject makeJsonObject(Object javaObject) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		javaToJson(javaObject, jsonObject);
		return jsonObject;
	}
	
	public void javaToJson(Object javaObject, JSONObject jsonObject) throws JSONException {
		int i = 0;
		Field[] fields = jsonClass.getFields();
		try {
			for(i = 0; i < fields.length; i++) {
				Field field = fields[i];
				
				Object fieldValue = field.get(javaObject);
				if(fieldValue == null) {
					jsonObject.put(field.getName(), JSONObject.NULL);
				} else {
					jsonObject.put(field.getName(), field.get(javaObject));					
				}					
			}
		} catch(IllegalArgumentException e) {
			throw new IllegalStateException(String.format("The object you specified ('%s') is not an instance of the class '%s' for this JSONToJavaAdapter",
					jsonClass.getSimpleName(), javaObject.getClass().getSimpleName()));
		} catch(IllegalAccessException e) {
			throw new IllegalStateException(String.format("field.get() threw an IllegalAccessException for field '%s'", fields[i].getName()));			
		}
	}
	
	public void jsonToJava(JSONObject jsonObject, Object javaObject) throws JSONException {
		int i = 0;
		Field[] fields = jsonClass.getFields();
		try {
			for(i = 0; i < fields.length; i++) {
				Field field = fields[i];			
				Object jsonValue = jsonObject.get(field.getName());	
				if(jsonValue == JSONObject.NULL) {
					field.set(javaObject, null);
				} else if(jsonValue.getClass() == JSONArray.class) {
					JSONArray jsonArray = (JSONArray)jsonValue;
					if(jsonArray.length() < 1) {						
						field.set(javaObject, null);
					} else {
						Object setObject;
						Object firstObject = jsonArray.get(0);
						if(firstObject.getClass() == String.class) {
							String[] stringArray = new String[jsonArray.length()];
							for(int j = 0; j < stringArray.length; j++) {
								stringArray[j] = (String) jsonArray.get(j);
							}
							setObject = stringArray;
						} else {
							throw new UnsupportedOperationException(String.format("Arrays of type '%s' are not supported yet", firstObject.getClass().getSimpleName()));
						}
												
						field.set(javaObject, setObject);
					}
					
				} else {
					field.set(javaObject, jsonValue);
				}
			}	
		} catch(IllegalArgumentException e) {
			Field field = fields[i];
			Object jsonValue = jsonObject.get(field.getName());
			System.out.println(jsonValue.getClass());
			
			
			throw new IllegalStateException(String.format("The object you specified ('%s') is not an instance of the class '%s' for this JSONToJavaAdapter, got IllegalArgumentException when trying to set field '%s'",
					jsonClass.getSimpleName(), javaObject.getClass().getSimpleName(), fields[i].getName()));
		} catch(IllegalAccessException e) {
			throw new IllegalStateException(String.format("field.get() threw an IllegalAccessException for field '%s'", fields[i].getName()));			
		}	
	}
}
