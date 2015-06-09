package com.example.usbconnection.util;

public class StaticFinal {


	public static final int DATA_LENGTH = 64;
	/** 数据发送成功 */
	public final static int SENDDATA_SUCCESS = 1;
	/** 数据发送失败 */
	public final static int SENDDATA_FAIL = 2;
	/** 收到数据 */
	public final static int RECEIVERDATA_SUCCESS = 3;
	public final static int RECEIVERDATA_FAIL = 4;
	public final static int SENDDATA_RECEIVERDATA_SUCCESS = 5;
	public final static int COMPLETED = 6;
	public final static int RECEIVEDATA_BULK_SUCCESS = 7;
	public final static int RECEIVEDATA_CONTROL_SUCCESS = 8;
	public final static int SENDDATA_BULK_SUCCESS = 9;
	public final static int SENDDATA_CONTROL_SUCCESS = 10;
	public final static int SEND_MUSICDATA_SUCCESS = 7777;
	public final static int DEVICE_CONNECTION_SUCCESS = 77;
	public final static int DEVICE_CONNECTION_FAIL = 78;
	public final static int ACTION_USB_DEVICE_ATTACHED = 79;
	public final static int ACTION_USB_DEVICE_DETACHED = 80;
	public final static int TIME_OUT = 5000;
	/** 写入音乐文件完毕 */
	public final static int WRITEFILE_SUCCESS = 5001;
	public static final int WRITEFILE = 5002;
	
	
	public  static final byte EFFECT_TYPE = 0x02;
	public  static final byte EFFECT_STRENGTH = 0x03;
	public  static final byte HEADSET_VOLUME = 0x04;
	public  static final byte MIC_VOLUME = 0x05;
	
	
}
