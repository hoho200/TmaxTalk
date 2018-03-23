package com.tmax.chat;

import javafx.beans.property.SimpleStringProperty;

public class Client {//�̿��� Table�� �� ��ü
	private SimpleStringProperty nickName;
	private SimpleStringProperty statusMsg;
	
	//������
	public Client() {}	
	public Client(String nickName, String statusMsg) {
		this.nickName = new SimpleStringProperty(nickName);
		this.statusMsg = new SimpleStringProperty(statusMsg);
	}
	
	public String getNickName() {
		return nickName.get();
	}
	public void setNickName(String nickName) {
		this.nickName.set(nickName);
	}
	public String getStatusMsg() {
		return statusMsg.get();
	}
	public void setStatusMsg(String statusMsg) {
		this.statusMsg.set(statusMsg);
	}
	
	@Override
    public boolean equals(Object obj) {
        return obj instanceof Client &&
                ((Client) obj).getNickName().equals(nickName.get());
    }
	
	@Override
	public int hashCode() {
		return nickName.get().hashCode();
	}
}
