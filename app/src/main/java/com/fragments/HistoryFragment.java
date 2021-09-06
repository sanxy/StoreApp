package com.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.adapter.files.HistoryRecycleAdapter;
import com.roaddo.store.OrderHistoryDetailsActivity;
import com.roaddo.store.R;
import com.datepicker.files.SlideDateTimeListener;
import com.datepicker.files.SlideDateTimePicker;
import com.dialogs.OpenListView;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;
import com.view.calendarview.CustomCalendarView;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class HistoryFragment extends Fragment implements HistoryRecycleAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    View view;
    public GeneralFunctions generalFunc;
    public String userProfileJson;
    public MTextView earningFareHTxt;
    public MTextView earningFareVTxt;
    public MTextView totalOrderHTxt;
    public MTextView totalOrderVTxt;
    MTextView titleTxt;
    ImageView backImgView;
    MaterialEditText fromDateEditBox;
    MaterialEditText toDateEditBox;
    ProgressBar loading_history;
    boolean mIsLoading = false;
    boolean isNextPageAvailable = false;
    MTextView noOrdersTxt;
    public MTextView avgRatingCalcTxt;
    RecyclerView historyRecyclerView;
    ErrorView errorView;
    String next_page_str = "";
    HistoryRecycleAdapter historyRecyclerAdapter;
    ArrayList<HashMap<String, String>> listData = new ArrayList<>();
    String fromSelectedTime = "";
    String toSelectedTime = "";
    String previousHeaderDate = "";
    Date fromDateDay = null;
    Date toDateDay = null;

    LinearLayout filterArea;
    MTextView filterTxt;
    public int subFilterPosition = 0;
    String selSubFilterType = "";
    ArrayList<HashMap<String, String>> subFilterlist;
    CustomCalendarView calendar_view;
    private LinearLayout calContainerView;
    private LinearLayout mainContent;
    private View convertView = null;
    String SELECTED_DATE = "";

    int HISTORYDETAILS = 1;
    int HISTORYCLICK = -1;

    NestedScrollView nestedScrollView;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_history, container, false);
        Logger.d("STATE","onCreateView");
        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

        titleTxt = (MTextView) view.findViewById(R.id.titleTxt);
        backImgView = (ImageView) view.findViewById(R.id.backImgView);

        filterArea = (LinearLayout) view.findViewById(R.id.filterArea);
        filterTxt = (MTextView) view.findViewById(R.id.filterTxt);


        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        backImgView.setVisibility(View.GONE);
        fromDateEditBox = (MaterialEditText) view.findViewById(R.id.fromDateEditBox);
        toDateEditBox = (MaterialEditText) view.findViewById(R.id.toDateEditBox);
        loading_history = (ProgressBar) view.findViewById(R.id.loading_history);
        historyRecyclerView = (RecyclerView) view.findViewById(R.id.historyRecyclerView);
        noOrdersTxt = (MTextView) view.findViewById(R.id.noOrdersTxt);
        avgRatingCalcTxt = (MTextView) view.findViewById(R.id.avgRatingCalcTxt);
        errorView = (ErrorView) view.findViewById(R.id.errorView);
        calContainerView = (LinearLayout) view.findViewById(R.id.calContainerView);
        mainContent = (LinearLayout) view.findViewById(R.id.mainContent);
        mainContent.setVisibility(View.GONE);
        earningFareHTxt = (MTextView) view.findViewById(R.id.earningFareHTxt);
        earningFareVTxt = (MTextView) view.findViewById(R.id.earningFareVTxt);
        totalOrderHTxt = (MTextView) view.findViewById(R.id.totalOrderHTxt);
        totalOrderVTxt = (MTextView) view.findViewById(R.id.totalOrderVTxt);
        listData.clear();
        historyRecyclerAdapter = new HistoryRecycleAdapter(getActContext(), listData, generalFunc, false);
        historyRecyclerView.setAdapter(historyRecyclerAdapter);
        historyRecyclerAdapter.setOnItemClickListener(this);

        backImgView.setOnClickListener(new setOnClickList());
        filterArea.setOnClickListener(new setOnClickList());

        fromDateEditBox.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.mipmap.ic_calendar_history, 0);
        toDateEditBox.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.mipmap.ic_calendar_history, 0);

        addCalenderView();
        setView(false);

      /*  long fromDateMillis = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
        Time fromDate = new Time();
        fromDate.set(fromDateMillis);

        fromDateDay = Utils.convertStringToDate("ddMMyyyy", getDateFromMilliSec(fromDateMillis, "ddMMyyyy"));

        fromSelectedTime = getDateFromMilliSec(fromDateMillis, "yyyy-MM-dd", Locale.ENGLISH);
        fromDateEditBox.setText(getDateFromMilliSec(fromDateMillis, "dd MMM yyyy"));

        long toDateMillis = System.currentTimeMillis();
        Time toDate = new Time();
        toDate.set(toDateMillis);



        toDateDay = Utils.convertStringToDate("ddMMyyyy", getDateFromMilliSec(toDateMillis, "ddMMyyyy"));
        toSelectedTime = getDateFromMilliSec(toDateMillis, "yyyy-MM-dd", Locale.ENGLISH);
        toDateEditBox.setText(getDateFromMilliSec(toDateMillis, "dd MMM yyyy"));

        fromDateEditBox.getLabelFocusAnimator().start();
        toDateEditBox.getLabelFocusAnimator().start();*/

        nestedScrollView = view.findViewById(R.id.nestedScrollView);

        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {


            if (calendar_view != null) {
                showHideCalender(false);
            }

            if (v.getChildAt(v.getChildCount() - 1) != null) {

                if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                        scrollY > oldScrollY) {

                    int visibleItemCount = historyRecyclerView.getLayoutManager().getChildCount();
                    int totalItemCount = historyRecyclerView.getLayoutManager().getItemCount();
                    int firstVisibleItemPosition = ((LinearLayoutManager) historyRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                    int lastInScreen = firstVisibleItemPosition + visibleItemCount;
                    Logger.d("SIZEOFLIST", "::" + lastInScreen + "::" + totalItemCount + "::" + isNextPageAvailable);
                    if ((lastInScreen == totalItemCount) && !(mIsLoading) && isNextPageAvailable) {
                        mIsLoading = true;
                        historyRecyclerAdapter.addFooterView();
                        getPastOrders(true, fromSelectedTime, toSelectedTime);
                    } else if (!isNextPageAvailable) {
                        historyRecyclerAdapter.removeFooterView();
                    }
                }
            }
        });

        removeInput();
        setLabels();

        earningFareVTxt.setText("--");
        totalOrderVTxt.setText("--");


        // getPastOrders(false, fromSelectedTime, toSelectedTime);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d("STATE","onResume");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.d("STATE","onViewCreated");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Logger.d("STATE","onViewStateRestored");
    }

    public void setView(boolean callApi) {

        if (callApi)
        {
            if (Utils.checkText(SELECTED_DATE)){
                mainContent.setVisibility(View.GONE);
                getPastOrders(false, fromSelectedTime, toSelectedTime);
            }
        }

        showHideCalender(false);
        calendar_view.setTitleTextColor(Color.parseColor("#141414"));
        calendar_view.setLeftImage(/*generalFunc.isRTLmode() ? */R.drawable.ic_left_arrow_circle/* : R.drawable.ic_right_arrow_circle*/);
        calendar_view.setRightImage(/*generalFunc.isRTLmode() ? */R.drawable.ic_right_arrow_circle /*: R.drawable.ic_left_arrow_circle*/);
        calendar_view.setRightImageTint(getResources().getColor(R.color.appThemeColor_1));
        calendar_view.setLeftImageTint(getResources().getColor(R.color.appThemeColor_1));

    }

    public void setLabels() {
        ((MTextView) view.findViewById(R.id.avgRatingTxt)).setText(generalFunc.retrieveLangLBl("Avg. Rating", "LBL_AVG_RATING"));
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_EARNING_HISTORY"));
        fromDateEditBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_From"));
        toDateEditBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_TO"));

        earningFareHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TOTAL_EARNINGS"));
        totalOrderHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TOTAL_ORDERS"));
    }

    public String getDateFromMilliSec(long dateInMillis, String dateFormat) {
//        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);


        String convertdate = "";
        Locale locale = new Locale(generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        SimpleDateFormat original_formate = new SimpleDateFormat(dateFormat);
        String dateString = original_formate.format(new Date(dateInMillis));
        SimpleDateFormat date_format = new SimpleDateFormat(dateFormat, locale);

        try {
            Date datedata = original_formate.parse(dateString);
            convertdate = date_format.format(datedata);
        } catch (ParseException e) {
            e.printStackTrace();
            Logger.d("getDateFormatedType", "::" + e.toString());
        }


        return convertdate;
    }

    public String getDateFromMilliSec(long dateInMillis, String dateFormat, Locale locale) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, locale);

        String dateString = formatter.format(new Date(dateInMillis));

        return dateString;
    }


    public void removeInput() {
        Utils.removeInput(fromDateEditBox);
        Utils.removeInput(toDateEditBox);

        fromDateEditBox.setOnTouchListener(new setOnTouchList());
        toDateEditBox.setOnTouchListener(new setOnTouchList());

        fromDateEditBox.setOnClickListener(new setOnClickList());
        toDateEditBox.setOnClickListener(new setOnClickList());

    }

    public Context getActContext() {
        return getActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

        }
    }

    public void openFromDateSelection() {
        new SlideDateTimePicker.Builder(getActivity().getSupportFragmentManager())
                .setListener(new SlideDateTimeListener() {
                    @Override
                    public void onDateTimeSet(Date date) {


                        if (Utils.convertStringToDate("ddMMyyyy", getDateFromMilliSec(date.getTime(), "ddMMyyyy")).equals(toDateDay) || Utils.convertStringToDate("ddMMyyyy", getDateFromMilliSec(date.getTime(), "ddMMyyyy")).before(toDateDay)) {

                            fromDateDay = Utils.convertStringToDate("ddMMyyyy", getDateFromMilliSec(date.getTime(), "ddMMyyyy"));

                            String selectedDateTime = Utils.convertDateToFormat("yyyy-MM-dd HH:mm:ss", date);
                            String selectedDateTimeZone = Calendar.getInstance().getTimeZone().getID();

                            fromSelectedTime = Utils.convertDateToFormat("yyyy-MM-dd", date);

                            fromDateEditBox.setText(getDateFromMilliSec(date.getTime(), "dd MMM yyyy"));

                            getPastOrders(false, fromSelectedTime, toSelectedTime);
                        } else {
                            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_FROM_DATE_RESTRICT"));
                        }
                    }

                    @Override
                    public void onDateTimeCancel() {

                    }
                })
                .setTimePickerEnabled(false)
                .setInitialDate(new Date())
                .setMaxDate(new Date())
                .setIs24HourTime(false)
                .setIndicatorColor(getResources().getColor(R.color.appThemeColor_2))
                .build()
                .show();
    }

    public void openToDateSelection() {
        new SlideDateTimePicker.Builder(getActivity().getSupportFragmentManager())
                .setListener(new SlideDateTimeListener() {
                    @Override
                    public void onDateTimeSet(Date date) {

                        if (Utils.convertStringToDate("ddMMyyyy", getDateFromMilliSec(date.getTime(), "ddMMyyyy")).equals(fromDateDay) || Utils.convertStringToDate("ddMMyyyy", getDateFromMilliSec(date.getTime(), "ddMMyyyy")).after(fromDateDay)) {

                            toDateDay = Utils.convertStringToDate("ddMMyyyy", getDateFromMilliSec(date.getTime(), "ddMMyyyy"));

                            String selectedDateTime = Utils.convertDateToFormat("yyyy-MM-dd HH:mm:ss", date);
                            String selectedDateTimeZone = Calendar.getInstance().getTimeZone().getID();

                            toSelectedTime = Utils.convertDateToFormat("yyyy-MM-dd", date);

                            toDateEditBox.setText(getDateFromMilliSec(date.getTime(), "dd MMM yyyy"));

                            getPastOrders(false, fromSelectedTime, toSelectedTime);
                        } else {
                            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_TO_DATE_RESTRICT"));
                        }

                    }

                    @Override
                    public void onDateTimeCancel() {

                    }

                })
                .setTimePickerEnabled(false)
                .setInitialDate(new Date())
                .setMaxDate(new Date())
                .setIs24HourTime(false)
                .setIndicatorColor(getResources().getColor(R.color.appThemeColor_2))
                .build()
                .show();
    }


    private void setDate(Date date) {
        mainContent.setVisibility(View.GONE);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        String date_formatted = date_format.format(cal.getTime());
        SELECTED_DATE = date_formatted;
        Logger.d("DATE_SELECTED", "setDate" + date_formatted);
        showHideCalender(false);
        removeNextPageConfig();
        getPastOrders(false, fromSelectedTime, toSelectedTime);

    }

    private void addCalenderView() {
        LayoutInflater infalInflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = infalInflater.inflate(R.layout.ride_history_cal, null);
        calendar_view = (CustomCalendarView) convertView.findViewById(R.id.calendar_view);

        calendar_view.setCalendarEventListener(new CustomCalendarView.CalendarEventListener() {
            @Override
            public void onCalendarTitleViewClick() {
                if (calendar_view.findViewById(R.id.weekLayout).getVisibility() == View.VISIBLE) {
                    showHideCalender(false);
                } else {
                    showHideCalender(true);

                }
            }

            @Override
            public void onCalendarPreviousButtonClick() {

            }

            @Override
            public void onCalendarNextButtonClick() {

            }

            @Override
            public void onCalendarDateSelected(Date date) {
                Logger.d("DATE_SELECTED", "onCalendarDateSelected");
                setDate(date);
            }

            @Override
            public void onCalendarMonthChanged(Date date) {
                Logger.d("DATE_SELECTED", "onCalendarMonthChanged");
                setDate(date);
            }

            @Override
            public void onCalendarCurrentDayFound(Date date) {
                Logger.d("DATE_SELECTED", "onCalendarCurrentDayFound");
                setDate(date);
            }
        });

        calContainerView.addView(convertView);
    }

    public void showHideCalender(boolean show) {
        if (show) {
            calendar_view.setArrowImage(/*generalFunc.isRTLmode() ? */R.drawable.ic_sort_up /*: R.drawable.ic_left_arrow_circle*/);
            calendar_view.showDateSelectionView();
            calendar_view.setTitleLayoutBGColor(getContext().getResources().getColor(R.color.white));
            calendar_view.setWeekLayoutBGColor(getContext().getResources().getColor(R.color.white));
        } else {
            calendar_view.hideDateSelectionView();
            calendar_view.setArrowImage(/*generalFunc.isRTLmode() ? */R.drawable.ic_sort_down /*: R.drawable.ic_left_arrow_circle*/);
            calendar_view.setTitleLayoutBGColor(getContext().getResources().getColor(R.color.appThemeColor_bg_parent_1));
            calendar_view.setWeekLayoutBGColor(getContext().getResources().getColor(R.color.appThemeColor_bg_parent_1));
        }
    }

    @Override
    public void onItemClickList(View view, int position) {

        Bundle bundle = new Bundle();
        bundle.putSerializable("DATA_PARAMS", listData.get(position));
        HISTORYCLICK = position;
       /* bundle.putString("vDate", listData.get(position).get("vDate"));
        bundle.putString("TYPE", "" + HistoryRecycleAdapter.TYPE_HEADER);
        bundle.putString("vOrderNo", listData.get(position).get("vOrderNo"));
        bundle.putString("tOrderRequestDate_Org", listData.get(position).get("tOrderRequestDate_Org"));
        bundle.putString("tOrderRequestDate", listData.get(position).get("tOrderRequestDate"));
        bundle.putString("UseName", listData.get(position).get("UseName"));
        bundle.putString("TotalItems", listData.get(position).get("TotalItems"));
        bundle.putString("iStatus", listData.get(position).get("iStatus"));
        bundle.putString("fTotalGenerateFare", listData.get(position).get("fTotalGenerateFare"));
        bundle.putString("TYPE", "" + HistoryRecycleAdapter.TYPE_ITEM);
        bundle.putString("iOrderId", listData.get(position).get("iOrderId"));*/

        new StartActProcess(getActContext()).startActWithData(OrderHistoryDetailsActivity.class, bundle);
    }

    public void getPastOrders(boolean isLoadMore, String fromSelectedTime, String toSelectedTime) {

        if (isLoadMore == false) {
            Log.d("getPastOrders", "getPastOrders: 1");
            listData.clear();
            historyRecyclerAdapter.notifyDataSetChanged();
            isNextPageAvailable = false;
            mIsLoading = true;
        }

        Log.d("getPastOrders", "getPastOrders: 2");
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading_history.getVisibility() != View.VISIBLE && isLoadMore == false) {
            loading_history.setVisibility(View.VISIBLE);
        }

        noOrdersTxt.setVisibility(View.GONE);

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getOrderHistory");
        parameters.put("iGeneralUserId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);
        parameters.put("vFromDate", SELECTED_DATE);
       /* if (Utils.checkText(fromSelectedTime)) {
            parameters.put("dDateOrig", fromSelectedTime);
        }*/
        // parameters.put("vToDate", toSelectedTime);
        if (isLoadMore == true) {
            parameters.put("page", next_page_str);
        }
        parameters.put("vSubFilterParam", selSubFilterType);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(),false,generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            noOrdersTxt.setVisibility(View.GONE);
            JSONObject responseObj = generalFunc.getJsonObject(responseString);
            swipeRefreshLayout.setRefreshing(false);

            if (responseObj != null && !responseObj.equals("")) {

                closeLoader();

                if (generalFunc.checkDataAvail(Utils.action_str, responseObj) == true) {
                    String nextPage = generalFunc.getJsonValueStr("NextPage", responseObj);
                    String TotalOrder = generalFunc.getJsonValueStr("TotalOrder", responseObj);

                    String TotalEarning = generalFunc.getJsonValueStr("TotalEarning", responseObj);

                    earningFareVTxt.setText(TotalEarning.equals("") ? "--" : generalFunc.convertNumberWithRTL(TotalEarning));
                    totalOrderVTxt.setText(TotalOrder.equals("") ? "--" : generalFunc.convertNumberWithRTL(TotalOrder));
                    float floatrating = GeneralFunctions.parseFloatValue(0, generalFunc.getJsonValueStr("AvgRating", responseObj));
                    String AvgRating = generalFunc.convertNumberWithRTL("" + floatrating);

                    Log.d("AvgRating", "getPastOrders: " + AvgRating);

                    avgRatingCalcTxt.setText(AvgRating);

                    JSONArray arr_orders = generalFunc.getJsonArray(Utils.message_str, responseObj);


                    if (arr_orders != null) {
                        for (int i = 0; i < arr_orders.length(); i++) {
                            JSONObject obj_temp = generalFunc.getJsonObject(arr_orders, i);

                            String vDate = generalFunc.getJsonValueStr("vDate", obj_temp);

                          /*  if (!previousHeaderDate.equalsIgnoreCase(vDate)) {

                                HashMap<String, String> mapHeader = new HashMap<String, String>();
                                mapHeader.put("vDate", vDate);
                                mapHeader.put("TYPE", "" + HistoryRecycleAdapter.TYPE_HEADER);

                                listData.add(mapHeader);
                                previousHeaderDate = vDate;
                            }*/

                            JSONArray arr_date = generalFunc.getJsonArray(Utils.data_str, obj_temp);
                            for (int j = 0; j < arr_date.length(); j++) {
                                HashMap<String, String> map = new HashMap<String, String>();

                                JSONObject obj_date_temp = generalFunc.getJsonObject(arr_date, j);
                                map.put("iOrderId", generalFunc.getJsonValueStr("iOrderId", obj_date_temp));

                                String vOrderNo = generalFunc.getJsonValueStr("vOrderNo", obj_date_temp);
                                map.put("vOrderNo", vOrderNo);
                                map.put("vOrderNoConverted", generalFunc.convertNumberWithRTL(vOrderNo));


                                String tOrderRequestDate_Org = generalFunc.getJsonValueStr("tOrderRequestDate_Org", obj_date_temp);
                                map.put("tOrderRequestDate_Org", generalFunc.convertNumberWithRTL(tOrderRequestDate_Org));


                                map.put("ConvertedOrderRequestDate", generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(tOrderRequestDate_Org, Utils.OriginalDateFormate, CommonUtilities.OriginalDateFormate)));
                                map.put("ConvertedOrderRequestTime", generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(tOrderRequestDate_Org, Utils.OriginalDateFormate, CommonUtilities.OriginalTimeFormate)));

                                map.put("vAvgRating", "" + generalFunc.parseFloatValue(0, generalFunc.getJsonValueStr("vAvgRating", obj_temp)));

                                map.put("tOrderRequestDate", generalFunc.getJsonValueStr("tOrderRequestDate", obj_date_temp));
                                map.put("UseName", generalFunc.getJsonValueStr("UseName", obj_date_temp));

                                String TotalItems = generalFunc.getJsonValueStr("TotalItems", obj_date_temp);
                                map.put("TotalItems", TotalItems);
                                map.put("TotalItemsConverted", generalFunc.convertNumberWithRTL(TotalItems));

                                map.put("iStatus", generalFunc.getJsonValueStr("iStatus", obj_date_temp));

                                map.put("vService_BG_color", generalFunc.getJsonValueStr("vService_BG_color", obj_date_temp));
                                map.put("vService_TEXT_color", generalFunc.getJsonValueStr("vService_TEXT_color", obj_date_temp));


                                String EarningFare = generalFunc.getJsonValueStr("EarningFare", obj_date_temp);

                                map.put("vImage", generalFunc.getJsonValueStr("vImage", obj_date_temp));
                                map.put("vAvgRating", "" + generalFunc.parseFloatValue(0, generalFunc.getJsonValueStr("vAvgRating", obj_temp)));

                                map.put("EarningFare", EarningFare);
                                map.put("EarningFareConverted", generalFunc.convertNumberWithRTL(EarningFare));
                                map.put("iStatusCode", generalFunc.getJsonValueStr("iStatusCode", obj_date_temp));
                                map.put("LBL_AMT_GENERATE_PENDING", generalFunc.retrieveLangLBl("Earning amount pending", "LBL_AMT_GENERATE_PENDING"));
                                map.put("LBL_ITEMS", generalFunc.retrieveLangLBl("", "LBL_ITEMS"));

                                map.put("TYPE", "" + HistoryRecycleAdapter.TYPE_ITEM);

                                listData.add(map);
                            }

                        }
                        buildFilterTypes(responseObj);


                    }
                    if (!nextPage.equals("") && !nextPage.equals("0")) {
                        next_page_str = nextPage;
                        isNextPageAvailable = true;
                    } else {
                        removeNextPageConfig();
                    }

                    historyRecyclerAdapter.notifyDataSetChanged();
                    if (HISTORYCLICK != -1) {
                        historyRecyclerView.scrollToPosition(HISTORYCLICK);
                    }
                } else {
                    buildFilterTypes(responseObj);
                    avgRatingCalcTxt.setText("--");
                    if (listData.size() == 0) {
                        removeNextPageConfig();
                        noOrdersTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                        noOrdersTxt.setVisibility(View.VISIBLE);
                        earningFareVTxt.setText("--");
                        totalOrderVTxt.setText("--");
                    }
                }
            } else {
                if (isLoadMore == false) {
                    removeNextPageConfig();
                    generateErrorView();
                    earningFareVTxt.setText("--");
                    totalOrderVTxt.setText("--");
                    avgRatingCalcTxt.setText("--");
                }

            }
            mIsLoading = false;

            mainContent.setVisibility(View.VISIBLE);
            closeLoader();

        });
        exeWebServer.execute();
    }

    private void buildFilterTypes(JSONObject responseObj) {
        String eFilterSel = generalFunc.getJsonValueStr("eFilterSel", responseObj);

        JSONArray subFilterOptionArr = generalFunc.getJsonArray("subFilterOption", responseObj);

        if (subFilterOptionArr != null && subFilterOptionArr.length() > 0) {
            subFilterlist = new ArrayList<>();
            for (int i = 0; i < subFilterOptionArr.length(); i++) {
                JSONObject obj_temp = generalFunc.getJsonObject(subFilterOptionArr, i);
                HashMap<String, String> map = new HashMap<String, String>();
                String vTitle = generalFunc.getJsonValueStr("vTitle", obj_temp);
                map.put("vTitle", vTitle);
                String vSubFilterParam = generalFunc.getJsonValueStr("vSubFilterParam", obj_temp);
                map.put("vSubFilterParam", vSubFilterParam);

                if (vSubFilterParam.equalsIgnoreCase(eFilterSel)) {
                    selSubFilterType = eFilterSel;
                    filterTxt.setText(vTitle);
                    subFilterPosition = i;
                }
                subFilterlist.add(map);
            }
        }
    }

    public void removeNextPageConfig() {
        next_page_str = "";
        isNextPageAvailable = false;
        mIsLoading = false;
        historyRecyclerAdapter.removeFooterView();
    }

    public void closeLoader() {
        if (loading_history.getVisibility() == View.VISIBLE) {
            loading_history.setVisibility(View.GONE);
        }
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(() -> getPastOrders(false, fromSelectedTime, toSelectedTime));
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        setView(true);
    }

    public class setOnTouchList implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP && !view.hasFocus()) {
                view.performClick();
            }
            return true;
        }
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActivity());
            switch (view.getId()) {

                case R.id.fromDateEditBox:
                    openFromDateSelection();
                    break;
                case R.id.toDateEditBox:
                    openToDateSelection();
                    break;

                case R.id.filterArea:
                    BuildType();
                    break;

            }
        }
    }


    public void BuildType() {
        OpenListView.getInstance(getActContext(), generalFunc.retrieveLangLBl("Select Type", "LBL_SELECT_TYPE"), populateSubArrayList(), OpenListView.OpenDirection.BOTTOM, true, true, position -> {

            subFilterPosition = position;
            selSubFilterType = subFilterlist.get(position).get("vSubFilterParam");
            filterTxt.setText(subFilterlist.get(position).get("vTitle"));

            removeNextPageConfig();
            getPastOrders(false, fromSelectedTime, toSelectedTime);

        }).show(subFilterPosition, "vTitle");

    }

    private ArrayList<String> populateSubArrayList() {
        ArrayList<String> typeNameList = new ArrayList<>();

        for (int i = 0; i < subFilterlist.size(); i++) {
            typeNameList.add((subFilterlist.get(i).get("vTitle")));
        }
        return typeNameList;
    }
}
