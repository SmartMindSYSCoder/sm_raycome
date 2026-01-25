package com.sm.sm_raycome.iknetbluetoothlibrary;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;



import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.sm.sm_raycome.R;
import com.sm.sm_raycome.iknetbluetoothlibrary.util.FrameUtil;
import com.sm.sm_raycome.iknetbluetoothlibrary.util.PermissionUtil;

/**
 * Bluetooth Manager
 * @author 1knet.com
 *
 */
public class BluetoothManager extends ContextWrapper{
	private static final String TAG = "BluetoothManager";
	
	private static BluetoothManager instance;
	private boolean isRegistBroadcast = false;

	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	private static Context mContext;
	private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
	
	private MyReceiver myReceiver;
	private OnBTMeasureListener mOnBTMeasureListener;
	
	private int onDiscoveryFinishedCount = 0;
	
	public static final int MIN_POWER = 3600;

	private static MeasureState measureState = MeasureState.DEFAULT; // Measurement State

	public enum MeasureState {
		DEFAULT, OPENING, OPENED, RESEARCHING, CONNECTING, CONNECTED, MEASURING
	}

	public static BluetoothManager getInstance(Context context) {
		if (instance == null) {
			instance = new BluetoothManager(context);
		}else if(mContext != null && !mContext.toString().equals(context.toString())){
			instance = new BluetoothManager(context);
		}
		return instance;
	}
	
	private BluetoothManager(Context context){
		super(context);
		mContext = context;
	}

	public void updateContext(Context context) {
		mContext = context;
		attachBaseContext(context);
	}
	
	/**
	 * Initialize SDK
	 */
	public void initSDK(){
		initReceiver();
	}
	
	/**
	 * Start Bluetooth Affair
	 */
	public void startBTAffair(OnBTMeasureListener onBTMeasureListener){
	initReceiver();
	if(!_bluetooth.isEnabled()){
	Log.e(TAG, "Bluetooth not enabled");
	return;
	}
	Log.v(TAG, "Start Bluetooth Affair");
	mOnBTMeasureListener = onBTMeasureListener;
	
	if(!TextUtils.isEmpty(BluetoothService.ConnectedBTAddress)){
	mOnBTMeasureListener.onConnected(true, _bluetooth.getRemoteDevice(BluetoothService.ConnectedBTAddress));
	startMeasure();
	} else {
	// Do not attempt to set discoverable mode; it requires BLUETOOTH_PRIVILEGED.
	searchBluetooth();
	}
	
	}
	
