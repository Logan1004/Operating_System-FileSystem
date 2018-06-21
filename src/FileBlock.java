import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileBlock {
    private String upperBlockName;
    private File fileFolder;
    private File initFile;
    private double space;
    private File bitFile;
    private int fileNum;
    public int[][] bitmap = new int [32][32];
    private FileWriter recoverWriter;
    private FileWriter bitFileWriter;

    public double getSpace() {
        return space;
    }

    public void setSpace(double space) {
        this.space = space;
    }

    private ArrayList<File> files = new ArrayList<File>();

    public FileBlock(){
        space=0;
    }
    public FileBlock(String upperBlockName, File curFileBlock, boolean haveNotCreated) throws IOException{
        if (haveNotCreated) {
            space = 0;
            this.upperBlockName = upperBlockName;
            this.fileFolder = curFileBlock;

            initFile = new File(curFileBlock.getPath() + File.separator + "init.txt");

            curFileBlock.mkdir();

            initFile.createNewFile();

        }else{}//已经创建过 读取文件
    }

    public String getUpperBlockName() {
        return upperBlockName;
    }

    public File getFileFolder() {
        return fileFolder;
    }

    public File getBitFile() {
        return bitFile;
    }

    public int getFileNum() {
        return fileNum;
    }

    public FileWriter getRecoverWriter() {
        return recoverWriter;
    }

    public FileWriter getBitFileWriter() {
        return bitFileWriter;
    }
}
