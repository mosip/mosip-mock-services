package io.mosip.mock.sbi.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.exception.SBIException;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;

public class SBIMockService implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SBIMockService.class);	

	protected String biometricType;
	
	protected Thread runningThread = null;
	protected int serverPort = 0;
	protected ServerSocket serverSocket = null;
	protected boolean isStopped = false;
	
	/**
	 * Set filedirectory path and deviceType 
	 */
	public SBIMockService(String[] args) {
		super();
	}

	@Override
	public void run() {
		synchronized (this)
		{
			this.runningThread = Thread.currentThread ();
		}
		
		try
		{
			createServerSocket ();
			while (!isStopped ())
			{
				Socket clientSocket = null;
				try
				{
					clientSocket = this.serverSocket.accept ();
				}
				catch (IOException ex)
				{
					if (isStopped ())
					{
						LOGGER.info ("SBI Mock Service Stopped.");
						return;
					}
					throw new SBIException (ex.hashCode() + "", "SBI Mock Service Error Accepting Client Connection", new Throwable (ex.getLocalizedMessage()));
				}
				new Thread (new SBIWroker (clientSocket, getServerPort(), getBiometricType ())).start ();
			}			
		}
		catch (SBIException ex)
		{
			LOGGER.error("SBI Mock Service Error", ex);			
		}
		catch (Exception ex)
		{
			LOGGER.error("SBI Mock Service Error", ex);			
		}
		
		LOGGER.info ("SBI Mock Service Stopped.");
	}
	
	private void createServerSocket () throws SBIException
	{
		try
		{
			this.serverPort = getAvailabilePort ();
			InetAddress addr = InetAddress.getByName (ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.SERVER_ADDRESS));
			this.serverSocket = new ServerSocket (this.serverPort, 50, addr);

			LOGGER.info ("SBI Proxy Service started on port " + this.serverPort);
		}
		catch (IOException ex)
		{
			throw new SBIException (ex.hashCode() + "", "SBI Proxy Service Cannot open port " + this.serverPort, new Throwable (ex.getLocalizedMessage()));
		}
	}

	private int getAvailabilePort ()
	{
		int port = Integer.parseInt(ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.MIN_PORT));
		for (; port <= Integer.parseInt(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MAX_PORT)); port++)
		{
			if (!checkHostAvailability (port))
			{
				break;
			}
		}
		return port;
	}

	private static boolean checkHostAvailability (int port)
	{
		try
		{
			Socket s = new Socket (ApplicationPropertyHelper.getPropertyKeyValue (SBIConstant.SERVER_ADDRESS), port);
			return true;
		}
		catch (Exception ex)
		{
		}
		return false;
	}

	public boolean isStopped() {
		return isStopped;
	}

	public void setStopped(boolean isStopped) {
		this.isStopped = isStopped;
	}

	public String getBiometricType() {
		return biometricType;
	}

	public void setBiometricType(String biometricType) {
		this.biometricType = biometricType;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}		
}