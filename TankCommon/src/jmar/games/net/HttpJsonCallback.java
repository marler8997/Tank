package jmar.games.net;

import org.json.JSONException;
import org.json.JSONObject;

public interface HttpJsonCallback {
	public void Error(String error);
	public void Success(JSONObject json) throws JSONException;
}
