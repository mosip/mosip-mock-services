package org.biometric.provider;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.mosip.kernel.crypto.jce.core.CryptoCore;

@Configuration
//@PropertySource(value = { "application.properties" })
public class WebMvcConfigure implements WebMvcConfigurer {
 
 
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
	      new InfoRequest(new CryptoCore()), "/info");
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
	      new CaptureRequest(new CryptoCore()), "/capture");
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
	    PropertySourcesPlaceholderConfigurer props = new PropertySourcesPlaceholderConfigurer();
		props.setLocations(new Resource[] { new ClassPathResource("classpath:application.properties") });
	    return bean;
	}
	
}