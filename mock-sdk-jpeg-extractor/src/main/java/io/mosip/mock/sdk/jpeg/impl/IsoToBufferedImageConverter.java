package io.mosip.mock.sdk.jpeg.impl;

import java.awt.image.BufferedImage;

import io.mosip.biometrics.util.ConvertRequestDto;

@FunctionalInterface
public interface IsoToBufferedImageConverter {
	
	BufferedImage converte(ConvertRequestDto convertRequestDto) throws Exception;

}
