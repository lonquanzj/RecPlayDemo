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
	/** ��������ӿ� */
	private UsbInterface bulkInterface;
	/** ���ƴ���ӿ� */
	private UsbInterface controlInterface;
	
	private UsbEndpoint inEndpoint;
	private UsbEndpoint outEndpoint;

	private PendingIntent intent;
	public boolean isConnection = false;

	/** ����ÿ�δ������յ���¼������ */
	public byte[] receiveMusicData = new byte[512];
	/** ��������ÿ�δ������յ���¼������ */
	public List<byte[]> reciveMusicDataList = new ArrayList<byte[]>();

	/** ���Ʒ������ݸ����� */
	private ProtocolPack sendProtocolPackData = new ProtocolPack(); 
	/** ���Ʒ������ݸ����� ��*/
//	public List<byte[]> sendProtocolPackDataList = new ArrayList<byte[]>();
	/** ���������ƽ������� */
//	public byte[] receiveProtocolPackData = new byte[64];
	/** ���������ƽ������� �� */
//	public byte[][] receiveProtocolPackDataArray=new byte[10000][128];
	
	/**��������������� �Ƿ�һֱ���ڽ��� */
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

	
	/********************************����USB�豸***************************************************************************/	
	
	/**
	 * ��ȡһ���豸
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
	 * ��ȡȨ��,�����ȡ�ɹ��������豸.��������ʧ��.
	 */
	public String getUsbDevicePermission() {
		getDevice();
		if (usbDevice == null) {
			isConnection = false;
			return "�豸Ϊ��";
		}

		if (usbManager.hasPermission(usbDevice)) {
			connectionUsbDevice();
			return "�豸���ӳɹ�";
		} else {
			usbManager.requestPermission(usbDevice, intent);
			return "�豸��ȡȨ��";
		}
	}


	/**
	 * �����豸,���ӳɹ���ʧ�����ͳɹ���ʧ����Ϣ
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
	 * ע��㲥
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
	 * ��һ����������ʾ�豸��Ϣ
	 */
	public void showDeviceInfoDialog(String message) {
		AlertDialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("�豸��Ϣ").setMessage(message)
				.setPositiveButton("�˳�", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				});
		dialog = builder.create();
		dialog.show();
	}

	/**
	 * ��ȡ�豸��Ϣ
	 */
	public String getDeviceInfo() {
		StringBuffer stringBuffer = new StringBuffer();

		getDevice();

		if (usbDevice != null) {
			stringBuffer
					.append("�豸��VendorId:" + usbDevice.getVendorId() + "\n");
			stringBuffer.append("�豸��ProductId:" + usbDevice.getProductId()
					+ "\n");
			stringBuffer.append("�豸������:" + usbDevice.getDeviceName() + "\n");
			stringBuffer
					.append("�豸��Э��:" + usbDevice.getDeviceProtocol() + "\n");
			stringBuffer.append("�ӿڸ�����" + usbDevice.getInterfaceCount() + "\n");
			stringBuffer.append("Subclass:" + usbDevice.getDeviceSubclass()
					+ "\n");
			stringBuffer.append("class:" + usbDevice.getDeviceClass() + "\n");
			stringBuffer.append("�Ƿ���Ȩ�ޣ�" + usbManager.hasPermission(usbDevice)
					+ "\n");

			for (int j = 0; j < usbDevice.getInterfaceCount(); j++) {
				UsbInterface interface1 = usbDevice.getInterface(j);
				stringBuffer.append("�ӿ�-" + j + "=" + interface1.toString()
						+ "\n");
				stringBuffer.append("\n");
				stringBuffer.append(j + "-�ӿڵ����������ĸ���="
						+ interface1.getEndpointCount() + "\n");

				for (int i = 0; i < interface1.getEndpointCount(); i++) {
					stringBuffer.append("\n");
					UsbEndpoint endpoint = interface1.getEndpoint(i);
					if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
						stringBuffer.append(j + "-�ӿ�," + i + "-Ϊ�����:"
								+ endpoint + "direction="
								+ endpoint.getDirection() + ",type="+endpoint.getType()+"\n");
						stringBuffer.append("\n");

					} else if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
						stringBuffer.append(j + "-�ӿ�," + i + "-Ϊ�����:"
								+ endpoint + "direction="
								+ endpoint.getDirection() + ",type="+endpoint.getType()+"\n");
					} else {
						stringBuffer
								.append("type=" + endpoint.getType()
										+ ",\n������������:"
										+ endpoint.getAttributes()
										+ "\n �����������=" + endpoint.getAddress()
										+ "\n ���������ͱ��"
										+ endpoint.getDirection() + "\n");
					}
					//

				}

			}
		} else {
			stringBuffer.append("��,ľ���豸����");
		}

		return new String(stringBuffer);

	}
	
	/********************************��������***************************************************************************/
//	public class UsbTransData {
		/**
		 * 
		 * @return ���������򷵻ؽ��յ���¼������ ,û�������򷵻�null
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
		 * �������ð���Ϣ
		 * 
		 * @param1 num ������
		 * @param2 data ������
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
		 * ���ƴ��䷢��һ������
		 * 
		 * @param bytes
		 *            Ҫ���͵��ֽ�����
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
		 * ��ȡ¼������
		 */
		public void receiveMusicDataByBulk() {
			
			isRecoder=true;
			new Thread(new Runnable() {
				

				@Override
				public void run() {

					/** 64λ��2ά���� */
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
						
						//�޳���0000��������
						byte [] bTemp = new byte[4];
						byte [] bTemp1 = {0, 0, 0, 0};
						System.arraycopy(receiveMusicData, 0, bTemp, 0, 4);
						if(!Arrays.equals(bTemp, bTemp1)){
							//���������ظ������⣬��������һ������
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



