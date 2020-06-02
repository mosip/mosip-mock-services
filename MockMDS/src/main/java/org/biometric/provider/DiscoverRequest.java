package org.biometric.provider;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DiscoverRequest extends HttpServlet {  
  
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
		if(req.getMethod().contentEquals("MOSIPDISC") || req.getMethod().contentEquals("GET"))
            doPost(req, res);
        if(req.getMethod().contentEquals("OPTIONS"))
            CORSManager.doOptions(req, res);
            if(req.getMethod().contentEquals("POST"))
			CORSManager.doOptions(req, res);
    }
	
	@Override
    protected void doPost(
      HttpServletRequest request, 
      HttpServletResponse response) throws ServletException, IOException {
		String info = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/" + "discoverInfo" + ".txt")));
        response.setContentType("application/json");
        response = CORSManager.setCors(response);
        PrintWriter out = response.getWriter();
        out.println(info);
    }
}