package com.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.roaddo.store.CardPaymentActivity;
import com.roaddo.store.R;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.utils.Logger;
import com.utils.ModelUtils;
import com.utils.Utils;
import com.view.MButton;
import com.view.MaterialRippleLayout;
import com.view.MyProgressDialog;
import com.view.editBox.MaterialEditText;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddCardFragment extends Fragment implements TextWatcher {

    private static final char SPACE_CHAR = ' ';
    GeneralFunctions generalFunc;
    View view;

    CardPaymentActivity cardPayAct;

    JSONObject userProfileJsonObj;
    MButton btn_type2;
    MaterialEditText creditCardBox;
    MaterialEditText cvvBox;
    MaterialEditText mmBox;
    MaterialEditText yyBox;

    String required_str = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_card, container, false);

        cardPayAct = (CardPaymentActivity) getActivity();
        generalFunc = cardPayAct.generalFunc;
        userProfileJsonObj = cardPayAct.userProfileJsonObj;
        btn_type2 = ((MaterialRippleLayout) view.findViewById(R.id.btn_type2)).getChildView();
        creditCardBox = (MaterialEditText) view.findViewById(R.id.creditCardBox);
        cvvBox = (MaterialEditText) view.findViewById(R.id.cvvBox);
        mmBox = (MaterialEditText) view.findViewById(R.id.mmBox);
        yyBox = (MaterialEditText) view.findViewById(R.id.yyBox);

        if (getArguments().getString("PAGE_MODE").equals("ADD_CARD")) {
            String LBL_ADD_CARD=generalFunc.retrieveLangLBl("", "LBL_ADD_CARD");
            cardPayAct.changePageTitle(LBL_ADD_CARD);
            btn_type2.setText(LBL_ADD_CARD);
        } else {
            String LBL_CHANGE_CARD=generalFunc.retrieveLangLBl("Change Card", "LBL_CHANGE_CARD");
            cardPayAct.changePageTitle(LBL_CHANGE_CARD);
            btn_type2.setText(LBL_CHANGE_CARD);
        }

        btn_type2.setId(Utils.generateViewId());
        btn_type2.setOnClickListener(new setOnClickList());

        int maxLength = 24;
        int monthmaxLegth = 2;
        int cvvMaxLegth = 5;
        int yearMaxLength = 4;
        creditCardBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        mmBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(monthmaxLegth)});
        cvvBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(cvvMaxLegth)});
        yyBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(yearMaxLength)});

        creditCardBox.addTextChangedListener(new TextWatcher() {
            private static final char space = ' ';

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && (s.length() % 5) == 0) {
                    final char c = s.charAt(s.length() - 1);
                    if (space == c) {
                        s.delete(s.length() - 1, s.length());
                    }
                }
                // Insert char where needed.
                if (s.length() > 0 && (s.length() % 5) == 0) {
                    char c = s.charAt(s.length() - 1);
                    // Only if its a digit where there should be a space we insert a space
                    if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(space)).length <= 3) {
                        s.insert(s.length() - 1, String.valueOf(space));
                    }
                }

            }
        });


        setLabels();


        mmBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        yyBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        cvvBox.setInputType(InputType.TYPE_CLASS_NUMBER  | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        cvvBox.setImeOptions(EditorInfo.IME_ACTION_DONE);
        creditCardBox.setInputType(InputType.TYPE_CLASS_PHONE);

        creditCardBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});
        mmBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        cvvBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        yyBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        creditCardBox.addTextChangedListener(this);


        return view;
    }

    public void setLabels() {

        creditCardBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CARD_NUMBER_HEADER_TXT"), generalFunc.retrieveLangLBl("", "LBL_CARD_NUMBER_HINT_TXT"));
        cvvBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CVV_HEADER_TXT"), generalFunc.retrieveLangLBl("", "LBL_CVV_HINT_TXT"));
        mmBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_EXP_MONTH_HINT_TXT"), generalFunc.retrieveLangLBl("", "LBL_EXP_MONTH_HINT_TXT"));
        yyBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_EXP_YEAR_HINT_TXT"), generalFunc.retrieveLangLBl("", "LBL_EXP_YEAR_HINT_TXT"));

        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD");
    }

    public boolean validateExpYear(Calendar now) {
        return yyBox.getText().toString() != null && !ModelUtils.hasYearPassed(GeneralFunctions.parseIntegerValue(0,yyBox.getText().toString()), now);
    }

    public void checkDetails() {

        Card card = new Card(Utils.getText(creditCardBox), generalFunc.parseIntegerValue(0, Utils.getText(mmBox)),
                generalFunc.parseIntegerValue(0, Utils.getText(yyBox)), Utils.getText(cvvBox));

        Logger.d("Card No", ":" + card.validateNumber() + "::num::" + card.getNumber());
        boolean cardNoEntered = Utils.checkText(creditCardBox) ? (card.validateNumber() ? true :
                Utils.setErrorFields(creditCardBox, generalFunc.retrieveLangLBl("", "LBL_INVALID")))
                : Utils.setErrorFields(creditCardBox, required_str);
        boolean cvvEntered = Utils.checkText(cvvBox) ? (card.validateCVC() ? true :
                Utils.setErrorFields(cvvBox, generalFunc.retrieveLangLBl("", "LBL_INVALID"))) : Utils.setErrorFields(cvvBox, required_str);
        boolean monthEntered = Utils.checkText(mmBox) ? (card.validateExpMonth() ? true :
                Utils.setErrorFields(mmBox, generalFunc.retrieveLangLBl("", "LBL_INVALID"))) : Utils.setErrorFields(mmBox, required_str);
        boolean yearEntered = Utils.checkText(yyBox) ? (validateExpYear(Calendar.getInstance())? true :
                Utils.setErrorFields(yyBox, generalFunc.retrieveLangLBl("", "LBL_INVALID"))) : Utils.setErrorFields(yyBox, required_str);
        boolean yearEntedcount = true;
        if (yearEntered == true) {
            yearEntedcount = yyBox.getText().toString().length() == 4 ? true : Utils.setErrorFields(yyBox, generalFunc.retrieveLangLBl("", "LBL_INVALID"));
        }

        if (cardNoEntered == false || cvvEntered == false || monthEntered == false || yearEntered == false || yearEntedcount == false) {
            return;
        }

        if (card.validateCard()) {
            generateToken(card);
        }
    }

    public MyProgressDialog showLoader() {
        MyProgressDialog myPDialog = new MyProgressDialog(getActContext(), false, generalFunc.retrieveLangLBl("Loading", "LBL_LOADING_TXT"));
        myPDialog.show();

        return myPDialog;
    }

    public void generateToken(final Card card) {

        final MyProgressDialog myPDialog = showLoader();

        String STRIPE_PUBLISH_KEY = generalFunc.getJsonValueStr("STRIPE_PUBLISH_KEY", userProfileJsonObj);
        Stripe stripe = new Stripe(getActContext());

        stripe.createToken(card, STRIPE_PUBLISH_KEY, new TokenCallback() {
            public void onSuccess(Token token) {
                // TODO: Send Token information to your backend to initiate a charge
                myPDialog.close();
                Logger.d("Token", "::" + token.getId());
                CreateCustomer(card, token.getId());
            }

            public void onError(Exception error) {
                myPDialog.close();
                Logger.d("Error", "::" + error.toString());
                generalFunc.showError();
            }
        });
    }

    public void CreateCustomer(Card card, String vStripeToken) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GenerateCustomer");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("vStripeToken", vStripeToken);
        parameters.put("UserType", Utils.app_type);
        parameters.put("CardNo", Utils.maskCardNumber(card.getNumber()));

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseObj=generalFunc.getJsonObject(responseString);
            if (responseObj != null && !responseObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);

                if (isDataAvail == true) {
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValueStr(Utils.message_str, responseObj));
                    cardPayAct.changeUserProfileJson(generalFunc.getJsonValueStr(Utils.message_str, responseObj));
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

    public Context getActContext() {
        return cardPayAct.getActContext();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.hideKeyboard(getActContext());
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() > 0 && (s.length() % 5) == 0) {
            final char c = s.charAt(s.length() - 1);
            if (SPACE_CHAR == c) {
                s.delete(s.length() - 1, s.length());
            }
        }
        // Insert char where needed.
        if (s.length() > 0 && (s.length() % 5) == 0) {
            char c = s.charAt(s.length() - 1);
            // Only if its a digit where there should be a space we insert a space
            if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(SPACE_CHAR)).length <= 3) {
                s.insert(s.length() - 1, String.valueOf(SPACE_CHAR));
            }
        }
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActContext());
            int i = view.getId();
            if (i == btn_type2.getId()) {
                checkDetails();
            }
        }
    }
}
