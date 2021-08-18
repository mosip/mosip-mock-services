package io.mosip.mock.sbi.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBIDeviceHelper;
import io.mosip.mock.sbi.devicehelper.face.SBIFaceHelper;
import io.mosip.mock.sbi.devicehelper.finger.single.SBIFingerSingleHelper;
import io.mosip.mock.sbi.devicehelper.finger.slap.SBIFingerSlapHelper;
import io.mosip.mock.sbi.devicehelper.iris.binacular.SBIIrisDoubleHelper;
import io.mosip.mock.sbi.devicehelper.iris.monocular.SBIIrisSingleHelper;
import io.mosip.mock.sbi.exception.SBIException;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;

public class SBIMockService implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SBIMockService.class);	

	protected static String profileId = SBIConstant.PROFILE_DEFAULT;
	protected String purpose;
	protected String biometricType;
	protected HashMap <String, SBIDeviceHelper> deviceHelpers = new HashMap<>();
	
	protected Thread runningThread = null;
	protected int serverPort = 0;
	protected ServerSocket serverSocket = null;
	protected boolean isStopped = false;
	
	/**
	 * Set Purpose and biometricType
	 */
	public SBIMockService(String purpose, String biometricType) {
		super();
		setPurpose (purpose);
		setBiometricType (biometricType);
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
			initDeviceHelpers();
			while (!isStopped ())
			{
				Socket clientSocket = null;
				try
				{
					clientSocket = this.serverSocket.accept ();
					//clientSocket.setKeepAlive(true);
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
				new Thread (new SBIWorker (this, clientSocket, getServerPort(), getBiometricType ())).start ();
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
	
	private void initDeviceHelpers() {
		if (getBiometricType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMTRIC_DEVICE)) ||
				getBiometricType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)))
		{
			if (getPurpose ().equalsIgnoreCase(SBIConstant.PURPOSE_REGISTRATION))
				this.deviceHelpers.put(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP), SBIFingerSlapHelper.getInstance(getServerPort(), getPurpose ()));
			else if (getPurpose ().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH))
				this.deviceHelpers.put(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE), SBIFingerSingleHelper.getInstance(getServerPort(), getPurpose ()));
		}
		if (getBiometricType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMTRIC_DEVICE)) ||
				getBiometricType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)))
		{
			if (getPurpose ().equalsIgnoreCase(SBIConstant.PURPOSE_REGISTRATION))
				this.deviceHelpers.put(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE), SBIIrisDoubleHelper.getInstance(getServerPort(), getPurpose ()));		
			else if (getPurpose ().equalsIgnoreCase(SBIConstant.PURPOSE_AUTH))
				this.deviceHelpers.put(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE), SBIIrisSingleHelper.getInstance(getServerPort(), getPurpose ()));		
		}
		if (getBiometricType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMTRIC_DEVICE)) ||
				getBiometricType().equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)))
		{
			this.deviceHelpers.put(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE) + "_" + ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE), SBIFaceHelper.getInstance(getServerPort(), getPurpose ()));
		}
	}
	
	public SBIDeviceHelper getDeviceHelper (String deviceTypeName)
    {
        if (this.deviceHelpers != null && this.deviceHelpers.size() >= 0)
        {
            if (this.deviceHelpers.containsKey(deviceTypeName) )
            {
                return this.deviceHelpers.get(deviceTypeName);
            }
        }

        return null;
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

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getProfileId() {
		return profileId;
	}

	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}		
}