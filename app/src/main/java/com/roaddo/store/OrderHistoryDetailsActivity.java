package com.roaddo.store;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;

import com.general.files.AppFunctions;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.squareup.picasso.Picasso;
import com.utils.Utils;
import com.view.MTextView;
import com.view.simpleratingbar.SimpleRatingBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class OrderHistoryDetailsActivity extends AppCompatActivity {

    LinearLayout billDetails;
    ArrayList<String> dataList = new ArrayList<>();
    GeneralFunctions generalFunc;

    ImageView backImgView;
    MTextView titleTxt;
    String iOrderId = "";
    MTextView billTitleTxt;
    View convertView = null;
    LinearLayout farecontainer;
    MTextView resturantAddressTxt, deliveryaddressTxt, resturantAddressHTxt, destAddressHTxt;

    MTextView deliverystatusTxt;
    ProgressBar loading_history;
    View contentView;
    HashMap<String, String> dataParams = null;
    private boolean isExpanded = false;
    private ArrayList<HashMap<String, String>> fareList = new ArrayList<>();

    MTextView paidviaTextH;
    LinearLayout cancelArea;
    LinearLayout deliveryCancelDetails;
    LinearLayout sourceLocCardArea;
    MTextView deliverycanclestatusTxt;
    MTextView oredrstatusTxt;
    ImageView restaurantImgView;

    int size;
    private String vImage;
    private String vAvgRating;
    private JSONObject userProfileJsonObj;
    private String SYSTEM_PAYMENT_FLOW;

    private String eTakeAway;
    private MTextView eTakeAwayOrderTxt;

    private JSONObject DeliveryPreferences;
    CardView viewPreferenceArea;
    MTextView viewPreferenceTxtView;

    NestedScrollView scrollContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history_details);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        size = (int) this.getResources().getDimension(R.dimen._55sdp);

        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        SYSTEM_PAYMENT_FLOW = generalFunc.getJsonValueStr("SYSTEM_PAYMENT_FLOW", userProfileJsonObj);

        scrollContainer = (NestedScrollView) findViewById(R.id.scrollContainer);
        billDetails = (LinearLayout) findViewById(R.id.billDetails);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        billTitleTxt = (MTextView) findViewById(R.id.billTitleTxt);
        farecontainer = (LinearLayout) findViewById(R.id.farecontainer);
        resturantAddressTxt = (MTextView) findViewById(R.id.resturantAddressTxt);
        resturantAddressHTxt = (MTextView) findViewById(R.id.resturantAddressHTxt);
        deliveryaddressTxt = (MTextView) findViewById(R.id.deliveryaddressTxt);
        destAddressHTxt = (MTextView) findViewById(R.id.destAddressHTxt);
        deliverystatusTxt = (MTextView) findViewById(R.id.deliverystatusTxt);

        eTakeAwayOrderTxt = (MTextView) findViewById(R.id.eTakeAwayOrderTxt);

        paidviaTextH = (MTextView) findViewById(R.id.paidviaTextH);
        sourceLocCardArea = (LinearLayout) findViewById(R.id.sourceLocCardArea);
        deliveryCancelDetails = (LinearLayout) findViewById(R.id.deliveryCancelDetails);
        cancelArea = (LinearLayout) findViewById(R.id.cancelArea);
        deliverycanclestatusTxt = (MTextView) findViewById(R.id.deliverycanclestatusTxt);
        oredrstatusTxt = (MTextView) findViewById(R.id.oredrstatusTxt);
        restaurantImgView = (ImageView) findViewById(R.id.restaurantImgView);

        contentView = findViewById(R.id.contentView);
        contentView.setVisibility(View.GONE);
        scrollContainer.setVisibility(View.GONE);

        viewPreferenceArea = (CardView) findViewById(R.id.viewPreferenceArea);
        viewPreferenceTxtView = (MTextView) findViewById(R.id.viewPreferenceTxtView);
        viewPreferenceArea.setOnClickListener(new setOnClickList());

        loading_history = (ProgressBar) findViewById(R.id.loading_history);

        backImgView.setOnClickListener(new setOnClickList());

        dataParams = (HashMap<String, String>) getIntent().getSerializableExtra("DATA_PARAMS");

        iOrderId = dataParams.get("iOrderId");
        titleTxt.setVisibility(View.VISIBLE);

        setLabel();

        getOrderDetails();

    }

    public void setLabel() {

        billTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_BILL_DETAILS"));
        destAddressHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DELIVERY_ADDRESS"));
        titleTxt.setText(generalFunc.retrieveLangLBl("RECEIPT", "LBL_RECEIPT_HEADER_TXT"));
        eTakeAwayOrderTxt.setText(generalFunc.retrieveLangLBl("Take Away Order", "LBL_TAKE_WAY_ORDER"));
    }

    public void getOrderDetails() {

        if (loading_history.getVisibility() != View.VISIBLE) {
            loading_history.setVisibility(View.VISIBLE);
        }

        contentView.setVisibility(View.GONE);
        scrollContainer.setVisibility(View.GONE);

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GetOrderDetailsRestaurant");
        parameters.put("iOrderId", iOrderId);
        parameters.put("UserType", Utils.userType);
        parameters.put("IS_FROM_HISTORY", "Yes");

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        //exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {


            closeLoader();
            JSONObject responseObj=generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {


                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);

                if (isDataAvail == true) {
                    contentView.setVisibility(View.VISIBLE);
                    scrollContainer.setVisibility(View.VISIBLE);

                    JSONObject messageObj = generalFunc.getJsonObject(Utils.message_str, responseObj);
                    DeliveryPreferences = generalFunc.getJsonObject("DeliveryPreferences",responseObj);

                    resturantAddressTxt.setText(generalFunc.getJsonValueStr("vRestuarantLocation", messageObj));
                    deliveryaddressTxt.setText(generalFunc.getJsonValueStr("DeliveryAddress", messageObj));
                    vImage = generalFunc.getJsonValueStr("companyImage", messageObj);
                    eTakeAway = generalFunc.getJsonValueStr("eTakeAway", messageObj);
                    boolean iseTakeAway = eTakeAway.equalsIgnoreCase("Yes") ? true : false;
                    eTakeAwayOrderTxt.setVisibility(iseTakeAway?View.VISIBLE:View.GONE);
                    sourceLocCardArea.setVisibility(iseTakeAway?View.GONE:View.VISIBLE);
                    String vAvgRating = generalFunc.getJsonValueStr("vAvgRating", messageObj);
                    if (vAvgRating.isEmpty()) vAvgRating="0.0";
                    ((SimpleRatingBar) findViewById(R.id.ratingBar)).setRating(Float.parseFloat(vAvgRating));
                    setImage();

                    boolean isPreference = generalFunc.getJsonValueStr("Enable", DeliveryPreferences).equalsIgnoreCase("Yes") ? true : false;
                    viewPreferenceArea.setVisibility(isPreference?View.VISIBLE:View.GONE);

                    ((MTextView)findViewById(R.id.orderNoHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_ORDER_NO_TXT"));
                    ((MTextView)findViewById(R.id.orderNoVTxt)).setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("vOrderNo", messageObj)));

                     ((MTextView)findViewById(R.id.orderDateVTxt)).setText(generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(generalFunc.getJsonValueStr("tOrderRequestDate_Org", messageObj), Utils.OriginalDateFormate, Utils.getDetailDateFormat(getActContext()))));


                    resturantAddressHTxt.setText(generalFunc.getJsonValueStr("vCompany", messageObj));
                    JSONArray itemListArr = null;
                    itemListArr = generalFunc.getJsonArray("itemlist", messageObj);
                    if (billDetails.getChildCount() > 0) {
                        billDetails.removeAllViewsInLayout();
                    }

                    if (itemListArr!=null) {
                        addItemDetailLayout(itemListArr);
                    }

                    String LBL_PAID_VIA = generalFunc.retrieveLangLBl("", "LBL_PAID_VIA");
                    String ePaymentOption = generalFunc.getJsonValueStr("ePaymentOption", messageObj);
                    if (ePaymentOption.equalsIgnoreCase("Cash")) {
                        ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.drawable.ic_cash_payment);
                        paidviaTextH.setText(LBL_PAID_VIA + " " + generalFunc.retrieveLangLBl("", "LBL_CASH_TXT"));
                    } else if (ePaymentOption.equalsIgnoreCase("Card")) {
                        if (Utils.checkText(SYSTEM_PAYMENT_FLOW) && !SYSTEM_PAYMENT_FLOW.equalsIgnoreCase("Method-1")) {
                            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.mipmap.ic_menu_wallet);
                            paidviaTextH.setText(generalFunc.retrieveLangLBl("", "LBL_PAID_VIA_WALLET"));
                        } else {
                            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.mipmap.ic_card_new);
                            paidviaTextH.setText(LBL_PAID_VIA + " " + generalFunc.retrieveLangLBl("", "LBL_CARD"));
                        }
                    }








                    JSONArray FareDetailsArr = null;
                    FareDetailsArr = generalFunc.getJsonArray("FareDetailsArr", messageObj);
                    addFareDetailLayout(FareDetailsArr);


                    deliverystatusTxt.setText(Html.fromHtml(generalFunc.getJsonValueStr("vStatusNew", messageObj)));

                    /*String orderMessage = generalFunc.getJsonValueStr("OrderMessage", messageObj);
                    orderMsgTxtView.setText(orderMessage);

                    if (orderMessage.equals("")) {
                        orderMsgTxtView.setVisibility(View.GONE);
                    } else {
                        orderMsgTxtView.setVisibility(View.VISIBLE);
                    }
*/
                    if (generalFunc.getJsonValueStr("iStatusCode", messageObj).equalsIgnoreCase("6") && generalFunc.getJsonValueStr("ePaid", messageObj).equals("Yes")) {
                        boolean ePaid = true;
                        ePaymentOption = generalFunc.getJsonValueStr("ePaymentOption", messageObj);
                        deliverystatusTxt.setVisibility(View.VISIBLE);
                        deliverystatusTxt.setText(Html.fromHtml(generalFunc.getJsonValueStr("OrderStatusValue", messageObj)));
                        findViewById(R.id.PayTypeArea).setVisibility(View.VISIBLE);

                    }else if (generalFunc.getJsonValueStr("iStatusCode", messageObj).equalsIgnoreCase("8")) {
                        deliveryCancelDetails.setVisibility(View.GONE);
                        deliverycanclestatusTxt.setText(generalFunc.getJsonValueStr("OrderStatustext", messageObj));
                        if (!generalFunc.getJsonValueStr("CancelOrderMessage", messageObj).equals("") && generalFunc.getJsonValueStr("CancelOrderMessage", messageObj) != null) {
                            deliveryCancelDetails.setVisibility(View.VISIBLE);
                            deliverycanclestatusTxt.setVisibility(View.GONE);
                            oredrstatusTxt.setVisibility(View.VISIBLE);
                            oredrstatusTxt.setText(generalFunc.getJsonValueStr("CancelOrderMessage", messageObj));
                        }
                    } else if (generalFunc.getJsonValueStr("iStatusCode", messageObj).equalsIgnoreCase("7")) {
                        deliveryCancelDetails.setVisibility(View.VISIBLE);
                        cancelArea.setVisibility(View.GONE);
                        if (!generalFunc.getJsonValueStr("CancelOrderMessage", messageObj).equals("") && generalFunc.getJsonValueStr("CancelOrderMessage", messageObj) != null) {
                            oredrstatusTxt.setVisibility(View.VISIBLE);
                            oredrstatusTxt.setText(generalFunc.getJsonValueStr("CancelOrderMessage", messageObj));
                        }

                    }  else {
//                        deliverystatusTxt.setVisibility(View.GONE);
                        findViewById(R.id.paymentMainArea).setVisibility(View.GONE);
                    }

                    deliverystatusTxt.setText(generalFunc.getJsonValueStr("vStatusNew", messageObj));


                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)), true);
                }
            } else {
                generalFunc.showError(true);
            }
        });
        exeWebServer.execute();
    }

    public void closeLoader() {
        if (loading_history.getVisibility() == View.VISIBLE) {
            loading_history.setVisibility(View.GONE);
        }
    }

    private void setImage() {
        if (Utils.checkText(vImage)) {

            Picasso.get()
                    .load(vImage)
                    .placeholder(R.mipmap.ic_no_pic_user)
                    .error(R.mipmap.ic_no_pic_user)
                    .into(restaurantImgView);
        }
    }

    private void addItemDetailLayout(JSONArray jobjArray) {


        for (int i = 0; i < jobjArray.length(); i++) {
            JSONObject jobject = generalFunc.getJsonObject(jobjArray, i);
            try {
                additemDetailRow(jobject.getString("vImage"), jobject.getString("MenuItem"), jobject.getString("SubTitle"), jobject.getString("fTotPrice"), /*" x " + */"" + jobject.get("iQty"), jobject.getString("TotalDiscountPrice"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void addFareDetailLayout(JSONArray jobjArray) {

        for (int i = 0; i < jobjArray.length(); i++) {
            JSONObject jobject = generalFunc.getJsonObject(jobjArray, i);
            try {
                String data = jobject.names().getString(0);
                addFareDetailRow(data, jobject.get(data).toString(),(jobjArray.length() - 1) == i ? true : false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
    private void addFareDetailRow(String row_name, String row_value, boolean isLast) {
        View convertView = null;
        if (row_name.equalsIgnoreCase("eDisplaySeperator")) {
            convertView = new View(getActContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dipToPixels(getActContext(), 1));
            params.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen._5sdp));
            convertView.setBackgroundColor(Color.parseColor("#dedede"));
            convertView.setLayoutParams(params);
        } else {
            LayoutInflater infalInflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.design_fare_deatil_row, null);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, (int) getResources().getDimension(R.dimen._10sdp), 0, isLast ? (int) getResources().getDimension(R.dimen._10sdp) : 0);
            convertView.setLayoutParams(params);

            MTextView titleHTxt = (MTextView) convertView.findViewById(R.id.titleHTxt);
            MTextView titleVTxt = (MTextView) convertView.findViewById(R.id.titleVTxt);

            titleHTxt.setText(generalFunc.convertNumberWithRTL(row_name));
            titleVTxt.setText(generalFunc.convertNumberWithRTL(row_value));

            if (isLast) {
                // CALCULATE individual fare & show
                titleHTxt.setTextColor(getResources().getColor(R.color.black));
                titleHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Poppins_SemiBold.ttf");
                titleHTxt.setTypeface(face);
                titleVTxt.setTypeface(face);
                titleVTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                titleVTxt.setTextColor(getResources().getColor(R.color.appThemeColor_1));

            }

        }

        if (convertView != null)
            farecontainer.addView(convertView);
    }

    private void additemDetailRow(String itemImage, String menuitemName, String subMenuName, String itemPrice, String qty, String discountprice) {
        final LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_select_bill_design, null);

        MTextView billItems = (MTextView) view.findViewById(R.id.billItems);
        MTextView billItemsQty = (MTextView) view.findViewById(R.id.billItemsQty);
        ImageView imageFoodType = (ImageView) view.findViewById(R.id.imageFoodType);
        CardView foodImageArea = (CardView) view.findViewById(R.id.foodImageArea);
        MTextView serviceTypeNameTxtView = (MTextView) view.findViewById(R.id.serviceTypeNameTxtView);
        MTextView strikeoutbillAmount = (MTextView) view.findViewById(R.id.strikeoutbillAmount);

        final MTextView billAmount = (MTextView) view.findViewById(R.id.billAmount);
        foodImageArea.setVisibility(View.VISIBLE);

        Picasso.get().load(Utils.getResizeImgURL(getActContext(), itemImage, size, size)).placeholder(R.mipmap.ic_no_icon).error(R.mipmap.ic_no_icon).into(imageFoodType);

        billAmount.setText(generalFunc.convertNumberWithRTL(itemPrice));
        billItemsQty.setText(generalFunc.convertNumberWithRTL(qty));

        billItems.setText(menuitemName);
        /*if (!subMenuName.equalsIgnoreCase("")) {
            serviceTypeNameTxtView.setVisibility(View.VISIBLE);
            serviceTypeNameTxtView.setText(subMenuName);
        } else {
            serviceTypeNameTxtView.setVisibility(View.GONE);
        }
*/


        if (discountprice != null && !discountprice.equals("")) {
            SpannableStringBuilder spanBuilder = new SpannableStringBuilder();

            SpannableString origSpan = new SpannableString(billAmount.getText());

            origSpan.setSpan(new StrikethroughSpan(), 0, billAmount.getText().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            spanBuilder.append(origSpan);

            strikeoutbillAmount.setVisibility(View.VISIBLE);
            strikeoutbillAmount.setText(spanBuilder);
            billAmount.setText(discountprice);
        } else {
            strikeoutbillAmount.setVisibility(View.GONE);
            billAmount.setTextColor(getResources().getColor(R.color.appThemeColor_1));
            billAmount.setPaintFlags(billAmount.getPaintFlags());
        }


        billDetails.addView(view);
    }


    private void addFareDetailRow(String row_name, String row_value) {
        LayoutInflater infalInflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = infalInflater.inflate(R.layout.design_fare_breakdown_row, null);
        TableRow FareDetailRow = (TableRow) convertView.findViewById(R.id.FareDetailRow);
        TableLayout fair_area_table_layout = (TableLayout) convertView.findViewById(R.id.fair_area);
        MTextView titleHTxt = (MTextView) convertView.findViewById(R.id.titleHTxt);
        MTextView titleVTxt = (MTextView) convertView.findViewById(R.id.titleVTxt);

        titleHTxt.setText(generalFunc.convertNumberWithRTL(row_name));
        titleVTxt.setText(generalFunc.convertNumberWithRTL(row_value));


        titleHTxt.setTextColor(Color.parseColor("#303030"));
        titleVTxt.setTextColor(Color.parseColor("#111111"));

        if (convertView != null)
            farecontainer.addView(convertView);
    }

    public Context getActContext() {
        return OrderHistoryDetailsActivity.this;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActContext());
            int i = view.getId();
            if (i == R.id.backImgView) {
                onBackPressed();
            }else if (i == R.id.viewPreferenceArea) {
                Bundle bundle = new Bundle();
                bundle.putString("DeliveryPreferences",DeliveryPreferences.toString());
                new StartActProcess(getActContext()).startActWithData(UserPrefrenceActivity.class,bundle);
            }
        }
    }

}
