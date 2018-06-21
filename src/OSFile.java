import java.io.File;

public class OSFile {
    private String osFileName;
    private File osFile;
    private String osFileFolder;
    private double osFileCapacity;

    public OSFile(File myFile, String blockName, double capacity){
        osFileCapacity = capacity;
        this.osFile = myFile;
        this.osFileFolder = blockName;
        osFileName = myFile.getName();
    }


    public String getFilePath(){
        return osFile.toString();
    }

    public String getOsFileName() {
        return osFile.getName();
    }

    public File getOsFile() {
        return osFile;
    }

    public String getOsFileFolder() {
        return osFileFolder;
    }

    public double getOsFileCapacity() {
        return osFileCapacity;
    }

    @Override
    public String toString(){
        return osFileName;
    }
}
