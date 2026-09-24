package io.mosip.proxy.abis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jms.autoconfigure.JmsAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;

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
import io.mosip.proxy.abis.controller.ProxyAbisController;
import io.mosip.proxy.abis.listener.Listener;

/**
 * Main application class for Proxy ABIS.
 * <p>
 * This class initializes and runs the Proxy ABIS application using Spring Boot.
 * It scans the necessary packages, configures JPA repositories, and enables JMS
 * messaging. On startup, it initializes the necessary components and starts
 * listening to ABIS queues.
 * </p>
 */
@ComponentScan(basePackages = { "io.mosip.proxy.abis", "${mosip.auth.adapter.impl.basepackage}" })
@EntityScan(basePackages = { "io.mosip.proxy.abis.*" })
@SpringBootApplication
@EnableAutoConfiguration(exclude = {
		JmsAutoConfiguration.class,
		// kernel-core registers these via AutoConfiguration.imports; mock-abis does not use them
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
@EnableJpaRepositories(basePackages = { "io.mosip.proxy.abis.*" })
@EnableJms
public class ProxyAbisApplication {

	/**
	 * Main method to start the Proxy ABIS application.
	 * <p>
	 * It initializes the Spring application context, starts the ABIS listener, and
	 * sets up the controller to use the listener.
	 * </p>
	 *
	 * @param args Command-line arguments passed to the application.
	 */
	public static void main(String[] args) {
		ConfigurableApplicationContext configurableApplicationContext = SpringApplication
				.run(ProxyAbisApplication.class, args);
		configurableApplicationContext.getBean(Listener.class).runAbisQueue();
		configurableApplicationContext.getBean(ProxyAbisController.class)
				.setListener(configurableApplicationContext.getBean(Listener.class));
	}
}
