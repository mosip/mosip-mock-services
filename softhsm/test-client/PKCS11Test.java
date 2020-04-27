import sun.security.pkcs11.SunPKCS11;
import java.security.Provider;
import java.util.Enumeration;
import java.security.KeyStore;
import java.security.Security;
import java.util.Collections;
import java.security.Key;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.KeyPairGenerator;

import java.security.cert.X509Certificate;
 
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.util.Date;
import java.util.Scanner;  
import java.math.BigInteger;


public class PKCS11Test {

	private static final String CONFIG_PATH = "/config/softhsm-application.conf";
	private static final String KEY_STORE_TYPE = "PKCS11";
	private static final String CERTIFICATE_DN = "CN=IN, O=MOSIP, L=Bangalore, ST=KA, C=Mosip-test";
	private static final String ALIAS = "dummy-test-key";

	public static void main(String[] args) throws Exception {

		System.out.println("Loading config from " + CONFIG_PATH);  
		Provider provider = new SunPKCS11(CONFIG_PATH);
		
		Security.addProvider(provider);
		
		Security.addProvider(new BouncyCastleProvider());
		KeyStore mosipKeyStore = KeyStore.getInstance(KEY_STORE_TYPE, provider);
		
		
		//Hard coded password used only for testing.
		String PARTITION_SECRET = "1111";
		if (args.length > 0){
			PARTITION_SECRET = args[0];
		}

		mosipKeyStore.load(null, PARTITION_SECRET.toCharArray());
		System.out.println("Logged in to HSM ");  
		Enumeration<String> keyAliases = mosipKeyStore.aliases();

		System.out.println("Listing Alias ");  
		int count =0;
		while (keyAliases.hasMoreElements())  {
			System.out.println("Alias: " + keyAliases.nextElement());
			count++;
		}
		System.out.println("Found " + count + " Alias ");  
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA"); 
  
        kpg.initialize(2048); 
        KeyPair kp = kpg.generateKeyPair(); 

        X509V3CertificateGenerator v3CertGen =  new X509V3CertificateGenerator();
        v3CertGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        v3CertGen.setIssuerDN(new X509Principal(CERTIFICATE_DN));
        v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24));
        v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365*10)));
        v3CertGen.setSubjectDN(new X509Principal(CERTIFICATE_DN));
        v3CertGen.setPublicKey(kp.getPublic());
        v3CertGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        X509Certificate cert = v3CertGen.generateX509Certificate(kp.getPrivate()); 

        mosipKeyStore.setKeyEntry(ALIAS, kp.getPrivate(), PARTITION_SECRET.toCharArray(), new Certificate[] {cert});

		System.out.println("Key generated successful.");

		System.out.println("Press ctrl c to exit");
		while(true){
			
			//Do Nothing just wait
		}
	}
}