package com.example.usbconnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
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
	
	SeekBar sbar_effectStrength;
	SeekBar sbar_microphoneVolume;
	SeekBar sbar_headsetVolume;
	
	TextView tv_effectStrenth;
	TextView tv_headVol;
	TextView tv_micVol;
	
	Button btn_shanbiSwitch;
	Button btn_recStart;
	Button btn_playMusic;
	
	private GridView gv;
	
	public boolean isRecoder = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		usbDevicesUtil = new UsbDevicesUtil(this, handler);
		
		tv_message = (TextView) findViewById(R.id.tv_message);
		
		tv_effectStrenth = (TextView) findViewById(R.id.tv_effectStrength);
		tv_headVol = (TextView) findViewById(R.id.tv_headsetVolume);
		tv_micVol = (TextView) findViewById(R.id.tv_microphoneVolume);
		
		btn_recStart = (Button) findViewById(R.id.btn_recStart);
		btn_shanbiSwitch = (Button) findViewById(R.id.btn_shanbiSwitch);
		btn_playMusic = (Button) findViewById(R.id.btn_playMusic);
		
		initGridView();
		initSeekBar();
		
		srollview = (ScrollView) findViewById(R.id.srollview);

		connectionDevice();
	}
	
	public void initGridView(){

		
		// ׼��Ҫ��ӵ�������Ŀ
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < 5; i++) {
			Map<String, Object> item = new HashMap<String, Object>();
			// item.put("imageItem", R.drawable.icon);//���ͼ����Դ��ID
			item.put("textItem", "Ч��" + (i + 1));// ��������ItemText
			items.add(item);
		}

		// ʵ����һ��������
		SimpleAdapter adapter = new SimpleAdapter(this, items,
				R.layout.grid_item, new String[] { "imageItem", "textItem" },
				new int[] { R.id.image_item, R.id.text_item });

		// ���GridViewʵ��
		gv = (GridView) findViewById(R.id.mygridview);
		// ΪGridView����������
		gv.setAdapter(adapter);

		gv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Toast.makeText(MainActivity.this, "��ѡ����" + (position + 1) + " ��", Toast.LENGTH_SHORT).show();
				usbDevicesUtil.sendCtrlPack(StaticFinal.EFFECT_TYPE, (byte)(position+1));
			}
		});
		
		gv.setSelector(new ColorDrawable(R.color.selectedBgColor));
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
/*		rb_xiaoguo1.setEnabled(false);
		rb_xiaoguo2.setEnabled(false);
		rb_xiaoguo3.setEnabled(false);
		rb_xiaoguo4.setEnabled(false);
		rb_xiaoguo5.setEnabled(false);
		rb_xiaoguo6.setEnabled(false);*/
		
		sbar_effectStrength.setEnabled(false);
		sbar_microphoneVolume.setEnabled(false);
		
		btn_shanbiSwitch.setEnabled(false);
	}

	/*
	 * ¼��ģʽ
	 */
	public void recMode(View view){
		/*rb_xiaoguo1.setEnabled(true);
		rb_xiaoguo2.setEnabled(true);
		rb_xiaoguo3.setEnabled(true);
		rb_xiaoguo4.setEnabled(true);
		rb_xiaoguo5.setEnabled(true);
		rb_xiaoguo6.setEnabled(true);*/
		
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
