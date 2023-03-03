package io.mosip.mock.sdk.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.model.SDKInfo;

public class SDKInfoService extends SDKService{
	private String apiVersion; 
	private String sample1,sample2, sample3; 
	Logger LOGGER = LoggerFactory.getLogger(SDKInfoService.class);

	public SDKInfoService(String apiVersion,String sample1, String sample2, String sample3)
	{
		this.apiVersion = apiVersion;
		this.sample1 = sample1;
		this.sample2 = sample2; 
		this.sample3 = sample3;
	}
	
	public SDKInfo getSDKInfo()
	{
		SDKInfo sdkInfo = new SDKInfo(this.apiVersion, this.sample1, this.sample2, this.sample3);
		List<BiometricType> supportedModalities = new ArrayList<>();
		supportedModalities.add(BiometricType.FINGER);
		supportedModalities.add(BiometricType.FACE);
		supportedModalities.add(BiometricType.IRIS);
		sdkInfo.setSupportedModalities(supportedModalities);
		Map<BiometricFunction, List<BiometricType>> supportedMethods = new HashMap<>();
		supportedMethods.put(BiometricFunction.MATCH, supportedModalities);
		supportedMethods.put(BiometricFunction.QUALITY_CHECK, supportedModalities);
		supportedMethods.put(BiometricFunction.EXTRACT, supportedModalities);
		supportedMethods.put(BiometricFunction.CONVERT_FORMAT, supportedModalities);
		sdkInfo.setSupportedMethods(supportedMethods);
		return sdkInfo;
	}
}