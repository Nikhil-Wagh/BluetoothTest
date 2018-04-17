package com.example.nikhil.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Message;
import android.support.design.internal.SnackbarContentLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private int REQUEST_ENABLE_BT = 32;
    private String TAG = "APP";
    private java.util.UUID APP_UUID = java.util.UUID.fromString("BLUETOOTH_APP");

    private EditText speedEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        speedEditText = findViewById(R.id.speedEditText);
        settingUP();
    }

    private void settingUP() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null){
            Snackbar snackbar = Snackbar.make(findViewById(R.id.myRelativeLayout),
                    R.string.bluetooth_not_supported,
                    Snackbar.LENGTH_INDEFINITE
            );
            snackbar.show();
        }

        if(!bluetoothAdapter.isEnabled()){
            Log.i(TAG, "Bluetooth disabled");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() <= 0){
            Snackbar.make(findViewById(R.id.myRelativeLayout),
                    R.string.no_paired_device,
                    Snackbar.LENGTH_LONG)
                    .show();
        }

        for(BluetoothDevice device: pairedDevices){
            String deviceName = device.getName();
            String deviceAddress = device.getAddress();
            Log.i(TAG, deviceName + " " + deviceAddress);
        }

    }


    private class ConnectThread extends Thread{
        private BluetoothSocket mSocket;
        private OutputStream outputStream;

        public ConnectThread(BluetoothDevice bluetoothDevice){
            BluetoothSocket temp = null;

            try {
                temp = bluetoothDevice.createRfcommSocketToServiceRecord(APP_UUID);
            }catch (IOException e){
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mSocket = temp;
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try{
                mSocket.connect();
            } catch (IOException e) {
//                e.printStackTrace();
                try{
                    mSocket.close();
                }catch (IOException closeException){
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
            sendData(mSocket);
        }

        private void sendData(BluetoothSocket mSocket){
            try {
                outputStream = mSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            int speed = Integer.parseInt(speedEditText.getText().toString().trim());

            try{
                byte b = (byte) (speed & 0xFF);
                outputStream.write(b);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }

    }
}
