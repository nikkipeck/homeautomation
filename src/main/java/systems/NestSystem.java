package main.java.systems;

import main.java.authentication.LoginAuthenticator;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import javax.json.JsonObject;

/**
 * Class that implements Nest as a device system
 */
public class NestSystem extends LoginAuthenticator implements DeviceSystem {
	
	private static final String DEV_URL = "https://developer-api.nest.com";
	private Vector<String> deviceIds = new Vector<String>();
	private static final int REDIRECT_LIMIT = 3;
	
	/**
	 * Constructor for a NestSystem
	 * delegates to LoginAuthenticator to save login information
	 * @param postUrl location for login post request
	 * @param requestBody hashtable of key-value pairs for login
	 */
	public NestSystem(String postUrl, Hashtable<String, String> requestBody) {
		super(postUrl, requestBody);
	}

	/**
	 * Implementation of DeviceSystem.registerDevice
	 * stores devices in a Vector
	 * @param deviceId device to be registered
	 */
	@Override
	public void registerDevice(String deviceId) {
		deviceIds.add(deviceId);
	}
	
	/**
	 * Implementation of DeviceSystem.getRegisteredDevices
	 * returns registered devices in a Vector<String>
	 */
	@Override
	public Vector<String> getRegisteredDevices(){
		return deviceIds;
	}	

