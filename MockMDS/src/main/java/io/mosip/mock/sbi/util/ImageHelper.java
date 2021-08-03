package io.mosip.mock.sbi.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;

public class ImageHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(ImageHelper.class);	

	 /**
     * Converts an image to another format
     *
     * @param inputImagePath Path of the source image
     * @param outputImagePath Path of the destination image
     * @param formatName the format to be converted to, one of: jpeg, png,
     * bmp, wbmp, and gif
     * @return true if successful, false otherwise
     * @throws IOException if errors occur during writing
     */
	public static void toJ2000(String inputFile, String outputFile) throws IOException {		
        System.out.println( "Output File:" + outputFile);

        Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("jp2");
    	if (writers.hasNext())
    	{
    		ImageWriter writer = writers.next();
    		//System.out.println ("writer>>" + writer.toString ());
            J2KImageWriteParam param = (J2KImageWriteParam) writer.getDefaultWriteParam();
            //param.setSOP(true);
            //param.setSourceBands (new int [] {0});
            //param.setWriteCodeStreamOnly(true);
            //param.setProgressionType("layer");
            //param.setNumDecompositionLevels (1);
            param.setLossless(false);
            param.setCompressionMode(J2KImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("JPEG2000");
            param.setCompressionQuality(0.90f);
            param.setEncodingRate(2.0f);  
            //param.setEncodingRate(0.50f);
            param.setFilter(J2KImageWriteParam.FILTER_97);

            File f = new File(outputFile);
            ImageOutputStream ios = ImageIO.createImageOutputStream(f);
            writer.setOutput(ios);
            FileInputStream fis = new FileInputStream(new File(inputFile));
    		BufferedImage image = ImageIO.read(fis);
    		fis.close();
    		
            writer.write(null, new IIOImage(image, null, null), param);
            
            writer.dispose();
            
            ios.flush();
            ios.close();
    	}    	
	}         
}
