package com.example.usbconnection.util;

public class ProtocolPack {
	private byte[] protocolPack = new byte[64];
	
	ProtocolPack() {
		// TODO 自动生成的构造函数存根
		initProtocol();
	}
	
	public byte[] GetPackData(){
		return protocolPack;
	}
	
	//初始化包
	public void initProtocol(){
		protocolPack[2] = 0x01;
	}
	
	//写包序号
	public void writeSerial(short serial){
		protocolPack[1] = (byte)serial;
		protocolPack[0] = (byte)(serial >> 8);
	}
	
	//CRC校验
	public void writeCrc(){
		byte temp = 0;
		for(int i = 0; i < 39; i++){
			temp += protocolPack[i];
		}
		protocolPack[63] = temp;
	}
	
	//写数据
	public void writeData(byte[] data){
		System.arraycopy(data, 0, protocolPack, 4, data.length);
	}
	
	//写整个包
	public void writePack(short serial, byte[] data){
		writeSerial(serial);
		writeData(data);
		writeCrc();
	}
}
