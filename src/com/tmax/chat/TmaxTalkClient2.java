package com.tmax.chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

public class TmaxTalkClient2 extends BorderPane implements Initializable,ClientImpl {
	private String ids;
	private String password;
	private String nickName;
	private String statusMsg;
	@FXML private Button btnSend;
	@FXML private TextField txtField;
	@FXML private TextArea txtArea;
	@FXML private TableView<Client> userTable;
	
	String keyNick;
	Map<String, String> clientStatus=new HashMap<String, String>();//nick,status
	private final String SERVERIP="127.0.0.1";
	
	Socket socket=null;
	BufferedWriter bw=null;
	
	public TmaxTalkClient2() {}	
	public TmaxTalkClient2(String ids, String password) {
		this.ids = ids;
		this.password = password;
	}
	public TmaxTalkClient2(String ids, String password, String nickName) {
		this.ids = ids;
		this.password = password;
		this.nickName = nickName;
	}
	
	public String getIds() {
		return ids;
	}
	public void setIds(String ids) {
		this.ids = ids;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getStatusMsg() {
		return statusMsg;
	}
	public void setStatusMsg(String statusMsg) {
		this.statusMsg = statusMsg;
	}
		
	@Override
	public void connect() {
		try {
			socket=new Socket(SERVERIP,7080);
			bw=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			bw.write("/connect-info:Nickname["+nickName+"]\r\n");
			bw.flush();
			Thread readerThread=new Thread(new IncomingReader(socket));
			readerThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void sendMsg(ActionEvent event) {
		if(socket!=null) {
			String msg=txtField.getText();
			try {
				bw.write(nickName==null?ids:nickName+": "+msg+"\r\n");
				bw.flush();
			} 
			catch (SocketException e) {txtField.appendText("[서버와의 연결이 끊어졌습니다]\r\n");}
			catch (IOException e) {e.printStackTrace();}
			txtField.setText("");
		}
	}
	
	public class IncomingReader implements Runnable{
		Socket sock;
		BufferedReader br;
		
		public IncomingReader(Socket socket) {
			try {
				sock=socket;
				br=new BufferedReader(new InputStreamReader(sock.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			try {
				String message;
				Collection<Client> clientCollection=null;
				ObservableList<Client> tableList=null;
				while ((message=br.readLine())!=null) {
					if(message.startsWith("/table-info")) {
						////////////////날아온 정보로 테이블 내용 업데이트
						message=message.substring(message.indexOf("{")+1,message.indexOf("}"));
						String[] messageFirstSplit=message.split(",");
						for(String nickStatus:messageFirstSplit) {///////상태 메세지에 =랑, 못쓰게 할것
							String[] messageSecontSplit=nickStatus.split("=");
							clientStatus.put(messageSecontSplit[0].trim(), messageSecontSplit[1].trim());
						}
						System.out.println(clientStatus);/////////////////////////////////출력
						Set<String> keys=clientStatus.keySet();
						Iterator<String> key=keys.iterator();
						while(key.hasNext()) {
							keyNick=key.next();
							System.out.println("keyNick:"+keyNick);/////////////////////////////////////출력
							Client client=new Client(keyNick,clientStatus.get(keyNick));
							if(!userTable.getItems().contains(client))
								userTable.getItems().add(client);
					
							//들어오는 Map 데이터랑 Table상 데이터랑 비교 어캐? (Table에)있으면 냅두고 없으면 제거해야함
							////https://stackoverflow.com/questions/30610011/how-to-determine-if-record-exist-in-tableview-in-javafx 참고
						}
						continue;
					}
					txtArea.appendText(message+"\r\n");
				}
			} catch (IOException e1) {
				txtArea.appendText("[채팅 서버가 종료되었습니다]");
			}
		}
	}
	
	private void setTableColumn() {
		userTable.getColumns().get(1).setEditable(true);
		
		TableColumn nick=userTable.getColumns().get(0);
		nick.setCellValueFactory(new PropertyValueFactory("nickName"));
		 
		TableColumn statusMsg=userTable.getColumns().get(1);
		statusMsg.setCellValueFactory(new PropertyValueFactory("statusMsg"));
	}
	 
	public void shutDown() {
		try {
			bw=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			bw.write("/exit-info:"+nickName+"\r\n");
			bw.flush();
		} catch (IOException e) {e.printStackTrace();}
		Platform.exit();
		System.exit(0);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		connect();
		btnSend.setOnAction(event->sendMsg(event));	
		txtField.setOnKeyPressed((event)->{if(event.getCode().equals(KeyCode.ENTER)){sendMsg(null);}});
		setTableColumn();
	}

}
