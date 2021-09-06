package com.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adapter.files.InactiveRecycleAdapter;
import com.roaddo.store.ListOfDocumentActivity;
import com.roaddo.store.MainActivity;
import com.roaddo.store.R;
import com.roaddo.store.RestaurantDetailActivity;
import com.roaddo.store.SetAvailabilityActivity;
import com.roaddo.store.SetWorkingHoursActivity;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MaterialRippleLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 19-06-2017.
 */

public class InactiveFragment extends Fragment implements InactiveRecycleAdapter.OnItemClickList {

    public int totalVehicles = 0;
    View view;
    ArrayList<HashMap<String, String>> list;
    InactiveRecycleAdapter inactiveRecycleAdapter;
    GeneralFunctions generalFunc;
    MainActivity mainActivity;

    boolean isRestaurantDetailProcess = false;

    boolean isdocprogress = false;
    boolean isCompanyStateprogress = false;
    boolean isdriveractive = false;
    boolean isavailable = false;
    JSONObject userProfileJsonObj;
    String app_type = "";
    String iCompanyId = "";

    MButton btn_type2;
    int submitBtnId;
    boolean isbtnClick = false;
    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_inactive, container, false);

        mainActivity = (MainActivity) getActivity();
        generalFunc = mainActivity.generalFunc;

        mRecyclerView = (RecyclerView) view.findViewById(R.id.inActiveRecyclerView);
        btn_type2 = ((MaterialRippleLayout) view.findViewById(R.id.btn_type2)).getChildView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);

        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        iCompanyId = generalFunc.getJsonValueStr("iCompanyId", userProfileJsonObj);
        app_type = generalFunc.getJsonValueStr("APP_TYPE", userProfileJsonObj);

        submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);

        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_CHECK_ACC_STATUS"));
        btn_type2.setOnClickListener(new setOnClickList());
        return view;
    }

    private void setData() {
        list = new ArrayList<>();
        HashMap<String, String> map1 = new HashMap<String, String>();
        map1.put("title", generalFunc.retrieveLangLBl("Registration Successful", "LBL_REGISTRATION_SUCCESS"));
        map1.put("msg", "");
        map1.put("btn", "");
        map1.put("line", "start");
        map1.put("state", "true");
        list.add(map1);


        HashMap<String, String> map2 = new HashMap<String, String>();
        if (!isRestaurantDetailProcess) {
            map2.put("title", generalFunc.retrieveLangLBl("Restaurant Details", "LBL_RESTAURANT_DETAILS"));
            map2.put("msg", generalFunc.retrieveLangLBl("Please provide us your restaurant address and contact person details", "LBL_PROVIDE_US_RESTAURANT_DETAIL"));
            map2.put("btn", generalFunc.retrieveLangLBl("ADD INFORMATION", "LBL_ADD_INFORMATION"));
            map2.put("line", "two");
            map2.put("state", isRestaurantDetailProcess + "");

        } else {
            map2.put("title", generalFunc.retrieveLangLBl("Restaurant Details Upload Successful", "LBL_RESTAURANT_DETAILS_UPLOAD_SUCCESSFUL"));
            map2.put("msg", "");
            map2.put("btn", "");
            map2.put("line", "two");
            map2.put("state", isRestaurantDetailProcess + "");
        }
        list.add(map2);


        HashMap<String, String> map3 = new HashMap<String, String>();
        if (!isdocprogress) {
            map3.put("title", generalFunc.retrieveLangLBl(" Upload Documents", "LBL_UPLOAD_DOCUMENTS"));
            map3.put("msg", generalFunc.retrieveLangLBl("Please provide your Food Service License or Certificate", "LBL_FOOD_SERVICE_LICENSE"));
            map3.put("btn", generalFunc.retrieveLangLBl("Upload Food Certificate", "LBL_UPLOAD_FOOD_CERTIFICATE"));
            map3.put("line", "three");
            map3.put("state", isdocprogress + "");

        } else {
            map3.put("title", generalFunc.retrieveLangLBl("Upload your Documents Successful", "LBL_UPLOADDOC_SUCCESS"));
            map3.put("msg", "");
            map3.put("btn", "");
            map3.put("line", "three");
            map3.put("state", isdocprogress + "");
        }
        list.add(map3);


        HashMap<String, String> map4 = new HashMap<String, String>();
        if (!isCompanyStateprogress) {

            map4.put("title", generalFunc.retrieveLangLBl("Set Working Hours", "LBL_SET_WORKING_HOURS"));
            map4.put("msg", generalFunc.retrieveLangLBl("Setup your business timings to fulfill your online orders", "LBL_BUSINESS_TIMING"));
            map4.put("btn", generalFunc.retrieveLangLBl("SET TIMINGS", "LBL_SET_TIMING"));
            map4.put("line", "four");
            map4.put("state", isCompanyStateprogress + "");

        } else {
            map4.put("title", generalFunc.retrieveLangLBl("Setup your business timings added successfully.", "LBL_SETUP_BUSINESS_TIMING_ADDED"));
            map4.put("msg", "");
            map4.put("btn", "");
            map4.put("line", "four");
            map4.put("state", isCompanyStateprogress + "");
        }
        list.add(map4);

       /* if (app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {

            if (!isavailable) {
                HashMap<String, String> map5 = new HashMap<String, String>();
                map5.put("title", generalFunc.retrieveLangLBl(" Add your availability", "LBL_ADD_YOUR_AVAILABILITY"));
                map5.put("msg", generalFunc.retrieveLangLBl("Add your availability for scheduled booking requests ", "LBL_ADD_AVAILABILITY_DOC_NOTE"));
                map5.put("btn", generalFunc.retrieveLangLBl("", "LBL_SET_AVAILABILITY_TXT"));
                map5.put("line", "five");
                map5.put("state", isavailable + "");
                list.add(map5);

            } else {
                HashMap<String, String> map5 = new HashMap<String, String>();
                map5.put("title", generalFunc.retrieveLangLBl("Availability set successfully", "LBL_AVAILABILITY_ADD_SUCESS_MSG"));
                map5.put("msg", "");
                map5.put("btn", "");
                map5.put("line", "five");
                map5.put("state", isavailable + "");
                list.add(map5);
            }

            if (isdriveractive) {
                HashMap<String, String> map6 = new HashMap<String, String>();
                map6.put("title", generalFunc.retrieveLangLBl("", "LBL_ADMIN_APPROVE"));
                map6.put("msg", "");
                map6.put("btn", "");
                map6.put("line", "end");
                map6.put("state", isdriveractive + "");
                list.add(map6);

            } else {
                HashMap<String, String> map6 = new HashMap<String, String>();
                map6.put("title", generalFunc.retrieveLangLBl("Waiting for admin's approval", "LBL_WAIT_ADMIN_APPROVE"));
                map6.put("msg", generalFunc.retrieveLangLBl("We will check your provided information and get back to you soon.", "LBL_WAIT_ADMIN_APPROVE_NOTE"));
                map6.put("btn", "");
                map6.put("line", "end");
                map6.put("state", isdriveractive + "");
                list.add(map6);
            }
        } else {*/
            if (isdriveractive) {
                HashMap<String, String> map5 = new HashMap<String, String>();
                map5.put("title", generalFunc.retrieveLangLBl("", "LBL_ADMIN_APPROVE"));
                map5.put("msg", "");
                map5.put("btn", "");
                map5.put("line", "end");
                map5.put("state", isdriveractive + "");
                list.add(map5);

            } else {
                HashMap<String, String> map5 = new HashMap<String, String>();
                map5.put("title", generalFunc.retrieveLangLBl("Waiting for admin's approval", "LBL_WAIT_ADMIN_APPROVE"));
                map5.put("msg", generalFunc.retrieveLangLBl("We will check your provided information and get back to you soon.", "LBL_WAIT_ADMIN_APPROVE_NOTE"));
                map5.put("btn", "");
                map5.put("line", "end");
                map5.put("state", isdriveractive + "");
                list.add(map5);
            }
        // }

        inactiveRecycleAdapter = new InactiveRecycleAdapter(mainActivity.getActContext(), list, mainActivity.generalFunc);
        mRecyclerView.setAdapter(inactiveRecycleAdapter);
        inactiveRecycleAdapter.setOnItemClickList(this);
    }

    @Override
    public void onItemClick(int position) {
        Utils.hideKeyboard(getActivity());
        if (position == 1) {
            Bundle bn = new Bundle();
            bn.putString("PAGE_TYPE", "Company"); //Driver
            bn.putString("iDriverVehicleId", "");
            bn.putString("doc_file", "");
            bn.putString("iDriverVehicleId", "");
            bn.putString("IS_OPEN_FROM_STEPPERS", "Yes");
            new StartActProcess(mainActivity.getActContext()).startActWithData(RestaurantDetailActivity.class, bn);
        } else if (position == 2) {
            Bundle bn = new Bundle();
            bn.putString("PAGE_TYPE", "Company"); //Driver
            bn.putString("iDriverVehicleId", "");
            bn.putString("doc_file", "");
            bn.putString("iDriverVehicleId", "");
            new StartActProcess(mainActivity.getActContext()).startActWithData(ListOfDocumentActivity.class, bn);
        } else if (position == 3) {
            Bundle bn = new Bundle();

            new StartActProcess(getActivity()).startActWithData(SetWorkingHoursActivity.class, bn);


        } else if (position == 4) {
            new StartActProcess(getActivity()).startAct(SetAvailabilityActivity.class);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isbtnClick) {
            getDriverStateDetails();
        }
    }

    public void getDriverStateDetails() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getCompanyStates");
        parameters.put("iCompanyId", iCompanyId);
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mainActivity.getActContext(), parameters);
        exeWebServer.setLoaderConfig(mainActivity.getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                JSONObject responseObj=generalFunc.getJsonObject(responseString);
                if (responseObj != null && !responseObj.equals("")) {
                    boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);

                    if (isDataAvail == true) {

                        if (generalFunc.getJsonValueStr("IS_COMPANY_DETAIL_COMPLETED", responseObj).equalsIgnoreCase("Yes")) {
                            isRestaurantDetailProcess = true;
                        } else {
                            isRestaurantDetailProcess = false;
                        }

                        if (generalFunc.getJsonValueStr("IS_DOCUMENT_PROCESS_COMPLETED", responseObj).equalsIgnoreCase("Yes")) {
                            isdocprogress = true;

                        } else {
                            isdocprogress = false;

                        }

                        if (generalFunc.getJsonValueStr("IS_WORKING_HOURS_COMPLETED", responseObj).equalsIgnoreCase("Yes")) {
                            isCompanyStateprogress = true;
                        } else {
                            isCompanyStateprogress = false;
                        }

                        if (generalFunc.getJsonValueStr("IS_COMPANY_STATE_ACTIVATED", responseObj).equalsIgnoreCase("yes")) {
                            isdriveractive = true;
                        } else {
                            isdriveractive = false;
                        }

                        if (generalFunc.getJsonValueStr("IS_DRIVER_MANAGE_TIME_AVAILABLE", responseObj).equalsIgnoreCase("yes")) {
                            isavailable = true;
                        } else {
                            isavailable = false;
                        }

                        totalVehicles = generalFunc.parseIntegerValue(0, generalFunc.getJsonValueStr("TotalVehicles", responseObj));

                        if (app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                            if (isRestaurantDetailProcess && isdocprogress && isCompanyStateprogress && isdriveractive /*&& isavailable*/) {
                                setData();
                                if (!isbtnClick) {
                                    MyApp.getInstance().restartWithGetDataApp();
                                    return;
                                } else {
                                    handleDailog();
                                    return;

                                }
                            }
                        } else {
                            if (isRestaurantDetailProcess && isdocprogress && isCompanyStateprogress && isdriveractive) {
                                setData();
                                if (!isbtnClick) {
                                    MyApp.getInstance().restartWithGetDataApp();
                                    return;
                                } else {

                                    handleDailog();
                                    return;

                                }
                            }

                        }
                        if (isbtnClick) {
                            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_DRIVER_STATUS_INCOMPLETE"));
                            isbtnClick = false;
                        }

                        setData();

                    } else {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                    }
                }
            }
        });
        exeWebServer.execute();
    }

    public void handleDailog() {
        final GenerateAlertBox generateAlertBox = new GenerateAlertBox(getActivity());
        generateAlertBox.setCancelable(false);
        generateAlertBox.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_DRIVER_STATUS_COMPLETE"));
        generateAlertBox.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {
                generateAlertBox.closeAlertBox();
                MyApp.getInstance().restartWithGetDataApp();
            }
        });

        //generateAlertBox.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_RETRY_TXT"));
        generateAlertBox.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));

        generateAlertBox.showAlertBox();
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == submitBtnId) {
                isbtnClick = true;

                getDriverStateDetails();

            }

        }
    }
}
