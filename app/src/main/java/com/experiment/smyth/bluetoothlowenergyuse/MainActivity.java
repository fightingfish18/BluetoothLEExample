package com.experiment.smyth.bluetoothlowenergyuse;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends Activity {
    private BluetoothDevice prymeButton;
    private BluetoothGatt mGatt;
    private BluetoothAdapter mAdapter;
    private static final UUID serviceId = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private static final UUID descriptorId = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final int characteristicProperty = BluetoothGattCharacteristic.PROPERTY_NOTIFY;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button connectButton = (Button) findViewById(R.id.button);
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices = mAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            if (device.getName().equals("PTT-Z")) {
                prymeButton = device;
                break;
            }
        }
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prymeButton.connectGatt(MainActivity.this, true, new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        super.onConnectionStateChange(gatt, status, newState);
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            Log.i("Bluetooth Device:", "connected");
                            mGatt = gatt;
                            mGatt.discoverServices();
                        }
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        super.onServicesDiscovered(gatt, status);
                        try {
                            BluetoothGattService buttonService = mGatt.getService(serviceId);
                            List<BluetoothGattCharacteristic> serviceCharacteristics = buttonService.getCharacteristics();
                            for (BluetoothGattCharacteristic characteristic : serviceCharacteristics) {
                                if (characteristic.getProperties() == characteristicProperty) {
                                    mGatt.setCharacteristicNotification(characteristic, true);
                                    BluetoothGattDescriptor buttonDescriptor = characteristic.getDescriptor(descriptorId);
                                    buttonDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    mGatt.writeDescriptor(buttonDescriptor);
                                }
                            }
                        } catch (Exception e) {
                            Log.i("Bluetooth Device: ", "protocol failure");
                        }
                    }

                    @Override
                    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        super.onCharacteristicRead(gatt, characteristic, status);
                    }

                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                        super.onCharacteristicChanged(gatt, characteristic);
                        Log.i("Value", Arrays.toString(characteristic.getValue()));
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
