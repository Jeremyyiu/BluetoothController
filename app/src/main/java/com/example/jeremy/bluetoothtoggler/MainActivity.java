package com.example.jeremy.bluetoothtoggler;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.gc.materialdesign.views.Switch;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter btAdapter = null;
    BluetoothObserver bluetoothObserver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        Switch bluetoothSwitch = (Switch) findViewById(R.id.bluetoothSwitch);
        initSwitch(bluetoothSwitch);

        final Uri BLUETOOTH_URL = Settings.System.getUriFor(Settings.System.BLUETOOTH_ON);
        bluetoothObserver = new BluetoothObserver(new Handler());
        getApplicationContext().getContentResolver()
                .registerContentObserver(BLUETOOTH_URL, true, bluetoothObserver);
    }

    private void initSwitch(Switch bluetoothSwitch) {
        int bluetoothStatus = checkBluetoothStatus();
        if (bluetoothStatus == 1) {
            bluetoothSwitch.setChecked(true);
        } else {
            bluetoothSwitch.setChecked(false);
        }

        bluetoothSwitch.setOncheckListener(new Switch.OnCheckListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onCheck(Switch view, boolean check) {
                Context context = getApplicationContext();

                //Checks if this app can modify system settings
                boolean canWriteSettings = Settings.System.canWrite(context);
                if (canWriteSettings) {
                    if (check) {
                        turnOnBluetooth();
                    } else {
                        turnOffBluetooth();
                    }
                } else {
                    //If currently cant modify system settings, app will ask for permission
                    Toast.makeText(context, "Please Enable Write Permissions", Toast.LENGTH_SHORT).show();
                    askWritePermissions();
                }
            }
        });
    }

    private void turnOnBluetooth() {
        btAdapter.enable();
    }

    private void turnOffBluetooth() {
        btAdapter.disable();
    }

    /**
     * Shows the modify system settings panel to allow the user to add WRITE_SETTINGS permissions for this app.
     */
    private void askWritePermissions() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        getApplicationContext().startActivity(intent);
    }

    public int checkBluetoothStatus() {
        int status = btAdapter.getState();
        if (status == btAdapter.STATE_ON) {
            return 1;
        } else if (status == btAdapter.STATE_OFF) {
            return 0;
        }
        //returns -1, as it is either STATE_TURNING_ON or STATE_TURNING_OFF
        return -1;
    }

    private void bluetoothToggle() {
        Switch bluetoothSwitch = findViewById(R.id.bluetoothSwitch);
        if (bluetoothSwitch.isCheck()) {
            bluetoothSwitch.setChecked(false);
        } else {
            bluetoothSwitch.setChecked(true);
        }
    }

    /**
     * Bluetooth Observer: Handle the change in bluetooth connection in real time and change whether the switch is checked or not
     */
    private class BluetoothObserver extends ContentObserver {
        public BluetoothObserver(Handler h) {
            super(h);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            bluetoothToggle();
        }
    }
}