package com.example.usbconnection.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;

@SuppressLint("NewApi")
public class UsbDevicesUtil {
	private Context mContext;
	private Handler handler;
	
	private UsbManager usbManager;
	private UsbDeviceConnection usbDeviceConnection;
	private UsbDevice usbDevice;
	/** 批量传输接口 */
	private UsbInterface bulkInterface;
	/** 控制传输接口 */
	private UsbInterface controlInterface;
	
	private UsbEndpoint inEndpoint;
	private UsbEndpoint outEndpoint;

	private PendingIntent intent;
	public boolean isConnection = false;

	/** 批量每次从声卡收到的录音数据 */
	public byte[] receiveMusicData = new byte[512];
	/** 保存批量每次从声卡收到的录音数据 */
	public List<byte[]> reciveMusicDataList = new ArrayList<byte[]>();

	/** 控制发送数据给声卡 */
	private ProtocolPack sendProtocolPackData = new ProtocolPack(); 
	/** 控制发送数据给声卡 表*/
//	public List<byte[]> sendProtocolPackDataList = new ArrayList<byte[]>();
	/** 从声卡控制接收数据 */
//	public byte[] receiveProtocolPackData = new byte[64];
	/** 从声卡控制接收数据 表 */
//	public byte[][] receiveProtocolPackDataArray=new byte[10000][128];
	
	/**批量传输接收数据 是否一直处于接收 */
	public boolean isRecoder=false;
	
	public static final int DATA_LENGTH = 64;
	private final static String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	protected static final String TAG = UsbDevicesUtil.class.getSimpleName();
	
	public UsbDevicesUtil(Handler handler) {
		this.handler = handler;
	}

	public UsbDevicesUtil(Context context, Handler handler) {
		this.mContext = context;
		this.handler = handler;

		registerReceiver();
	}

	
	/********************************连接USB设备***************************************************************************/	
	
	/**
	 * 获取一个设备
	 */
	private void getDevice() {
		usbManager = (UsbManager) mContext
				.getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
		Iterator<UsbDevice> iterator = usbDevices.values().iterator();
		while (iterator.hasNext()) {
			usbDevice = iterator.next();
		}
	}

	/**
	 * 获取权限,如果获取成功则连接设备.否则连接失败.
	 */
	public String getUsbDevicePermission() {
		getDevice();
		if (usbDevice == null) {
			isConnection = false;
			return "设备为空";
		}

		if (usbManager.hasPermission(usbDevice)) {
			connectionUsbDevice();
			return "设备连接成功";
		} else {
			usbManager.requestPermission(usbDevice, intent);
			return "设备获取权限";
		}
	}


	/**
	 * 连接设备,连接成功或失败则发送成功或失败消息
	 */
	@SuppressLint("NewApi")
	private void connectionUsbDevice() {

		usbDeviceConnection = usbManager.openDevice(usbDevice);
		if (usbDeviceConnection != null) {
			isConnection = true;
			handler.sendEmptyMessage(StaticFinal.DEVICE_CONNECTION_SUCCESS);
		} else {
			isConnection = false;
			handler.sendEmptyMessage(StaticFinal.DEVICE_CONNECTION_FAIL);
		}

	}
	
	/**
	 * 注册广播
	 */
	private void registerReceiver() {
		intent = PendingIntent.getBroadcast(mContext, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		mContext.registerReceiver(mUsbReceiver, filter);
	}

	public void unregisterReceiver() {
		mContext.unregisterReceiver(mUsbReceiver);
	}

	private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		@SuppressLint("NewApi")
		@Override
		public void onReceive(Context arg0, Intent intent) {
			String actionString = intent.getAction();
			if (actionString.equals(ACTION_USB_PERMISSION)) {
				connectionUsbDevice();
			} else if (actionString
					.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
				handler.sendEmptyMessage(StaticFinal.ACTION_USB_DEVICE_ATTACHED);
			} else if (actionString
					.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
				handler.sendEmptyMessage(StaticFinal.ACTION_USB_DEVICE_DETACHED);
				closeThread();
			}

		}

	};

	public  boolean isRun=true;
	public void setIsRun(boolean isRun){
		this.isRun=isRun;
	}

	
	public void closeThread(){
//		this.isRun=false;
		this.isRecoder=false;
	}
	public void startThread(){
		this.isRecoder=true;
	}

