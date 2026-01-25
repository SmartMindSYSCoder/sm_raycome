package com.sm.sm_raycome.iknetbluetoothlibrary;

import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.sm.sm_raycome.iknetbluetoothlibrary.BluetoothStateMachineGatt.ResolveResultCallback;
import com.sm.sm_raycome.iknetbluetoothlibrary.model.ResultFromTurg;
import com.sm.sm_raycome.iknetbluetoothlibrary.util.FrameUtil;


@SuppressLint("NewApi")
public class BluetoothGATTConnModel {
	private static final String TAG = "BluetoothGATTConnModel";
	
	private UUID uuidService, uuidCharacteristicRead, uuidCharacteristicWrite;
	private static boolean isConnected = false;	//是否已连接
	private Handler mHandler;
	private Context mContext;
	private BluetoothStateMachineGatt btStateMachineGatt;
	
	private BluetoothGatt bluetoothGatt;
	private BluetoothDevice mRemoteDevice;
	private BluetoothGattCharacteristic characteristicRead;
	private BluetoothGattCharacteristic characteristicWrite;

	public BluetoothGATTConnModel(Context context, Handler handler) {
//		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mHandler = handler;
		mContext = context;
		
		if(btStateMachineGatt == null){
			btStateMachineGatt = new BluetoothStateMachineGatt(this, new ResolveResultCallback() {
				@Override
				public void onResolveResult(ResultFromTurg result) {
					mHandler.obtainMessage(BluetoothService.MESSAGE_READ,
							result.getFlag(), result.getSflag(), result.getDataBuff()).sendToTarget();
				}
			});
			
		}
		
	}

	public void connectTo(final BluetoothDevice remoteDevice) {
		if(remoteDevice == null) return;
		//延时连接，防止蓝牙服务还未绑定起来，就连接，导致异常
//		new Handler().postDelayed(new Runnable() {
//			
//			@Override
//			public void run() {
				if(mRemoteDevice != null && mRemoteDevice.equals(remoteDevice) && BluetoothService.ConnectedBTAddress != null){
					//已连接该设备，直接测量
					mHandler.obtainMessage(BluetoothService.MESSAGE_CONNECTED, -1, -1,
							mRemoteDevice.getName()).sendToTarget();
				}else{
					bluetoothGatt = remoteDevice.connectGatt(mContext, true, bluetoothGattCallback);
					mRemoteDevice = remoteDevice;
				}
//			}
//		}, 500);
		
	}
	
	public void disconnect(){
		bluetoothGatt.disconnect();
	}
	
	private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			String name = gatt.getDevice().getName();
			if(newState == BluetoothProfile.STATE_CONNECTED){
//				Log.v(TAG, "成功连接该蓝牙设备：" + name);
				Log.v(TAG, "启动发现服务:" + bluetoothGatt.discoverServices());
				
			}else if(newState == BluetoothProfile.STATE_DISCONNECTED){
				Log.v(TAG, "断开连接：" + name);
				isConnected = false;
			}
			if(status == BluetoothGatt.GATT_SUCCESS){
				Log.v(TAG, "onConnectionStateChange:启动服务成功");
			}
		};
		
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if(status == BluetoothGatt.GATT_SUCCESS){
				Log.v(TAG, "onServicesDiscovered:启动服务成功");
				isConnected = true;
				btStateMachineGatt.start();
				//找到可用的service和characteristic
				exit:
				for(BluetoothGattService s : gatt.getServices()){
					int charaSize = s.getCharacteristics().size();
					if(charaSize < 2) continue;
					characteristicRead = null;
					characteristicWrite = null;
					for(int i=0; i<charaSize; i++){
						BluetoothGattCharacteristic c = s.getCharacteristics().get(i);
						if(c.getDescriptors() != null && c.getDescriptors().size() != 0){
							if(characteristicWrite == null && c.getProperties() == BluetoothGattCharacteristic.PROPERTY_WRITE){
								characteristicWrite = c;
							}else if(characteristicRead == null && c.getProperties() == BluetoothGattCharacteristic.PROPERTY_NOTIFY){
								characteristicRead = c;
							}
						}
						if(characteristicRead != null && characteristicWrite != null) break exit;
					}
				}
				if(characteristicRead != null && characteristicWrite != null){
					gatt.setCharacteristicNotification(characteristicRead, true);
					mHandler.obtainMessage(BluetoothService.MESSAGE_CONNECTED, -1, -1,
							mRemoteDevice.getName()).sendToTarget();
				}else{
					Log.v(TAG, "未找到可用的characteristic");
					disconnect();
				}
				
			}else if(status == BluetoothGatt.GATT_FAILURE){
				Log.v(TAG, "启动服务失败");
				isConnected = false;
			}
		};
		
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			byte[] buff = characteristic.getValue();
			Log.v(TAG, "onCharacteristicChanged:" + FrameUtil.byte2hex(buff) + ",字节个数：" + buff.length);
			btStateMachineGatt.addData(buff);
		};
		
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if(status == BluetoothGatt.GATT_SUCCESS){
				Log.v(TAG, "写入成功：" + FrameUtil.byte2hex(characteristic.getValue()));
			}else if(status == BluetoothGatt.GATT_FAILURE){
				Log.v(TAG, "写入失败：" + FrameUtil.byte2hex(characteristic.getValue()));
			}
		};
		
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			
		};
		
	};
	
	public void writeCharacteristic(byte[] data){
		if(characteristicWrite == null || data == null) return;
		Log.v(TAG, "写入数据：" + FrameUtil.byte2hex(data));
		
		characteristicWrite.setValue(data);
//		characteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
		boolean w = bluetoothGatt.writeCharacteristic(characteristicWrite);
		Log.v(TAG, "写入操作：" + w);
		if(!w){
			disconnect();
		}
		
	}
	
	public boolean isConnected(){
		return isConnected;
	}
	
	public interface MyBluetoothGattCallback{
		void onConnectionStateChange(int status, int newState);
		void onServicesDiscovered(int status);
	}
	
}
