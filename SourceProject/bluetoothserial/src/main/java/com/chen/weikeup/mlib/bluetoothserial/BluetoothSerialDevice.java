package com.chen.weikeup.mlib.bluetoothserial;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import androidx.annotation.RequiresPermission;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.UUID;

/**
 * 藍芽裝置物件。
 *
 * @author Weikeup
 */
public class BluetoothSerialDevice {
    /**
     * 藍芽序列傳輸服務的 UUID。
     */
    public static String SERIAL_PORT_SERVICE_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    /**
     * <p>用來傳送資料。</p>
     * <p>使用 out.print(data); 傳送資料</p>
     */
    public PrintStream out;

    /**
     * <p>用來接收資料。</p>
     * <p>使用 in.readLine(); 接收資料</p>
     */
    public BufferedReader in;

    private BluetoothDevice device;
    private BluetoothSocket socket;
    private UUID uuid;
    private boolean hadBeenConnected = false;

    @RequiresPermission(Manifest.permission.BLUETOOTH)
    BluetoothSerialDevice(BluetoothDevice device) throws IOException {
        this.device = device;
        uuid = UUID.fromString(SERIAL_PORT_SERVICE_UUID);
        socket = device.createRfcommSocketToServiceRecord(uuid);
    }

    /**
     * 取得目前連線用的 UUID 物件。
     *
     * @return UUID 物件。
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * 設定連線用的 UUID 物件。
     *
     * @param uuid UUID 物件。
     */
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * 與藍芽裝置是否連線。
     *
     * @return 如果已與藍芽裝置連線，回傳 true，否則回傳 false。
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    /**
     * 取得目前連線的 BluetoothSocket 物件。
     *
     * @return BluetoothSocket 物件。
     */
    public BluetoothSocket getSocket() {
        return socket;
    }

    /**
     * 取得目前藍芽的 BluetoothDevice 物件。
     *
     * @return BluetoothDevice 物件。
     */
    public BluetoothDevice getDevice() {
        return device;
    }

    /**
     * <p>與藍芽裝置建立連線。</p>
     * <p>註：此方法會阻塞呼叫該方法的執行續！</p>
     *
     * @throws IOException
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN})
    public void connect() throws IOException {
        if (hadBeenConnected) {
            if (socket.isConnected()) socket.close();
            socket = device.createRfcommSocketToServiceRecord(uuid);
            hadBeenConnected = false;
        }
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        getSocket().connect();
        if (isConnected()) {
            out = new PrintStream(getOutputStream());
            in = new BufferedReader(new InputStreamReader(getInputStream()));
            hadBeenConnected = true;
        }
    }

    /**
     * 關閉連線。
     */
    public void close() throws IOException {
        if (socket != null) socket.close();
    }

    /**
     * 取得目前連線的 InputStream。
     *
     * @return InputStream 物件。
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
        return socket == null ? null : socket.getInputStream();
    }

    /**
     * 取得目前連線的 OutputStream。
     *
     * @return OutputStream 物件。
     * @throws IOException
     */
    public OutputStream getOutputStream() throws IOException {
        return socket == null ? null : socket.getOutputStream();
    }

    /**
     * 取得裝置名稱。
     *
     * @return 裝置名稱。
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public String getName() {
        return device.getName();
    }

    /**
     * 取得裝置位址。
     *
     * @return 裝置位址。
     */
    public String getAddress() {
        return device.getAddress();
    }
}
