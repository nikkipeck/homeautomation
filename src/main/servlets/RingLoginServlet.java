package main.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import main.java.systems.RingSystem;

/**
 * Class to handle a login to a Ring System
 * post by RingLogin.html
 */
public class RingLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Handle post request to login to Ring
	 * @param req httprequest object stores login credentials
	 * @param resp http response object used to communicate data to jsp 
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String authToken = null;
	
		//validate credentials
		String username = req.getParameter("username");
		String password = req.getParameter("password");
		if(username == null || password == null || username.length() < 1 || password.length() < 1)
			response(resp, "invalid login credentials for Ring Camera");
		
		String postUrl = "https://oauth.ring.com/oauth/token";
		
		try {			
			Hashtable<String,String> reqbody = new Hashtable<String,String>();
			reqbody.put("client_id","ring_official_android");;
			reqbody.put("grant_type","password");
			reqbody.put("username",username);
			reqbody.put("password",password);
			reqbody.put("scope","client");
			
			RingSystem rs = new RingSystem(postUrl, reqbody);
			
			Hashtable<String,String> loginTokens = rs.loginAuthenticate();
			if(loginTokens.containsKey("ERROR"))
				response(resp, "Login error: " + loginTokens.get("ERROR"));
			authToken = loginTokens.get("access_token");
			if(authToken != null) {
				//get the old session and invalidate
	            HttpSession oldSession = req.getSession(false);
	            if (oldSession != null) {
	                oldSession.invalidate();
	            }
	            //generate a new session
	            HttpSession newSession = req.getSession(true);

	            //setting session to expire in 10 mins
	            newSession.setMaxInactiveInterval(10*60);
	            
	            //send the nest system as a session attribute, to prevent having to re-authenticate
	            newSession.setAttribute("system", rs);

	            Cookie authtoken = new Cookie("authToken", authToken);
	            resp.addCookie(authtoken);

				resp.sendRedirect("RingFunctions.jsp");
			}
		}
		catch(IllegalArgumentException iae)
		{
			response(resp, "login contained illegal arguments");
		}
		catch(Exception e)
		{
			response(resp, "Exception caught " + e);
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
}