	/**
	 * 用一个弹出框显示设备信息
	 */
	public void showDeviceInfoDialog(String message) {
		AlertDialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("设备信息").setMessage(message)
				.setPositiveButton("退出", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				});
		dialog = builder.create();
		dialog.show();
	}

	/**
	 * 获取设备信息
	 */
	public String getDeviceInfo() {
		StringBuffer stringBuffer = new StringBuffer();

		getDevice();

		if (usbDevice != null) {
			stringBuffer
					.append("设备的VendorId:" + usbDevice.getVendorId() + "\n");
			stringBuffer.append("设备的ProductId:" + usbDevice.getProductId()
					+ "\n");
			stringBuffer.append("设备的名字:" + usbDevice.getDeviceName() + "\n");
			stringBuffer
					.append("设备的协议:" + usbDevice.getDeviceProtocol() + "\n");
			stringBuffer.append("接口个数：" + usbDevice.getInterfaceCount() + "\n");
			stringBuffer.append("Subclass:" + usbDevice.getDeviceSubclass()
					+ "\n");
			stringBuffer.append("class:" + usbDevice.getDeviceClass() + "\n");
			stringBuffer.append("是否有权限：" + usbManager.hasPermission(usbDevice)
					+ "\n");

			for (int j = 0; j < usbDevice.getInterfaceCount(); j++) {
				UsbInterface interface1 = usbDevice.getInterface(j);
				stringBuffer.append("接口-" + j + "=" + interface1.toString()
						+ "\n");
				stringBuffer.append("\n");
				stringBuffer.append(j + "-接口的输入输出点的个数="
						+ interface1.getEndpointCount() + "\n");

				for (int i = 0; i < interface1.getEndpointCount(); i++) {
					stringBuffer.append("\n");
					UsbEndpoint endpoint = interface1.getEndpoint(i);
					if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
						stringBuffer.append(j + "-接口," + i + "-为输入点:"
								+ endpoint + "direction="
								+ endpoint.getDirection() + ",type="+endpoint.getType()+"\n");
						stringBuffer.append("\n");

					} else if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
						stringBuffer.append(j + "-接口," + i + "-为输出点:"
								+ endpoint + "direction="
								+ endpoint.getDirection() + ",type="+endpoint.getType()+"\n");
					} else {
						stringBuffer
								.append("type=" + endpoint.getType()
										+ ",\n传输类型属性:"
										+ endpoint.getAttributes()
										+ "\n 输入输出属性=" + endpoint.getAddress()
										+ "\n 描述符类型编号"
										+ endpoint.getDirection() + "\n");
					}
					//

				}

			}
		} else {
			stringBuffer.append("亲,木有设备连接");
		}

		return new String(stringBuffer);

	}
	
	/********************************传输数据***************************************************************************/
//	public class UsbTransData {
		/**
		 * 
		 * @return 若有数据则返回接收到的录音数据 ,没有数据则返回null
		 */
		public List<byte[]> getRecoderData() {
			

				if (reciveMusicDataList.size() > 0) {
					List<byte[]> tempList = new ArrayList<byte[]>();
					synchronized (UsbDevicesUtil.this) {
						tempList.addAll(reciveMusicDataList);
						reciveMusicDataList.clear();
					}
					return tempList;
				}
				return null;

//			}

		}
		
		/**
		 * 发送设置包信息
		 * 
		 * @param1 num 包代号
		 * @param2 data 包数据
		 */
		short serial = 0x1234;
		public void sendCtrlPack(byte num, byte data) {

			byte[] temp = new byte[60];
			temp[0] = 0x01;
			temp[1] = num;
			temp[2] = data;
			sendProtocolPackData.writePack(serial, temp);
			serial++;
			sendDataByBulk(sendProtocolPackData.GetPackData());
		}


		/**
		 * 控制传输发送一条数据
		 * 
		 * @param bytes
		 *            要发送的字节数组
		 */
		public void sendDataByBulk(final byte[] data) {
			new Thread(new Runnable() {

				@SuppressLint("NewApi")
				@Override
				public void run() {
					controlInterface = usbDevice.getInterface(6);
					if (outEndpoint == null) {
						outEndpoint = controlInterface.getEndpoint(1);
					}
					usbDeviceConnection.claimInterface(controlInterface, true);

					int a = usbDeviceConnection.bulkTransfer(outEndpoint,data, 
							data.length, StaticFinal.TIME_OUT);
					
					Message msg = Message.obtain();
					if (a != -1) {
						msg.what = StaticFinal.SENDDATA_CONTROL_SUCCESS;
					} else {
						msg.what = StaticFinal.SENDDATA_FAIL;
					}

					handler.sendMessage(msg);
				}
			}).start();

		}
		

		/**
		 * 读取录音数据
		 */
		public void receiveMusicDataByBulk() {
			
			isRecoder=true;
			new Thread(new Runnable() {
				

				@Override
				public void run() {

					/** 64位的2维数组 */
					if (controlInterface != null) {
						usbDeviceConnection.releaseInterface(controlInterface);
					}
					bulkInterface = usbDevice.getInterface(5);

					if (inEndpoint == null) {
						inEndpoint = bulkInterface.getEndpoint(0);
					}
					usbDeviceConnection.claimInterface(bulkInterface, true);
					
					reciveMusicDataList.clear();
					while (isRecoder) {
						usbDeviceConnection.bulkTransfer(inEndpoint,receiveMusicData, receiveMusicData.length, StaticFinal.TIME_OUT);
						
						//剔除｛0000｝的数据
						byte [] bTemp = new byte[4];
						byte [] bTemp1 = {0, 0, 0, 0};
						System.arraycopy(receiveMusicData, 0, bTemp, 0, 4);
						if(!Arrays.equals(bTemp, bTemp1)){
							//避免数据重复的问题，重新设置一个数组
							byte[] receiverMusicData_temp = new byte[receiveMusicData.length-4];
							System.arraycopy(receiveMusicData, 4, receiverMusicData_temp, 0, receiveMusicData.length-4);
							synchronized (UsbDevicesUtil.this) {
			 					reciveMusicDataList.add(receiverMusicData_temp);
							}
						}
					}
				}
			}).start();
		}
		
//	}
}



