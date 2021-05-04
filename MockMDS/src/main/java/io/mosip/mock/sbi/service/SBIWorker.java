package io.mosip.mock.sbi.service;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;

public class SBIWorker implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SBIWorker.class);	
	
	private SBIMockService mockService;
	private Socket clientSocket;
	private int serverPort;
	private String biometricType;
	
	/**
	 * Constructor SBIWroker 
	 */
	public SBIWorker(SBIMockService mockService, Socket clientSocket, int serverPort, String biometricType) {
		super();
		setMockService(mockService);
		setClientSocket(clientSocket);
		setServerPort(serverPort);
		setBiometricType(biometricType);
	}

	@Override
	public void run() {
		BufferedOutputStream bos = null;
		BufferedReader reader = null;
		try
		{
			if (getClientSocket() != null)
			{
				bos = new BufferedOutputStream (getClientSocket().getOutputStream ());
				reader = new BufferedReader (new InputStreamReader (getClientSocket().getInputStream (), "UTF-8"));
			}
			
			StringBuilder out = new StringBuilder ();
			String line;
			int contentLength = 0;
			try
			{
				while (! ( (line = reader.readLine ()).equals ("")))
				{
					if (line.indexOf ("content-length:") >= 0 || line.indexOf ("Content-Length:") >= 0)
					{
						contentLength = Integer.parseInt (line.substring (16, line.length ()));
					}
					out.append (line);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace ();
			}

			char[] requestBody = new char[contentLength];

			reader.read (requestBody, 0, contentLength);
			String strJsonRequest = out.append (new String (requestBody, 0, requestBody.length)).toString ();
			LOGGER.info("Request data :: "+strJsonRequest);
			String [] arrMethodName = strJsonRequest.split("HTTP/1.1");
			String strMethodName = null;
			if (arrMethodName != null && arrMethodName.length > 0)
			{
				arrMethodName = arrMethodName[0].trim().split("/");
				if (arrMethodName != null && arrMethodName.length > 0)
				{
					strMethodName = arrMethodName[0].trim();
				}
			}
			LOGGER.info("Method Name :: "+strMethodName);
			String corsHeaderMethods = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.CORS_HEADER_METHODS);
			
			String responseJson = null;
			if (strMethodName != null && corsHeaderMethods.contains (strMethodName))
			{
				LOGGER.info("Method Valid ::");
				if (strMethodName.equals("OPTIONS")) {
					responseJson = SBIResponseInfo.generateOptionsResponse();
				}
				else
				{
					SBIServiceResponse serviceResponse = new SBIServiceResponse (getServerPort ());
					responseJson = SBIResponseInfo.generateResponse ("en", getServerPort (), serviceResponse.getServiceresponse (getMockService(), getClientSocket(), strJsonRequest));				
				}
			}
			else
			{
				LOGGER.info("Method InValid ::");
				responseJson = SBIResponseInfo.generateErrorResponse ("en", getServerPort (), "500", "");
			}
			LOGGER.info("Response data :: "+responseJson);
			bos.write (responseJson.getBytes ());
			bos.flush ();
		}
		catch (IOException e)
		{
			try
			{
				bos.write (SBIResponseInfo.generateErrorResponse ("en", getServerPort (), "999", e.getMessage ()).getBytes ());
			}
			catch (IOException e1)
			{
				LOGGER.error (e1.getMessage ());
				e1.printStackTrace ();
			}
			e.printStackTrace ();
		}
		catch (Exception e)
		{
			LOGGER.info ("Catching exception");
			try
			{
				bos.write (SBIResponseInfo.generateErrorResponse ("en", getServerPort (), "999", e.getMessage ()).getBytes ());
			}
			catch (IOException e1)
			{
				LOGGER.error (e1.getMessage ());
				e1.printStackTrace ();
			}
			e.printStackTrace ();
		}
		finally
		{
			try
			{
				bos.close ();
			}
			catch (IOException e)
			{
				LOGGER.error (e.getMessage ());
				e.printStackTrace ();
			}
			try
			{
				reader.close ();
				if (getClientSocket() != null)
					getClientSocket().close ();
			}
			catch (IOException e)
			{
				LOGGER.error (e.getMessage ());
				e.printStackTrace ();
			}
		}
	}
     
	
	public SBIMockService getMockService() {
		return mockService;
	}

	public void setMockService(SBIMockService mockService) {
		this.mockService = mockService;
	}

	public Socket getClientSocket() {
		return clientSocket;
	}
	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	public int getServerPort() {
		return serverPort;
	}
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getBiometricType() {
		return biometricType;
	}
	public void setBiometricType(String biometricType) {
		this.biometricType = biometricType;
	}	
	
	
}
