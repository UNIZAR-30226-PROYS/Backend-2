package es.eina.error;

import org.json.JSONObject;

public class Constants {

	/**
	 * Standard error JSON.
	 */
	public static final String ERROR;


	static {
		JSONObject json = new JSONObject();
		json.put("error", 1);
		ERROR = json.toString();
	}
}
