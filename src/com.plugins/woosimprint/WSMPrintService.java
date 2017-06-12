package com.plugins.woosimprint;

import com.testap.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.woosim.printer.WoosimBarcode;
import com.woosim.printer.WoosimCmd;
import com.woosim.printer.WoosimImage;
import com.woosim.printer.WoosimService;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import jpos.JposException;

/**
 * Created by dev01 on 12/2/16.
 */

public class WSMPrintService extends CordovaPlugin{

    private static final String TAG = WSMPrintService.class.getSimpleName();
    private final boolean DEBUG = true;

    private final String ACTION_EXECUTE_PRINTER = "executePrinter";
    private final String ACTION_PRINTER_INFO = "printerInfo";

    // Message types sent from the BluetoothPrintService Handler
    public static final int MESSAGE_DEVICE_NAME = 1;
    public static final int MESSAGE_TOAST = 2;
    public static final int MESSAGE_READ = 3;

    // Key names received from the BluetoothPrintService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    //Service methods
    private String CURRENT_METHOD="";
    private final String METHOD_PRINT_TEXT = "printText";
    private final String METHOD_PRINT_BLUETOOTH_TEXT = "printBluetoothText";


    private final String METHOD_SELECT_PRINTER = "selectPrinter";

    // Layout Views
    private boolean mEmphasis = false;
    private boolean mUnderline = false;
    private int mCharsize = 1;
    private int mJustification = 0;
    private TextView mTrack1View;
    private TextView mTrack2View;
    private TextView mTrack3View;
    private Menu mMenu = null;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the print services
    private BluetoothPrintService mPrintService = null;
    private WoosimService mWoosim = null;
    private Context context=null;
    CallbackContext callbackContext=null;
    private JSONArray args=null;

    public static void setContext(Context context) {
         context=context;//this.cordova.getActivity().getApplicationContext();
    }
    /**
     * Executes the request.
     *
     * This method is called from the WebView thread. To do a non-trivial amount
     * of work, use: cordova.getThreadPool().execute(runnable);
     *
     * To run on the UI thread, use:
     * cordova.getActivity().runOnUiThread(runnable);
     *
     * @param action
     *            The action to execute.
     * @param args
     *            The exec() arguments.
     * @param callbackContext
     *            The callback context used when calling back into JavaScript.
     * @return Whether the action was valid.
     */
    @Override
    public boolean execute(final String action, final JSONArray args,
                           final CallbackContext callbackContext) throws JSONException {
        this.callbackContext=callbackContext;
        this.args=args;

        if (DEBUG) {
            Log.d(TAG, "execute(" + action + ", " + args + ", "
                    + callbackContext + ")");
        }
        String method = args.getString(0);
        CURRENT_METHOD=method;
        PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
        r.setKeepCallback(true);
        if(mPrintService!=null)
        {
            mPrintService.stop();
        }
        if (mBluetoothAdapter == null) {
            // Get local Bluetooth adapter
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        }

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(cordova.getActivity(), R.string.toast_bt_na, Toast.LENGTH_LONG).show();
            callbackContext.error("Bluetooth is not supported");
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //cordova.setActivityResultCallback (this);
            cordova.getActivity().startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session

            return true;
        }
        if (action.equals(ACTION_PRINTER_INFO))
        {
            try
            {
                if (CURRENT_METHOD.equals(METHOD_SELECT_PRINTER))
                {
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        //cordova.setActivityResultCallback (this);
                        cordova.getActivity().startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                        // Otherwise, setup the chat session
                    } else {

                        callbackContext.sendPluginResult(r);

                        Intent serverIntent = new Intent(this.cordova.getActivity().getApplicationContext(), DeviceListActivity.class);
                        this.cordova.startActivityForResult((CordovaPlugin) this,serverIntent, REQUEST_CONNECT_DEVICE_SECURE);

                        return true;
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
                callbackContext.error(e.getMessage());
                return false;
            }
        }else if (action.equals(ACTION_EXECUTE_PRINTER)) {

            if (CURRENT_METHOD.equals(METHOD_PRINT_BLUETOOTH_TEXT))
            {
                try{
                     setupPrint();
                    connectDevice(args.getString(1),true);
                    //printText(args.getString(2));
                    callbackContext.sendPluginResult(r);
                    /*callbackContext.sendPluginResult(new PluginResult(
                            PluginResult.Status.OK, "Resultado"));*/
                }catch (Exception e){
                    e.printStackTrace();
                    callbackContext.error(e.getMessage());
                    return false;
                }

            }else{
                try {
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        //cordova.setActivityResultCallback (this);
                        this.cordova.startActivityForResult((CordovaPlugin) this,enableIntent, REQUEST_ENABLE_BT);
                        // Otherwise, setup the chat session
                        return true;
                    } else {
                        if (mPrintService == null)  setupPrint();
                    }
                    Intent serverIntent=null;
                    serverIntent = new Intent(cordova.getActivity().getApplicationContext(), DeviceListActivity.class);
                    this.cordova.startActivityForResult((CordovaPlugin) this,serverIntent, REQUEST_CONNECT_DEVICE_SECURE);

                    callbackContext.sendPluginResult(r);
                    /*printText(args.getString(1));

                    callbackContext.sendPluginResult(new PluginResult(
                            PluginResult.Status.OK, "Resultado"));*/
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    callbackContext.error(e.getMessage());
                    return false;
                }
            }
        }else {
            callbackContext.error("Action is not matched");
            return false;
        }

