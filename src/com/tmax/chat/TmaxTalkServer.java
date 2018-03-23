package com.tmax.chat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public class TmaxTalkServer implements Initializable,Runnable {
	@FXML private Button btnRun;
	@FXML private Button btnStop;
	@FXML private Button btnSend;
	@FXML private TextField textFieldIp;
	@FXML private TextField textFieldMsg;
	@FXML private TextArea textArea;
	
	ServerSocket serverSocket=null;
	List<BufferedWriter> clientOutputStreams;//Socket.getoutputstream��
	Map<String, Socket> clientSockets;//ip,socket
	Map<String, String> clientNicks=new HashMap<String, String>();//ip,nick
	Map<String, String> clientStatus=new HashMap<String, String>();//nick,status

	public TmaxTalkServer() {}
	
	@Override
	public void run() {
		Socket socket=null;
		clientOutputStreams=new ArrayList<BufferedWriter>();
		clientSockets=new HashMap<String, Socket>();
		try {
			serverSocket=new ServerSocket(7080);
			while(true) {
				socket=serverSocket.accept();
				BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				clientOutputStreams.add(bw);
				Thread readerThread = new Thread(new ClientHandler(socket));
				readerThread.start();
				textArea.appendText("["+socket.getInetAddress().getHostAddress()+" ���� ����]\r\n");
			}
			
		} catch (IOException e) {e.printStackTrace();}
	}
	
	public void sendMsg(ActionEvent event){
		String msg=textFieldMsg.getText();
		String serverMsg="Server: "+msg+"\r\n";
		textArea.appendText(serverMsg);
		textFieldMsg.setText("");
		if(msg.startsWith("/")) {
			if(msg.equals("/help")) {
				textArea.appendText("[��ɾ� �ȳ�]\r\n����: /kick ������IP�ּ�\r\n�̿��� ���: /clientlist\r\n");
			}
			else if(msg.startsWith("/kick") && msg.length()>6) {
				String ipToKick=msg.substring(msg.indexOf("/kick")+6);
				if(clientSockets.containsKey(ipToKick))
					try {
						clientStatus.remove(clientNicks.get(ipToKick));
						informEveryone(clientNicks.get(ipToKick),null);
						BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(clientSockets.get(ipToKick).getOutputStream()));
						bw.write("[Ƽ�ƽ��忡�� ������ ���� �Ǿ����ϴ�]");
						bw.close();
						clientSockets.get(ipToKick).close();
						clientSockets.remove(ipToKick);
					} catch (IOException e) {e.printStackTrace();}
				else {textArea.appendText("������ IP�ּҰ� �������� �ʰų� �߸� �Է��ϼ̽��ϴ�\r\n");}
			}
			else if(msg.equals("/clientlist")) {
				textArea.appendText("[�̿��� ���(Nickname,IP)]\r\n");
				Set<String> clientKeys=clientSockets.keySet();
				for(String clientKey:clientKeys) {
					textArea.appendText("Nickname["+clientNicks.get(clientKey)+"],"+clientSockets.get(clientKey).toString()+"\r\n");
				}
			}
			return;
		}
		Iterator it=clientOutputStreams.iterator();
		while (it.hasNext()) {
			try {
				BufferedWriter bw=(BufferedWriter)it.next();
				bw.write(serverMsg);
				bw.flush();
			} 
			//catch (SocketException e) {textArea.appendText("����� ������ ���ų� ������ �߻��߽��ϴ�\r\n");}///���� Ȥ�� Client ���� ���Ŀ� ��� �������� Ȯ��. �̿��ڰ� �ƹ��� �������� �����ߴ�.
			catch (IOException e) {e.printStackTrace();}
		}
		
	}
	
	public void tellEveryone(String message) {
		Iterator it=clientOutputStreams.iterator();
		
		while (it.hasNext()) {
			try {
				BufferedWriter bw=(BufferedWriter) it.next();
				bw.write(message);
				bw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void informEveryone(String kickNick,String statusMsg) {
		Iterator it=clientOutputStreams.iterator();
		while (it.hasNext()) {
			try {
				if(kickNick==null && statusMsg==null) {
					BufferedWriter bw=(BufferedWriter) it.next();
					bw.write("/table-info:"+clientStatus.toString()+"\r\n");
					bw.flush();
				}
				else if(kickNick != null && statusMsg==null) {
					BufferedWriter bw=(BufferedWriter) it.next();
					bw.write("/exit-info:"+kickNick+"\r\n");
					bw.flush();
				}
				else if(kickNick != null && statusMsg != null) {
					BufferedWriter bw=(BufferedWriter) it.next();
					bw.write("/statusMsg-info:"+kickNick+"="+statusMsg+"\r\n");
					bw.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class ClientHandler implements Runnable{
		Socket sock;
		BufferedReader br;
		
		public ClientHandler(Socket clientSocket) {
			try {
				sock=clientSocket;
				clientSockets.put(clientSocket.getInetAddress().getHostAddress(), clientSocket);
				br=new BufferedReader(new InputStreamReader(sock.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			String message;
			String nick;
			int a=2;
			try {
				while ((message=br.readLine())!=null) {
					if(message.startsWith("/connect-info:")) {//���� �� �̿��� ���� ���(nickname)
						nick=message.substring(message.indexOf("Nickname")+9,message.indexOf("]"));
						if(clientStatus.containsKey(nick)) {
							nick=nick+"("+a+")";
							/*BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
							bw.write("/nickChange-to:"+nick);
							bw.flush();*/
							clientNicks.put(sock.getInetAddress().getHostAddress(),nick);
							clientStatus.put(nick, null);
							a++;
						}
						else {
							clientNicks.put(sock.getInetAddress().getHostAddress(),nick);
							clientStatus.put(nick, null);
						}
						informEveryone(null,null);
						textArea.appendText(message+"\r\n");
						continue;
					}else if(message.startsWith("/exit-info")) {//////////����� �̿��� ���� ����
						nick=message.substring(message.indexOf(":")+1);
						clientStatus.remove(nick);
						informEveryone(nick,null);
						continue;
					}else if(message.startsWith("/statusMsg-info")) {/////////��� ����� �̿��� ���� ����(nickname,newMsg)
						String nickAndMsg=message.substring(message.indexOf(":")+1);
						String[] nickEqMsg=nickAndMsg.split("=");
						clientStatus.replace(nickEqMsg[0].trim(),nickEqMsg[1].trim());
						informEveryone(nickEqMsg[0].trim(),nickEqMsg[1].trim());
						continue;
					}
					textArea.appendText(message+"\r\n");
					tellEveryone(message+"\r\n");
				}
			} 
			catch (SocketException e) {textArea.appendText("�̿��ڰ� ����(or ����)ó�� �Ǿ����ϴ�\r\n");}
			catch (Exception e) {e.printStackTrace();}
		}

	}
	
	private void handleStartBtn(ActionEvent event) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				textArea.appendText("[���� ����]\r\n");
				textArea.appendText("[���� ��ɾ� �ȳ��� /help �� �Է��ϼ���]\r\n");
				textArea.appendText("[Client�� ��ٸ�����...]\r\n");
			}
		});
		Thread thread=new Thread(this);
		thread.start();
		btnRun.setDisable(true);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		btnRun.setOnAction(event->handleStartBtn(event));
		btnStop.setOnAction(event->System.exit(0));
		btnSend.setOnAction(event->sendMsg(event));
		textFieldMsg.setOnKeyPressed((event)->{if(event.getCode().equals(KeyCode.ENTER)){sendMsg(null);}});
	}

	

}
