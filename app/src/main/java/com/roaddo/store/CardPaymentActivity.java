package com.roaddo.store;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.fragments.AddCardFragment;
import com.fragments.ViewCardFragment;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.Utils;
import com.view.MTextView;

import org.json.JSONObject;

public class CardPaymentActivity extends AppCompatActivity {

    public GeneralFunctions generalFunc;
    public JSONObject userProfileJsonObj;
    MTextView titleTxt;
    ImageView backImgView;
    ViewCardFragment viewCardFrag;
    AddCardFragment addCardFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_card_payment);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);

        setLabels();

        backImgView.setOnClickListener(new setOnClickList());

        openViewCardFrag();


    }


    public void setLabels() {
        changePageTitle(generalFunc.retrieveLangLBl("", "LBL_CARD_PAYMENT_DETAILS"));
    }

    public void changePageTitle(String title) {
        titleTxt.setText(title);
    }

    public void changeUserProfileJson(String userProfileJson) {
        this.userProfileJsonObj = generalFunc.getJsonObject(userProfileJson);

        Bundle bn = new Bundle();
        new StartActProcess(getActContext()).setOkResult(bn);

        openViewCardFrag();

        generalFunc.showMessage(getCurrView(), generalFunc.retrieveLangLBl("", "LBL_INFO_UPDATED_TXT"));
    }

    public View getCurrView() {
        return generalFunc.getCurrentView(CardPaymentActivity.this);
    }

    public void openViewCardFrag() {

        if (viewCardFrag != null) {
            viewCardFrag = null;
            addCardFrag = null;
            Utils.runGC();
        }
        viewCardFrag = new ViewCardFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, viewCardFrag).commit();
    }

    public void openAddCardFrag(String mode) {

        if (addCardFrag != null) {
            addCardFrag = null;
            viewCardFrag = null;
            Utils.runGC();
        }

        Bundle bundle = new Bundle();
        bundle.putString("PAGE_MODE", mode);
        bundle.putString("carno", generalFunc.getJsonValueStr("vCreditCard", userProfileJsonObj));
//        bundle.putString("month",generalFunc.getJsonValue("vExpMonth", userProfileJson));
//        bundle.putString("year",generalFunc.getJsonValue("vExpYear", userProfileJson));
        addCardFrag = new AddCardFragment();
        addCardFrag.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, addCardFrag).commit();
    }

    public Context getActContext() {
        return CardPaymentActivity.this;
    }

    @Override
    public void onBackPressed() {
        backImgView.performClick();
        return;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(CardPaymentActivity.this);
            if (i == R.id.backImgView) {
                if (addCardFrag == null) {
                    CardPaymentActivity.super.onBackPressed();
                } else {
                    openViewCardFrag();
                }
            }
        }
    }

}
