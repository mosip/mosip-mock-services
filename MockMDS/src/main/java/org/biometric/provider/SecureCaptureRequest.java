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

public class SecureCaptureRequest extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7250199164515356577L;
	
	ObjectMapper oB = null;

	
	@Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
		if(req.getMethod().contentEquals("CAPTURE"))
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
		CaptureRequestDeviceDetailDto bio = captureRequestDto.mosipBioRequest.get(0);
		String result="";
		if(bio.type.equalsIgnoreCase("FIR")) {
			if(bio.deviceId.equals("1") && bio.deviceSubId.equals("1"))
				result  = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") +"/files/MockMDS/leftFingerPrintSecureCapture.txt")));
			else if(bio.deviceId.equals("1") && bio.deviceSubId.equals("2"))
				result  = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") +"/files/MockMDS/rightFingerPrintSecureCapture.txt")));
			else if(bio.deviceId.equals("1") && bio.deviceSubId.equals("3"))
				result  = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") +"/files/MockMDS/thumbsFingerPrintSecureCapture.txt")));
		}else if(bio.type.equalsIgnoreCase("IIR")) {
			result  = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") +"/files/MockMDS/irisSecureCapture.txt")));
		}else if(bio.type.equalsIgnoreCase("Face")) {
			result  = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") +"/files/MockMDS/faceSecureCapture.txt")));
		}
		response.setContentType("application/json");
		response = CORSManager.setCors(response);
        PrintWriter out = response.getWriter();
        out.println(result);
	}
}