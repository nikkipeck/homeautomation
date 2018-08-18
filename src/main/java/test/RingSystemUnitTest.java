package main.java.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

import main.java.systems.RingSystem;

import java.util.Properties;

import org.junit.jupiter.api.Test;

/**
 * Unit test for Ring
 */
public class RingSystemUnitTest {
	private static RingSystem rs = null;
	private Properties testProps = new Properties();
	private String username;
	private String password;
	
	public RingSystemUnitTest() {		
		InputStream in = null;
		try {
			File pfile = new File("./src/main/resources/ha.properties");
			in = new FileInputStream(pfile);
			testProps.load(in);
			in.close();
		}
		catch(IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		
		//if we couldn't get the file from the file system, try the classloader
		if(in == null) {
			try {
				String filename = "main/resources/ha.properties";
				ClassLoader cl = getClass().getClassLoader();
				URL res = Objects.requireNonNull(cl.getResource(filename),"Can't find configuration file " + filename);
				
				in = new FileInputStream(res.getFile());
				testProps.load(in);
				in.close();
			}
			catch(IOException ioex) {
				System.out.println(ioex.getMessage());
			}
		}
			
		if(testProps.containsKey("username"))
			username = testProps.getProperty("username");
		else
			fail("no username in properties file");
		
		if(username == null)
			fail("no username in properties file");

		if(testProps.containsKey("password"))
			password = testProps.getProperty("password");
		else
			fail("no password in properties file");
		
		if(password == null)
			fail("no password in properties file");
				
	}
	
	@Test
	public void putDeviceTest(){
		String authToken = getDevices();
		if(rs == null)
			fail("Ring System not autheticated");
		try {
			String resp = rs.devicesLigthOn(authToken);
			//Note: my device will always fail this test, it's a 1st gen camera with no light
			int erind = resp.indexOf("error");
			if(erind > -1)
				fail(resp.substring(erind+8, resp.indexOf(".\"}")));
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			fail(e.getMessage());
		}
	}
	
	@Test
	public void postDeviceTest(){
		String authToken = getDevices();
		if(rs == null)
			fail("Ring System not authenticated");
		try {
			String resp = rs.devicesSetDoorbotDoNotDisturb(authToken, 10);
			int erind = resp.indexOf("error");
			if(erind > -1)
				fail(resp.substring(erind+8, resp.indexOf(".\"}")));
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			fail(e.getMessage());
		}
	}
	
	//authentication and device helper
	String getDevices() {
		String authToken = "";
		try{
			String postUrl = "https://oauth.ring.com/oauth/token";
						
			Hashtable<String,String> reqbody = new Hashtable<String,String>();
			reqbody.put("client_id","ring_official_android");;
			reqbody.put("grant_type","password");
			reqbody.put("username",username);
			reqbody.put("password",password);
			reqbody.put("scope","client");
			
			rs = new RingSystem(postUrl, reqbody);
			
			Hashtable<String,String> loginTokens = rs.loginAuthenticate();
			
			if(loginTokens.containsKey("ERROR"))
				fail("Error in login hashtable" + loginTokens.get("ERROR"));
			
			authToken = loginTokens.get("access_token");
			
			rs.registerAllDevices(authToken);
		}
		catch(IllegalArgumentException iae)
		{
			fail("login contained illegal arguments");
		}
		return authToken;
	}
}
