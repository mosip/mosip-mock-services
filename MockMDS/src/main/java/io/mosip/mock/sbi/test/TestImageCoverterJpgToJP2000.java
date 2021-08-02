package io.mosip.mock.sbi.test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.util.ImageHelper;

public class TestImageCoverterJpgToJP2000 {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestImageCoverterJpgToJP2000.class);	
	private static String fingerName = "Left_Ring";	

	public static void main(String[] args) {
		//Finger Conversion 
        String path = "F:\\Home Projects\\Essl\\Mosip\\mosip-mock-services\\MockMDS\\Profile\\Automatic\\Finger\\img";
        try {
        	File root = new File( path );
            File[] listJP2 = root.listFiles(new MyFileNameFilter (fingerName + ".jp2"));

            int count = 1;
            if (listJP2 != null) 
            	count = listJP2.length == 0 ? 1 : listJP2.length + 1;

            Collection<File> list = listFileTree(root);
            System.out.println( "Input File:" + list);
            if (list == null) return;

            boolean isLeftOrRight = false; // left = false ... right = true
            for (File f : list ) {
                System.out.println( "Input File:" + f.getAbsoluteFile() );
                
                ImageHelper.toJ2000(f.getAbsoluteFile() + "", path + File.separator + changeOutputFileExtension (f, count));
                count++;
                f.delete();
            }
            System.out.println("Image converted successfully.");
            
        } catch (IOException ex) {
            System.out.println("Error during converting image.");
            ex.printStackTrace();
        }

		/*
		//Iris Conversion 
        String path = "F:\\Home Projects\\Essl\\Mosip\\mosip-mock-services\\MockMDS\\Profile\\Automatic\\Iris\\img";
        //String oututImage = "F:\\Home Projects\\Essl\\Mosip\\mosip-mock-services\\MockMDS\\Profile\\Automatic\\Iris\\img/02_L.jp2";
        try {
        	File root = new File( path );
            File[] listJP2_left = root.listFiles(new MyFileNameFilter ("Left_Iris.jp2"));
            File[] listJP2_right = root.listFiles(new MyFileNameFilter ("Right_Iris.jp2"));

            int count_left = 1, count_right = 1;
            if (listJP2_left != null) 
            	count_left = listJP2_left.length == 0 ? 1 : listJP2_left.length + 1;
                        
            if (listJP2_right != null) 
            	count_right = listJP2_right.length == 0 ? 1 : listJP2_right.length + 1;

            Collection<File> list = listFileTree(root);
            System.out.println( "Input File:" + list);
            if (list == null) return;

            boolean isLeftOrRight = false; // left = false ... right = true
            for (File f : list ) {
                System.out.println( "Input File:" + f.getAbsoluteFile() );
                
                if (f.getName().contains ("_L"))
                {
                    ImageHelper.toJ2000(f.getAbsoluteFile() + "", path + File.separator + changeOutputFileExtension (f, count_left));
                    count_left++;
                }
        		if (f.getName().contains ("_R"))
        		{
                    ImageHelper.toJ2000(f.getAbsoluteFile() + "", path + File.separator + changeOutputFileExtension (f, count_right));
                    count_right++;
        		}
                //f.delete();
            }
            System.out.println("Image converted successfully.");
            
        } catch (IOException ex) {
            System.out.println("Error during converting image.");
            ex.printStackTrace();
        }
        */
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
	
	private static String changeOutputFileExtension(File file, int fileNumber) {
    	return String.format("%04d", fileNumber) + getFileNameChangeIris (getFileNameWithoutExtension(file)) + ".jp2";
    }
	
	private static String getFileNameChangeIris(String fileName) {
		/*
		 // for iris
		if (fileName.contains ("_L"))
			return "Left_Iris";
		if (fileName.contains ("_R"))
			return "Right_Iris";
		*/
		return fingerName;
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
