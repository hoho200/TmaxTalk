package com.tmax.chat;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class AppMain extends Application{
	public static String screenLogin="login";
	public static String screenLoginFile="/com/tmax/chat/Login.fxml";
	public static String screenSignIn="signIn";
	public static String screenSignInFile="/com/tmax/chat/SignIn.fxml";
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		ScreensController mainContainer=new ScreensController();
		mainContainer.loadScreen(AppMain.screenLogin, AppMain.screenLoginFile);
		mainContainer.loadScreen(AppMain.screenSignIn, AppMain.screenSignInFile);
		
		mainContainer.setScreen(AppMain.screenLogin);
		Group root=new Group();
		root.getChildren().addAll(mainContainer);
		Scene scene=new Scene(root);
		primaryStage.setTitle("TmaxTalk");
		primaryStage.setScene(scene);
		primaryStage.show();
		
		/*primaryStage.setOnHiding(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				System.exit(0);
			}
		});*/
	
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
