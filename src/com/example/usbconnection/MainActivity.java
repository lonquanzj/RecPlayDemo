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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.usbconnection.util.DataUtil;
import com.example.usbconnection.util.UsbDevicesUtil;
import com.example.usbconnection.util.StaticFinal;

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
			case StaticFinal.DEVICE_CONNECTION_SUCCESS:
				tv_message.setText("�豸���ӳɹ�\n");
				break;
			case StaticFinal.DEVICE_CONNECTION_FAIL:
				tv_message.append("�豸����ʧ��\n");
				break;
			case StaticFinal.ACTION_USB_DEVICE_ATTACHED:
				tv_message.append("�豸�Ѳ���\n");
				break;
			case StaticFinal.ACTION_USB_DEVICE_DETACHED:
				tv_message.append("�豸���Ƴ�\n");
				break;
			
			case StaticFinal.WRITEFILE:
				tv_message.append("���ݱ�����...\n");
				break;
				
			case StaticFinal.SENDDATA_CONTROL_SUCCESS:
				tv_message.append("���ƴ���ɹ�\n");
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
	
	RadioButton rb_xiaoguo1;
	RadioButton rb_xiaoguo2;
	RadioButton rb_xiaoguo3;
	RadioButton rb_xiaoguo4;
	RadioButton rb_xiaoguo5;
	RadioButton rb_xiaoguo6;
	
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
		
		usbDevicesUtil = new UsbDevicesUtil(this, handler);
		
		tv_message = (TextView) findViewById(R.id.tv_message);
		
		btn_recStart = (Button) findViewById(R.id.btn_recStart);
		btn_shanbiSwitch = (Button) findViewById(R.id.btn_shanbiSwitch);
		btn_playMusic = (Button) findViewById(R.id.btn_playMusic);
		
		initRadioButton();
		initSeekBar();
		
		srollview = (ScrollView) findViewById(R.id.srollview);

		connectionDevice();
	}
	
	public void initRadioButton(){
		rb_xiaoguo1 = (RadioButton) findViewById(R.id.rb_xiaoguo1);
		rb_xiaoguo2 = (RadioButton) findViewById(R.id.rb_xiaoguo2);
		rb_xiaoguo3 = (RadioButton) findViewById(R.id.rb_xiaoguo3);
		rb_xiaoguo4 = (RadioButton) findViewById(R.id.rb_xiaoguo4);
		rb_xiaoguo5 = (RadioButton) findViewById(R.id.rb_xiaoguo5);
		rb_xiaoguo6 = (RadioButton) findViewById(R.id.rb_xiaoguo6);
		
		final RadioGroup[] radioGroup = new RadioGroup[2];
		radioGroup[0] = (RadioGroup) findViewById(R.id.rg_first);
		radioGroup[1] = (RadioGroup)/* lessonSelectView.*/findViewById(R.id.rg_second);
		
		for (int i = 0; i < 2; i++) {
			radioGroup[i].setOnCheckedChangeListener(new OnCheckedChangeListener() {
						public void onCheckedChanged(RadioGroup group,
								int checkedId) {
							// �ж��Ƿ����ְ�ť��ѡ��(checked)
							if (gainedSelectedValue(group, checkedId)) {
								group.requestFocus();
								gainedSelectedValue(group, checkedId);
							}
						}
					});

			radioGroup[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
						public void onFocusChange(View v, boolean hasFocus) {
							if (hasFocus) {
								RadioGroup group = (RadioGroup) v;
								for (int j = 0; j < 3; j++) {
									if (!radioGroup[j].equals(group)) {
										if (radioGroup[j].getCheckedRadioButtonId() != -1) {
											radioGroup[j].clearCheck();
										}
									}
								}
							}
						}
					});
		}
	}
	
	/**
	 * �õ�RadioGroup��ѡ���ֵ(��ѡ���ĸ�RadioButton) �ж�RadioGroup���Ƿ���RadioButton��checked
	 * 
	 * @param radioGroup
	 * @param checkedId
	 */
	private boolean gainedSelectedValue(RadioGroup radioGroup, int checkedId) {
		for (int i = 0; i < radioGroup.getChildCount(); i++) {
			RadioButton btn = (RadioButton) radioGroup.getChildAt(i);
			if (btn.getId() == checkedId) {
				String selectedValue = (String) btn.getText();
				return btn.isChecked();
			}
		}
		return false;
	}

	public void initSeekBar(){
		/*
		 * Ч��ǿ�ȵ���
		 */
		sbar_effectStrength = (SeekBar) findViewById(R.id.sbar_effectStrength);
		sbar_effectStrength.setEnabled(false);
		sbar_effectStrength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO �Զ����ɵķ������
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO �Զ����ɵķ������
				
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO �Զ����ɵķ������
				usbDevicesUtil.sendCtrlPack(StaticFinal.EFFECT_STRENGTH, (byte) arg1);
			}
		});
		
		/*
		 * ��Ͳ��������
		 */
		sbar_microphoneVolume = (SeekBar) findViewById(R.id.sbar_microphoneVolume);
		sbar_microphoneVolume.setEnabled(false);
		sbar_microphoneVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
		
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO �Զ����ɵķ������
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO �Զ����ɵķ������
				
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO �Զ����ɵķ������
				usbDevicesUtil.sendCtrlPack(StaticFinal.MIC_VOLUME, (byte)arg1);
			}
		});
		
		sbar_headsetVolume = (SeekBar) findViewById(R.id.sbar_headsetVolume);
		sbar_headsetVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
		
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO �Զ����ɵķ������
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO �Զ����ɵķ������
				
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO �Զ����ɵķ������
				usbDevicesUtil.sendCtrlPack(StaticFinal.HEADSET_VOLUME, (byte)arg1);
			}
		});
	}

//	static Timer timer;
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
		rb_xiaoguo1.setEnabled(false);
		rb_xiaoguo2.setEnabled(false);
		rb_xiaoguo3.setEnabled(false);
		rb_xiaoguo4.setEnabled(false);
		rb_xiaoguo5.setEnabled(false);
		rb_xiaoguo6.setEnabled(false);
		
		sbar_effectStrength.setEnabled(false);
		sbar_microphoneVolume.setEnabled(false);
		
		btn_shanbiSwitch.setEnabled(false);
	}

	/*
	 * ¼��ģʽ
	 */
	public void recMode(View view){
		rb_xiaoguo1.setEnabled(true);
		rb_xiaoguo2.setEnabled(true);
		rb_xiaoguo3.setEnabled(true);
		rb_xiaoguo4.setEnabled(true);
		rb_xiaoguo5.setEnabled(true);
		rb_xiaoguo6.setEnabled(true);
		
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
