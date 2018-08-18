package main.java.systems;

import java.util.Hashtable;
import java.util.Vector;

/**
 * This interface represents a device system
 */
public interface DeviceSystem {
	/**
	 * This method registers a device. Cannot not be called before getDevice
	 * This is the beginning of a persistence model that is not fully implemented.
	 * @param deviceId represents device for registration
	 */
	public void registerDevice(String deviceid);
	
	/**
	 * This method processes a get request for all devices in a user account for a device system.
	 * . Cannot not be called before LoginAuthenticator
	 * @param url location for get request
	 * @param authToken oauth token for get request
	 */
	public Hashtable<String, String> getDevice(String url, String authToken);
	
	/**
	 * This method processes a put request for a system device
	 * Cannot be called before LoginAuthenticator
	 * @param url location for put request
	 * @param authToken oauth token for put request
	 * @param body for put request
	 */
	public Hashtable<String, String> putDevice(String url, String authToken, String body);
	
	/**
	 * This method processes a post request for a system device
	 * Cannot be called before LoginAuthenticator
	 * @param url location for post request
	 * @param authToken oauth token for post request
	 * @param body for post request
	 */
	public Hashtable<String, String> postDevice(String url, String authToken, String body);
	
	/**
	 * This method returns currently registered devices for a system
	 */
	public Vector<String> getRegisteredDevices();
}