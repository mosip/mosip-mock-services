package org.biometric.provider;


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

@Configuration
@PropertySource(value = { "application.properties" })
public class WebMvcConfigure implements WebMvcConfigurer {
	private Environment env;
 
	@Autowired
	public WebMvcConfigure(Environment env) {
		this.env = env;
	}
	
    @Override
    public void configureDefaultServletHandling(
      DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
    
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Bean
	public ServletRegistrationBean infoBean() {
		ServletRegistrationBean bean = new ServletRegistrationBean(
	      new InfoRequest(env.getProperty("server.port")), "/info");
	    bean.setLoadOnStartup(1);
	    return bean;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Bean
	public ServletRegistrationBean discoverBean() {
		ServletRegistrationBean bean = new ServletRegistrationBean(
	      new DiscoverRequest(), "/device");
	    bean.setLoadOnStartup(1);
	    return bean;
	}
    
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Bean
	public ServletRegistrationBean captureBean() {
		ServletRegistrationBean bean = new ServletRegistrationBean(
	      new CaptureRequest(), "/capture");
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
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer properties() {		
		PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
		Resource[] resources = new ClassPathResource[] {new ClassPathResource("application.properties")};
		ppc.setLocations(resources);
		ppc.setTrimValues(true);
		return ppc;
	}	
}