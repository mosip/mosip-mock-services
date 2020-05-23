package org.biometric.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.registration.mdm.dto.CaptureRequestDeviceDetailDto;
import io.mosip.registration.mdm.dto.CaptureRequestDto;

public class CaptureRequest extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7250199164515356577L;
	
	ObjectMapper oB = null;

	
	@Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
		if(req.getMethod().contentEquals("RCAPTURE"))
			doPost(req, res);
		if(req.getMethod().contentEquals("OPTIONS"))
			CORSManager.doOptions(req, res);
    }
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if(oB==null)
			oB = new ObjectMapper();
		BufferedReader bR = request.getReader();
		String s="";
		String sT = "";
		while((s = bR.readLine())!=null) {
			sT = sT+s;
		}
		CaptureRequestDto captureRequestDto = (CaptureRequestDto)(oB.readValue(sT.getBytes(), CaptureRequestDto.class));
		CaptureRequestDeviceDetailDto bio = captureRequestDto.getMosipBioRequest().get(0);
		String result="";
		if(bio.getType().equalsIgnoreCase("FIR")) {
			if(bio.getDeviceId().equals("1") && bio.getDeviceSubId().equals("1"))
				result  = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") +"\\files\\MockMDS\\leftFingerPrintCapture.txt")));
			else if(bio.getDeviceId().equals("1") && bio.getDeviceSubId().equals("2"))
				result  = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") +"\\files\\MockMDS\\rightFingerPrintCapture.txt")));
			else if(bio.getDeviceId().equals("1") && bio.getDeviceSubId().equals("3"))
				result  = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") +"\\files\\MockMDS\\thumbsFingerPrintCapture.txt")));
		}else if(bio.getType().equalsIgnoreCase("IIR")) {
			result  = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") +"\\files\\MockMDS\\irisCapture.txt")));
		}else if(bio.getType().equalsIgnoreCase("Face")) {
			result  = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") +"\\files\\MockMDS\\faceCapture.txt")));
		}
		response.setContentType("text/html");
		response.setHeader("Access-Control-Allow-Origin","*");
        PrintWriter out = response.getWriter();
        out.println(result);
	}
}