	/**
	 * Stop Bluetooth Affair and disconnect
	 */
	public void stopBTAffair() {
		if (isRegistBroadcast) {
			mContext.unregisterReceiver(myReceiver);
			isRegistBroadcast = false;
		}
		if(_bluetooth.isDiscovering()){
			_bluetooth.cancelDiscovery();
		}
		stopMeasure();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				sendBroadcast(new Intent(BluetoothService.ACTION_BT_DISCONNECT_TO));
			}
		}, 800);
	}
	
	/**
	 * Call setScanMode via reflection to set Bluetooth visibility
	 */
	private void setBtDiscoverable(){
		try {
			Class<?> ba = _bluetooth.getClass();
			Method m = ba.getMethod("setScanMode", new Class<?>[]{int.class});
			boolean b = (Boolean) m.invoke(_bluetooth, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
			Log.v(TAG, "setBtDiscoverable-Set Bluetooth Visibility: " + b);
		} catch (Exception e) {
			e.printStackTrace();
			Log.v(TAG, "setBtDiscoverable-Set Bluetooth Visibility Error");
		}
	}

	/**
	 * Start Measurement
	 */
	public boolean startMeasure(){
		if(TextUtils.isEmpty(BluetoothService.ConnectedBTAddress)){
//			Toast.makeText(mContext, "Bluetooth device not connected!", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		// Query power, if > 3600 start measurement. Send start command after device returns power.
		sendData(RaycomeCommands.CMD_QUERY_POWER);
		Log.v(TAG, "Send query power command: " + RaycomeCommands.CMD_QUERY_POWER);
		
		return true;
	}
	
	/**
	 * Stop Measurement
	 */
	public void stopMeasure(){
		if(TextUtils.isEmpty(BluetoothService.ConnectedBTAddress)){
			return;
		}
		sendData(RaycomeCommands.CMD_STOP_MEASURE);
		Log.v(TAG, "Send stop measure command: " + RaycomeCommands.CMD_STOP_MEASURE);
	}
	
	/**
	 * Is Bluetooth device connected status
	 * @return
	 */
	public boolean isConnectBT(){
		if(TextUtils.isEmpty(BluetoothService.ConnectedBTAddress)){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * Close phone Bluetooth and unregister receiver
	 */
	public void closeBT(){
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (isRegistBroadcast) {
					mContext.unregisterReceiver(myReceiver);
					isRegistBroadcast = false;
				}
				// disable() is deprecated and should not be used by apps.
				// We just unregister receivers and clear state.
			}
		}, 100);
	}
	
	/**
	 * Send data to Bluetooth device, e.g. "cc80020301030003"
	 * @param dataStr
	 */
	private void sendData(String dataStr) {
		Log.i(TAG, "sendData: " + dataStr); // Debug log
		byte[] data = hex2byte(dataStr.getBytes());
		Intent intent = new Intent(BluetoothService.ACTION_BLUETOOTH_DATA_WRITE);
		intent.putExtra(BluetoothService.ACTION_BLUETOOTH_DATA_EXTRA_BYTEARRAY, data);
		sendBroadcast(intent);
	}
	
	private void initReceiver() {
		if (!isRegistBroadcast) {
			myReceiver = new MyReceiver();
			IntentFilter filter = new IntentFilter(BluetoothService.ACTION_BLUETOOTH_CONNECT);
			filter.addAction(BluetoothService.ACTION_BLUETOOTH_CONNECT2);
			filter.addAction(BluetoothService.ACTION_BLUETOOTH_DATA_READ);
			filter.addAction(BluetoothService.ACTION_BLUETOOTH_RUNNING);
			filter.addAction(BluetoothService.ACTION_BLUETOOTH_POWER);
			filter.addAction(BluetoothService.ACTION_ERROR_MEASURE);
			filter.addAction(BluetoothDevice.ACTION_FOUND);
			filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
			filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
			mContext.registerReceiver(myReceiver, filter);
			isRegistBroadcast = true;
			Log.i(TAG, "Receiver registered successfully");
		}
	}

	// Listen to Bluetooth connection state
	private class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i(TAG, "onReceive: " + action); // Debug log
			if (BluetoothService.ACTION_BLUETOOTH_CONNECT.equals(action)) {
				// Is first handshake successful
				boolean connect = intent.getBooleanExtra(BluetoothService.ACTION_BLUETOOTH_CONNECT_EXTRA_BOOLEAN,
						false);

				if (connect) {
					if(BluetoothService.connectedDeviceType == BluetoothService.DeviceType.TYPE_88A){
						sendData(RaycomeCommands.CMD_HANDSHAKE_2);// cccc020301010001 connect Bluetooth, execute second handshake
						Log.v(TAG, "Send connect BP monitor command: " + RaycomeCommands.CMD_HANDSHAKE_2);
					}else if(BluetoothService.connectedDeviceType == BluetoothService.DeviceType.TYPE_9000){
						// Type 9000 device does not need second handshake
						if (deviceList.size() > 0) {
							mOnBTMeasureListener.onConnected(connect, deviceList.get(0));
						}
					}
				} else {
					if (deviceList.size() > 0) {
						mOnBTMeasureListener.onConnected(connect, deviceList.get(0));
					}
				}
				
			} else if(BluetoothService.ACTION_BLUETOOTH_CONNECT2.equals(action)){
				// Is second handshake successful
				boolean connect = intent.getBooleanExtra(BluetoothService.ACTION_BLUETOOTH_CONNECT_EXTRA_BOOLEAN,false);
				mOnBTMeasureListener.onConnected(connect, deviceList.get(0));
				if(connect){
					sendData(RaycomeCommands.CMD_QUERY_POWER);
					Log.v(TAG, "Send query power command: " + RaycomeCommands.CMD_QUERY_POWER);
				}
			} else if (BluetoothService.ACTION_ERROR_MEASURE.equals(action)) {
				// Measurement failed
				Log.v(TAG, "Measurement failed");
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						sendData(RaycomeCommands.CMD_POWER_OFF);
						Log.v(TAG, "Send power off command: " + RaycomeCommands.CMD_POWER_OFF);
						mOnBTMeasureListener.onMeasureError();
					}
				}, 8000);
			} else if (BluetoothService.ACTION_BLUETOOTH_POWER.equals(action)) {
				// Set device power change
				String power = intent.getStringExtra("power");
				mOnBTMeasureListener.onPower(power);
				if(Integer.parseInt(power) > MIN_POWER){
					sendData(RaycomeCommands.CMD_START_MEASURE);
					Log.v(TAG, "Send start measure command: " + RaycomeCommands.CMD_START_MEASURE);
					setMeasureState(MeasureState.MEASURING);
				}
			} else if (BluetoothService.ACTION_BLUETOOTH_RUNNING.equals(action)) {
				// Measuring, data is changing, update display constantly
				String running = intent.getStringExtra("running");
				mOnBTMeasureListener.onRunning(running);
			} else if (BluetoothService.ACTION_BLUETOOTH_DATA_READ.equals(action)) {
				// Measurement finished, show result
				postDataReadAction(context, intent);
			}else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String str = device.getName() + "-" + device.getAddress();
				Log.i(TAG, "Found device: " + str); // Changed to Log.i for visibility
				
				if (check(device.getAddress())) {
					// User requested filter: must start with "BP"
					String name = device.getName();
					if (name != null && name.startsWith("BP")) {
						Log.i(TAG, "Device matched filter. Connecting immediately: " + name);
						deviceList.add(device);
						connectToBT(device.getAddress());
						if (_bluetooth.isDiscovering()) {
							_bluetooth.cancelDiscovery();
						}
					}
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//				startBtService();
				++onDiscoveryFinishedCount;
				if(onDiscoveryFinishedCount == 1){
					Log.v(TAG, "Search finished - Device count: " + deviceList.size());
					mOnBTMeasureListener.onFoundFinish(deviceList);
					// Logic moved to ACTION_FOUND for immediate connection
					// if(deviceList.size() > 0){
					// 	connectToBT(deviceList.get(0).getAddress());
					// }
				}
			} else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
				// Disconnected
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if(device != null){
					mOnBTMeasureListener.onDisconnected(device);
				}

				deviceList.clear();
			} else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				switch (state) {
				case BluetoothAdapter.STATE_OFF:
					setMeasureState(MeasureState.DEFAULT);
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
					break;
				case BluetoothAdapter.STATE_ON:
					setMeasureState(MeasureState.OPENED);
					break;
				case BluetoothAdapter.STATE_TURNING_ON:
					setMeasureState(MeasureState.OPENING);
					break;
				}
			} else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
				setMeasureState(MeasureState.RESEARCHING);
			} else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
				switch (state) {
				case BluetoothAdapter.STATE_CONNECTING:
					setMeasureState(MeasureState.CONNECTING);
					break;
				case BluetoothAdapter.STATE_CONNECTED:
					// Verify after connection successful before starting measure
					setMeasureState(MeasureState.CONNECTED);
					break;
				default:
					break;
				}
			}
		}

	}
	
	/** Connect Bluetooth Device */
	public void connectToBT(String addr){
		Log.v(TAG, "Start connecting Bluetooth: " + addr);
		if(FrameUtil.isServiceRunning("com.iknet.iknetbluetoothlibrary.BluetoothService", mContext)){
			Intent intent = new Intent(BluetoothService.ACTION_BT_CONNECT_TO);
			intent.putExtra("addr", addr);
			mContext.sendBroadcast(intent);
		}else{
			// Start Service, connect Bluetooth device
			Intent intent2 = new Intent(mContext.getApplicationContext(),BluetoothService.class);
			intent2.putExtra("PREFS_BLUETOOTH_PRE_ADDR_STRING", addr);
			mContext.startService(intent2);
		}
		
	}
	
	private void postDataReadAction(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		MeasurementResult result = (MeasurementResult) bundle.getSerializable("result");
		sendData(RaycomeCommands.CMD_STOP_MEASURE);// Send stop command after measurement
		Log.v(TAG, "Send stop measure command: " + RaycomeCommands.CMD_STOP_MEASURE);
		mOnBTMeasureListener.onMeasureResult(result);

        // Auto-Power Off / Disconnect after success (User requirement)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sendData(RaycomeCommands.CMD_POWER_OFF);
                Log.v(TAG, "Send power off command (Auto after success): " + RaycomeCommands.CMD_POWER_OFF);
                // Listeners should handle the resulting ACTION_ACL_DISCONNECTED from the OS
            }
        }, 5000);
	}
	
	/**
	 * Search Bluetooth Device
	 */
	public void searchBluetooth() {
		if(Build.VERSION.SDK_INT >= 23){
			requestLocationPerm();
		}else{
			doDiscovery();
		}
	}
	
	public static final int REQUEST_FINE_LOCATION = PermissionUtil.REQUEST_FINE_LOCATION;
	/** API 23+ requires location permission for Bluetooth scan */
	private void requestLocationPerm() {
		if (!PermissionUtil.checkLocationPermission(this)) {
			PermissionUtil.requestLocationPerm(mContext);
			
		} else {
			doDiscovery();
		}
	}
	
	/**
	 * Search Device
	 */
	private void doDiscovery() {
		onDiscoveryFinishedCount = 0;
		deviceList.clear();
		if(!_bluetooth.isEnabled()){
			return;
		}
		if (_bluetooth.isDiscovering()) {
			_bluetooth.cancelDiscovery();
		}
		_bluetooth.startDiscovery();
		Log.v(TAG, "Start Search");
	}
	
	/** 扫描le蓝牙设备 */
	/*@SuppressLint("NewApi")
	private void scanLeDevice(boolean enable){
		if(enable){
			_bluetooth.startLeScan(leScanCallback);
		}else{
			_bluetooth.stopLeScan(leScanCallback);
		}
	}
	
	@SuppressLint("NewApi")
	private LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
		
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			String str = device.getName() + "\n" + device.getAddress();
			Log.v(TAG, "搜索到的ble设备：" + str);
			String arg = str.substring(0, 3);
			if (check(device.getAddress())) {
				if (arg.equals("RBP")) {
					deviceList.add(device);
					_bluetooth.stopLeScan(leScanCallback);
					startBtService();
				}
			}
		}
	};*/
	
	// Check if address exists in the list to avoid duplicate addition
	private boolean check(String address) {
		int count = deviceList.size();
		for (int i = 0; i < count; i++) {
			if (deviceList.get(i).getAddress().equals(address))
				return false;
		}
		return true;
	}
	
	/**
	 * Hex string to byte array
	 * 
	 * @param b
	 * @return
	 */
	private static byte[] hex2byte(byte[] b) {
		if ((b.length % 2) != 0) {
			System.out.println("ERROR: Conversion failed  le= " + b.length + " b:" + b.toString());
			return null;
		}
		byte[] b2 = new byte[b.length / 2];
		for (int n = 0; n < b.length; n += 2) {
			// if(n+2<=b.length){
			String item = new String(b, n, 2);
			// Two chars per byte, convert hex string back to byte
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		b = null;
		return b2;
	}

	public static MeasureState getMeasureState() {
		return measureState;
	}

	/**
	 * Mark measurement process state
	 * 
	 * @param measureState
	 */
	public static void setMeasureState(MeasureState measureState) {
		BluetoothManager.measureState = measureState;
	}

	public interface OnBTMeasureListener {
		/**
		 * Search finished, if deviceList.size() is 0, no device found
		 * @param deviceList
		 */
		void onFoundFinish(List<BluetoothDevice> deviceList);
		/**
		 * Is connected successfully
		 * @param isConnected
		 */
		void onConnected(boolean isConnected, BluetoothDevice device);
		void onPower(String power);
		void onRunning(String running);
		void onMeasureError();
		void onMeasureResult(MeasurementResult result);
		void onDisconnected(BluetoothDevice device);
	}
	
}
