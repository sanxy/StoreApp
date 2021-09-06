package com.roaddo.store;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.adapter.files.GalleryImagesRecyclerAdapter;

import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.squareup.picasso.Picasso;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.carouselview.CarouselView;
import com.view.carouselview.ViewListener;

import java.util.ArrayList;

public class PrescriptionActivity extends AppCompatActivity implements GalleryImagesRecyclerAdapter.OnItemClickListener {
    GeneralFunctions generalFunc;
    ImageView backImgView;
    MTextView titleTxt;
    MTextView noteTxt, noDescTxt;
    ImageView rightImgView;
    RecyclerView imageListRecyclerView;
    MButton btn_type2, btn_type2_confirm;
    GalleryImagesRecyclerAdapter adapter;
    ArrayList<String> listData = new ArrayList<>();
    AppCompatImageView noImgView;
    ProgressBar loading_images;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String IMAGE_DIRECTORY_NAME = "Temp";
    private static final int SELECT_PICTURE = 2;
    private static final int SELECT_HISTROY_IMAGE = 4;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private Uri fileUri;
    MTextView attechTxt;
    private String selectedImagePath = "";
    private String pathForCameraImage = "";

    View carouselContainerView;
    CarouselView carouselView;
    MTextView closeCarouselTxtView;
    LinearLayout confirmBtnArea;
    String iImageId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);
        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        backImgView = (ImageView) findViewById(R.id.backImgView);
        rightImgView = (ImageView) findViewById(R.id.rightImgView);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        attechTxt = (MTextView) findViewById(R.id.attechTxt);
        carouselContainerView = findViewById(R.id.carouselContainerView);
        carouselView = (CarouselView) findViewById(R.id.carouselView);
        noteTxt = (MTextView) findViewById(R.id.noteTxt);
        noDescTxt = (MTextView) findViewById(R.id.noDescTxt);
        closeCarouselTxtView = (MTextView) findViewById(R.id.closeCarouselTxtView);
        imageListRecyclerView = (RecyclerView) findViewById(R.id.imageListRecyclerView);
        confirmBtnArea = (LinearLayout) findViewById(R.id.confirmBtnArea);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        btn_type2_confirm = ((MaterialRippleLayout) findViewById(R.id.btn_type2_confirm)).getChildView();
        btn_type2_confirm.setOnClickListener(new setOnClick());


        btn_type2.setId(Utils.generateViewId());
        noImgView = (AppCompatImageView) findViewById(R.id.noImgView);
        loading_images = (ProgressBar) findViewById(R.id.loading_images);
        btn_type2.setOnClickListener(new setOnClick());
        attechTxt.setOnClickListener(new setOnClick());

        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_DONE"));
        noteTxt.setText(generalFunc.retrieveLangLBl("", "LBL_GALLERY_IMG_NOTE"));
        backImgView.setOnClickListener(new setOnClick());
        rightImgView.setOnClickListener(new setOnClick());
        closeCarouselTxtView.setOnClickListener(new setOnClick());
        closeCarouselTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_CLOSE_TXT"));
        titleTxt.setText(generalFunc.retrieveLangLBl("", "Prescription"));
        String LBL_ATTACH_PRESCRIPTION=generalFunc.retrieveLangLBl("", "LBL_ATTACH_PRESCRIPTION");
        btn_type2.setText(LBL_ATTACH_PRESCRIPTION);
        attechTxt.setText(LBL_ATTACH_PRESCRIPTION);
        noDescTxt.setText(generalFunc.retrieveLangLBl("", "LBL_NOPRESCRIPTION"));
        noteTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PRESCRIPTION_BODY_TEXT"));
        btn_type2_confirm.setText(generalFunc.retrieveLangLBl("", "LBL_CONFIRM_TXT"));

        rightImgView.setVisibility(View.GONE);

        listData = (ArrayList<String>) getIntent().getSerializableExtra("imageList");
        adapter = new GalleryImagesRecyclerAdapter(getActContext(), listData, generalFunc, false, true, false);

        GridLayoutManager gridLay = new GridLayoutManager(getActContext(), adapter.getNumOfColumns());

        imageListRecyclerView.setLayoutManager(gridLay);
        imageListRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }


    @Override
    public void onItemClickList(View v, int position) {
        carouselContainerView.setVisibility(View.VISIBLE);
        carouselView.setViewListener(viewListener);
        carouselView.setPageCount(listData.size());
        carouselView.setCurrentItem(position);
    }


    @Override
    public void onDeleteClick(View v, int position) {


    }


    ViewListener viewListener = position -> {
        ImageView customView = new ImageView(getActContext());

        CarouselView.LayoutParams layParams = new CarouselView.LayoutParams(CarouselView.LayoutParams.MATCH_PARENT, CarouselView.LayoutParams.MATCH_PARENT);
//        layParams.leftMargin = Utils.dipToPixels(getActContext(), 15);
//        layParams.rightMargin = Utils.dipToPixels(getActContext(), 15);
        customView.setLayoutParams(layParams);

        customView.setPadding(Utils.dipToPixels(getActContext(), 15), 0, Utils.dipToPixels(getActContext(), 15), 0);
        customView.setImageResource(R.mipmap.ic_no_icon);


        Picasso.get()
                .load(Utils.getResizeImgURL(getActContext(), listData.get(position), ((int) Utils.getScreenPixelWidth(getActContext())) - Utils.dipToPixels(getActContext(), 30), 0, Utils.getScreenPixelHeight(getActContext()) - Utils.dipToPixels(getActContext(), 30)))
                .placeholder(R.mipmap.ic_no_icon).error(R.mipmap.ic_no_icon)
                .into(customView, null);

        return customView;
    };

    public Context getActContext() {
        return PrescriptionActivity.this;
    }

    public class setOnClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.backImgView) {
                PrescriptionActivity.super.onBackPressed();
            } else if (i == R.id.rightImgView) {


            } else if (i == closeCarouselTxtView.getId()) {
                if (carouselContainerView.getVisibility() == View.VISIBLE) {
                    carouselContainerView.setVisibility(View.GONE);
                }
            }
        }
    }


    public View getCurrView() {
        return generalFunc.getCurrentView(PrescriptionActivity.this);
    }


}
