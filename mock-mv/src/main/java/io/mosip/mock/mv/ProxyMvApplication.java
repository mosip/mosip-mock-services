package io.mosip.mock.mv;

import io.mosip.kernel.applicanttype.api.impl.ApplicantTypeImpl;
import io.mosip.kernel.idgenerator.machineid.impl.MachineIdGeneratorImpl;
import io.mosip.kernel.idgenerator.mispid.impl.MispIdGeneratorImpl;
import io.mosip.kernel.idgenerator.regcenterid.impl.RegistrationCenterIdGeneratorImpl;
import io.mosip.kernel.idgenerator.rid.impl.RidGeneratorImpl;
import io.mosip.kernel.idgenerator.tokenid.impl.TokenIdGeneratorImpl;
import io.mosip.kernel.idgenerator.vid.impl.VidGeneratorImpl;
import io.mosip.kernel.idgenerator.vid.util.VidFilterUtils;
import io.mosip.kernel.idobjectvalidator.config.IdObjectValidatorConfig;
import io.mosip.kernel.idvalidator.mispid.impl.MispIdValidatorImpl;
import io.mosip.kernel.idvalidator.prid.impl.PridValidatorImpl;
import io.mosip.kernel.idvalidator.rid.impl.RidValidatorImpl;
import io.mosip.kernel.idvalidator.uin.impl.UinValidatorImpl;
import io.mosip.kernel.idvalidator.vid.impl.VidValidatorImpl;
import io.mosip.kernel.licensekeygenerator.misp.impl.MISPLicenseKeyGeneratorImpl;
import io.mosip.kernel.licensekeygenerator.misp.util.MISPLicenseKeyGeneratorUtil;
import io.mosip.kernel.pdfgenerator.impl.PDFGeneratorImpl;
import io.mosip.kernel.qrcode.generator.zxing.QrcodeGeneratorImpl;
import io.mosip.kernel.templatemanager.velocity.builder.TemplateManagerBuilderImpl;
import io.mosip.kernel.transliteration.icu4j.impl.TransliterationImpl;
import io.mosip.kernel.websub.api.config.IntentVerificationConfig;
import io.mosip.kernel.websub.api.config.WebSubClientConfig;
import io.mosip.kernel.websub.api.config.publisher.RestTemplateHelper;
import io.mosip.kernel.websub.api.config.publisher.WebSubPublisherClientConfig;
import io.mosip.mock.mv.queue.Listener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jms.autoconfigure.JmsAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.web.client.RestTemplate;

/**
 * Main class for the Proxy MV (Matching Verification) Application.
 * <p>
 * This class is responsible for bootstrapping the Spring Boot application and
 * initializing required components such as JMS listeners and REST templates.
 * </p>
 */
@ComponentScan(basePackages = { "io.mosip.mock.mv" })
@SpringBootApplication
@EnableAutoConfiguration(exclude = {
		JmsAutoConfiguration.class,
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		// kernel-core registers these via AutoConfiguration.imports; mock-mv does not use them
		ApplicantTypeImpl.class,
		MachineIdGeneratorImpl.class,
		VidGeneratorImpl.class,
		VidFilterUtils.class,
		TokenIdGeneratorImpl.class,
		RegistrationCenterIdGeneratorImpl.class,
		MispIdGeneratorImpl.class,
		MISPLicenseKeyGeneratorImpl.class,
		MISPLicenseKeyGeneratorUtil.class,
		RidGeneratorImpl.class,
		PridValidatorImpl.class,
		RidValidatorImpl.class,
		UinValidatorImpl.class,
		VidValidatorImpl.class,
		MispIdValidatorImpl.class,
		TemplateManagerBuilderImpl.class,
		PDFGeneratorImpl.class,
		QrcodeGeneratorImpl.class,
		TransliterationImpl.class,
		IdObjectValidatorConfig.class,
		IntentVerificationConfig.class,
		WebSubClientConfig.class,
		WebSubPublisherClientConfig.class,
		RestTemplateHelper.class
})
@EnableJms
public class ProxyMvApplication {

	/**
	 * The entry point of the Spring Boot application.
	 * <p>
	 * This method is responsible for launching the Spring Boot application,
	 * retrieving the {@link Listener} bean, and starting the adjudication and
	 * verification queues.
	 * </p>
	 *
	 * @param args command-line arguments (not used).
	 */
	public static void main(String[] args) {
		ConfigurableApplicationContext configurableApplcnConetxt = SpringApplication.run(ProxyMvApplication.class,
				args);
		Listener listener = configurableApplcnConetxt.getBean(Listener.class);
		listener.runAdjudicationQueue();
		listener.runVerificationQueue();
	}

	/**
	 * Creates a {@link RestTemplate} bean.
	 * <p>
	 * This method provides a {@link RestTemplate} instance that can be used for
	 * making RESTful web service calls.
	 * </p>
	 *
	 * @return a {@link RestTemplate} instance.
	 */
	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}
}
