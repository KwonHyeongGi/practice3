package com.example.practice2;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService {
    // Debugging
    private static final String TAG = "BluetoothService";
    private static final boolean D = true; /** */

    // Intent request code

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // RFCOMM Protocol
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter btAdapter;
    private Activity mActivity;
    private Handler mHandler;

    private ConnectThread mConnectThread; // 변수명 다시
    private ConnectedThread mConnectedThread; // 변수명 다시

    private int mState;
    public int mMode;

    // 상태를 나타내는 상태 변수
    static final int STATE_NONE = 0; // 우리는 아무것도하지 않고있다.
    static final int STATE_LISTEN = 1; // 지금 들어오는 것을 듣고있다.
    static final int STATE_CONNECTING = 2; // 이제 발신을 시작합니다.
    static final int STATE_CONNECTED = 3; // 이제 리모컨에 연결됨
    static final int STATE_FAIL = 7; // 연결실패

    // Constructors
    public BluetoothService(Activity activity, Handler handler) {
        mActivity = activity;
        mHandler = handler;

        // BluetoothAdapter 얻기
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /** 블루투스 지원여부 확인 */

    public boolean getDeviceState() { // 메소드생성
        Log.d(TAG, "블루투스 지원여부 확인");

        if (btAdapter == null) { // 지원여부확인(null 일경우 지원x)
            Log.d(TAG, "블루투스 지원안함");
            return false;
        } else {
            Log.d(TAG, "블루투스 지원");
            return true;
        }
    }

    /** 블루투스 활성화 요청 (getDeviceState 가 true 를 반환시 활성화를 요청) */

    public void enableBluetooth() { // 메소드 생성
        Log.i(TAG, "블루투스 연결여부 확인중");

        if (btAdapter.isEnabled()) {
            // 기기의 블루투스를 사용할 수 있을 경우
            Log.d(TAG, "블루투스 연결");
            scanDevice();
        } else {
            // 기기의 블루투스 상태가 Off인 경우
            Log.d(TAG, "블루투스 연결요청");

            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); //확인취소알림창띄우기
            mActivity.startActivityForResult(i, REQUEST_ENABLE_BT); //onActivityResult()메소드로 들어온다.
        }
    }


    /** 사용 가능한 기기 검색    */

    public void scanDevice() { //메소드 생성
        Log.d(TAG, "장치 검색");

        Intent serverIntent = new Intent(mActivity, DeviceListActivity.class); // 값이 mainactivity로 반환
        mActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    // Bluetooth 상태 초기화
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget(); /** */
    }

    // Bluetooth 상태 가져옴
    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
        Log.d(TAG, "시작");

        // 연결을 시도하는 모든 스레드를 취소하십시오.
        if (mConnectThread == null) {

        } else {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // 현재 연결을 실행중인 모든 스레드 취소
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_LISTEN);

    }

    /** 스캔하고 장치 정보를 얻은 후     */

    public void getDeviceInfo(Intent data) {
        // 장치 MAC 주소 가져 오기
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // BluetoothDevice 객체 가져 오기
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        Log.d(TAG, "Get Device Info \n" + "address : " + address);

        connect(device); // address라는 String에는 선택한 기기의 주소가 담겨져 있고, 그 주소를 BluetoothDevice로 전달
    }


    // ConnectThread 초기화 device의 모든 연결 제거
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "연결장치 : " + device);

        // 연결을 시도하는 모든 스레드를 취소하십시오.
        if (mState == STATE_CONNECTING) {
            if (mConnectThread == null) {

            } else {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // 현재 연결을 실행중인 모든 스레드 취소
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 주어진 장치와 연결하기 위해 스레드를 시작하십시오.
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    // ConnectedThread 초기화
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");

        // 연결을 완료 한 스레드 취소
        if (mConnectThread == null) {

        } else {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // 현재 연결을 실행중인 모든 스레드 취소
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 스레드를 시작하여 연결을 관리하고 전송을 수행합니다.
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        setState(STATE_CONNECTED);
    }

    // 모든 thread stop
    public synchronized void stop() {
        Log.d(TAG, "정지");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    // 값을 쓰는 부분(보내는 부분)
    public void write(byte[] out, int mode) { // 임시 객체 만들기
        ConnectedThread r; // ConnectedThread 의 복사본 동기화
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        } // 동기화되지 않은 쓰기 수행
        r.write(out, mode); //ConnectedThread클래스 내에 있는 write함수를 호출하여 메시지를 보낸다.
    }

    // 연결 실패했을때
    private void connectionFailed() {
        setState(STATE_FAIL);
    }

    // 연결을 잃었을 때
    private void connectionLost() {
        setState(STATE_LISTEN);

    }

    /** 이 스레드는 장치와 나가는 연결을 시도하는 동안 실행됩니다. 연결이 성공하거나 실패 할 때 바로 실행됩니다. */

    private class ConnectThread extends Thread { /** ConnectThread : 연결을하는 중간단계*/
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            /**
             주어진 BluetoothDevice에 연결하기 위해 BluetoothSocket을 가져옵니다.
             MY_UUID는 응용 프로그램의 UUID 문자열이며 서버에서도 사용됩니다.*/

            // 디바이스 정보를 얻어서 BluetoothSocket 생성
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            }
            catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // 연결을 시도하기 전에는 항상 기기 검색을 중지한다.
            // 기기 검색이 계속되면 연결속도가 느려지기 때문이다.
            btAdapter.cancelDiscovery();

            // BluetoothSocket 연결 시도
            try {
                // BluetoothSocket 연결 시도에 대한 return 값은 succes 또는 exception이다.
                mmSocket.connect();
                Log.d(TAG, "연결 성공");
            }
            catch (IOException e) {
                connectionFailed(); // 연결 실패시 불러오는 메소드
                Log.d(TAG, "연결 실패");
                try {
                    mmSocket.close();
                }
                catch (IOException e2) {
                    Log.e(TAG, "연결 실패시 소켓을 닫을 수 없음\n", e2);
                }
                //연결 중 혹은 연결 대기상태인 메소드를 호출
                BluetoothService.this.start();
                return;
            }

            // ConnectThread 클래스를 reset한다.
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // ConnectThread를 시작한다.
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            }
            catch (IOException e) {
                Log.e(TAG, "연결 소켓 close ()가 실패했습니다.", e);
            }
        }
    }

    /**
     * 이 스레드는 원격 장치와의 연결 중에 실행됩니다.
     * 모든 송수신 전송을 처리합니다.
     */
    private class ConnectedThread extends Thread { /** 연결을 완료하는 단계 */
    private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread 만들기");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // BluetoothSocket의 inputstream 과 outputstream을 얻는다.
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e) {
                Log.e(TAG, "임시 소켓이 생성되지 않았습니다.", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "mConnectedThread 시작");
            byte[] buffer = new byte[1024];
            int bytes;

            // 연결된 상태에서 InputStream 수신 대기
            while (true) {
                try {
                    // InputStream으로부터 값을 받는 읽는 부분(값을 받는다)
                    bytes = mmInStream.read(buffer);

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * 연결된 OutStream 에 씁니다.         */

        public void write(byte[] buffer, int mode) {
            try {
                // 값을 쓰는 부분(값을 보낸다)
                mmOutStream.write(buffer);
                mMode = mode;
                if(mode == MainActivity.MODE_REQUEST) {
                    mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
                }
            }
            catch (IOException e) {
                Log.e(TAG, "쓰기 중 예외", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "연결 소켓 close ()가 실패했습니다.", e);
            }
        }
    }

}