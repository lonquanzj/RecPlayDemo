package com.example.usbconnection.util;

public class ProtocolPack {
	private byte[] protocolPack = new byte[64];
	
	ProtocolPack() {
		// TODO �Զ����ɵĹ��캯�����
		initProtocol();
	}
	
	public byte[] GetPackData(){
		return protocolPack;
	}
	
	//��ʼ����
	public void initProtocol(){
		protocolPack[2] = 0x01;
	}
	
	//д�����
	public void writeSerial(short serial){
		protocolPack[1] = (byte)serial;
		protocolPack[0] = (byte)(serial >> 8);
	}
	
	//CRCУ��
	public void writeCrc(){
		byte temp = 0;
		for(int i = 0; i < 39; i++){
			temp += protocolPack[i];
		}
		protocolPack[63] = temp;
	}
	
	//д����
	public void writeData(byte[] data){
		System.arraycopy(data, 0, protocolPack, 4, data.length);
	}
	
	//д������
	public void writePack(short serial, byte[] data){
		writeSerial(serial);
		writeData(data);
		writeCrc();
	}
}