        return true;


    }

    private void setupPrint() {
        mCharsize = 1;
        /*Spinner spinner = (Spinner) findViewById(R.id.spn_charsize);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this.cordova.getActivity().getApplicationContext(), R.array.char_size_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 1) mCharsize = 2;
                        else if (position == 2) mCharsize = 3;
                        else if (position == 3) mCharsize = 4;
                        else if (position == 4) mCharsize = 5;
                        else if (position == 5) mCharsize = 6;
                        else if (position == 6) mCharsize = 7;
                        else if (position == 7) mCharsize = 8;
                        else mCharsize = 1;
                    }
                    public void onNothingSelected(AdapterView<?> parent) { }
                }
        );

        mTrack1View = (TextView)findViewById(R.id.view_track1);
        mTrack2View = (TextView)findViewById(R.id.view_track2);
        mTrack3View = (TextView)findViewById(R.id.view_track3);
        */
        // Initialize the BluetoothPrintService to perform bluetooth connections
        /*if (mPrintService!=null)
        {
            mPrintService.stop();
        }*/
        mPrintService = new BluetoothPrintService(this.cordova.getActivity().getApplicationContext(), mHandler);
        if (mWoosim==null)
        mWoosim = new WoosimService(mHandler);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(cordova.getActivity().getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    //redrawMenu();
                    if (CURRENT_METHOD.equals(METHOD_PRINT_BLUETOOTH_TEXT))
                    {
                        try{
                            Log.d(TAG, "printing");
                            printText(args.getString(2));
                            callbackContext.sendPluginResult(new PluginResult(
                                    PluginResult.Status.OK, "Resultado"));
                        }catch (Exception e){
                            e.printStackTrace();
                            callbackContext.error(e.getMessage());
                        }
                    }
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getInt(TOAST), Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_READ:
                    mWoosim.processRcvData((byte[])msg.obj, msg.arg1);
                    break;
                case WoosimService.MESSAGE_PRINTER:
                    switch (msg.arg1) {
                        case WoosimService.MSR:
                            if (msg.arg2 == 0) {
                                Toast.makeText(getApplicationContext(), "MSR reading failure", Toast.LENGTH_SHORT).show();
                            } else {
                                byte[][] track = (byte[][])msg.obj;
                               /* if (track[0] != null) {
                                    String str = new String(track[0]);
                                    mTrack1View.setText(str);
                                }
                                if (track[1] != null) {
                                    String str = new String(track[1]);
                                    mTrack2View.setText(str);
                                }
                                if (track[2] != null) {
                                    String str = new String(track[2]);
                                    mTrack3View.setText(str);
                                }*/
                            }
                            break;
                    }
                    break;
            }
        }
    };
    private Context getApplicationContext()
    {
        return cordova.getActivity().getApplicationContext();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(DEBUG) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {

                    if (CURRENT_METHOD==METHOD_PRINT_TEXT)
                    {
                        connectDevice(data, true);
                        try {
                            printText(args.getString(1));

                            //mPrintService.stop();
                            callbackContext.sendPluginResult(new PluginResult(
                                    PluginResult.Status.OK, "Invoice printed"));
                        }catch (Exception e){
                            e.printStackTrace();
                            callbackContext.error(e.getMessage());
                        }

                    }
                    else{
                        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                        callbackContext.sendPluginResult(new PluginResult(
                                PluginResult.Status.OK, address));

                    }
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a print
                    setupPrint();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    if(DEBUG) Log.d(TAG, "BT not enabled");
                    Toast.makeText(getApplicationContext(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();

                }
        }
    }
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        connectDevice( address,  secure);
    }
    private void connectDevice(String address, boolean secure) {

        // Get the BLuetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mPrintService.connect(device, secure);
    }

    private void sendData(byte[] data) {
        // Check that we're actually connected before trying printing
        if (mPrintService.getState() != BluetoothPrintService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (data.length > 0)
            mPrintService.write(data);
    }


    /**
     * On click function for sample print button.
     */
    public void pirnt2inchReceipt(View v) {
        InputStream inStream = cordova.getActivity().getResources().openRawResource(R.raw.receipt2);
        sendData(WoosimCmd.initPrinter());
        try {
            byte[] data = new byte[inStream.available()];
            while (inStream.read(data) != -1)
            {
                sendData(data);
            }
        } catch (IOException e) {
            Log.e(TAG, "sample 2inch receipt print fail.", e);
        } finally {
            if (inStream != null)
                try {
                    inStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public void pirntBMPImage(Bitmap b) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bmp = b;//BitmapFactory.decodeResource(getResources(), R.drawable.logo1, options);
        if (bmp == null) {
            Log.e(TAG, "resource decoding is failed");
            return;
        }
        byte[] data = WoosimImage.printBitmap(0, 0, 384, 200, bmp);
        bmp.recycle();

        sendData(WoosimCmd.setPageMode());
        sendData(data);
        sendData(WoosimCmd.PM_setStdMode());
    }

    public void printText(String string) throws IOException {

        byte[] text = null;

        if (string.isEmpty())
            return;
        else {
            try {
                text = string.getBytes("US-ASCII");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byteStream.write(WoosimCmd.setTextStyle(mEmphasis, mUnderline, false, mCharsize, mCharsize));
        byteStream.write(WoosimCmd.setTextAlign(mJustification));
        byteStream.write(text);
        byteStream.write(WoosimCmd.printData());

        sendData(WoosimCmd.initPrinter());
        sendData(byteStream.toByteArray());
        Log.d(TAG, "printing text sent");
    }



    /**
     * On click function for barcode print button.
     * @throws IOException
     */
    public void print1DBarcode(View v) throws IOException {
        final byte[] barcode =  {0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x30};
        final byte[] barcode8 = {0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37};
        final byte[] barcodeUPCE = {0x30,0x36,0x35,0x31,0x30,0x30,0x30,0x30,0x34,0x33,0x32,0x37};
        final byte[] cmd_print = WoosimCmd.printData();
        final String title1 = "UPC-A Barcode\r\n";
        byte[] UPCA = WoosimBarcode.createBarcode(WoosimBarcode.UPC_A, 2, 60, true, barcode);
        final String title2 = "UPC-E Barcode\r\n";
        byte[] UPCE = WoosimBarcode.createBarcode(WoosimBarcode.UPC_E, 2, 60, true, barcodeUPCE);
        final String title3 = "EAN13 Barcode\r\n";
        byte[] EAN13 = WoosimBarcode.createBarcode(WoosimBarcode.EAN13, 2, 60, true, barcode);
        final String title4 = "EAN8 Barcode\r\n";
        byte[] EAN8 = WoosimBarcode.createBarcode(WoosimBarcode.EAN8, 2, 60, true, barcode8);
        final String title5 = "CODE39 Barcode\r\n";
        byte[] CODE39 = WoosimBarcode.createBarcode(WoosimBarcode.CODE39, 2, 60, true, barcode);
        final String title6 = "ITF Barcode\r\n";
        byte[] ITF = WoosimBarcode.createBarcode(WoosimBarcode.ITF, 2, 60, true, barcode);
        final String title7 = "CODEBAR Barcode\r\n";
        byte[] CODEBAR = WoosimBarcode.createBarcode(WoosimBarcode.CODEBAR, 2, 60, true, barcode);
        final String title8 = "CODE93 Barcode\r\n";
        byte[] CODE93 = WoosimBarcode.createBarcode(WoosimBarcode.CODE93, 2, 60, true, barcode);
        final String title9 = "CODE128 Barcode\r\n";
        byte[] CODE128 = WoosimBarcode.createBarcode(WoosimBarcode.CODE128, 2, 60, true, barcode);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(512);
        byteStream.write(title1.getBytes()); byteStream.write(UPCA); byteStream.write(cmd_print);
        byteStream.write(title2.getBytes()); byteStream.write(UPCE); byteStream.write(cmd_print);
        byteStream.write(title3.getBytes()); byteStream.write(EAN13); byteStream.write(cmd_print);
        byteStream.write(title4.getBytes()); byteStream.write(EAN8); byteStream.write(cmd_print);
        byteStream.write(title5.getBytes()); byteStream.write(CODE39); byteStream.write(cmd_print);
        byteStream.write(title6.getBytes()); byteStream.write(ITF); byteStream.write(cmd_print);
        byteStream.write(title7.getBytes()); byteStream.write(CODEBAR); byteStream.write(cmd_print);
        byteStream.write(title8.getBytes()); byteStream.write(CODE93); byteStream.write(cmd_print);
        byteStream.write(title9.getBytes()); byteStream.write(CODE128); byteStream.write(cmd_print);

        sendData(WoosimCmd.initPrinter());
        sendData(byteStream.toByteArray());
    }

    public void print2DBarcode(View v) throws IOException {
        final byte[] barcode = {0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x30};
        final byte[] cmd_print = WoosimCmd.printData();
        final String title1 = "PDF417 2D Barcode\r\n";
        byte[] PDF417 = WoosimBarcode.create2DBarcodePDF417(2, 3, 4, 2, false, barcode);
        final String title2 = "DATAMATRIX 2D Barcode\r\n";
        byte[] dataMatrix = WoosimBarcode.create2DBarcodeDataMatrix(0, 0, 6, barcode);
        final String title3 = "QR-CODE 2D Barcode\r\n";
        byte[] QRCode = WoosimBarcode.create2DBarcodeQRCode(0, (byte)0x4d, 5, barcode);
        final String title4 = "Micro PDF417 2D Barcode\r\n";
        byte[] microPDF417 = WoosimBarcode.create2DBarcodeMicroPDF417(2, 2, 0, 2, barcode);
        final String title5 = "Truncated PDF417 2D Barcode\r\n";
        byte[] truncPDF417 = WoosimBarcode.create2DBarcodeTruncPDF417(2, 3, 4, 2, false, barcode);
        // Maxicode can be printed only with RX version
        final String title6 = "Maxicode 2D Barcode\r\n";
        final byte[] mxcode = {0x41,0x42,0x43,0x44,0x45,0x31,0x32,0x33,0x34,0x35,0x61,0x62,0x63,0x64,0x65};
        byte[] maxCode = WoosimBarcode.create2DBarcodeMaxicode(4, mxcode);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(512);
        byteStream.write(title1.getBytes()); byteStream.write(PDF417); byteStream.write(cmd_print);
        byteStream.write(title2.getBytes()); byteStream.write(dataMatrix); byteStream.write(cmd_print);
        byteStream.write(title3.getBytes()); byteStream.write(QRCode); byteStream.write(cmd_print);
        byteStream.write(title4.getBytes()); byteStream.write(microPDF417); byteStream.write(cmd_print);
        byteStream.write(title5.getBytes()); byteStream.write(truncPDF417); byteStream.write(cmd_print);
        byteStream.write(title6.getBytes()); byteStream.write(maxCode); byteStream.write(cmd_print);

        sendData(WoosimCmd.initPrinter());
        sendData(byteStream.toByteArray());
    }

    public void printGS1Databar(View v) throws IOException {
        final byte[] data = {0x30,0x30,0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x30};
        final byte[] cmd_print = WoosimCmd.printData();
        final String title0 = "GS1 Databar type0\r\n";
        byte[] gs0 = WoosimBarcode.createGS1Databar(0, 2, data);
        final String title1 = "GS1 Databar type1\r\n";
        byte[] gs1 = WoosimBarcode.createGS1Databar(1, 2, data);
        final String title2 = "GS1 Databar type2\r\n";
        byte[] gs2 = WoosimBarcode.createGS1Databar(2, 2, data);
        final String title3 = "GS1 Databar type3\r\n";
        byte[] gs3 = WoosimBarcode.createGS1Databar(3, 2, data);
        final String title4 = "GS1 Databar type4\r\n";
        byte[] gs4 = WoosimBarcode.createGS1Databar(4, 2, data);
        final String title5 = "GS1 Databar type5\r\n";
        final byte[] data5 = {0x5b,0x30,0x31,0x5d,0x39,0x30,0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x30,0x38,
                0x5b,0x33,0x31,0x30,0x33,0x5d,0x30,0x31,0x32,0x32,0x33,0x33};
        byte[] gs5 = WoosimBarcode.createGS1Databar(5, 2, data5);
        final String title6 = "GS1 Databar type6\r\n";
        final byte[] data6 = {0x5b,0x30,0x31,0x5d,0x39,0x30,0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x30,0x38,
                0x5b,0x33,0x31,0x30,0x33,0x5d,0x30,0x31,0x32,0x32,0x33,0x33,
                0x5b,0x31,0x35,0x5d,0x39,0x39,0x31,0x32,0x33,0x31};
        byte[] gs6 = WoosimBarcode.createGS1Databar(6, 4, data6);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(512);
        byteStream.write(title0.getBytes()); byteStream.write(gs0); byteStream.write(cmd_print);
        byteStream.write(title1.getBytes()); byteStream.write(gs1); byteStream.write(cmd_print);
        byteStream.write(title2.getBytes()); byteStream.write(gs2); byteStream.write(cmd_print);
        byteStream.write(title3.getBytes()); byteStream.write(gs3); byteStream.write(cmd_print);
        byteStream.write(title4.getBytes()); byteStream.write(gs4); byteStream.write(cmd_print);
        byteStream.write(title5.getBytes()); byteStream.write(gs5); byteStream.write(cmd_print);
        byteStream.write(title6.getBytes()); byteStream.write(gs6); byteStream.write(cmd_print);

        sendData(WoosimCmd.initPrinter());
        sendData(byteStream.toByteArray());
    }

    public void setMSRDoubleTrackMode(View v) {
        clearMSRInfo();
        sendData(WoosimCmd.MSR_doubleTrackMode());
        mWoosim.clearRcvBuffer();
    }

    public void setMSRTripleTrackMode(View v) {
        clearMSRInfo();
        sendData(WoosimCmd.MSR_tripleTrackMode());
        mWoosim.clearRcvBuffer();
    }

    public void cancelMSRMode(View v) {
        sendData(WoosimCmd.MSR_exit());
    }

    public void clearMSRInfo(View v) {
        clearMSRInfo();
    }

    private void clearMSRInfo() {
        mTrack1View.setText("");
        mTrack2View.setText("");
        mTrack3View.setText("");
    }
}
