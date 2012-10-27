package jmar.games.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import jmar.CallbackThread;
import jmar.SyncLockObject;
import jmar.games.tank.Settings;

import org.json.JSONException;
import org.json.JSONObject;

public class HttpRequest {

	public static void HttpPostJson(final HttpURLConnection connection, final byte[] postData, final SyncLockObject syncObject, final HttpJsonCallback callback) {
		if(!syncObject.requestlock()) return;		
		new Thread(new Runnable() {
			public void run() {
				try {
					String response = HttpRequest.HttpPost(connection, postData);
					try {
						JSONObject json = new JSONObject(response);
						
						String errorMessage = json.optString("error");
						if(errorMessage != null && errorMessage != "") {
							callback.Error(errorMessage);
						} else {
							callback.Success(new JSONObject(response));
						}
					} catch(JSONException e) {
						callback.Error(String.format("Failed to parse JSON from server '%s': %s", response, e.getMessage()));
					}
				} catch (IOException e) {
					callback.Error(String.format("IOException: %s", e.getMessage()));
				} catch (Exception e) {
					callback.Error(String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage()));					
				} finally {
					syncObject.unlock();
				}
			}
		}).start();
	}
	
	public static JSONObject HttpPostJson(HttpURLConnection connection, byte[] postData) throws IOException, JSONException {
		String response = HttpRequest.HttpPost(connection, postData);
		try {
			return new JSONObject(response);
		} catch(JSONException e) {
			throw new JSONException(String.format("Invalid JSON from server '%s': %s", response, e.getMessage()));
		}
	}
	
	public static String HttpPost(HttpURLConnection connection, byte[] postData) throws IOException {          
          connection.setRequestMethod("POST");
          connection.setDoOutput(true);
          connection.setReadTimeout(10000);
          
          connection.connect();
          
          OutputStream outputStream = connection.getOutputStream();
          
          outputStream.write(postData);
          outputStream.flush();
          
          InputStream inputStream  = connection.getInputStream();
          byte[] bytes = new byte[256];
          StringBuilder builder = new StringBuilder();
          
          while(true) {
        	  int bytesRead = inputStream.read(bytes);
        	  if(bytesRead <= 0) {
        		  return builder.toString();
        	  }
        	  builder.append(new String(bytes, 0, bytesRead));
          }		
	}
}
