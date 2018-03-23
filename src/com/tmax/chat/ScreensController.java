package com.tmax.chat;

import java.util.HashMap;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class ScreensController extends StackPane {
	private HashMap<String, Node> screens=new HashMap<String, Node>();
	
	public ScreensController() {}
	
	public void addScreen(String name,Node screen) {
		screens.put(name, screen);
	}
	public void getScreen(String name) {
		screens.get(name);
	}
	
	public boolean loadScreen(String name,String resource) {
		try {
			FXMLLoader myLoader=new FXMLLoader(getClass().getResource(resource));
			Parent loadScreen=(Parent)myLoader.load();
			ControlledScreen myScreenController=((ControlledScreen)myLoader.getController());
			myScreenController.setScreenParent(this);
			addScreen(name, loadScreen);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;			
		}
	}
	
	public boolean setScreen(final String name) {
		if(screens.get(name)!=null) {
			final DoubleProperty opacity=opacityProperty();
			
			if(!getChildren().isEmpty()) {
				Timeline fade=new Timeline(
						new KeyFrame(Duration.ZERO,new KeyValue(opacity, 1)),
						new KeyFrame(new Duration(1000), new EventHandler<ActionEvent>() {
							
							@Override
							public void handle(ActionEvent event) {
								getChildren().remove(0);
								getChildren().add(0,screens.get(name));
								Timeline fadeIn = new Timeline(
										new KeyFrame(Duration.ZERO,new KeyValue(opacity, 0)),
										new KeyFrame(new Duration(800),new KeyValue(opacity,1)));
								fadeIn.play();
							}},new KeyValue(opacity,0)));
				fade.play();
			}
			else {
				setOpacity(0);
				getChildren().add(screens.get(name));
				Timeline fadeIn = new Timeline(
						new KeyFrame(Duration.ZERO,new KeyValue(opacity, 0)),
						new KeyFrame(new Duration(2500),new KeyValue(opacity,1)));
				fadeIn.play();
			}
			return true;
		}
		else {
			System.out.println("스크린이 없습니다.");
			return false;
		}
	}
	
	public boolean unloadScreen(String name) {
		if(screens.remove(name)==null) {
			System.out.println("Screen이 존재하지 않았음");
			return false;
		}
		return true;
	}
	
}
