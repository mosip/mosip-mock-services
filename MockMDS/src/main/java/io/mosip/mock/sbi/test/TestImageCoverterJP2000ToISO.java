package io.mosip.mock.sbi.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.util.BioUtilHelper;

public class TestImageCoverterJP2000ToISO {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestImageCoverterJP2000ToISO.class);	
	private static String fingerName = "Left_Ring";	

	public static void main(String[] args) {
		//Finger Conversion 
        String path = "F:\\Home Projects\\Essl\\Mosip\\mosip-mock-services\\MockMDS\\Profile\\Automatic\\Finger\\img";
        FileOutputStream tmpOutputStream = null;
        try {
        	File root = new File( path );
            Collection<File> list = listFileTree(root);
            System.out.println( "Input File:" + list);
            if (list == null) return;

            for (File f : list ) {
                System.out.println( "Input File:" + f.getAbsoluteFile());
                String fileName = f.getAbsolutePath();
                byte[] imageData = Files.readAllBytes(Paths.get(fileName));
                byte [] isoData = BioUtilHelper.getFingerIsoFromJP2000("Registration", getBiometricSubType(f), imageData);
        		if (isoData != null)
        		{
        			// Write bytes to tmp file.
        		    File tmpImageFile = new File(path + File.separator + getFileNameWithoutExtension(f) + ".iso");
    		        tmpOutputStream = new FileOutputStream(tmpImageFile);
    		        tmpOutputStream.write(isoData);
        		}
        		//f.delete();
            }
            System.out.println("Image converted successfully.");
            
        } catch (Exception ex) {
            System.out.println("Error during converting image.");
            ex.printStackTrace();
        }
        finally
		{
			try
			{
		        if(tmpOutputStream != null)
	                tmpOutputStream.close();
			}
			catch (Exception ex)
			{}
		}
   }	
	
	public static Collection<File> listFileTree(File dir) {
	    Set<File> fileTree = new HashSet<File>();
	    if(dir==null||dir.listFiles()==null){
	        return fileTree;
	    }
	    for (File entry : dir.listFiles()) {
	        if (entry.isFile()) fileTree.add(entry);
	        else fileTree.addAll(listFileTree(entry));
	    }
	    return fileTree;
	}
		
	private static String getBiometricSubType(File file) {
		String biometricSubType = "UNKNOWN";
		if (file != null && file.exists()) {
		String fileName = file.getName();
		//Iris
		if (fileName.contains ("Left_Iris"))
			return "Left";
		if (fileName.contains ("Right_Iris"))
			return "Right";

		//Finger
		if (fileName.contains ("Right_Thumb"))
			return "Right Thumb";
		if (fileName.contains ("Right_Index"))
			return "Right IndexFinger";
		if (fileName.contains ("Right_Middle"))
			return "Right MiddleFinger";
		if (fileName.contains ("Right_Ring"))
			return "Right RingFinger";
		if (fileName.contains ("Right_Little"))
			return "Right LittleFinger";
		if (fileName.contains ("Left_Thumb"))
			return "Left Thumb";
		if (fileName.contains ("Left_Index"))
			return "Left IndexFinger";
		if (fileName.contains ("Left_Middle"))
			return "Left MiddleFinger";
		if (fileName.contains ("Left_Ring"))
			return "Left RingFinger";
		if (fileName.contains ("Left_Little"))
			return "Left LittleFinger";
		}
		return "UNKNOWN";
	}
	
	private static String getFileNameWithoutExtension(File file) {
		String fileName = "";
	 
		try {
			if (file != null && file.exists()) {
				String name = file.getName();
				fileName = name.replaceFirst("[.][^.]+$", "");
			}
		} catch (Exception e) {
	    	e.printStackTrace();
	    	fileName = "";
		}
	 
	    return fileName;	 
	 }
	 
	// FileNameFilter implementation
	public static class MyFileNameFilter implements FilenameFilter {

		private String extension;

		public MyFileNameFilter(String extension) {
			this.extension = extension.toLowerCase();
		}

		@Override
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(extension);
		}

	}
}
