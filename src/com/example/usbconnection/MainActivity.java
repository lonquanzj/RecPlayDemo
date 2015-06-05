package com.example.usbconnection;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.usbconnection.util.DataUtil;
import com.example.usbconnection.util.UsbDevicesUtil;

public class MainActivity extends Activity {
	// private static final String TAG = "MainActivity";
	private UsbDevicesUtil usbDevicesUtil;
	public TextView tv_message;
	/**
	 * �������ݵ��߳��Ƿ���
	 */
	boolean isReceiver;

	long start = 0, end = 0;

	private int index = 0;
	


	private DataUtil dataUtil;
	private AlertDialog dialog;
	private ScrollView srollview;
	// private byte[][] sendData;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UsbDevicesUtil.DEVICE_CONNECTION_SUCCESS:
				tv_message.setText("�豸���ӳɹ�\n");
				break;
			case UsbDevicesUtil.DEVICE_CONNECTION_FAIL:
				tv_message.append("�豸����ʧ��\n");
				break;
			case UsbDevicesUtil.ACTION_USB_DEVICE_ATTACHED:
				tv_message.append("�豸�Ѳ���\n");
				break;
			case UsbDevicesUtil.ACTION_USB_DEVICE_DETACHED:
				tv_message.append("�豸���Ƴ�\n");
				break;
			
			case UsbDevicesUtil.WRITEFILE:
				tv_message.append("���ݱ�����...\n");
				break;
				
			case 10086:
				tv_message.append("����ת���ɹ�!\n");
			default:
				break;
//				tv_message.append(usedTime + " ");
			}
		}

	};
	private Button btn_sendMusicData;
	private AlertDialog.Builder builder;
	ListView listView;
	
	Button btn_xiaoguo1;
	Button btn_xiaoguo2;
	Button btn_xiaoguo3;
	Button btn_xiaoguo4;
	Button btn_xiaoguo5;
	Button btn_xiaoguo6;
	
	SeekBar sbar_effectStrength;
	SeekBar sbar_microphoneVolume;
	SeekBar sbar_headsetVolume;
	
	Button btn_shanbiSwitch;
	Button btn_recStart;
	Button btn_playMusic;
	
	public boolean isRecoder = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv_message = (TextView) findViewById(R.id.tv_message);
		
		btn_xiaoguo1 = (Button) findViewById(R.id.btn_xiaoguo1);
		btn_xiaoguo2 = (Button) findViewById(R.id.btn_xiaoguo2);
		btn_xiaoguo3 = (Button) findViewById(R.id.btn_xiaoguo3);
		btn_xiaoguo4 = (Button) findViewById(R.id.btn_xiaoguo4);
		btn_xiaoguo5 = (Button) findViewById(R.id.btn_xiaoguo5);
		btn_xiaoguo6 = (Button) findViewById(R.id.btn_xiaoguo6);
		btn_recStart = (Button) findViewById(R.id.btn_recStart);
		btn_shanbiSwitch = (Button) findViewById(R.id.btn_shanbiSwitch);
		btn_playMusic = (Button) findViewById(R.id.btn_playMusic);
		
		sbar_effectStrength = (SeekBar) findViewById(R.id.sbar_effectStrength);
		sbar_effectStrength.setEnabled(false);
		sbar_microphoneVolume = (SeekBar) findViewById(R.id.sbar_microphoneVolume);
		sbar_microphoneVolume.setEnabled(false);
		sbar_headsetVolume = (SeekBar) findViewById(R.id.sbar_headsetVolume);
		
		usbDevicesUtil = new UsbDevicesUtil(this, handler);
		
		srollview = (ScrollView) findViewById(R.id.srollview);


		connectionDevice();
	}


	static Timer timer;

	/**
	 * ��ʼ¼��
	 * @param view
	 */
	public void recStart(View view) {
		if (!usbDevicesUtil.isConnection) {
			tv_message.setText("�豸δ��������\n");
			return;
		}
		if (!isRecoder) {
			tv_message.append("��ʼ¼��\n");
			((Button)view).setText("ֹͣ¼��");
//			btn_recStart.setText("ֹͣ¼��");
			

//			timer = new Timer(true);
//			timer.schedule(new TimerTask() {
//				@Override
//				public void run() {
					
			usbDevicesUtil.receiveMusicDataByBulk();
			
//				}
//			}, 0, 50);
			
			dataUtil = new DataUtil(MainActivity.this, handler);

			new Thread(new Runnable() {
				
				@Override
				public void run() {
					while(usbDevicesUtil.isRecoder){
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//						List<byte[]> list=usbDevicesUtil.getRecoderData();
//						if(list!=null){
							dataUtil.writeFile(usbDevicesUtil.getRecoderData());
//						}
					}
					dataUtil.closeOutputStream();
					
				}
			}).start();
		} else {
			tv_message.append("¼����ֹͣ\n");
			
//			timer.cancel();
			
			usbDevicesUtil.closeThread();
			((Button)view).setText("��ʼ¼��");
			setHeader();
		}
		isRecoder=!isRecoder;

	}
 
	/*
	 * ת����wav�ļ�
	 */
	public void setHeader(){
		tv_message.append("wav�ļ�ת����...\n");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				if(dataUtil==null){
					dataUtil = new DataUtil(MainActivity.this, handler);
				}
				dataUtil.copyWaveFile();
				handler.sendEmptyMessage(10086);
			}
		}).start();
	}

	/*
	 * ����ģʽ
	 */
	public void playMode(View view){
		btn_xiaoguo1.setEnabled(false);
		btn_xiaoguo2.setEnabled(false);
		btn_xiaoguo3.setEnabled(false);
		btn_xiaoguo4.setEnabled(false);
		btn_xiaoguo5.setEnabled(false);
		btn_xiaoguo6.setEnabled(false);
		
		sbar_effectStrength.setEnabled(false);
		sbar_microphoneVolume.setEnabled(false);
		
		btn_shanbiSwitch.setEnabled(false);
	}

	/*
	 * ¼��ģʽ
	 */
	public void recMode(View view){
		btn_xiaoguo1.setEnabled(true);
		btn_xiaoguo2.setEnabled(true);
		btn_xiaoguo3.setEnabled(true);
		btn_xiaoguo4.setEnabled(true);
		btn_xiaoguo5.setEnabled(true);
		btn_xiaoguo6.setEnabled(true);
		
		sbar_effectStrength.setEnabled(true);
		sbar_microphoneVolume.setEnabled(true);
		
		btn_shanbiSwitch.setEnabled(true);
	}
	
	public void getDeviceInfo(View view) {
		 usbDevicesUtil.showDeviceInfoDialog(usbDevicesUtil.getDeviceInfo());
	}

	public void connectionDevice(View view) {
		tv_message.setText(usbDevicesUtil.getUsbDevicePermission() + "\n");
	}

	public void connectionDevice() {
		tv_message.setText(usbDevicesUtil.getUsbDevicePermission() + "\n");
	}

	public void clear(View view) {
		tv_message.setText("");
	}

	public void stop(View view) {
		usbDevicesUtil.closeThread();
	}

	public void chooice(View view) {
		if (dialog != null) {
			dialog.show();
		}

	}

	@Override
	protected void onDestroy() {
		isReceiver = false;
		usbDevicesUtil.closeThread();
		usbDevicesUtil.unregisterReceiver();
		super.onDestroy();
	}

}
