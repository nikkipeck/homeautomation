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

import main.java.systems.RingSystem;

/**
 * Class to handle the implemented functions for a Ring System
 * post by RingFunction.jsp
 */
public class RingFunctionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private RingSystem rs = null;
	private String token = "";
	private int timeInMins = -1;
	
	/**
	 * Handle put request to turn floodlights on
	 * @param req httprequest object stores session and cookies
	 * @param resp http response object used to communicate data to jsp
	 * TODO: set this up for other put requests, not just lights 
	 */
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException{
		mineSessionDetails(req, resp);
		try {
			//hit the lights
			String lights = rs.devicesLigthOn(token);
			displayStringResponse(resp, lights);
		}
		catch(Exception e) {
			response(resp, e.getMessage());
		}
	}
	
	/**
	 * Handle post request to set do not disturb for doorbot
	 * @param req httprequest object stores system and session data
	 * @param resp http response object used to communicate data to jsp
	 * TODO: set this up for other put requests, not just dnd 
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException{
		mineSessionDetails(req, resp);
		
		String timestr = req.getParameter("timeInMins");
		timeInMins = Integer.parseInt(timestr);
		
		try {
			//set doorbot do not disturb
			String dnd = rs.devicesSetDoorbotDoNotDisturb(token, timeInMins);
			displayStringResponse(resp, dnd);
		}
		catch(Exception e) {
			response(resp, e.getMessage());
		}
	}
	
	/**
	 * Handle get request to discover all devices for this login
	 * @param req httprequest object stores system and session data
	 * @param resp http response object used to communicate data to jsp 
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		mineSessionDetails(req, resp);
		
		//use RingSystem to get all cameras
		try {
			rs.registerAllDevices(token);
		}
		catch(Exception e) {
			response(resp, e.getMessage());
		}
		
		Vector<String> devices = rs.getRegisteredDevices();
		displayVectorResponse(resp, devices);
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
			out.println("<tr><th>Ring Camera </th>");			
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
			out.println("<tr><th>Ring Camera</th>");
			
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
		//get ring system from the session
		HttpSession reqSession = req.getSession(false);
		rs = (RingSystem)reqSession.getAttribute("system");
		
		try {
			if(rs == null) {
				resp.sendRedirect("RingLogin.html"); //TODO; does this work?
				response(resp, "Unavailabe Ring system");
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
				resp.sendRedirect("RingLogin.html"); //TODO; does this work?
				response(resp, "Please login to Ring System");			
			}
		}
		catch(IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}


}
