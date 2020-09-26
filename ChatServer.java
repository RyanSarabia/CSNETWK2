import java.io.*;
import java.util.*;

import javax.swing.JOptionPane;

import java.net.*;
import java.sql.*;
import static java.lang.System.out;

public class ChatServer {
	Vector<String> users = new Vector<String>();
	Vector<HandleClient> clients = new Vector<HandleClient>();
	Vector<String> logs = new Vector<String>();

	public void process() throws Exception  {
		ServerSocket server = new ServerSocket(9999,10, InetAddress.getLocalHost());
		out.println("Server Running...");
		out.println("Server listening on port: " + 	server.getLocalPort());
		out.println("IP address: " + server.getInetAddress().getHostAddress());
		int i =0;
		while( true) {
			Socket client = server.accept();
			HandleClient c = new HandleClient(client);
			clients.add(c);
		}
	}
	public static void main(String ... args) throws Exception {
		new ChatServer().process();
	} 

  	public void broadcast(String user, String message)  {
		// send message to all connected users
		
		String dest = "";
	    for ( HandleClient c : clients )
	    	if ( ! c.getUserName().equals(user)){
				c.sendMessage(user,message);
				dest = dest + ", " +c.getUserName();
			}
		if (! user.equals("Server"))
			newLog(user, dest.substring(1), "Send message");
	 }

	public void broadcastFile(String user, File file){
		String dest = "";
		for ( HandleClient c : clients )
			if ( ! c.getUserName().equals(user) ){
				c.sendFile(user, file);
				dest += ","+c.getUserName();
			}
		newLog(user, dest.substring(1), "File Send");
	}
	 
	public void newLog(String source, String dest, String event){
		Timestamp time = new Timestamp(System.currentTimeMillis());
		String log = time+" "+source+" to "+dest+" "+event;
		logs.add(log);
	}

  	class  HandleClient extends Thread {
        String name = "";
		BufferedReader input;
		PrintWriter output;
		File fileToSend;
		Socket client;

		public HandleClient(Socket  client) throws Exception {
			// get input and output streams
			input = new BufferedReader( new InputStreamReader( client.getInputStream())) ;
			output = new PrintWriter ( client.getOutputStream(),true);
			this.client = client;
			// read name
			name  = input.readLine();
			users.add(name); // add to vector
			newLog(name, "Server", "Login");
			start();
		}

        public void sendMessage(String uname,String  msg) {
	  		output.println( uname + ":" + msg);
		}

		public void sendFile(String uname, File file)  {
			sendMessage(uname,"sent "+file.getName());
			output.println("SEND_FILE_CODE_123456");
			output.println(file.getName());
			fileToSend = file;
		}

		public void writeFile(){
			try{
				DataInputStream disReader = new DataInputStream(new FileInputStream(fileToSend));
				DataOutputStream dosWriter = new DataOutputStream(client.getOutputStream());
				long fileSize = fileToSend.length();
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

		
        public String getUserName() {  
            return name; 
        }
        public void run()  {
			String line;
			broadcast("Server", name+ " has connected to the chat.");
			try{
				while(true){
					line = input.readLine();
					if (line.equals("end")){
						broadcast("Server", name+ " has disconnected...");
						clients.remove(this);
						users.remove(name);
						newLog(name, "Server", "Logout");
						if(users.size()==0){
							out.println("Both users disconnected...");
							out.println("Shutting down server");
							int saveLogs = JOptionPane.showConfirmDialog(null, "Save logs?", "Do you want to save logs?", JOptionPane.YES_NO_OPTION);
							if(saveLogs == JOptionPane.YES_OPTION)
								{
									try{
										File logText = new File("CHAT_LOGS.txt");
										PrintWriter logWriter = new PrintWriter(logText);

										for(String log: logs){
											logWriter.println(log);
										}
										logWriter.flush();
										logWriter.close();
										System.out.println("Log text file created!");
									}
									catch(IOException e){}
								}
							System.exit(0);
						}
						break;
					}
					// else if(line.equals("printLogs")){
					// 	String textLogs = "";
					// 	for(String log: logs){
					// 		textLogs = textLogs + log + "\n";
					// 	}
					// 	textLogs = textLogs + "end of file";
					// 	sendMessage("Print Logs", textLogs);
					// }
					else if(line.equals("sendFile")){

						String fileName = input.readLine();
						File dir = new File("serverDirectory");
						if( ! dir.exists())
							dir.mkdirs();
						

						File file = new File("serverDirectory/"+fileName);
						file.createNewFile();

						DataOutputStream dosWriter = new DataOutputStream(new FileOutputStream(file));
						DataInputStream disReader = new DataInputStream(client.getInputStream());
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

						if(users.size() < 2)
							sendMessage("Server", "No other user connected. File sending cancelled.");
						else 
							broadcastFile(name, file);
						
					}
					else if (line.equals("fileSendReady"))
						writeFile();
					else if (line.equals("fileSendFailure")){
						newLog("Server", name, "File Send Failure");
						System.out.println("File sending failed");
					}
					else if (users.size() < 2)
						sendMessage("Server", "No other users present. Message not sent");
					else 
						broadcast(name,line); // method  of outer class - send messages to all		
				} // end of while
			} // try
			catch(Exception ex) {
			System.out.println(ex.getMessage());
			}
        } // end of run()
   	} // end of inner class

} // end of Server