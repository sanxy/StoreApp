package com.roaddo.store;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cropper.CropImage;
import com.fragments.EditProfileFragment;
import com.fragments.ProfileFragment;
import com.general.files.AppFunctions;
import com.general.files.GeneralFunctions;
import com.general.files.ImageFilePath;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.general.files.UploadProfileImage;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.utils.Utils.generateImageParams;

public class MyProfileActivity extends AppCompatActivity {

    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String IMAGE_DIRECTORY_NAME = "Temp";
    private static final int SELECT_PICTURE = 2;
    private static final int CROP_IMAGE = 3;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public GeneralFunctions generalFunc;
    public JSONObject userProfileJsonObj;
    public boolean isMobile = false;
    public boolean isEmail = false;
    MTextView titleTxt;
    ImageView backImgView;
    SelectableRoundedImageView userProfileImgView;
    SelectableRoundedImageView editIconImgView;
    ProfileFragment profileFrag;
    EditProfileFragment editProfileFrag;
    RelativeLayout userImgArea;
    String SITE_TYPE = "";
    String SITE_TYPE_DEMO_MSG = "";
    Menu menu;
    boolean isEdit = false;
    androidx.appcompat.app.AlertDialog alertDialog;
    ImageView profileimageback;
    String vContactName = "";
    private Uri fileUri;

    public static String getImageUrlWithAuthority(Context context, Uri uri) {
        InputStream is = null;
        if (uri.getAuthority() != null) {
            try {
                is = context.getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                return writeToTempImageAndGetPathUri(context, bmp).toString();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Uri writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, Utils.TempProfileImageName, null);
        return Uri.parse(path);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        vContactName = generalFunc.getJsonValueStr("vContactName", userProfileJsonObj);

        isEdit = getIntent().getBooleanExtra("isEdit", false);
        isMobile = getIntent().getBooleanExtra("isMobile", false);
        isEmail = getIntent().getBooleanExtra("isEmail", false);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        userProfileImgView = (SelectableRoundedImageView) findViewById(R.id.userProfileImgView);
        editIconImgView = (SelectableRoundedImageView) findViewById(R.id.editIconImgView);
        userImgArea = (RelativeLayout) findViewById(R.id.userImgArea);
        profileimageback = (ImageView) findViewById(R.id.profileimageback);

        backImgView.setOnClickListener(new setOnClickList());
        userImgArea.setOnClickListener(new setOnClickList());

        new CreateRoundedView(getResources().getColor(R.color.editBox_primary), Utils.dipToPixels(getActContext(), 15), 0,
                Color.parseColor("#00000000"), editIconImgView);

        editIconImgView.setColorFilter(getResources().getColor(R.color.appThemeColor_TXT_1));

        userProfileImgView.setImageResource(R.mipmap.ic_no_pic_user);
        (new AppFunctions(getActContext())).checkProfileImage(userProfileImgView, userProfileJsonObj, "vImage", profileimageback);

        SITE_TYPE = generalFunc.getJsonValueStr("SITE_TYPE", userProfileJsonObj);
        SITE_TYPE_DEMO_MSG = generalFunc.getJsonValueStr("SITE_TYPE_DEMO_MSG", userProfileJsonObj);


            openEditProfileFragment();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case GeneralFunctions.MY_PERMISSIONS_REQUEST: {
                if (generalFunc.isPermisionGranted()) {
                    new ImageSourceDialog().run();
                }
                break;

            }
        }
    }

    public void changePageTitle(String title) {
        titleTxt.setText(title);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {

            // perform your desired action here

            // return 'true' to prevent further propagation of the key event
            return true;
        }

        // let the system handle all other key events
        return super.onKeyDown(keyCode, event);
    }









    public Context getActContext() {
        return MyProfileActivity.this;
    }



