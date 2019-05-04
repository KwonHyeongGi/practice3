package com.example.practice2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.practice2.R;

public class Rehabilitation extends AppCompatActivity {
    /**
     * private static final int REQUEST_CONNECT_DEVICE = 1;
     * private static final int REQUEST_ENABLE_BT = 2;
     * private static final String TAG = "BluetoothService";
     * private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
     * <p>
     * private static final boolean D = true;
     * <p>
     * private ConnectThread mConnectThread;
     * private ConnectedThread mConnectedThread;
     * private BluetoothAdapter BtAdapter;
     * <p>
     * TextView mTvBluetoothStatus;
     * TextView mTvReceiveData;
     * TextView mTvSendData;
     * Button mBtnBluetoothOn;
     * Button mBtnBluetoothOff;
     * Button mBtnConnect;
     * Button mBtnSendData;
     * <p>
     * btAdapter = BluetoothAdapter.getDefaultAdapter();
     * <p>
     * Handler mBluetoothHandler;
     * ConnectedBluetoothThread mThreadConnectedBluetooth;
     * BluetoothDevice mBluetoothDevice;
     * BluetoothSocket mBluetoothSocket;
     * <p>
     * ProgressBar progressBar;
     * TextView percent;
     * Button increase;
     * Button decrease;
     * Button cw;
     * Button ccw;
     * Button start;
     * Button stop;
     * Button startProgressBar;
     * Handler handler;
     * AlertDialog.Builder dialog;
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rehabilitation);


/**
        increase = (Button) findViewById(R.id.increase);
        decrease = (Button) findViewById(R.id.decrease);
        cw = (Button) findViewById(R.id.cw);
        ccw = (Button) findViewById(R.id.ccw);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);

        increase.setOnClickListener(onClickListener);
        decrease.setOnClickListener(onClickListener);
        cw.setOnClickListener(onClickListener);
        ccw.setOnClickListener(onClickListener);
        start.setOnClickListener(onClickListener);




    }
    Button.OnClickListener onClickListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()){

                case R.id.increase :
                    if(mThreadConnectedBluetooth != null) {
                        mThreadConnectedBluetooth.write(mTvSendData.getText().toString());
                        mTvSendData.setText("");
                    }
                    break;

                case R.id.decrease :
                    if(mThreadConnectedBluetooth != null) {
                        mThreadConnectedBluetooth.write(mTvSendData.getText().toString());
                        mTvSendData.setText("");
                    }
                    break;

                case R.id.cw :
                    if(mThreadConnectedBluetooth != null) {
                        mThreadConnectedBluetooth.write(mTvSendData.getText().toString());
                        mTvSendData.setText("");
                    }
                    break;

                case R.id.ccw :
                    if(mThreadConnectedBluetooth != null) {
                        mThreadConnectedBluetooth.write(mTvSendData.getText().toString());
                        mTvSendData.setText("");
                    }
                    break;

                case R.id.start :
                    if(mThreadConnectedBluetooth != null) {
                        mThreadConnectedBluetooth.write(mTvSendData.getText().toString());
                        mTvSendData.setText("");
                    }
                    break;

                case R.id.stop :
                    if(mThreadConnectedBluetooth != null) {
                        mThreadConnectedBluetooth.write(mTvSendData.getText().toString());
                        mTvSendData.setText("");
                    }
                    break;


            }
            mBluetoothHandler = new Handler(){
                public void handleMessage(android.os.Message msg){
                    if(msg.what == BT_MESSAGE_READ){
                        String readMessage = null;
                        try {
                            readMessage = new String((byte[]) msg.obj, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        mTvReceiveData.setText(readMessage);
                    }
                }
            };
        }
    };
 */

    }
}
