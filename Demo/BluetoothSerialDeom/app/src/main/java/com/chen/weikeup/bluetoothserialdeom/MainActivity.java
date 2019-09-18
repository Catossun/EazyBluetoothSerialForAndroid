package com.chen.weikeup.bluetoothserialdeom;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chen.weikeup.mlib.bluetoothserial.BluetoothSerialAdapter;
import com.chen.weikeup.mlib.bluetoothserial.BluetoothSerialDevice;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_ENABLE_BLUETOOTH = 1;

    private Button refreshBtListBtn;
    private Button sendDataBtn;
    private EditText sendData;
    private ListView btList;
    private ListView btDataIn;

    private BluetoothSerialDevice connectedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refreshBtListBtn = findViewById(R.id.refresh_bt_list);
        sendDataBtn = findViewById(R.id.send_data_btn);
        sendData = findViewById(R.id.send_data);
        btList = findViewById(R.id.bt_list);
        btDataIn = findViewById(R.id.bt_data_in);

        refreshBtListBtn.requestFocus();

        //重新整理藍芽裝置清單
        refreshBtListBtn.setOnClickListener(v -> {
            //判斷裝置是否支援藍芽
            if (BluetoothSerialAdapter.isSupport()) {
                //判斷藍芽是否開啟
                if (BluetoothSerialAdapter.isEnable()) {
                    //取得已配對的藍芽裝置
                    Set<BluetoothSerialDevice> deviceSet = BluetoothSerialAdapter.getDevices();

                    //將裝置顯示在清單中
                    List<BluetoothSerialDevice> deviceList = new ArrayList<>(deviceSet);
                    ArrayList<String> names = new ArrayList<>();
                    for (BluetoothSerialDevice device : deviceSet) {
                        names.add(device.getName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
                    btList.setAdapter(adapter);

                    //按下清單來取得藍芽裝置
                    btList.setOnItemClickListener((parent, view, position, id) -> {
                        BluetoothSerialDevice device = deviceList.get(position);

                        //斷開先前的連線
                        if (connectedDevice != null && connectedDevice.isConnected()) {
                            try {
                                connectedDevice.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        Snackbar.make(v, "連線中...", Snackbar.LENGTH_SHORT).show();
                        //開新的執行續來連線
                        AsyncTask.execute(() -> {
                            try {
                                device.connect(); //執行續會阻塞在此

                                if (device.isConnected()) {
                                    connectedDevice = device;
                                    runOnUiThread(() -> {
                                        Snackbar.make(v, "連線成功！", Snackbar.LENGTH_SHORT).show();
                                    });

                                    ArrayAdapter<String> dataComeInAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
                                    runOnUiThread(() -> {
                                        btDataIn.setAdapter(dataComeInAdapter);
                                    });
                                    //持續接收資料
                                    while (connectedDevice.isConnected()) {
                                        //如果有資料進來
                                        if (connectedDevice.in.ready()) {
                                            //讀取一筆資料
                                            String dataIn = connectedDevice.in.readLine();

                                            //將資料顯示在畫面上
                                            runOnUiThread(() -> {
                                                dataComeInAdapter.insert(dataIn, 0);
                                            });
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                runOnUiThread(() -> {
                                    Snackbar.make(v, "連線失敗！", Snackbar.LENGTH_SHORT).show();
                                });
                            }
                        });
                    });
                } else {
                    //向使用者請求開啟藍芽
                    BluetoothSerialAdapter.callRequestEnableBluetoothActivity(this, REQUEST_ENABLE_BLUETOOTH);
                }
            } else {
                Snackbar.make(v, "此裝置不支援藍芽！", Snackbar.LENGTH_SHORT).show();
            }
        });

        //發送資料
        sendDataBtn.setOnClickListener(v -> {
            if (connectedDevice != null && connectedDevice.isConnected()) {
                connectedDevice.out.println(sendData.getText());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(connectedDevice!= null && connectedDevice.isConnected()){
            try {
                connectedDevice.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    //使用者同意動作
                }
                break;
        }
    }
}
