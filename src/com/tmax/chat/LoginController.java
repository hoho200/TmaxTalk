package com.tmax.chat;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LoginController implements Initializable,ControlledScreen {
	@FXML private Label lblStatus;
    @FXML private TextField txtUserName;
    @FXML private TextField txtPassword;
    @FXML private Button goToSignIn;
    @FXML private Button logIn;
    
    private ScreensController myController;
    
    public boolean Login(ActionEvent event) throws Exception{
    	String id=txtUserName.getText();
    	String pw=txtPassword.getText();
    	if(id.isEmpty() || pw.isEmpty()) {
    		lblStatus.setText("���̵�/��й�ȣ�� �Է��ϼ���");
    		return false;
    	}
    	else if(!SignInController.clientList.containsKey(id)){
        	lblStatus.setText("�������� �ʴ� ���̵� �Դϴ�");
        	txtUserName.setText("");
        	txtPassword.setText("");
        	return false;
        }
        else if(!SignInController.clientList.get(id).getPassword().equals(pw)) {
        	lblStatus.setText("��й�ȣ�� Ȯ���ϼ���");
            txtPassword.setText("");
            return false;
        }
        else{
        	FXMLLoader fxmlloader=new FXMLLoader(getClass().getResource("/com/tmax/chat/Main.fxml"));
        	fxmlloader.setRoot(SignInController.clientList.get(id));
        	fxmlloader.setController(SignInController.clientList.get(id));
        	Parent root = fxmlloader.load();////////////////////SignInController���� new�� TmaxTalkClient�� ��Ʈ�ѷ��� �ؾ� �ű��� id,nickname�� ���µ�....[��]
        	TmaxTalkClient controller=fxmlloader.getController();
        	Scene mainScene = new Scene(root);
            Stage app_stage=(Stage)((Node)event.getSource()).getScene().getWindow();
            app_stage.setScene(mainScene);
            app_stage.setOnHidden(e -> controller.shutDown());
            app_stage.show();
            return true;
        }
    }

	@Override
	public void setScreenParent(ScreensController screenParent) {
		myController=screenParent;
	}
	
	public void signIn(ActionEvent event) {
		myController.setScreen(AppMain.screenSignIn);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		goToSignIn.setOnAction(event->signIn(event));
		logIn.setOnAction(event->{
			try {
				Login(event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}    
}