    public void openEditProfileFragment() {

        if (editProfileFrag != null) {
            editProfileFrag = null;
            Utils.runGC();
        }
        editProfileFrag = new EditProfileFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragContainer, editProfileFrag).commit();
    }



    public EditProfileFragment getEditProfileFrag() {
        return this.editProfileFrag;
    }



    public void changeUserProfileJson(String userProfileJson) {
        this.userProfileJsonObj = generalFunc.getJsonObject(userProfileJson);

        Bundle bn = new Bundle();
        bn.putString("UserProfileJson", userProfileJson);

        generalFunc.storeData(Utils.WALLET_ENABLE, generalFunc.getJsonValue("WALLET_ENABLE", userProfileJson));
        generalFunc.storeData(Utils.REFERRAL_SCHEME_ENABLE, generalFunc.getJsonValue("REFERRAL_SCHEME_ENABLE", userProfileJson));

        new StartActProcess(getActContext()).setOkResult(bn);



        generalFunc.showMessage(getCurrView(), generalFunc.retrieveLangLBl("", "LBL_INFO_UPDATED_TXT"));
    }

    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public void chooseFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);

    }

    public void chooseFromCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the rider's current state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onBackPressed() {



        super.onBackPressed();
    }

    public View getCurrView() {
        return generalFunc.getCurrentView(MyProfileActivity.this);
    }

    public boolean isValidImageResolution(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);
        int width = options.outWidth;
        int height = options.outHeight;

        if (width >= Utils.ImageUpload_MINIMUM_WIDTH && height >= Utils.ImageUpload_MINIMUM_HEIGHT) {
            return true;
        }
        return false;
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE || requestCode == SELECT_PICTURE || requestCode == CROP_IMAGE)
        {
            if (resultCode == RESULT_OK)
            {

                if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE)
                {
                    // successfully captured the image
                    // display it in image view
//                    fileUri = Uri.parse(fileUriFilePath);
                    try {
                        cropImage(fileUri, fileUri);

                    } catch (Exception e) {
                        if (fileUri != null) {
//                            generalFunc.showMessage(getCurrView(), generalFunc.retrieveLangLBl("Some problem occurred.can't able to get cropped image.so we are uploading original captured image.", "LBL_CROP_ERROR_TXT"));
                            imageUpload(fileUri);
                        } else if (data != null) {
//                            generalFunc.showMessage(getCurrView(), generalFunc.retrieveLangLBl("Some problem occurred.can't able to get cropped image.so we are uploading original captured image.", "LBL_CROP_ERROR_TXT"));
                            imageUpload(data.getData());
                        } else {
                            generalFunc.showMessage(getCurrView(), generalFunc.retrieveLangLBl("", "LBL_ERROR_OCCURED"));

                        }
                        e.printStackTrace();
                    }

                } else if (requestCode == SELECT_PICTURE) {

                    try {
                        Uri cropPictureUrl = Uri.fromFile(getOutputMediaFile(MEDIA_TYPE_IMAGE));
                        String realPathFromURI = new ImageFilePath().getPath(getActContext(), data.getData());
                        File file = new File(realPathFromURI == null ? getImageUrlWithAuthority(this, data.getData()) : realPathFromURI);
                        if (file == null || realPathFromURI == null || realPathFromURI.equalsIgnoreCase("")) {
                            generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("Can't read selected image. Please try again.", "LBL_IMAGE_READ_FAILED"));
                            return;
                        }
                        if (file.exists()) {
                            if (Build.VERSION.SDK_INT > 23) {
                                cropImage(FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file), cropPictureUrl);
                            } else {
                                cropImage(Uri.fromFile(file), cropPictureUrl);
                            }

                        } else {
                            cropImage(data.getData(), cropPictureUrl);
                        }

                    } catch (Exception e) {
                        if (data != null) {
//                            generalFunc.showMessage(getCurrView(), generalFunc.retrieveLangLBl("Some problem occurred.can't able to get cropped image.so we are uploading original captured image.", "LBL_CROP_ERROR_TXT"));
                            imageUpload(data.getData());
                        } else {
                            generalFunc.showMessage(getCurrView(), generalFunc.retrieveLangLBl("", "LBL_ERROR_OCCURED"));

                        }
                        e.printStackTrace();
                    }
                } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    Uri resultUri = result.getUri();
                    imageUpload(resultUri);
                } else if (requestCode == CROP_IMAGE) {

                    imageUpload(fileUri);
                }
            } else {
                if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
                    generalFunc.showMessage(getCurrView(), generalFunc.retrieveLangLBl("", "LBL_FAILED_CAPTURE_IMAGE_TXT"));
                } else {
                    generalFunc.showMessage(getCurrView(), generalFunc.retrieveLangLBl("", "LBL_ERROR_OCCURED"));
                }
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri resultUri = result.getUri();
            imageUpload(resultUri);
        }
    }

    private void cropImage(final Uri sourceImage, Uri destinationImage) {
        try {
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            intent.setType("image/*");

            List<ResolveInfo> list = this.getPackageManager().queryIntentActivities(intent, 0);
            int size = list.size();
            if (size == 0) {
                //Utils.showToast(mContext, mContext.getString(R.string.error_cant_select_cropping_app));
                fileUri = sourceImage;
                intent.putExtra(MediaStore.EXTRA_OUTPUT, sourceImage);
                startActivityForResult(intent, CROP_IMAGE);
                return;
            } else {
                intent.setDataAndType(sourceImage, "image/*");
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("outputY", 256);
                intent.putExtra("outputX", 256);
                fileUri = destinationImage;
                //intent.putExtra("return-data", true);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, destinationImage);
                if (size == 1) {
                    Intent i = new Intent(intent);
                    ResolveInfo res = list.get(0);
                    i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                    startActivityForResult(i, CROP_IMAGE);
                } else {
                    Intent i = new Intent(intent);
                    i.putExtra(Intent.EXTRA_INITIAL_INTENTS, list.toArray(new Parcelable[list.size()]));
                    startActivityForResult(i, CROP_IMAGE);
                }
            }
        } catch (Exception e) {
            generalFunc.showGeneralMessage("", "Sorry - It seems your device doesn't support the crop/edit action!");
            imageUpload(fileUri);

            e.printStackTrace();
        }
    }

    private void imageUpload(Uri fileUri) {
        if (SITE_TYPE.equalsIgnoreCase("Demo") && generalFunc.getJsonValue("vEmail", generalFunc.retrieveValue(Utils.USER_PROFILE_JSON)).equalsIgnoreCase("Driver@gmail.com")) {
            generalFunc.showGeneralMessage("", SITE_TYPE_DEMO_MSG);
            return;
        }

        ArrayList<String[]> paramsList = new ArrayList<>();

        String iMemberId=generalFunc.getMemberId();

        paramsList.add(generateImageParams("iMemberId", iMemberId));
        paramsList.add(generateImageParams("MemberType", Utils.app_type));
        paramsList.add(generateImageParams("tSessionId", iMemberId.equals("") ? "" : generalFunc.retrieveValue(Utils.SESSION_ID_KEY)));
        paramsList.add(generateImageParams("GeneralUserType", Utils.app_type));
        paramsList.add(generateImageParams("GeneralMemberId", iMemberId));
        paramsList.add(generateImageParams("type", "uploadImage"));

        String selPath = new ImageFilePath().getPath(getActContext(), fileUri);

        boolean isStoragePermissionAvail = generalFunc.isStoragePermissionGranted();

        if (isValidImageResolution(selPath) && isStoragePermissionAvail) {
            new UploadProfileImage(MyProfileActivity.this, selPath, Utils.TempProfileImageName, paramsList, "").execute();

        } else {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Please select image which has minimum is 256 * 256 resolution.", "LBL_MIN_RES_IMAGE"));
        }

    }

    public void handleImgUploadResponse(String responseString) {

        if (responseString != null && !responseString.equals("")) {

            boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

            if (isDataAvail) {
                generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValue(Utils.message_str, responseString));
                changeUserProfileJson(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                (new AppFunctions(getActContext())).checkProfileImage(userProfileImgView, userProfileJsonObj, "vImage", profileimageback);

            } else {
                generalFunc.showGeneralMessage("",
                        generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
            }
        } else {
            generalFunc.showError();
        }
    }




    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(MyProfileActivity.this);

            switch (view.getId()) {
                case R.id.backImgView:

                    MyProfileActivity.super.onBackPressed();
                    break;

                case R.id.userImgArea:
                    if (generalFunc.isCameraStoragePermissionGranted()) {
                        new ImageSourceDialog().run();
                    } else {
                        generalFunc.showMessage(getCurrView(), "Allow this app to use camera.");

                    }

                    break;

            }
        }
    }

    public class AsteriskPasswordTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }

        private class PasswordCharSequence implements CharSequence {
            private CharSequence mSource;

            public PasswordCharSequence(CharSequence source) {
                mSource = source; // Store char sequence
            }

            public char charAt(int index) {
                return '*'; // This is the important part
            }

            public int length() {
                return mSource.length(); // Return default
            }

            public CharSequence subSequence(int start, int end) {
                return mSource.subSequence(start, end); // Return default
            }
        }
    }

    class ImageSourceDialog implements Runnable {

        @Override
        public void run() {


            final Dialog dialog_img_update = new Dialog(getActContext(), R.style.ImageSourceDialogStyle);

            dialog_img_update.setContentView(R.layout.design_image_source_select);

            MTextView chooseImgHTxt = (MTextView) dialog_img_update.findViewById(R.id.chooseImgHTxt);
            chooseImgHTxt.setText(generalFunc.retrieveLangLBl("Choose Category", "LBL_CHOOSE_CATEGORY"));

            SelectableRoundedImageView cameraIconImgView = (SelectableRoundedImageView) dialog_img_update.findViewById(R.id.cameraIconImgView);
            SelectableRoundedImageView galleryIconImgView = (SelectableRoundedImageView) dialog_img_update.findViewById(R.id.galleryIconImgView);

            ImageView closeDialogImgView = (ImageView) dialog_img_update.findViewById(R.id.closeDialogImgView);

            closeDialogImgView.setOnClickListener(v -> {
                if (dialog_img_update != null) {
                    dialog_img_update.cancel();
                }
            });

            int backColor=getResources().getColor(R.color.appThemeColor_Dark_1);
            int strokeColor=Color.parseColor("#00000000");
            int btnRadius=Utils.dipToPixels(getActContext(), 25);
            int filterColor=getResources().getColor(R.color.appThemeColor_TXT_1);

            new CreateRoundedView(backColor, btnRadius, 0, strokeColor, cameraIconImgView);

            cameraIconImgView.setColorFilter(filterColor);

            new CreateRoundedView(backColor, btnRadius, 0, strokeColor, galleryIconImgView);

            galleryIconImgView.setColorFilter(filterColor);

            cameraIconImgView.setOnClickListener(v -> {

                if (dialog_img_update != null) {
                    dialog_img_update.cancel();
                }

                if (!isDeviceSupportCamera()) {
                    generalFunc.showMessage(getCurrView(), generalFunc.retrieveLangLBl("", "LBL_NOT_SUPPORT_CAMERA_TXT"));
                } else {
                    chooseFromCamera();
                }

            });

            galleryIconImgView.setOnClickListener(v -> {

                if (dialog_img_update != null) {
                    dialog_img_update.cancel();
                }
                chooseFromGallery();
            });

            dialog_img_update.setCanceledOnTouchOutside(true);

            Window window = dialog_img_update.getWindow();
            window.setGravity(Gravity.BOTTOM);

            window.setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            dialog_img_update.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            dialog_img_update.show();

        }

    }

}
