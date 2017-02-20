package com.example.barbara.accessoryusb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity{

    UsbManager manager;
    UsbAccessory accessory;
    PendingIntent mPermissionIntent;
    UsbSerialDevice serialPort;
    Button but;
    EditText edi;
    TextView text;
    ParcelFileDescriptor mFileDescriptor;
    FileInputStream mInputStream;
    FileOutputStream mOutputStream;
    InputStream Input;


    private static final String ACTION_USB_PERMISSION = "com.example.barbara.accessoryusb.USB_PERMISSION";

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                data.concat("/n");
                tvAppend(text, data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        manager.requestPermission(accessory, mPermissionIntent);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);

        but = (Button) findViewById(R.id.butt);
        edi = (EditText) findViewById(R.id.edittext);
        text = (TextView) findViewById(R.id.textt);
        tvAppend(text,"200\n");


    }


    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    accessory = (UsbAccessory) intent.getParcelableExtra(manager.EXTRA_ACCESSORY);
                    tvAppend(text,"0\n");
                    accessory.getSerial();
                    if (intent.getBooleanExtra(manager.EXTRA_PERMISSION_GRANTED, false)) {
                        tvAppend(text,"1\n");
                        if(accessory != null){
                            tvAppend(text,"2\n");
                            if (serialPort != null) {
                                tvAppend(text,"3\n");
                                if (serialPort.open()) {
                                    tvAppend(text,"4\n");

                            serialPort.setBaudRate(115200);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            //call method to set up accessory communication

                            serialPort.read(mCallback);
                                    tvAppend(text, "Serial Connection Opened!\n");
                                }
                            }

                        }
                    }
                    else {
                        //Log.d(TAG, "permission denied for accessory " + accessory);
                    }
                }
            }
        }
    };




    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
    }



    public void send(View view) {
        String string = edi.getText().toString();
        serialPort.write(string.getBytes());
        tvAppend(text, "\nData Sent : " + string + "\n");

    }


}
