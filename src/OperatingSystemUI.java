import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OperatingSystemUI extends JFrame{
    private JFileChooser jFileChooser;
    private FileBlock fileBlock1;
    private FileBlock fileBlock2;
    private FileBlock fileBlock3;
    private DefaultMutableTreeNode jTreeNode;
    private DefaultTreeModel jTreeModel;
    private JTree jTree;
    private TableModel tableModel;
    private JTable jTable;
    private JScrollPane contentPane;
    private JScrollPane tablePane;
    private JPopupMenu jPopupMenu;
    private JPopupMenu jPopupMenuTable;
    private File bitFile;
    private File recover;
    private FileWriter bitFileWriter;
    private FileWriter recoverWriter;
    public int[][] bitmap = new int [32][32];
    private double MaxCapacity = 1024.0;
    private ArrayList<FileBlock> blocks = new ArrayList<FileBlock>();
    private ArrayList<File> filesMap = new ArrayList<File>();
    private Map<String, int[][] > filesBit = new HashMap<String, int[][]>();

    public static DefaultMutableTreeNode TraverseFolder(String path) {

        File file = new File(path);
        DefaultMutableTreeNode cur = new DefaultMutableTreeNode(new OSFile(file,file.getName(),1024));

        if (file.exists()) {
            File[] files = file.listFiles();
            if (files.length == 0) {
                if(file.isDirectory()) {//如果是空文件夹
                    return new DefaultMutableTreeNode(file, false);
                }
            }else{
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        //是目录的话，生成节点，并添加里面的节点
                        cur.add(TraverseFolder(file2.getAbsolutePath()));
                    }else{
                        //是文件的话直接生成节点，并把该节点加到对应父节点上
                        cur.add(new DefaultMutableTreeNode(new OSFile(file2,file.getName(),1024)));
                    }
                }
            }
        } else {//文件不存在
            return null;
        }
        return cur;
    }


    public static TableModel CreateModel(String blockName, String path){
        TableModel tableModel = new TableModel();
        File file = new File(path);
        if (file.exists()){
            File[] files = file.listFiles();
            for (File file1:files){
                tableModel.addRow(new OSFile(file1,blockName,1024));
            }

        }
        return tableModel;
    }

    public boolean DeleteCheck(File file){
        if (file.isDirectory()){
            if (file.getName().equals("1") || file.getName().equals("2") || file.getName().equals("3")){
                return false;
            }
        } else if (file.isFile()) {
            if (file.getName().equals("recover.txt") || file.getName().equals("init.txt") || file.getName().equals("bit.txt")) {
                return false;
            }
        }
        return true;
    }

    public boolean DeleteFile(File file){
        if (file.isFile()){
            try {
                BufferedReader in = new BufferedReader(new FileReader(file));
                String line = in.readLine();
                String line2 = in.readLine();
                MaxCapacity+=Double.parseDouble(line2);
            }catch (IOException e){
                e.printStackTrace();
            }
            file.delete();
            int[][] fileStore = filesBit.get(file.getPath());
            for (int i = 0; i < 32; i++){
                for (int k = 0; k < 32; k++){
                    if (bitmap[i][k] == 1 && fileStore[i][k] == 1){
                        bitmap[i][k] = 0;
                    }
                }
            }
            filesBit.remove(file.getPath());
            for (int i = 0; i < filesMap.size(); i++){
                if (filesMap.get(i).getName().equals(file.getName())){
                    filesMap.remove(i);
                    break;
                }
            }
        }else{
            File [] files = file.listFiles();
            for (File file1:files){
                DeleteFile(file1);
            }
            if (file.exists()) file.delete();
        }
        return true;
    }

    public boolean FormatDir(File file){
        if (file.isFile()){
            file.delete();
        }else {
            File [] files = file.listFiles();
            for (File file1 : files) {
                if (DeleteCheck(file1)) {
                    DeleteFile(file1);
                }
            }
        }
        return true;
    }

    public boolean rewriteBit()throws IOException{
        bitFileWriter = new FileWriter(bitFile);
        for (int i=0;i<32;i++){
            for (int j=0;j<32;j++){
                if (bitmap[i][j] == 0){
                    bitFileWriter.write("0");
                }else{
                    bitFileWriter.write("1");
                }
            }
            bitFileWriter.write("\r\n");
        }
        bitFileWriter.flush();
        for (int i=0;i<filesMap.size();i++){
            bitFileWriter.write(filesMap.get(i).getName() + ":");
            for (int k = 0; k < 32; k++){
                for (int j = 0; j < 32; j++){
                    try {
                        if (filesBit.get(filesMap.get(i).getPath())[k][j] == 1) {
                            bitFileWriter.write(String.valueOf(k * 32 + j) + " ");
                        }
                    }catch (Exception event){
                        System.out.println("wrong");
                    }
                }
            }
            bitFileWriter.write("\r\n");
        }
        bitFileWriter.flush();
        return false;
    }

    public boolean rewriteRecover(double maxCapacity)throws IOException{
        recoverWriter = new FileWriter(recover);
        recoverWriter.write(maxCapacity + "\r\n");
        recoverWriter.write(filesMap.size() + "\r\n");
        for (int i = 0; i < 32; i++){
            for (int k = 0; k < 32; k++){
                if (bitmap[i][k] == 0){
                    recoverWriter.write("0\r\n");
                }else{
                    recoverWriter.write("1\r\n");
                }
            }
        }
        for (int i = 0; i < filesMap.size(); i++){
            recoverWriter.write(filesMap.get(i).getPath() + "\r\n");
            int [][] bitTemp = filesBit.get(filesMap.get(i).getPath());
            for (int k = 0; k < 32; k++){
                for (int j = 0; j < 32; j++){
                    if (bitTemp[k][j] == 0){
                        recoverWriter.write("0\r\n");
                    }else {
                        recoverWriter.write("1\r\n");
                    }
                }
            }
        }
        recoverWriter.flush();
        return false;
    }

    public OperatingSystemUI() throws IOException{
        setTitle("OperatingSystemUI");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //初始化选择文件夹放置储存位置
        jFileChooser = new JFileChooser(File.listRoots()[0].getPath());
        jFileChooser.setDialogTitle("Choose a folder to place the demo");
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser.setPreferredSize(new Dimension(1200,1000));
        String curPath = "";
        if (jFileChooser.showOpenDialog(this) == jFileChooser.APPROVE_OPTION){
            curPath = jFileChooser.getSelectedFile().getPath();
        }
        File operationSystem = new File(curPath+File.separator+"MyFileSystem");
        File operationSystemReadme = new File(curPath + File.separator + "MyFileSystem" + File.separator + "ReadMe.txt");

        boolean haveNotCreated = false;
        if (!operationSystem.exists()) {
            haveNotCreated = true;
            try {
                operationSystem.mkdir();
                operationSystemReadme.createNewFile();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "The place is not support to create dir!", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
            FileWriter readMeWriter = new FileWriter(operationSystemReadme.getPath());
            readMeWriter.write("Operating System Demo!!!\n");
            readMeWriter.write("MAX folder Capacity: 1024KB\n");
            readMeWriter.write("Free-Space Management:bitmap\n");
            readMeWriter.write("Store-Space Management:FAT\n");
            readMeWriter.flush();
            readMeWriter.close();

            bitFile = new File(operationSystem.getPath() + File.separator + "bit.txt");
            bitFile.createNewFile();
            bitFileWriter = new FileWriter(bitFile);
            for (int i = 0; i < 32; i++) {
                for (int j = 0; j < 32; j++) {
                    bitmap[i][j] = 0;
                    bitFileWriter.write("0");
                }
                bitFileWriter.write("\r\n");
            }
            bitFileWriter.flush();
            recover = new File(operationSystem.getPath() + File.separator + "recover.txt");
            recover.createNewFile();
            recoverWriter = new FileWriter(recover);
            recoverWriter.write("1024");
            recoverWriter.flush();

            fileBlock1 = new FileBlock(operationSystem.getName(), new File(operationSystem.getPath() + File.separator + "1"), haveNotCreated);
            fileBlock2 = new FileBlock(operationSystem.getName(), new File(operationSystem.getPath() + File.separator + "2"), haveNotCreated);
            fileBlock3 = new FileBlock(operationSystem.getName(), new File(operationSystem.getPath() + File.separator + "3"), haveNotCreated);
            blocks.add(fileBlock1);
            blocks.add(fileBlock2);
            blocks.add(fileBlock3);
        }else{
            bitFile = new File(operationSystem.getPath() + File.separator + "bit.txt");
            recover = new File(operationSystem.getPath() + File.separator + "recover.txt");
            if (!recover.exists()){
                JOptionPane.showMessageDialog(null, "Recover File Missing", "Fail",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }else {

                BufferedReader reader = new BufferedReader(new FileReader(recover));
                String s=reader.readLine();
                if (s == null) {
                    JOptionPane.showMessageDialog(null, "Recover File Missing", "Fail",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                } else {
                    MaxCapacity = Double.parseDouble(s);
                    s = reader.readLine();
                    if (s == null) {
                        JOptionPane.showMessageDialog(null, "Recover File Missing", "Fail",
                                JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                    } else {
                        int fileNum = Integer.parseInt(s);
                        for (int i = 0; i < 32; i++) {
                            for (int k = 0; k < 32; k++) {
                                if (Integer.parseInt(reader.readLine()) == 0) {
                                    bitmap[i][k] = 0;
                                } else {
                                    bitmap[i][k] = 1;
                                }
                            }
                        }
                        String temp;
                        while ((temp = reader.readLine()) != null) {
                            File myFile = new File(temp);
                            filesMap.add(myFile);
                            int[][] tempBit = new int[32][32];
                            for (int i = 0; i < 32; i++) {
                                for (int k = 0; k < 32; k++) {
                                    String s1 = reader.readLine();
                                    System.out.println(s1);
                                    if (Integer.parseInt(s1) == 0) {
                                        tempBit[i][k] = 0;
                                    } else {
                                        tempBit[i][k] = 1;
                                    }
                                }
                            }
                            filesBit.put(myFile.getPath(), tempBit);
                        }
                        reader.close();
                    }
                }
            }

        }

        jTreeNode = TraverseFolder(operationSystem.getPath());
        jTreeModel = new DefaultTreeModel(jTreeNode);
        jTree = new JTree(jTreeModel);
        jTree.setEditable(false);
        jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree.setShowsRootHandles(true);
        contentPane = new JScrollPane(jTree);
        contentPane.setPreferredSize(new Dimension(200, 400));
        add(contentPane, BorderLayout.WEST);

        tableModel = CreateModel(operationSystem.getName(),operationSystem.getPath());
        jTable = new JTable(tableModel);
        jTable.getTableHeader().setFont(new Font(Font.DIALOG,Font.CENTER_BASELINE,24));
        jTable.setSelectionBackground(Color.ORANGE);
        jTable.updateUI();
        tablePane = new JScrollPane(jTable);
        add(tablePane, BorderLayout.CENTER);


        jTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                String curPath = operationSystem.getPath();
                TreePath treePath = e.getPath();
                for (int i=1;i<treePath.getPathCount();i++){
                    curPath = curPath + File.separator + treePath.getPathComponent(i).toString();
                }
                File file = new File(curPath);

                //System.out.println(curPath);
                tableModel.removeRows(0,tableModel.getRowCount());
                if (file.isDirectory()) {
                    if (file.exists()) {
                        File[] files = file.listFiles();
                        for (File file1 : files) {
                            tableModel.addRow(new OSFile(file1, treePath.getPathComponent(treePath.getPathCount() - 1).toString(), 1024));
                        }
                    }
                }else if (file.isFile()){
                    tableModel.addRow(new OSFile(file, treePath.getPathComponent(treePath.getPathCount() - 2).toString(), 1024));
                }
                jTable.updateUI();
            }
        });
        jTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                String curPath = operationSystem.getPath();
                TreePath treePath = event.getPath();
                DefaultMutableTreeNode parent = null;
                parent = (DefaultMutableTreeNode) (treePath.getLastPathComponent());

                for (int i=1;i<treePath.getPathCount();i++){
                    curPath = curPath + File.separator + treePath.getPathComponent(i).toString();
                }
                File file = new File(curPath);
                //System.out.println(curPath);
                tableModel.removeRows(0,tableModel.getRowCount());
                while (parent.getChildCount()>0) {
                    jTreeModel.removeNodeFromParent((MutableTreeNode) parent.getChildAt(0));
                }
                if (file.isDirectory()) {
                    if (file.exists()) {
                        File[] files = file.listFiles();
                        for (File file1 : files) {
                            DefaultMutableTreeNode node = null;
                            node = new DefaultMutableTreeNode(new OSFile(file1, file.getName(), 1024));
                            if (file1.isDirectory() && file1.canRead()){
                                node.add(new DefaultMutableTreeNode("temp"));
                            }
                            jTreeModel.insertNodeInto(node, parent,parent.getChildCount());
                            tableModel.addRow(new OSFile(file1, treePath.getPathComponent(treePath.getPathCount() - 1).toString(), file1.getTotalSpace()));
                        }
                    }
                }else if (file.isFile()){
                    tableModel.addRow(new OSFile(file, treePath.getPathComponent(treePath.getPathCount() - 2).toString(), file.getTotalSpace()));
                }
                jTable.updateUI();
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                tableModel.removeRows(0,tableModel.getRowCount());
                jTable.updateUI();
            }
        });


        jTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                jPopupMenuTable = new JPopupMenu();
                jPopupMenuTable.setPreferredSize(new Dimension(300,50));
                JMenuItem jOpenFileMenuItem = new JMenuItem("Open the file");
                jOpenFileMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        String filePath = ((String) tableModel.getValueAt(jTable.getSelectedRow(), 1));
                        try {
                            if(Desktop.isDesktopSupported()) {
                                Desktop desktop = Desktop.getDesktop();
                                desktop.open(new File(filePath));
                            }
                        } catch (IOException e1) {
                            JOptionPane.showMessageDialog(null, "Please Try again", "Fail to open",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                jPopupMenuTable.add(jOpenFileMenuItem);
                if (e.getClickCount()==2 && e.getButton()==MouseEvent.BUTTON1){
                    String fileName = ((String) tableModel.getValueAt(jTable.getSelectedRow(), 0));
                    String filePath = ((String) tableModel.getValueAt(jTable.getSelectedRow(), 1));
                    try {
                        if(Desktop.isDesktopSupported()) {
                            Desktop desktop = Desktop.getDesktop();
                            desktop.open(new File(filePath));
                        }
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "Please Try again", "Fail to open",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    JOptionPane.showMessageDialog(null, "File Name: " + fileName + "\nFile Path: " + filePath, "content",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                if (e.getButton()==MouseEvent.BUTTON3){
                    jPopupMenuTable.show(e.getComponent(),e.getX(),e.getY());

                }
            }
        });

        jPopupMenu = new JPopupMenu();
        jPopupMenu.setPreferredSize(new Dimension(300,200));

        JMenuItem jOpenMenuItem = new JMenuItem("Open a file");
        jOpenMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
                OSFile curOSFile = (OSFile)defaultMutableTreeNode.getUserObject();
                String curPath = curOSFile.getFilePath();
                try {
                    if(Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        desktop.open(new File(curPath));
                    }
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null, "Please Try again", "Fail to open",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JMenuItem jCreateFileMenuItem = new JMenuItem("Create a file");
        jCreateFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
                OSFile curOSFile = (OSFile)defaultMutableTreeNode.getUserObject();
                String curPath = curOSFile.getFilePath();
                File file = new File(curPath);
                if (file.isDirectory()) {
                    String fileName;
                    double fileCapacity;
                    JOptionPane inputPane = new JOptionPane();
                    inputPane.setPreferredSize(new Dimension(600, 600));
                    inputPane.setInputValue(JOptionPane.showInputDialog("Input the file name:"));
                    if (inputPane.getInputValue() == null) {
                        return;
                    }
                    fileName = inputPane.getInputValue().toString();
                    inputPane.setInputValue(JOptionPane.showInputDialog("Capacity(KB):"));
                    if (inputPane.getInputValue() == null) {
                        return;
                    }
                    fileCapacity = Double.parseDouble(inputPane.getInputValue().toString());
                    if (MaxCapacity-fileCapacity>=0) {

                        File curFile = new File(curPath + File.separator + fileName + ".txt");
                        OSFile newFile = new OSFile(curFile, file.getName(), fileCapacity);

                        if (curFile.exists()) {
                            JOptionPane.showMessageDialog(null, "Fail to create! File exists!", "Fail",
                                    JOptionPane.ERROR_MESSAGE);
                        } else {
                            try {
                                FileWriter fileWriter = new FileWriter(curFile);
                                fileWriter.write("File\r\n");
                                fileWriter.write(fileCapacity + "\r\n");
                                fileWriter.write("Filename:" + curFile.getName() + "\r\n");
                                fileWriter.write("Filepath:" + curFile.getPath() + "\r\n");
                                long time = newFile.getOsFile().lastModified();
                                String ctime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(time));
                                fileWriter.write("File edit time:" + ctime + "\r\n");
                                fileWriter.write("-----------------------------------------------------------------");
                                fileWriter.flush();
                                curFile.createNewFile();
                            } catch (IOException er) {
                                JOptionPane.showMessageDialog(null, "Fail to create", "Fail",
                                        JOptionPane.ERROR_MESSAGE);
                                er.printStackTrace();
                            }
                            filesMap.add(curFile);
                            int[][] curbitmap = new int [32][32];
                            int temp = 0;
                            for (int i=0;i<32;i++){
                                for (int j=0;j<32;j++){
                                    if (bitmap[i][j]==0){
                                        bitmap[i][j]=1;
                                        curbitmap[i][j]=1;
                                        temp++;
                                        if (temp == fileCapacity) break;
                                    }
                                }if (temp == fileCapacity) break;

                            }
                            filesBit.put(curFile.getPath(),curbitmap);
                            MaxCapacity -= fileCapacity;
                            try {
                                rewriteBit();
                                rewriteRecover(MaxCapacity);
                            }catch (IOException event){
                                event.printStackTrace();
                            }
                            tableModel.removeRows(0, tableModel.getRowCount());
                            tableModel.addRow(newFile);

                            jTable.updateUI();
                        }
                    }
                    else{
                        JOptionPane.showMessageDialog(null, "No enough space to create", "Fail",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }else{
                    JOptionPane.showMessageDialog(null,"Please choose a dir to create a new file","Fail",JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JMenuItem jCreateDirMenuItem = new JMenuItem("Create a dir");
        jCreateDirMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
                OSFile curOSFile = (OSFile)defaultMutableTreeNode.getUserObject();
                String curPath = curOSFile.getFilePath();
                File file = new File(curPath);
                if (file.isDirectory()) {
                    String fileName;
                    JOptionPane inputPane = new JOptionPane();
                    inputPane.setPreferredSize(new Dimension(600, 600));
                    inputPane.setInputValue(JOptionPane.showInputDialog("Input the dir name:"));
                    if (inputPane.getInputValue() == null) {
                        return;
                    }
                    fileName = inputPane.getInputValue().toString();
                    File curFile = new File(curPath+File.separator+fileName);
                    OSFile newFile = new OSFile(curFile,file.getName(),0);
                    if (curFile.exists()){
                        JOptionPane.showMessageDialog(null, "Fail to create! File exists!", "Fail",
                                JOptionPane.ERROR_MESSAGE);
                    }else {
                        curFile.mkdir();
                        File tempFile = new File(curFile.getPath()+File.separator+"init.txt");
                        try {
                            tempFile.createNewFile();
                        }catch (IOException er){
                            er.printStackTrace();
                        }
                        tableModel.removeRows(0, tableModel.getRowCount());
                        tableModel.addRow(newFile);

                        jTable.updateUI();
                    }
                }else{
                    JOptionPane.showMessageDialog(null,"Please choose a dir to create a new file","Fail",JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        JMenuItem jDeleteMenuItem = new JMenuItem("Delete");
        jDeleteMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
                OSFile curOSFile = (OSFile)defaultMutableTreeNode.getUserObject();
                String curPath = curOSFile.getFilePath();
                File file = new File(curPath);
                if (!DeleteCheck(file)){
                    JOptionPane.showMessageDialog(null, "The file is not allowed to be deleted!!", "Access fail", JOptionPane.ERROR_MESSAGE);
                }
                else{
                    int choose = JOptionPane.showConfirmDialog(null, "Are you sure to delete this file?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (choose == 0) {
                        if (DeleteFile(file)) {
                            JOptionPane.showMessageDialog(null, "Delete Success! Please Refresh the folder!","Delete Success",JOptionPane.INFORMATION_MESSAGE);
                            try {
                                rewriteBit();
                                rewriteRecover(MaxCapacity);
                            }catch (IOException event){
                                event.printStackTrace();
                            }
                        }

                    }
                }
            }
        });

        JMenuItem jFormatMenuItem = new JMenuItem("Format");
        jFormatMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
                OSFile curOSFile = (OSFile)defaultMutableTreeNode.getUserObject();
                String curPath = curOSFile.getFilePath();
                File file = new File(curPath);
                if (file.isFile()){
                    JOptionPane.showMessageDialog(null,"Please choose a dir to format","Fail",JOptionPane.ERROR_MESSAGE);
                }else{
                    int choose = JOptionPane.showConfirmDialog(null, "Are you sure to format this folder?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (choose == 0) {
                        if (FormatDir(file)) {
                            try {
                                rewriteBit();
                                rewriteRecover(MaxCapacity);
                            }catch (IOException event){
                                event.printStackTrace();
                            }
                            JOptionPane.showMessageDialog(null, "Format Success! Please Refresh the folder!","Format Success",JOptionPane.INFORMATION_MESSAGE);
                        }
                    }

                }
            }
        });

        jPopupMenu.add(jOpenMenuItem);
        jPopupMenu.add(jCreateFileMenuItem);
        jPopupMenu.add(jCreateDirMenuItem);
        jPopupMenu.add(jDeleteMenuItem);
        jPopupMenu.add(jFormatMenuItem);

        jTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton()==MouseEvent.BUTTON3){//right key
                    jPopupMenu.show(e.getComponent(),e.getX(),e.getY());
                }
            }
        });

        setSize(1200, 1000);
        setVisible(true);

    }
    public static void main(String args[]) throws IOException {
        new OperatingSystemUI();
    }
}
