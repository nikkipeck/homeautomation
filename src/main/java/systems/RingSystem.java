package main.java.systems;

import main.java.authentication.LoginAuthenticator;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.json.JsonArray;
import javax.json.JsonObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * class that implements Ring as a device system
 */
public class RingSystem extends LoginAuthenticator implements DeviceSystem {
	private String url = "https://api.ring.com/clients_api";
	private static final String MEDIA_STRING = "application/x-www-form-urlencoded";
	private Vector<String> deviceIds = new Vector<String>();
	
	/**
	 * Constructor for a RingSystem
	 * delegates to LoginAuthenticator to save login information
	 * @param postUrl location for login post request
	 * @param requestBody hashtable of key-value pairs for login
	 */
	public RingSystem(String postUrl, Hashtable<String, String> requestBody) {
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
	 * get requests, to get list of Ring devices 
	 * @param url location for get request
	 * @param authToken oauth token for get request
	 */
	@Override
	public Hashtable<String,String> getDevice(String url, String authToken) {
		Hashtable<String,String> data = new Hashtable<String,String>();
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				  .url(url)
				  .get()
				  .addHeader("Authorization", "Bearer " + authToken)
				  .addHeader("Content-Type", "application/json")
				  .addHeader("Cache-Control", "no-cache")
				  .build();
		
		Response response = null;
		try{
			response = client.newCall(request).execute();
			String jsonData = response.body().string();
			data.put("RESPONSE", jsonData);
		}
		catch(IOException ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace();
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
		MediaType mediaType = MediaType.parse(MEDIA_STRING);
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
			String jsonData = response.body().string();
			data.put("RESPONSE", jsonData);
		}
		catch(IOException ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace();
		}
		return data;
	}
	
	/**
	 * Implementation of DeviceSystem.postDevice
	 * posts requests, to set data on devices 
	 * @param url location for post request
	 * @param authToken oauth token for post request
	 */
	public Hashtable<String,String> postDevice(String url, String authToken, String body) {
		Hashtable<String,String> data = new Hashtable<String,String>();
		OkHttpClient client = new OkHttpClient();
		MediaType mediaType = MediaType.parse(MEDIA_STRING);
		RequestBody rbody = RequestBody.create(mediaType, body);
		Request request = new Request.Builder()
				  .url(url)
				  .post(rbody)
				  .addHeader("Authorization", "Bearer " + authToken)
				  .addHeader("Content-Type", "application/json")
				  .addHeader("Cache-Control", "no-cache")
				  .build();
		
		Response response = null;
		try{
			response = client.newCall(request).execute();
			String jsonData = response.body().string();
			data.put("RESPONSE", jsonData);
		}
		catch(IOException ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace();
		}
		return data;
	}
	
	/**
	 * Register all Ring Cameras for a given authorization token
	 * leverages getDevice
	 * @param authToken oauth token for get request
	 */
	public void registerAllDevices(String authToken) {
		String getDevicesUrl = url + "/ring_devices";
		Hashtable<String,String> devices = getDevice(getDevicesUrl, authToken);
		
		if(devices != null) {
			String devstr = (String)devices.get("RESPONSE");
			JsonObject jo = super.jsonFromString(devstr);
			
			JsonArray ja = jo.getJsonArray("stickup_cams");
			for (javax.json.JsonValue jvalue : ja) {
				if(jvalue != null) {
					System.out.println("jvalue " + jvalue);
					JsonObject job = (JsonObject)jvalue;
					registerDevice(job.getJsonNumber("id").toString());
					System.out.println("registered: " + job.getJsonNumber("id").toString());					
				}				
			}
		}
	}
	
	/**
	 * Turn the floodlights on for a camera
	 * leverages putDevice
	 * @param authToken oauth token for get request
	 */
	public String devicesLigthOn(String authToken) {
		if(deviceIds.size() < 1)
			return "error, no devices to light";
		String response = "";
		for(int d=0; d<deviceIds.size(); d++) {
			String deviceid = deviceIds.get(d).toString();
			var lightsUrl = url + "/doorbots/" + deviceid + "/floodlight_light_on";
			Hashtable<String,String> devices = putDevice(lightsUrl, authToken, null);
			response += "device " + deviceid + " response: " + devices.get("RESPONSE");
		}
		return response;
	}
	
	/**
	 * Set the doorbot do not disturb in minutes
	 * leverages postDevice
	 * @param authToken oauth token for get request
	 * @param timeInMins is the number of minutes to set do not disturb for
	 */
	public String devicesSetDoorbotDoNotDisturb(String authToken, int timeInMins) {
		if(deviceIds.size() < 1)
			return "error, no devices to set doorbot 'do not disturb'";
		String response = "";
		for(int d=0; d<deviceIds.size(); d++) {
			String deviceid = deviceIds.get(d).toString();
			var doorUrl = url + "/doorbots/" + deviceid + "/motion_snooze";
			String body = "time=" + timeInMins;
			Hashtable<String,String> devices = postDevice(doorUrl, authToken, body);
			response += "device " + deviceid + " response: " + devices.get("RESPONSE");
		}
		return response;
	}
}
