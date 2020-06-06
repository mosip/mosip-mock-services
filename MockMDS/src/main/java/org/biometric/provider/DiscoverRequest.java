package org.biometric.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.registration.mdm.dto.CaptureRequestDto;

public class DiscoverRequest extends HttpServlet {  
  
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ObjectMapper oB = null;
	
	@Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        //if(req.getMethod().contentEquals("MOSIPDISC"))
        if(req.getMethod().contentEquals("MOSIPDISC") || req.getMethod().contentEquals("GET") || req.getMethod().contentEquals("POST"))
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
		if (oB == null)
			oB = new ObjectMapper();
		BufferedReader bR = request.getReader();
		String s = "";
		String sT = "";
		while ((s = bR.readLine()) != null) {
			sT = sT + s;
		}
		@SuppressWarnings("unchecked")
		Map<String, String> requestMap=oB.readValue(sT.getBytes(),
				Map.class);
		System.out.println(requestMap);
		String info = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/MockMDS/" + "discoverInfo" + ".txt")));
        response.setContentType("application/json");
        response = CORSManager.setCors(response);
        PrintWriter out = response.getWriter();
        out.println(info);
    }
}