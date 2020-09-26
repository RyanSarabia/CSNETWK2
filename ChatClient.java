import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import static java.lang.System.out;


public class ChatClient extends JFrame implements ActionListener {
    String uname;
    PrintWriter pw;
    BufferedReader br;
    JTextArea  taMessages;
    JTextField tfInput;
    JButton btnSend,btnExit, btnLog, btnFile;
    FileChooser fc;
    Socket client;

    public ChatClient(String uname, String servername, int serverPort,  String serverAddress) throws Exception {
        super(uname);  // set title for frame
        this.uname = uname;
        client  = new Socket(serverAddress, serverPort);
        br = new BufferedReader( new InputStreamReader( client.getInputStream()) ) ;
        pw = new PrintWriter(client.getOutputStream(),true);
        pw.println(uname);  // send name to server
        buildInterface();
        this.addWindowListener(new WindowAdapter(){
            public void WindowClosing(WindowEvent e){
                pw.println("end");
                System.exit(0);
            }
        });
        new MessagesThread().start();  // create thread for listening for messages
    }
    
    public void buildInterface() {
        
        btnSend = new JButton("Send");
        btnExit = new JButton("Exit");
        // btnLog = new JButton("Logs");
        btnFile = new JButton("Send File");
        taMessages = new JTextArea();
        taMessages.setRows(10);
        taMessages.setColumns(50);
        taMessages.setEditable(false);
        tfInput  = new JTextField(50);
        JScrollPane sp = new JScrollPane(taMessages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(sp,"Center");
        JPanel bp = new JPanel( new FlowLayout());
        bp.add(tfInput);
        bp.add(btnSend);
        bp.add(btnFile);
        // bp.add(btnLog);
        bp.add(btnExit);
        add(bp,"South");
        btnSend.addActionListener(this);
        btnFile.addActionListener(this);
        btnExit.addActionListener(this);
        // btnLog.addActionListener(this);

        sp.getRootPane().setDefaultButton(btnSend);
        btnSend.requestFocus();
        setSize(500,400);
        setVisible(true);
        pack();
        setLocationRelativeTo(null);
    }
    
    public void actionPerformed(ActionEvent evt) {
        if ( evt.getSource() == btnExit ) {
            pw.println("end");  // send end to server so that server know about the termination
            System.exit(0);
        }
        // else if (evt.getSource() == btnLog)
        //     pw.println("printLogs");
        else if(evt.getSource() == btnFile){
            fc = new FileChooser();
                File curFile = fc.openFileChooser();
                pw.println("sendFile");
                pw.println(curFile.getName());
                try{
                    DataInputStream disReader = new DataInputStream(new FileInputStream(curFile));
                    DataOutputStream dosWriter = new DataOutputStream(client.getOutputStream());			
                    long fileSize = curFile.length();
                    dosWriter.writeLong(fileSize);
                    int count;
                    byte[] buffer = new byte[8192];
                    while ((count = disReader.read(buffer)) > 0)
                    {
                        dosWriter.write(buffer, 0, count);
                    }
                    dosWriter.flush();
                    disReader.close();
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
        }      
        else {
            // send message to server
            pw.println(tfInput.getText());
        }
    }

    public ChatClient getSelf(){return ChatClient.this;}
        
    
    public static void main(String ... args) {

    
        // take username from user
        String name = JOptionPane.showInputDialog(null,"Enter your name:", "Username",
             JOptionPane.PLAIN_MESSAGE);
        String serverPortString = JOptionPane.showInputDialog(null,"Enter the server port:", "Port Number",
            JOptionPane.PLAIN_MESSAGE);
        String serverAddress = JOptionPane.showInputDialog(null,"Enter the IP Address:", "IP Address",
            JOptionPane.PLAIN_MESSAGE);
        int serverPort = Integer.parseInt(serverPortString);
        String servername = "localhost";  
        try {
            ChatClient frame = new ChatClient( name ,servername, serverPort, serverAddress);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    frame.pw.println("end");
                    System.exit(0);
                }
            });
        } catch(Exception ex) {
            out.println( "Error --> " + ex.getMessage());
        }
        
    } // end of main
    
    // inner class for Messages Thread
    class  MessagesThread extends Thread {
        public void run() {
            
            String line;
            try {
                while(true) {
                    line = br.readLine();
                    System.out.println(line);
                    // if(line.substring(0,10).equals("Print Logs")){ //creates log text files
                    //     try{
                    //         Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    //         File logText = new File("chatLog.txt");
                    //         PrintWriter logWriter = new PrintWriter(logText);
                    //         String curLine = line.substring(11);
                    //         while(!curLine.equals("end of file")){
                    //             logWriter.println(curLine);
                    //             curLine = br.readLine();
                    //         }
                    //         logWriter.flush();
                    //         logWriter.close();
                    //         System.out.println("Log text file created!");
                    //     }
                    //     catch(IOException e){}
                    // } 
                    if(line.equals("SEND_FILE_CODE_123456")){
                        // System.out.println("ENTERS HERE");
                        String originalFilename = br.readLine();
                        
                        String fileType = originalFilename.substring(originalFilename.lastIndexOf('.')+1);
                        String fileExt = "."+fileType;
                        // FileNameExtensionFilter filter = new FileNameExtensionFilter(fileType, fileExt);
                        FileChooser fc = new FileChooser();
                        File path = fc.openDirectoryChooser();

                        File newFile = new File(path.getAbsolutePath()+fileExt);
                        newFile.createNewFile();

                        DataOutputStream dosWriter = new DataOutputStream(new FileOutputStream(newFile));
                        DataInputStream disReader = new DataInputStream(client.getInputStream());
                        pw.println("fileSendReady");
                        long fileSize = disReader.readLong();
                        int count;
                        byte[] buffer = new byte[8192];
                        while (fileSize > 0)
                        {
                            count = disReader.read(buffer);
                            dosWriter.write(buffer, 0, count);
                            fileSize -= count;
                        }
                        dosWriter.flush();
                        dosWriter.close();

                    }
                    else{
                        taMessages.append(line + "\n"); //appends to chatbox
                    }
                } // end of while
            } catch(Exception ex) {}
        }
    }
} //  end of client