package com.plugins.barcodeScanner;

import android.graphics.Color;
import android.text.InputType;
import android.text.Layout;
import android.text.method.SingleLineTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.widget.*;
import android.view.View;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.*;

import jpos.JposException;

/**
 * Created by dev01 on 1/2/17.
 */

public class BarCodeScanner extends CordovaPlugin {
    private static final String TAG = BarCodeScanner.class.getSimpleName();
    private final boolean DEBUG = true;

    private EditText textbox;
    private FrameLayout layout;
    private FrameLayout textboxContainer;

    private final String ACTION_CREATE = "create";
    private final String ACTION_SHOW = "show";
    private final String ACTION_HIDE = "hide";
    private final String ACTION_CLEAR = "clear";
    private final String METHOD_GET_CLAIMED = "getClaimed";


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        return;
    }

    @Override
    public boolean execute(final String action, final JSONArray args,
                           final CallbackContext callbackContext) throws JSONException {
        if (DEBUG) {
            Log.d(TAG, "execute(" + action + ", " + args + ", "
                    + callbackContext + ")");
        }

        if (action.equals(ACTION_CREATE)) {
            //callbackContext.error("Action is not matched");
            create(args);
            return true;
        } else if (action.equals(ACTION_SHOW)) {
            show(args);
            return true;
        } else if (action.equals(ACTION_HIDE)) {
            hide(args);
            return true;
        } else if (action.equals(ACTION_CLEAR)) {
            clear(args);
            return true;
        }

        return false;
    }

    private void create(JSONArray args) {
        JSONObject parametros = null;
        int x=0, y=0, width=0, height=0;
        try {
            parametros = args.getJSONObject(0);
            x = parametros.getInt("x");
            y = parametros.getInt("y");
            width = parametros.getInt("width");
            height = parametros.getInt("height");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        layout = (FrameLayout) webView.getView().getParent();

        final EditText EditText = new EditText(layout.getContext());
        //EditText.setBackgroundColor(Color.BLUE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        params.setMargins(x, y, width, height);
        EditText.setLayoutParams(params);
        EditText.setInputType(InputType.TYPE_NULL);//InputType.TYPE_TEXT_VARIATION_NORMAL |InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        EditText.setTransformationMethod(SingleLineTransformationMethod.getInstance());

        textbox = EditText;
        //textboxContainer = new FrameLayout(cordova.getActivity().getApplicationContext());
        //textboxContainer.addView(textbox);
        textbox.setVisibility(View.GONE);
        textbox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasfocus) {
                if (!hasfocus) {
                    cordova.getActivity().runOnUiThread(
                            new Runnable() {
                                public void run() {
                                    //textbox.setFocusableInTouchMode(true);
                                    textbox.clearFocus();
                                    textbox.setVisibility(View.GONE);
                                }
                            });
                }
            }
        });

        textbox.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {

                if (keyevent.getAction() == KeyEvent.ACTION_DOWN) {
                    try {
                        Log.d("BarCodeScanner[onkey]", "evento: " + keyevent.getAction() + "  keyCode:" + keyCode );//+ " char:" + (char) keyevent.getUnicodeChar());
                        Log.d("BarCodeScanner[onkey]", String.format("char: %c",(char)keyevent.getUnicodeChar()) );//+ " char:" + (char) keyevent.getUnicodeChar());keyevent.getUnicodeChar();
                    }catch (Exception e){e.printStackTrace();};

                }
                if ((keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (keyevent.getAction() == KeyEvent.ACTION_DOWN){


                        cordova.getActivity().runOnUiThread(
                            new Runnable() {
                                public void run() {
                                    String temptext = textbox.getText().toString();
                                    Log.d("BarCodeScanner", "Codigo escaneado: " + temptext);

                                    String script = String.format("window.plugins.barCodeScan.onChange('%s');", temptext);

                                    Log.d("BarCodeScanner", "ejecutando: " + script);
                                    webView.loadUrl("javascript:"+script);
                                    webView.loadUrl("javascript:window.plugins.barCodeScan.onBlur();");
                                    // Perform action on key press
                                    Toast.makeText(cordova.getActivity().getApplicationContext(), textbox.getText(), Toast.LENGTH_SHORT).show();
                                    textbox.setText("");
                                    textbox.setVisibility(View.GONE);
                                }
                            });

                    }


                    return true;
                }

                return false;
            }
        } );


        this.cordova.getActivity().runOnUiThread(
                new Runnable() {
                    public void run() {
                        layout.addView(textbox);
                    }
                }
        );
    }

    private void show(JSONArray args) {
        cordova.getActivity().runOnUiThread(
                new Runnable() {
                    public void run() {
                        textbox.setVisibility(View.VISIBLE);
                        //textbox.setFocusableInTouchMode(true);
                        textbox.requestFocus();
                        textbox.setText("");



                        webView.loadUrl("javascript:window.plugins.barCodeScan.onFocus();");
                    }
                }
        );
        //webView.loadUrl("javascript:BarCodeScan._onFocus();");

    }

    private void hide(JSONArray args) {
        //textbox.setText("");
        cordova.getActivity().runOnUiThread(
                new Runnable() {
                    public void run() {
                        //textbox.setFocusableInTouchMode(false);
                        textbox.clearFocus();
                        textbox.setVisibility(View.GONE);
                        textbox.setText("");
                        webView.loadUrl("javascript:window.plugins.barCodeScan.onBlur();");
                    }
                }
        );
    }

    private void clear(JSONArray args) {
        cordova.getActivity().runOnUiThread(
                new Runnable() {
                    public void run() {
                        textbox.setText("");
                    }
                });
    }


}
