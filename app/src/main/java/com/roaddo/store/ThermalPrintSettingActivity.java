package com.roaddo.store;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.general.files.Closure;
import com.general.files.CustomDialog;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.OpenMainProfile;
import com.general.files.SetGeneralData;
import com.general.files.thermalPrint.GenerateOrderBill;
import com.utils.Logger;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class ThermalPrintSettingActivity extends AppCompatActivity {
    MTextView titleTxt;
    ImageView backImgView;
    GeneralFunctions generalFunc;
    String userProfileJson = "";

    MTextView descTxt, txtStatus, txtStatusVal, txtAllowPrint, txtAutoPrint;
    MTextView connectTxt, disConnectTxt;
    ImageView allowAutoPrintInfo, autoPrintInfo;
    CheckBox cbAllowPrint, cbAllowAutoPrint;
    private MButton printSettingsBtn;
    boolean isConnected = false;

    private CustomDialog customDialog;
    public static final int REQUEST_COARSE_LOCATION = 200;
    private GenerateAlertBox currentAlertBox;
    private static String TAG = "---DeviceList";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermal_print_setting);

        init();
        setLabels();
        showConnectedStatusArea();
    }

    private void showConnectedStatusArea() {
        if (generalFunc.retrieveValue(Utils.THERMAL_PRINT_ALLOWED_KEY).equalsIgnoreCase("Yes")) {
            findViewById(R.id.statusArea).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.statusArea).setVisibility(View.GONE);
        }
    }

    private void init() {
        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);

        disConnectTxt = (MTextView) findViewById(R.id.disConnectTxt);
        connectTxt = (MTextView) findViewById(R.id.connectTxt);
        allowAutoPrintInfo = (ImageView) findViewById(R.id.allowAutoPrintInfo);
        autoPrintInfo = (ImageView) findViewById(R.id.autoPrintInfo);

        descTxt = (MTextView) findViewById(R.id.descTxt);
        txtAllowPrint = (MTextView) findViewById(R.id.txtAllowPrint);
        txtAutoPrint = (MTextView) findViewById(R.id.txtAutoPrint);
        txtStatus = (MTextView) findViewById(R.id.txtStatus);
        txtStatusVal = (MTextView) findViewById(R.id.txtStatusVal);
        cbAllowPrint = (CheckBox) findViewById(R.id.cbAllowPrint);
        cbAllowAutoPrint = (CheckBox) findViewById(R.id.cbAllowAutoPrint);

        printSettingsBtn = ((MaterialRippleLayout) findViewById(R.id.printSettingsBtn)).getChildView();
        printSettingsBtn.setId(Utils.generateViewId());

        allowAutoPrintInfo.setOnClickListener(new setOnClickList());
        autoPrintInfo.setOnClickListener(new setOnClickList());

        connectTxt.setOnClickListener(new setOnClickList());
        backImgView.setOnClickListener(new setOnClickList());
        disConnectTxt.setOnClickListener(new setOnClickList());
        printSettingsBtn.setOnClickListener(new setOnClickList());


        cbAllowPrint.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    findViewById(R.id.statusArea).setVisibility(View.VISIBLE);
                    findViewById(R.id.autoPrintArea).setVisibility(View.VISIBLE);

                    boolean nwStatus = MyApp.generateOrderBill != null && MyApp.btsocket != null;
                    if (isConnected != nwStatus) {
                        isConnected = MyApp.generateOrderBill != null && MyApp.btsocket != null;
                        reSetDetails(false);
                    }

                } else {
                    findViewById(R.id.statusArea).setVisibility(View.GONE);
                    findViewById(R.id.autoPrintArea).setVisibility(View.GONE);
                    cbAllowAutoPrint.setChecked(false);
                }
            }
        });

    }

    public Context getActContext() {
        return ThermalPrintSettingActivity.this;
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_T_PRINT_TITLE_TXT"));
        descTxt.setText(generalFunc.retrieveLangLBl("", "LBL_T_PRINT_DESC_TXT"));
        txtAllowPrint.setText(generalFunc.retrieveLangLBl("", "LBL_T_PRINT_ALLOW_TXT"));
        txtAutoPrint.setText(generalFunc.retrieveLangLBl("", "LBL_T_AUTO_PRINT_TXT"));
        printSettingsBtn.setText(generalFunc.retrieveLangLBl("", "LBL_T_PRINT_UPDATE_TXT"));
        txtStatus.setText(generalFunc.retrieveLangLBl("", "LBL_T_PRINTER_STATUS_TXT"));
        isConnected = MyApp.generateOrderBill != null && MyApp.btsocket != null;

        reSetDetails(true);

    }

    private void reSetDetails(boolean resetCheckBox) {

        if (resetCheckBox) {
            if (generalFunc.retrieveValue(Utils.THERMAL_PRINT_ALLOWED_KEY).equalsIgnoreCase("Yes")) {
                cbAllowPrint.setChecked(true);
                findViewById(R.id.autoPrintArea).setVisibility(View.VISIBLE);
            } else {
                cbAllowPrint.setChecked(false);
                findViewById(R.id.autoPrintArea).setVisibility(View.GONE);
            }


            if (generalFunc.retrieveValue(Utils.AUTO_PRINT_KEY).equalsIgnoreCase("Yes")) {
                cbAllowAutoPrint.setChecked(true);
            } else {
                cbAllowAutoPrint.setChecked(false);
            }
        }

        if (!isConnected) {
            connectTxt.setVisibility(View.VISIBLE);
            disConnectTxt.setVisibility(View.GONE);
            connectTxt.setText(generalFunc.retrieveLangLBl("", "LBL_T_PRINT_CONNECT_TXT"));
            txtStatusVal.setText(generalFunc.retrieveLangLBl("Dis Connected", "LBL_PRINTER_STATUS_DISCONNECTED"));
        } else {
            disConnectTxt.setVisibility(View.VISIBLE);
            connectTxt.setVisibility(View.GONE);
            disConnectTxt.setText(generalFunc.retrieveLangLBl("", "LBL_T_PRINT_DISCONNECT_TXT"));
            txtStatusVal.setText(generalFunc.retrieveLangLBl("Connected", "LBL_PRINTER_STATUS_CONNECTED"));
        }


    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActContext());

            int id = view.getId();
            if (id == printSettingsBtn.getId()) {
                printSettingsBtn.setEnabled(false);

                if (MyApp.btsocket != null && !cbAllowPrint.isChecked() && generalFunc.retrieveValue(Utils.THERMAL_PRINT_ALLOWED_KEY).equalsIgnoreCase("Yes")) {
                    if (currentAlertBox != null) {
                        currentAlertBox.closeAlertBox();
                        currentAlertBox = null;
                    }
                    currentAlertBox = generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_PRINTER_DISCONNECT_NOTE"/*LBL_T_PRINT_WARNING_TXT*/), generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"), generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"), buttonId -> {
                        if (buttonId == 0) {
                            currentAlertBox.closeAlertBox();
                            currentAlertBox = null;

                        } else if (buttonId == 1) {
                            currentAlertBox.closeAlertBox();
                            currentAlertBox = null;

                            confirmPrintSettings();
                        }
                    });
                } else {
                    confirmPrintSettings();
                }

                return;
            } else if (id == R.id.backImgView) {
//                onBackPressed();
                finish();
                return;
            } else if (id == R.id.allowAutoPrintInfo) {
                showMessage(true);
                return;
            } else if (id == R.id.autoPrintInfo) {
                showMessage(false);
                return;
            } else if (id == disConnectTxt.getId()) {
                disconnectPrinter();
                return;
            } else if (id == connectTxt.getId()) {
                connectBluetooth();

                return;
            }
        }
    }


    BluetoothAdapter bAdapter;
    private  void  connectBluetooth(){
        if (customDialog != null && customDialog.isShowing()) {
            return;
        }

        if (generalFunc.retrieveValue(Utils.THERMAL_PRINT_ENABLE_KEY).equalsIgnoreCase("Yes") && MyApp.btsocket == null) {

            if (ContextCompat.checkSelfPermission(getActContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions((Activity) getActContext(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_COARSE_LOCATION);
            } else {


                bAdapter = BluetoothAdapter.getDefaultAdapter();
                if(bAdapter == null)
                {
                    Toast.makeText(getApplicationContext(),"Bluetooth Not Supported",Toast.LENGTH_SHORT).show();
                }
                else{
                    if(!bAdapter.isEnabled()){
                        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),Utils.REQUEST_CONNECT_BT);
                    }else {
                        OpenPrinterList();
                    }
                }

            }
        }
    }

    private void showMessage(boolean isAllowAutoPrint) {
        final GenerateAlertBox generateAlertBox = new GenerateAlertBox(getActContext());
        generateAlertBox.setCancelable(false);
        generateAlertBox.setContentMessage("", generalFunc.retrieveLangLBl("", isAllowAutoPrint ? "LBL_AUTO_PRINT_NOTE" : "LBL_ALLOW_PRINT_NOTE"));
        generateAlertBox.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {
                generateAlertBox.closeAlertBox();
            }
        });
        generateAlertBox.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlertBox.showAlertBox();
    }

    private void confirmPrintSettings() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "updateThermalPrintStatus");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("eThermalAutoPrint", cbAllowAutoPrint.isChecked() ? "Yes" : "No");
        parameters.put("eThermalPrintEnable", cbAllowPrint.isChecked() ? "Yes" : "No");

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            printSettingsBtn.setEnabled(true);
            JSONObject responseObj = generalFunc.getJsonObject(responseString);
            if (responseObj != null && !responseObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);

                if (isDataAvail) {

                    if (!cbAllowPrint.isChecked() && MyApp.btsocket != null && generalFunc.retrieveValue(Utils.THERMAL_PRINT_ALLOWED_KEY).equalsIgnoreCase("Yes")) {
                        disConnectTxt.performClick();
                    }
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValueStr(Utils.message_str, responseObj));
                    JSONObject messageObj = generalFunc.getJsonObject(generalFunc.getJsonValueStr(Utils.message_str, responseObj));

                    new SetGeneralData(generalFunc, messageObj);
