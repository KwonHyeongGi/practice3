package com.example.practice2;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View.OnClickListener;
import java.util.Set;
/**
 이 활동은 대화 상자로 나타납니다. 발견 된 영역에서 페어링 된 모든 장치 및 장치를 나열합니다.
 사용자가 장치를 선택하면 장치의 MAC 주소가 결과 인 텐트의 상위 Activity로 다시 전송됩니다. */
public class DeviceListActivity extends Activity {
    // Debugging
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 창 설정
        setContentView(R.layout.device_list);

        // 사용자가 철회 할 경우 결과를 CANCELED로 설정
        setResult(Activity.RESULT_CANCELED);

        //장치 검색을 수행하기 위해 단추를 초기화하십시오.
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        // 배열 어댑터를 초기화하십시오. 하나는 이미 페어링 된 기기 용이고 다른 하나는 새로 발견 된 기기 용입니다.
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // 페어링 된 장치에 대한 ListView 찾기 및 설정
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // 찾기 및 새로 발견 된 장치에 대한 ListView 설정
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // 장치 검색시 브로드 캐스트 등록
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // 검색이 완료되면 브로드 캐스트에 등록하십시오.
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // 로컬 Bluetooth 어댑터 가져 오기
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // 현재 페어링 된 기기 세트 가져 오기
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // 페어링 된 장치가있는 경우 각 장치를 ArrayAdapter에 추가합니다.
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

        ActivityCompat.requestPermissions(this,

                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},

                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 우리가 더 이상 발견을하지 않고 있는지 확인하십시오.
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // 브로드 캐스트 수신기 등록 취소
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");

        // 제목에 스캔 표시
        setTitle(R.string.scanning);

        // 새 기기의 부제 사용
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // 이미 발견했다면 그만해라.
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // BluetoothAdapter에서 검색 요청
        mBtAdapter.startDiscovery();
    }

    // ListViews의 모든 장치에 대한 클릭 할 수있는 청취자
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // 비용이 많이 들고 연결을 시도하기 때문에 검색을 취소하십시오.
            mBtAdapter.cancelDiscovery();

            // 보기에서 마지막 17자인 장치 MAC 주소를 가져옵니다.
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // 결과 인 텐트를 작성하고 MAC 주소를 포함하십시오.
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // 결과 설정 및이 활동 완료
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    // 발견 된 장치를 청취하고 발견이 끝나면 제목을 변경하는 BroadcastReceiver

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // 검색에서 장치를 찾으면
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Intent에서 BluetoothDevice 개체 가져 오기
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 이미 페어링 된 경우 건너 뜁니다. 이미 목록에 있기 때문에 건너 뜁니다.
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // 검색이 완료되면 활동 제목을 변경하십시오.
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

}
