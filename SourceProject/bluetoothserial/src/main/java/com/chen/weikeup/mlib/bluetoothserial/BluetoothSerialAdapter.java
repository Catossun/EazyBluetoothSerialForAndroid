package com.chen.weikeup.mlib.bluetoothserial;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 對本機藍芽進行操作。
 *
 * @author Weikeup
 */
public class BluetoothSerialAdapter {
    private static String TAG = BluetoothSerialAdapter.class.getSimpleName();
    private static BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothSerialAdapter() {
    }

    /**
     * 取得所有已配對的裝置。
     *
     * @return 已配對的裝置之 Set 集合。
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN})
    public static Set<BluetoothSerialDevice> getDevices() {
        Set<BluetoothSerialDevice> devices = new HashSet<>();
        if (adapter != null) {
            adapter.cancelDiscovery();
            for (BluetoothDevice device : adapter.getBondedDevices()) {
                try {
                    BluetoothSerialDevice serialDevice = new BluetoothSerialDevice(device);
                    devices.add(serialDevice);
                } catch (IOException e) {
                    Log.e(TAG, "Error: ", e);
                }
            }
        }
        return devices;
    }

    /**
     * 裝置是否支援藍芽。
     *
     * @return 如果裝置支援藍芽，回傳 true，否則回傳 false。
     */
    public static boolean isSupport() {
        return adapter != null;
    }

    /**
     * 裝置藍芽是否開啟。
     *
     * @return 如果裝置已開啟藍芽，回傳 True，否則回傳 False。
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public static boolean isEnable() {
        return adapter != null && adapter.isEnabled();
    }

    /**
     * 向使用者發出開啟藍芽對話框。
     *
     * @param activity    用來執行 startActivityForResult() 的 Activity
     * @param requestCode 當 Activity 執行後，傳給 onActivityResult() 使用
     */
    public static void callRequestEnableBluetoothActivity(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }
}
