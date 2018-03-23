package com.tmax.chat;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SignInController implements Initializable,ControlledScreen{
	@FXML private Button goToLogin;
	@FXML private Button signIn;
	@FXML private TextField registerUserName;
	@FXML private PasswordField registerPassword;
	@FXML private PasswordField confirmPassword;
	@FXML private TextField registerUserNickName;
	@FXML private Label lblStatus;
	
	private ScreensController myController;
	
	public static HashMap<String, TmaxTalkClient> clientList=new HashMap<String, TmaxTalkClient>();
	
	public SignInController() {}
	
	@Override
	public void setScreenParent(ScreensController screenParent) {
		myController=screenParent;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		signIn.setOnAction(event->addOnClientList());
		goToLogin.setOnAction(event->toLogin());
	}

	public boolean addOnClientList() {
		String id=registerUserName.getText();
		String password=registerPassword.getText();
		String passwordCheck=confirmPassword.getText();
		String nickName=registerUserNickName.getText();
		
		if(id.isEmpty() || id.length()>10 || !Pattern.matches("^[a-zA-Z0-9]*$", id)) {
			openPopup("영문/숫자로 된 10자리 이하의\r\n아이디를 입력하세요");
			return false;
		}
		else if(password.isEmpty() || password.length()>10 
				|| !Pattern.matches("(?=([a-zA-Z0-9].*(\\W))|((\\W).*[a-zA-Z0-9])$).{8,12}", password)) {
			openPopup("8~12 자리의 숫자,특수문자가\r\n포함된 비밀번호를 입력하세요");
			return false;
		}
		else if(!password.equals(passwordCheck)) {
			openPopup("비밀번호가\r\n일치하지 않습니다");
			return false;
		}
		else{
			TmaxTalkClient client=new TmaxTalkClient(id, password, nickName);
			clientList.put(id, client);
			lblStatus.setText("success!");
			registerUserName.setDisable(true);
			registerPassword.setDisable(true);
			confirmPassword.setDisable(true);
			registerUserNickName.setDisable(true);
			signIn.setDisable(true);
			return true;
		}
	}
	 
	private void openPopup(String msg) {
		Stage primaryStage=(Stage)signIn.getScene().getWindow();
		Stage dialog=new Stage(StageStyle.UTILITY);
		Parent parent=null;
		try {
			parent=FXMLLoader.load(getClass().getResource("Warning.fxml"));
		} catch (IOException e) {}
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(primaryStage);
		dialog.setTitle("경고");
		
		Scene scene=new Scene(parent);
		dialog.setScene(scene);
		dialog.setResizable(false);
		dialog.show();
		
		Label lblWarning=(Label)parent.lookup("#lblWarning");
		lblWarning.setText(msg);
		Button btnCancel=(Button)parent.lookup("#btnOk");
		btnCancel.setOnAction(event->dialog.close());
				
	}

	private void toLogin() {
		myController.setScreen(AppMain.screenLogin);
	}

}
