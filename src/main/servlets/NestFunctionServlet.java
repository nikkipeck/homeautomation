package main.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import main.java.systems.NestSystem;

/**
 * Class to handle the implemented functions for a Nest System
 * post by NestFunction.jsp
 */
public class NestFunctionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private NestSystem ns = null;
	private String token = "";
	private int temperature= -1;
	
	/**
	 * Handle put request to change temperature
	 * @param req httprequest object stores temperature to put
	 * @param resp http response object used to communicate data to jsp
	 * TODO: set this up for other put requests, not just temp 
	 */
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException{
		mineSessionDetails(req, resp);
		
		String tempstr = req.getParameter("temperature");
		temperature = Integer.parseInt(tempstr);
		
		//use NestSystem to change the temp
		if(temperature < 0)
			response(resp, "Invalid temperature");
		
		Vector<String> devices = ns.getRegisteredDevices();
		String targetResp = "";
		for(String device: devices) {
			if(device == null || device.length() < 0)
				response(resp, "Invalid device");
				
			targetResp += ns.setTargetTemperatureFarenheit(token, temperature, device) + " ";
		}	
		
		displayStringResponse(resp, targetResp);
		
		targetResp = "";
	}
	
	/**
	 * Handle get request to discover all devices for this login
	 * @param req httprequest object stores system and session data
	 * @param resp http response object used to communicate data to jsp 
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		mineSessionDetails(req, resp);
		//use NestSytem to get all thermostats
		try {
			ns.registerAllThermostats(token);			
		}
		catch(Exception e) {			
			response(resp, e.getMessage());
		}
		
		Vector<String> devices = ns.getRegisteredDevices();
		displayVectorResponse(resp, devices);
	}
	
	/**
	 * Remind users that post is not supported by Nest 
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
		response(resp, "Post not supported by Nest API");
	}
	
	/**
	 * Write response to jsp with response String
	 * @param resp http response object used to communicate data to jsp
	 * @param respStr string response returned from system
	 * TODO:  a RequestDispatcher would be more flexible and elegant 
	 */
	private void displayStringResponse(HttpServletResponse resp, String respStr) {
		PrintWriter out = null;
		try {
			out = resp.getWriter();
			out.println("<html>");
			out.println("<body>");
			out.println("<table style=\"width:25%\">");
			out.println("<tr><th>Nest Thermostat</th>");			
			out.println("<td>" + respStr + "</td>");
			out.println("</tr></table></body></html>");
		}
		catch(IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		finally {
			out.close();
		}		
	}
	
	/**
	 * Write response to jsp with response Vector
	 * @param resp http response object used to communicate data to jsp
	 * @param devices Vector of device ids
	 * TODO:  a RequestDispatcher would be more flexible and elegant 
	 */
	private void displayVectorResponse(HttpServletResponse resp, Vector<String> devices) {
		PrintWriter out = null;
		try {
			out = resp.getWriter();
			out.println("<html>");
			out.println("<body>");
			out.println("<table style=\"width:25%\">");
			out.println("<tr><th>Nest Thermostat</th>");
			
			for(String device: devices) {
				out.println("<td>" + device + "</td>");
			}
			out.println("</tr></table></body></html>");
		}
		catch(IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		finally {
			out.close();
		}
	}
	
	/**
	 * Generic response writer
	 * @param resp http response object used to communicate data to jsp
	 * @param respStr message to write to jsp
	 * TODO:  a RequestDispatcher would be more flexible and elegant 
	 */
	private void response(HttpServletResponse resp, String msg){
		PrintWriter out = null;
		try {
			out = resp.getWriter();
			out.println("<html>");
			out.println("<body>");
			out.println("<t1>" + msg + "</t1>");
			out.println("</body>");
			out.println("</html>");
		}
		catch(IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		finally {
			out.close();
		}
	}
	
	/**
	 * Mine details from the request session object
	 * @param req http request object that stores the session and cookies
	 * @param resp http response object used to communicate back to jsp
	 */
	private void mineSessionDetails(HttpServletRequest req, HttpServletResponse resp) {
		//get nest system from the session
		HttpSession reqSession = req.getSession(false);
		ns = (NestSystem)reqSession.getAttribute("system");
		
		try {
			if(ns == null) {
				resp.sendRedirect("NestLogin.html"); //TODO; does this work?
				response(resp, "Unavailabe Nest system");
			}
		}
		catch(IOException e) {
			System.out.println(e.getMessage());
		}
		
		//get authtoken cookie
		Cookie[] cookies = req.getCookies();
		if(cookies != null){
			for(Cookie cookie : cookies){
				if(cookie.getName().equals("authToken")) 
					token = cookie.getValue();
			}
		}
		
		try {
			if(token == null) {
				resp.sendRedirect("NestLogin.html"); //TODO; does this work?
				response(resp, "Please login to Nest System");			
			}
		}
		catch(IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}
}
