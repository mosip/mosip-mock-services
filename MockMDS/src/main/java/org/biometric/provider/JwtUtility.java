package org.biometric.provider;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.crypto.jce.core.CryptoCore;

public class JwtUtility {
	
	@Autowired
	private CryptoCore jwsValidation;
	
	
	public  String getJwsPart(byte[] data) {
		String jwt = null;
		try {
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Files
				.readAllBytes(Paths.get(System.getProperty("user.dir") + "/files/keys/PrivateKey.pem")));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(spec);
        
        CertificateFactory certFactory;
        certFactory = CertificateFactory.getInstance("X.509");
        InputStream inputStream = JwtUtility.class.
        		getResourceAsStream(
                "/files/keys/MosipTestCert.pem");
        X509Certificate cert = (X509Certificate) certFactory
                .generateCertificate(inputStream); 
		
		
		jwt=jwsValidation.sign(data, privateKey, cert);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return jwt;
		
	}

}
