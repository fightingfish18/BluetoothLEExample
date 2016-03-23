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
import android.view.View;
import android.widget.Button;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends Activity {
    private BluetoothDevice prymeButton;
    private BluetoothGatt mGatt;
    private BluetoothAdapter mBluetoothAdapter;
    private static final UUID serviceId = Constants.serviceId;  // i.e. 00000000-0000-1000-8000-00805B9B34CB
    private static final UUID descriptorId = Constants.descriptorId;
    private static final int characteristicProperty = BluetoothGattCharacteristic.PROPERTY_NOTIFY;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Search for paired bluetooth devices
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            if (device.getName().equals("PTT-Z")) {
                prymeButton = device;
                break;
            }
        }

        // Start BLE connection
        final Button connectButton = (Button) findViewById(R.id.button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                prymeButton.connectGatt(MainActivity.this, true, new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                        int newState) {
                        super.onConnectionStateChange(gatt, status, newState);
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            // Device is connected
                            Log.i("Bluetooth Device:", "connected");
                            connectButton.setText("Connected!");

                            mGatt = gatt;
                            mGatt.discoverServices(); // look for services
                        }
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        super.onServicesDiscovered(gatt, status);
                        try {

                            // looking for specific service for device based on UUID
                            BluetoothGattService buttonService = mGatt.getService(serviceId);

                            // Get all characteristics
                            List<BluetoothGattCharacteristic> serviceCharacteristics = buttonService.getCharacteristics();

                            for (BluetoothGattCharacteristic characteristic : serviceCharacteristics) {
                                // scan for desired characteristic
                                if (characteristic.getProperties() == characteristicProperty) {
                                    // Listener when characteristic changes
                                    mGatt.setCharacteristicNotification(characteristic, true);

                                    // Tell the device to notify us
                                    BluetoothGattDescriptor buttonDescriptor = characteristic
                                            .getDescriptor(descriptorId);
                                    buttonDescriptor.setValue(
                                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
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
                    public void onCharacteristicChanged(BluetoothGatt gatt,
                                                        BluetoothGattCharacteristic characteristic) {
                        super.onCharacteristicChanged(gatt, characteristic);

                        // We detected a change!
                        Log.i("Value", Arrays.toString(characteristic.getValue()));

                        showOnPressColor(characteristic.getValue());
                    }
                });
            }
        });
    }

    private void showOnPressColor(final byte[] value) {

        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                View vPressStatus = findViewById(R.id.vPressStatus);
                vPressStatus.setVisibility(View.VISIBLE);

                if (value[0] == 1) {
                    vPressStatus.setBackgroundColor(getResources().getColor(R.color.red));
                } else {
                    vPressStatus.setBackgroundColor(getResources().getColor(R.color.blue));
                }
            }
        });


    }
}
