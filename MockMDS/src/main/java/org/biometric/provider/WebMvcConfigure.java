package org.biometric.provider;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import java.io.File;
import java.io.FileInputStream;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.crypto.jce.core.CryptoCore;
import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.registration.mdm.dto.BioMetricsDataDto;
import io.mosip.registration.mdm.dto.DeviceInfo;
import io.mosip.registration.mdm.dto.DeviceSBIInfo;
import io.mosip.registration.mdm.dto.DeviceSBISubType;

@Configuration
@PropertySource(value = { "application.properties" })
public class WebMvcConfigure implements WebMvcConfigurer {

	@Autowired
	private CryptoCore cryptoCore;
	
	@Autowired
	private Environment env;
	
	private static ObjectMapper oB = new ObjectMapper();

	BioMetricsDataDto bioMetricsData = null;
 
 
    @Override
    public void configureDefaultServletHandling(
      DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
    

	@SuppressWarnings("rawtypes")
	@Bean
	public ServletRegistrationBean infoBean() {
	    @SuppressWarnings("unchecked")
		ServletRegistrationBean bean = new ServletRegistrationBean(
	      new InfoRequest(cryptoCore, env.getProperty("server.port")), "/info");
	    bean.setLoadOnStartup(1);
	    return bean;
	}

	@SuppressWarnings("rawtypes")
	@Bean
	public ServletRegistrationBean discoverBean() {
	    @SuppressWarnings("unchecked")
		ServletRegistrationBean bean = new ServletRegistrationBean(
	      new DiscoverRequest(), "/device");
	    bean.setLoadOnStartup(1);
	    return bean;
	}
    
	
	@SuppressWarnings("rawtypes")
	@Bean
	public ServletRegistrationBean captureBean() {
	    @SuppressWarnings("unchecked")
	    
	    ServletRegistrationBean bean = new ServletRegistrationBean(
	    		      new CaptureRequest(cryptoCore, env.getProperty("mosip.device.build.version")), "/capture");
	    bean.setLoadOnStartup(1);
	    return bean;
	}
	
	@SuppressWarnings("rawtypes")
	@Bean
	public ServletRegistrationBean streamBean() {
	    @SuppressWarnings("unchecked")
		ServletRegistrationBean bean = new ServletRegistrationBean(
	      new StreamRequest(), "/stream");
	    bean.setLoadOnStartup(1);
	    //PropertySourcesPlaceholderConfigurer props = new PropertySourcesPlaceholderConfigurer();
		//props.setLocations(new Resource[] { new ClassPathResource("classpath:application.properties") });
	    return bean;
	}
	
	
	  @Bean 
	  public BioMetricsDataDto getBioMetricsDataDtoBean() { 
		  try {
			  if(env.getProperty("mosip.device.build.version").equals(SBIConstant.BIOMETRIC_VERSION)) {
				  File f = new File(System.getProperty("user.dir") + "/SBIfiles/MockMDS/" + "DeviceInfoFACE" + ".txt");
				  DeviceSBIInfo dto = oB.readValue(f, DeviceSBIInfo.class);
				  System.out.println(dto);  
			  }else {
				  File f = new File(System.getProperty("user.dir") + "/files/MockMDS/" + "DeviceInfoFACE" + ".txt");
				  DeviceSBIInfo dto = oB.readValue(f, DeviceSBIInfo.class);
				  System.out.println(dto);
			  }
			  
				
				/*
				 * bioMetricsData = oB.readValue( Base64.getDecoder() .decode(new
				 * String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") +
				 * "/SBIfiles/MockMDS/registration/" + "Left IndexFinger" + ".txt")))),
				 * BioMetricsDataDto.class);
				 */
				 
		  	} catch (IOException e) { 
			  // TODO Auto-generated catch block
			  e.printStackTrace(); 
			 }
		  return bioMetricsData; 
	 }
	 
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer properties() {		
		PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
		Resource[] resources = new ClassPathResource[] {new ClassPathResource("application.properties")};
		ppc.setLocations(resources);
		ppc.setTrimValues(true);
		return ppc;
	}
	
}