//                    setResult(RESULT_OK);
//                    backImgView.performClick();

                    showConnectedStatusArea();

                    String message = generalFunc.getJsonValueStr(Utils.message_str_one, responseObj);
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", Utils.checkText(message)?message:"LBL_INFO_UPDATED_TXT"));
                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }


    public void OpenPrinterList() {
        MyApp.generateOrderBill = GenerateOrderBill.getInstance();
        MyApp.generateOrderBill.init(this);

        if (customDialog != null) {
            customDialog.closeDialog(true);
        }

        customDialog = new CustomDialog(getActContext());
        customDialog.setDetails(generalFunc.retrieveLangLBl("Select Printer", "LBL_SELECT_PRINTER"), generalFunc.retrieveLangLBl("", "LBL_T_PRINTER_ALERT_TITLE_TXT"), null, generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"), false, R.drawable.ic_printer, true, true, 2);
        customDialog.setDirection(CustomDialog.OpenDirection.BOTTOM);
        customDialog.setRoundedViewBackgroundColor(R.color.appThemeColor_1);
        customDialog.setIconTintColor(R.color.white);
        customDialog.setBtnRadius(10);
        customDialog.setImgStrokWidth(10);
        customDialog.setTitleTxtColor(R.color.appThemeColor_1);
        customDialog.createDialog();
       /* customDialog.setNegativeButtonClick(new Closure() {
            @Override
            public void exec() {

            }
        });*/
        customDialog.setCloseDialogListener(new Closure() {
            @Override
            public void exec() {
                customDialog = null;
                isConnected = MyApp.generateOrderBill != null && MyApp.btsocket != null;
                Logger.d(TAG, "in setCloseDialogListener" + isConnected);
                reSetDetails(false);
            }
        });
        customDialog.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_COARSE_LOCATION:
            case Utils.REQUEST_ENABLE_BT:
            break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //  disconnectPrinter();
    }

    public void changeConnectionState() {
        isConnected = MyApp.generateOrderBill != null && MyApp.btsocket != null;
        reSetDetails(false);
    }

    private void disconnectPrinter() {
        try {

            if (customDialog != null) {
                customDialog.closeDialog(false);
            }


            if (MyApp.btsocket != null) {

                if (MyApp.outputStream != null) {
                    MyApp.outputStream.close();
                }

                if (MyApp.btsocket.isConnected())
                {
                    MyApp.btsocket.close();
                    Snackbar.make(connectTxt,generalFunc.retrieveLangLBl("", "LBL_PRINTER_DISCONNECTED"),Snackbar.LENGTH_LONG).show();
                }
                MyApp.btsocket = null;
            }
            changeConnectionState();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.REQUEST_CONNECT_BT && resultCode == Activity.RESULT_OK) {
            connectBluetooth();
        }else {
            Snackbar.make(connectTxt,generalFunc.retrieveLangLBl("", "LBL_ALLOW_BLUETOOTH"),Snackbar.LENGTH_LONG).show();
        }
    }
}
