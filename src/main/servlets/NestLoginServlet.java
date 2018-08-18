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

import main.java.systems.NestSystem;

/**
 * Class to handle a login to a Nest System
 * post by NestLogin.html
 */
public class NestLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Handle post request to login to Nest
	 * @param req httprequest object stores login credentials
	 * @param resp http response object used to communicate data to jsp 
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String authToken = null;
		
		String clientid = req.getParameter("client_id");
		String clientsecret = req.getParameter("client_secret");
		String code = req.getParameter("nest_code");
		//validate credentials
		if(clientid == null || clientid.length() < 1 || clientsecret == null || clientsecret.length() < 1 || code == null || code.length() < 1)
			response(resp, "invalid login credentials for Nest Thermostat");
		
		String grant_type = "authorization_code";
		String postUrl = "https://api.home.nest.com/oauth2/access_token";
		try {		
			Hashtable<String,String> reqbody = new Hashtable<String,String>();
			reqbody.put("client_id",clientid);
			reqbody.put("client_secret",clientsecret);
			reqbody.put("grant_type",grant_type);
			reqbody.put("code",code);
					
			NestSystem ns = new NestSystem(postUrl, reqbody);
			
			Hashtable<String,String> loginTokens = ns.loginAuthenticate();
			
			if(loginTokens.containsKey("ERROR"))
				response(resp,"Login error: " + loginTokens.get("ERROR"));
			
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
	            newSession.setAttribute("system", ns);
	            
	            //send the authtoken in the response as a cookie
	            Cookie authtoken = new Cookie("authToken", authToken);
	            resp.addCookie(authtoken);
	            resp.sendRedirect("NestFunctions.jsp");
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
