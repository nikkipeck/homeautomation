package main.java.authentication;

import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Set;

import javax.json.JsonReader;
import javax.json.Json;
import javax.json.JsonObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/** Superclass represents an open authentication (oauth) login to an endpoint
 * 
 * @author npeck
 ** @version 0.0
 */

public class LoginAuthenticator {
	protected Hashtable<String,String> requestBody = new Hashtable<String,String>();
	protected String url = "";
	protected String accessToken = "";
	protected String refreshToken = "";

	/**
	 * Constructor that sets up values for oauth POST
	 *  @param requestBody hashtable storing request body attributes
	 *  @param postUrl url to POST login request
	 * TODO: Nice to eventually make headers a flexible attribute
	 */
	public LoginAuthenticator(String postUrl, Hashtable<String,String>requestBody)  throws IllegalArgumentException{
		if(postUrl == null || postUrl.length() < 1)
			throw new IllegalArgumentException("url cannot be null");
		
		if(requestBody == null || requestBody.size() < 0)
			throw new IllegalArgumentException("request body cannot be null");		
		
		this.requestBody = requestBody;
		this.url = postUrl;
	}
	
	/**
	 * Method to POST login request and save access and refresh tokens
	 * @return Hasthable containing tokens, or errors
	 */
	public Hashtable<String,String> loginAuthenticate() {
		Hashtable<String, String> tokensOrErrors = new Hashtable<String, String>();
				
		OkHttpClient client = new OkHttpClient();
		MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
		
		String bodystr = stringifyBodyHash(requestBody);
		if(bodystr.indexOf("ERROR") > -1) {
			tokensOrErrors.put("ERROR", bodystr);
			return tokensOrErrors;
		}
		
		RequestBody body = RequestBody.create(mediaType, bodystr);
		Request request = new Request.Builder()
		  .url(this.url)
		  .post(body)
		  .addHeader("Content-Type", "application/x-www-form-urlencoded")
		  .addHeader("Cache-Control", "no-cache")
		  .build();
		
		Response response = null;
		try{
			response = client.newCall(request).execute();
			if(response != null) {
				String jsonData = response.body().string();
				
				if(jsonData.indexOf("error") > -1)
					throw new IOException(jsonData);
					
				
				JsonObject jo = jsonFromString(jsonData);
				if(jo.containsKey("access_token")) {
					accessToken = jo.getString("access_token");
					if(accessToken != null && accessToken.length() > 1)
						tokensOrErrors.put("access_token", accessToken);
				}
				if(jo.containsKey("refresh_token")) {
					refreshToken = jo.getString("refresh_token");
					if(refreshToken != null && refreshToken.length() > 1)
						tokensOrErrors.put("refresh_token", refreshToken);
				}
				return tokensOrErrors;
			}
		}
		catch(IOException ioe) {
			System.out.println(ioe.getMessage());
			tokensOrErrors.put("ERROR", ioe.getMessage());
			return tokensOrErrors;
		}
				
		tokensOrErrors.put("ERROR", "no response");
		return tokensOrErrors;
	}
	
	/**
	 * Method to turn body hashtable into a body string 
	 * @return String for body text, or error
	 */
	public String stringifyBodyHash(Hashtable<String,String> body) {
		StringBuffer bodystrb = new StringBuffer();
		Set<String> keys = body.keySet();
        for(String key: keys){
        	String value = body.get(key);
        	if(value == null || value.length() < 1) 
        		return "ERROR invalid body value for key";
        	
	        bodystrb.append(key);
	        bodystrb.append("=");
	        bodystrb.append(value);
	        bodystrb.append("&");
        }
        //chop the extra ampersand
        return bodystrb.substring(0,bodystrb.length()-1);
	}
	
	/**
	 * Method that turns a json string into a JsonObject 
	 * @return JsonObject
	 */
	public static JsonObject jsonFromString(String jsonStr) {
		JsonReader jsonReader = Json.createReader(new StringReader(jsonStr));
	    JsonObject object = jsonReader.readObject();
	    jsonReader.close();

	    return object;
	}
}
