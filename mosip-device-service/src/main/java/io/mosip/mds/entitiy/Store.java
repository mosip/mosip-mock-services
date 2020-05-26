package io.mosip.mds.entitiy;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.stream.FileImageInputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mds.dto.TestRun;
import io.mosip.mds.dto.getresponse.MasterDataResponseDto;
import io.mosip.mds.dto.getresponse.TestExtnDto;

public class Store {

    public static List<String> GetRunIds(String email)
    {
        List<String> files = new ArrayList<>();
        File f = new File(GetStorePath() + "runs/" + email);
        try
        {
            for(File sub:f.listFiles())
            {
                if(sub.isFile())
                    files.add(sub.getName());
            }
        }
        catch(Exception ex)
        {

        }
        return files;
    }

    public static List<String> GetUsers()
    {
        List<String> files = new ArrayList<>();
        File f = new File(GetStorePath() + "runs");
        try
        {
            for(File sub:f.listFiles())
            {
                if(sub.isDirectory())
                    files.add(sub.getName());
            }
        }
        catch(Exception ex)
        {

        }
        return files;
    }


    public static TestRun GetRun(String email, String runId)
    {
        TestRun result = null;
        File runFile = new File(GetStorePath() + "runs/" + email + File.separator + runId );
        if(!runFile.exists())
            return null;
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            result = mapper.readValue(new FileInputStream(runFile.getAbsolutePath()), TestRun.class);
        }
        catch(Exception ex)
        {
            // TODO write to log
        }
        return result;
    }

    public static TestRun SaveTestRun(String email, TestRun run)
    {
        File dir = GetOrCreateDirectory(GetStorePath() + "runs/" + email);
        File runFile = new File(dir.getAbsolutePath() + File.separator + run.runId);
        ObjectMapper mapper = new ObjectMapper();
            // Constructs a FileWriter given a file name, using the platform's default charset
        try
        {
            mapper.writeValue(runFile, run);
        }
        catch(Exception ex)
        {
            // TODO write to log
            return null;
        }
        return run;
    }

    private static String GetStorePath()
    {
        String storePath = System.getProperty("user.dir");
        if(!storePath.endsWith(File.separator))
            storePath += File.separator;
        File dataDir = GetOrCreateDirectory(storePath + "data/");
        storePath = dataDir.getAbsolutePath();
        if(!storePath.endsWith(File.separator))
            storePath += File.separator;
        return storePath;
    }
    
    private static File GetOrCreateDirectory(String path)
    {
        File f = new File(path);
        if(f.isDirectory())
            return f;
        if(f.exists())
            return null;
        if(f.mkdirs())
            return f;
        return null;
    }

    public static MasterDataResponseDto GetMasterData()
    {
        File masterDataFile = new File(GetStorePath() + "config/masterdata.json");
        if(!masterDataFile.exists())
            return null;
        try
        {
        ObjectMapper mapper = new ObjectMapper();
        return (MasterDataResponseDto)mapper.readValue(new FileImageInputStream(masterDataFile), MasterDataResponseDto.class); 
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    public static TestExtnDto[] GetTestDefinitions()
    {
        File testDeinitionsFile = new File(GetStorePath() + "config/test-definitions.json");
        if(!testDeinitionsFile.exists())
            return null;
        try
        {
        ObjectMapper mapper = new ObjectMapper();
        return (TestExtnDto[])mapper.readValue(new FileImageInputStream(testDeinitionsFile), TestExtnDto[].class); 
        }
        catch(Exception ex)
        {
            return null;
        }
    }


}