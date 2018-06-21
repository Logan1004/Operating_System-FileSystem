
import javax.swing.table.AbstractTableModel;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class TableModel extends AbstractTableModel {
    private Vector content = null;
    private String[] title_name = { "Name", "Path", "Category", "File Volume/KB", "Date Modified"};

    public TableModel(){
        content = new Vector();
    }

    public void addRow(OSFile myFile){
        Vector curFileVec = new Vector();
        curFileVec.add(0, myFile.getOsFileName());
        curFileVec.add(1, myFile.getFilePath());
        if (myFile.getOsFile().isFile()){
            curFileVec.add(2, "File");
            if (myFile.getOsFileName().equals("recover.txt")|| myFile.getOsFileName().equals("bit.txt")){
                curFileVec.add(3, 0);
            }else {
                try {
                    BufferedReader in = new BufferedReader(new FileReader(myFile.getOsFile()));
                    String line = in.readLine();
                    String line2 = in.readLine();
                    curFileVec.add(3, line2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }else {
            curFileVec.add(2, "Directory");
            curFileVec.add(3, "-");
        }
        long time = myFile.getOsFile().lastModified();
        String ctime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(time));
        curFileVec.add(4, ctime);
        content.add(curFileVec);
        //System.out.println(curFileVec);
    }


    public void removeRows(int row, int count){
        for (int i = 0; i < count; i++){
            if (content.size() > row){
                content.remove(row);
            }
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int colIndex){
        ((Vector) content.get(rowIndex)).remove(colIndex);
        ((Vector) content.get(rowIndex)).add(colIndex, value);
        this.fireTableCellUpdated(rowIndex, colIndex);
    }

    public String getColumnName(int col) {
        return title_name[col];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex){
        return false;
    }

    @Override
    public int getRowCount() {
        return content.size();
    }

    @Override
    public int getColumnCount() {
        return title_name.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return ((Vector) content.get(rowIndex)).get(columnIndex);
    }
}
