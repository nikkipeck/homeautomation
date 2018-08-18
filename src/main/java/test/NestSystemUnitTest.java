package main.java.test;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Properties;
import java.util.Vector;

import org.junit.jupiter.api.Test;

import main.java.systems.NestSystem;

/**
 * Unit tests for Nest
 */
public class NestSystemUnitTest {
	private static NestSystem ns = null;
	private Properties testProps = new Properties();
	private String client_id;
	private String client_secret;
	private String nest_code;
	private static final String GRANT_TYPE = "authorization_code";

	public NestSystemUnitTest() {
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
		
		//if we couldn't get the file from the file system, try the classloader running the risk of a stale nest_code
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
			
		if(testProps.containsKey("client_id"))
			client_id = testProps.getProperty("client_id");
		else
			fail("no client_id in properties file");
		
		if(client_id == null)
			fail("no client_id in properties file");

		if(testProps.containsKey("client_secret"))
			client_secret = testProps.getProperty("client_secret");
		else
			fail("no client_secret in properties file");
		
		if(client_secret == null)
			fail("no client_secret in properties file");
		
		if(testProps.containsKey("nest_code"))
			nest_code = testProps.getProperty("nest_code");
		else
			fail("no nest_code in properties file");
		
		if(nest_code == null)
			fail("no nest_code in properties file");			
				
	}
	
	@Test
	public void putDeviceTest() {
		String authToken = getDevices();
		if(ns == null)
			fail("Nest System not autheticated");
		try {
			Vector<String> regdevs = ns.getRegisteredDevices();
			for(int r=0; r<regdevs.size(); r++) {
				String resp = ns.setTargetTemperatureFarenheit(authToken, 69, regdevs.get(r));
				int erind = resp.indexOf("error");
				if(erind > -1)
					fail(resp.substring(erind+8, resp.indexOf(".\"}")));
			}
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			fail(e.getMessage());
		}
	}
	
	//Helper to authenticate and get device list
	String getDevices() {
		String authToken = null;
		try {		
			String postUrl = "https://api.home.nest.com/oauth2/access_token";
			
			Hashtable<String,String> reqbody = new Hashtable<String,String>();
			reqbody.put("client_id",client_id);
			reqbody.put("client_secret",client_secret);
			reqbody.put("grant_type",GRANT_TYPE);
			reqbody.put("code",nest_code);
					
			ns = new NestSystem(postUrl, reqbody);
			
			Hashtable<String,String> loginTokens = ns.loginAuthenticate();
			
			if(loginTokens.containsKey("ERROR"))
				fail("Error in login hashtable" + loginTokens.get("ERROR"));
			
			authToken = loginTokens.get("access_token");
			
			ns.registerAllThermostats(authToken);
		}
		catch(IllegalArgumentException iae)
		{
			fail("login contained illegal arguments");
		}
		catch(Exception e)
		{
			fail("Exception caught " + e);
		}
		return authToken;
	}
}