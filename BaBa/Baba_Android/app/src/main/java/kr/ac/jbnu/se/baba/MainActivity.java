package kr.ac.jbnu.se.baba;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 3;
    public static final int MESSAGE_TOAST = 4;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothService mBlueService = null;

    // Name of the connected device
    private String mConnectedDeviceName = null;

    private String preData = "";
    private ImageView babyIv;
    private Button situBtn, careConnBtn;
    private TextView mPeePooTv, mFlipTv, mInfoTv, mTimeTextView, mFlipCnt;

    private Thread timeThread = null;

    private String startTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            Objects.requireNonNull(this).finish();
        }

        mInfoTv = (TextView) findViewById(R.id.tv_receive);

        // 블루투스 연결안됐을때 연결하게 한다.
        if (!mBluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mBlueService == null) setup();
            else
                startActivityForResult(new Intent(getApplicationContext(), DeviceListActivity.class), REQUEST_CONNECT_DEVICE);
        }
    }


    private void setup() {
        Log.d(TAG, "setup()");
        // Initialize the BluetoothChatService to perform bluetooth connections
        mBlueService = new BluetoothService(this, mHandler);

        startActivityForResult(new Intent(this, DeviceListActivity.class), REQUEST_CONNECT_DEVICE);
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);

                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
//                            mInfoTv.setText(msg.arg1);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
//                            mInfoTv.setText("연결이 안됐습니다.");
                            break;
                        default:
                            break;
                    }
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d(TAG, readMessage);

                    if(!preData.equals(readMessage)) {
                        preData = readMessage;
                    }

                    if(readMessage.equals("t")){
                        final Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(),notification);
                        ringtone.play();
                        vibrator.vibrate(5000);
                        new String(readBuf, 0, msg.arg1);
                        Log.d(TAG, readMessage);
                    }

                    else{
                        new String(readBuf, 0, msg.arg1);
                        Log.d(TAG, readMessage);
                    }

                    break;
                default:
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:               //블루투스 디바이스와 커넥션함
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BluetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mBlueService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:                     //블루투스 온 상태 반환받음
                if (resultCode == Activity.RESULT_OK) { //블루투스 킴
                    setup();
                } else {
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getApplicationContext(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    Objects.requireNonNull(this).finish();
                }
        }
    }

//    @SuppressLint("HandlerLeak")
//    Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            int data1 = msg.arg1;
//
//            @SuppressLint("DefaultLocale") String result = String.format("%d", data1);
//            mInfoTv.setText(result);
//        }
//    };


}