	/**
	 * Implementation of DeviceSystem.getDevice
	 * get requests, to get list of Nest devices 
	 * @param url location for get request
	 * @param authToken oauth token for get request
	 */
	@Override
	public Hashtable<String,String> getDevice(String url, String authToken) {
		Hashtable<String,String> data = new Hashtable<String,String>();
		//redirects must be explicitly followed, because NEST strips the headers, including auth token when redirecting
		OkHttpClient client = new OkHttpClient().newBuilder()
			      .followRedirects(false)
			      .build();
		Request request = new Request.Builder()
				  .url(url)
				  .get()
				  .addHeader("Content-Type", "application/json")
				  .addHeader("Authorization", "Bearer " + authToken)
				  .addHeader("Cache-Control", "no-cache")
				  .build();
		
		Response response = null;
		try{
			response = client.newCall(request).execute();
			
			//Explicitly handle a redirect
			if(response.code() == 307) {
				for(int retries=0; retries<REDIRECT_LIMIT; retries++)
				{
					Request req2 = buildRedirectRequest(response, authToken, "GET", null);
					response = null;
					response = client.newCall(req2).execute();
					if(response.code() != 307)
						retries = REDIRECT_LIMIT; //short circuit retry loop
				}
			}
			String jsonData = response.body().string();
			data.put("RESPONSE", jsonData);
		}
		catch(IOException ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace();
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * Implementation of DeviceSystem.putDevice
	 * put requests, to set data on devices 
	 * @param url location for put request
	 * @param authToken oauth token for put request
	 */
	@Override
	public Hashtable<String,String> putDevice(String url, String authToken, String body) {
		Hashtable<String,String> data = new Hashtable<String,String>();
		
		OkHttpClient client = new OkHttpClient();
		MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
		RequestBody reqbody = RequestBody.create(mediaType,body);
		Request request = new Request.Builder()
				  .url(url)
				  .put(reqbody)
				  .addHeader("Authorization", "Bearer " + authToken)
				  .addHeader("Cache-Control", "no-cache")
				  .build();
		Response response = null;
		try{
			response = client.newCall(request).execute();
			if(response.code() == 307) {
				for(int retries=0; retries<REDIRECT_LIMIT; retries++)
				{
					Request req2 = buildRedirectRequest(response, authToken, "PUT", body);
					response = null;
					response = client.newCall(req2).execute();
					if(response.code() != 307)
						retries = REDIRECT_LIMIT; //short circuit retry loop
				}
			}
			String jsonData = response.body().string();
			data.put("RESPONSE", jsonData);
		}
		catch(IOException ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace();
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return data;
	}
	
	/**
	 * Implementation of DeviceSystem.postDevice
	 * Nest API does not support post requests, this method returns an error
	 */
	@Override
	public Hashtable<String,String> postDevice(String url, String authToken, String body) {
		Hashtable<String,String> data = new Hashtable<String,String>();
		data.put("ERROR", "Nest API does not support http post requests");
		return data;
	}
	
	/**
	 * Register all Nest Thermostats for a given authorization token
	 * leverages getDevice
	 * @param authToken oauth token for get request
	 */
	public void registerAllThermostats(String authToken) throws Exception {
		Hashtable<String,String> devices = getDevice(DEV_URL, authToken);
		if(devices != null) {
			String devstr = (String)devices.get("RESPONSE");
			if(devstr.indexOf("error") > -1) 
				throw new Exception("ERROR" + devstr);
			
			JsonObject jo = super.jsonFromString(devstr);			
			JsonObject devicesjson = jo.getJsonObject("devices");
			JsonObject thermoHolder = devicesjson.getJsonObject("thermostats");
			
			Set<String> keys = thermoHolder.keySet();
			for(String k:keys)
				registerDevice(k);			
		}
	}
	
	/**
	 * Set the target temperature in farenheit for a given device id and auth token
	 * leverages putDevice
	 * @param authToken oauth token for get request
	 * @param targetTemp temperature to set on device
	 * @param id of device to set target temperature for
	 */
	public String setTargetTemperatureFarenheit(String authToken, int targetTemp, String deviceId) {
		if(deviceId == null || deviceId.length() < -1)
			return "ERROR: deviceId must be present";
		if(targetTemp < 50 || targetTemp > 90)
			return "ERROR: target temperature out of range. Allowed range is 50-90";
		
		String response = "";
		
		String url = "https://developer-api.nest.com/devices/thermostats/" + deviceId+ "/target_temperature_f";
		String body = "{\"target_temperature_f\": "+ targetTemp + "}";
				
		Hashtable<String,String> devices = putDevice(url, authToken, body);
		response += "device " + deviceId + " response: " + devices.get("RESPONSE");
		return response;
	}
	
	/**
	 * Helper method for 307's from Nest.
	 * Nest strips all headers from redirect requests, making it necessary to rebuild a request with the proper headers
	 * when a redirect is received.
	 * @param response holds the location for the redirect
	 * @param authToken oauth token for redirect
	 * @param action either GET or PUT so the correct type of request can be build
	 * @param body to be added to put requests only
	 * 
	 */
	private Request buildRedirectRequest(Response response, String authToken, String action, String body) throws Exception{
		if(authToken == null || authToken.length() < 1)
			throw new Exception("Authorization token cannot be null");
		if(action == null || action.length() < 1)
			throw new Exception("Request action cannot be null");
		
		//get the redirect url
		String location = response.header("Location");
		
		//For some reason their redirect url packs a module name on the end, but will return a 404 if I don't strip it off.
		int temploc = location.indexOf("/target_temperature_f"); 
		if(temploc > 0)
			location = location.substring(0,temploc);
		
		if(location == null || location.length() < 1)
			throw new Exception("Response did not provide redirect url");
		
		if(action.equals("GET")) {
			Request request = new Request.Builder()
					  .url(location)
					  .get()
					  .addHeader("Content-Type", "application/json")
					  .addHeader("Authorization", "Bearer " + authToken)
					  .build();
			return request;
		}
		else if(action.equals("PUT")) {
			MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
			RequestBody reqbody = RequestBody.create(mediaType,body);
			Request request = new Request.Builder()
					  .url(location)
					  .put(reqbody)
					  .addHeader("Content-Type", "application/json")
					  .addHeader("Authorization", "Bearer " + authToken)
					  .build();
			return request;
		}
		throw new Exception("No redirect request built");
	}
}
