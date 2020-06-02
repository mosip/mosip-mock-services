package org.biometric.provider;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
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
	      new InfoRequest(), "/info");
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
	      new CaptureRequest(), "/capture");
	    bean.setLoadOnStartup(1);
	    return bean;
	}
	
	@SuppressWarnings("rawtypes")
	@Bean
	public ServletRegistrationBean secureCaptureBean() {
	    @SuppressWarnings("unchecked")
		ServletRegistrationBean bean = new ServletRegistrationBean(
	      new SecureCaptureRequest(), "/capture");
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
	    return bean;
	}

}