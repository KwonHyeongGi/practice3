package com.example.practice2;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    // Debugging
    private static final String TAG = "Main Activity";
    private static final boolean D = true;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_WRITE = 2;

    public static final int MODE_REQUEST = 1 ;
    // button state
    private int mSelectedBtn ;

    // synchronized flags
    private static final int STATE_SENDING = 1 ;
    private static final int STATE_NO_SENDING = 2 ;
    private int mSendingState ;

    private Button btn_Connect;
    private BluetoothService bluetoothService_obj = null; //BluetoothService 에 접근하기 위한 객체
    private Button mbtn1;
    private Button mbtn2;

    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");

        setContentView(R.layout.activity_main);

        mSelectedBtn = -1;

        btn_Connect = (Button) findViewById(R.id.bluetooth_connect);
        btn_Connect.setOnClickListener(mClickListener);

        mbtn1 = (Button) findViewById(R.id.btn1);
        mbtn1.setOnClickListener(mClickListener);
        mbtn2 = (Button) findViewById(R.id.btn2);
        mbtn2.setOnClickListener(mClickListener);
        if (bluetoothService_obj == null) {
            bluetoothService_obj = new BluetoothService(this, mHandler);
            mOutStringBuffer = new StringBuffer("");
        }
    }
    public void onButton1Clicked(View v) { //재활모드로 이동, Layout 리소스 XML에서 Button의 onClick 속성을 이용
        Intent intent = new Intent(MainActivity.this, Rehabilitation.class);
        startActivity(intent);
    }

    public void onButton2Clicked(View v) { // 보행보조모드로 이동
        Intent intent = new Intent(MainActivity.this, walkingassistance.class);
        startActivity(intent);
    }

    //BluetoothService 로부터 정보를 돌려받는 Handler
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            break;
                        case BluetoothService.STATE_FAIL:
                            break;
                    }
                    break;

                case MESSAGE_WRITE:
                    String writeMessage = null;
                    if(mSelectedBtn == 1){
                        writeMessage = mbtn1.getText().toString() ;
                        mSelectedBtn = -1;
                    }
                    else if ( mSelectedBtn == 2 ) {
                        writeMessage = mbtn2.getText().toString() ;
                        mSelectedBtn = -1 ;
                    }
                    else { // mSelectedBtn = -1 : not selected
                        byte[] writeBuf = (byte[]) msg.obj;
                        // 버퍼에서 문자열을 구성한다.
                        writeMessage = new String(writeBuf);
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);

        switch (requestCode) {

            case REQUEST_CONNECT_DEVICE:
                //연결할 장치와 함께 DeviceListActivity가 반환 될 때
                if (resultCode == Activity.RESULT_OK) {
                    bluetoothService_obj.getDeviceInfo(data);
                }
                break;
            // requestCode가 REQUEST_CONNECT_DEVICE인 경우 getDeviceInfo()메소드를 호출
            case REQUEST_ENABLE_BT:
                //블루투스 사용 가능 요청이 반환되면
                if (resultCode == Activity.RESULT_OK) {
                    // 블루투스가 활성화 되었을 때(resultCode가 Activity.RESULT_OK일때) 기기 검색을 통해 페어링
                    bluetoothService_obj.scanDevice();
                } else { // 취소눌렀을 때

                    Log.d(TAG, "블루투스가 연결되지 않았습니다.");
                }
                break;
        }
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bluetooth_connect: //모든 블루투스의 활성화는 블루투스 서비스 객체를 통해 접근한다.
                    if (bluetoothService_obj.getDeviceState()) // 블루투스 기기의 지원여부가 true 일때
                    {
                        bluetoothService_obj.enableBluetooth(); //블루투스 활성화 시작.
                    } else {
                        finish();
                    }
                    break;
                case R.id.btn1 :
                    if( bluetoothService_obj.getState() == BluetoothService.STATE_CONNECTED){ //연결된 상태에서만 값을 보낸다.
                        sendMessage("0", MODE_REQUEST);
                        mSelectedBtn = 1;
                    }else {
                        Toast.makeText(getApplicationContext(), "블루투스 연결을 먼저 해 주세요!! ", Toast.LENGTH_SHORT).show();
                    }
                    break ;
                case R.id.btn2 :
                    if( bluetoothService_obj.getState() == BluetoothService.STATE_CONNECTED){
                        sendMessage( "1", MODE_REQUEST ) ;
                        mSelectedBtn = 2 ;
                    }else {
                        Toast.makeText(getApplicationContext(), "블루투스 연결을 먼저 해 주세요!! ", Toast.LENGTH_SHORT).show();
                    }
                    break ;
                default:
                    break;
            }//switch
        }
    };

    /*메시지를 보낼 메소드 정의*/
    private synchronized void sendMessage( String message, int mode ) {
        if ( mSendingState == STATE_SENDING ) {
            try {
                wait() ;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mSendingState = STATE_SENDING ;
        // 무엇인가 시도하기 전에 우리가 실제로 연결되어 있는지 확인하십시오.
        if ( bluetoothService_obj.getState() != BluetoothService.STATE_CONNECTED ) {
            mSendingState = STATE_NO_SENDING ;
            return ;
        }
        // 실제로 보낼 항목이 있는지 확인하십시오.
        if ( message.length() > 0 ) {
            // 메시지 바이트를 가져 와서 BluetoothChatService 에 쓰기를 지시합니다.
            byte[] send = message.getBytes() ;
            bluetoothService_obj.write(send, mode) ;

            // 문자열 버퍼를 0으로 재설정하고 편집 텍스트 필드를 지 웁니다.
            mOutStringBuffer.setLength(0) ;
        }
        mSendingState = STATE_NO_SENDING ;
        notify() ;
    }
}