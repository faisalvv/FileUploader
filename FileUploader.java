package com.practicesuite.hipaaoffice.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

/**
 * Created by Faizal-user on 1/2/2018.
 */
public class FileUploader {

    /***file upload change code on 03-01-2018**/
    public static String mCM;
    public static ValueCallback<Uri> mUM;
    public static ValueCallback<Uri[]> mUMA;
    public  static int FCR=1;
    /**end here**/
    public static callBackListner listner;
    public static ValueCallback<Uri[]> filePathCallback;
    public static Activity callingActivity;
    public static WebView webView;

    public void Chooser(WebView webV, final Activity callingActivity1, callBackListner callBackListner){
        listner=callBackListner;
        callingActivity=callingActivity1;
        webView=webV;
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        if(Build.VERSION.SDK_INT >= 21){
            webSettings.setMixedContentMode(0);
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }else if(Build.VERSION.SDK_INT >= 19){
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }else if(Build.VERSION.SDK_INT < 19){
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        initChooser(webView,callingActivity,callBackListner);
    }
    public void initChooser(WebView webView1, Activity callingActivity1, callBackListner callBackListner1){
        webView=webView1;
        callingActivity=callingActivity1;
        listner=callBackListner1;
        AppLog.d("dooth-chat","initChooser");
        webView.setWebChromeClient(new WebChromeClient(){
            //For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg){
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                FileUploader.callingActivity.startActivityForResult(Intent.createChooser(i,"File Chooser"), FCR);
            }
            // For Android 3.0+, above method not supported in some android 3+ versions, in such case we use this
            public void openFileChooser(ValueCallback uploadMsg, String acceptType){
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                FileUploader.callingActivity.startActivityForResult(Intent.createChooser(i, "File Browser"), FCR);
            }
            //For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                FileUploader.callingActivity.startActivityForResult(Intent.createChooser(i, "File Chooser"), FCR);
            }
            //For Android 5.0+
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback1,FileChooserParams fileChooserParams){
                AppLog.d("dooth-chat","onShowFileChooser");
                filePathCallback=filePathCallback1;
                /**ShowFileChooser For Android 5.0 SDK_INT>=23 check permission For Android >= 6.0*/
                if (Build.VERSION.SDK_INT >= 23) {
                    // Log.d(TAG,"@@@ ShowFileChooser For Android 5.0 SDK_INT>=23 chk permission");
                    String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA};
                    if (!hasPermissions(FileUploader.callingActivity, PERMISSIONS)) {
                        AppLog.d("dooth-chat","!hasPermissions");
                        ActivityCompat.requestPermissions(FileUploader.callingActivity, PERMISSIONS, 100);
                        //listner.pendingOpenedChooserForPermission(filePathCallback, FileUploader.callingActivity);
                    }else {
                        AppLog.d("dooth-chat","already granded");
                        // listner.onOpenedChooser(filePathCallback,callingActivity);
                        openChooser();
                    }
                }else {//For Android <= 5.0
                    openChooser();
                    //listner.onOpenedChooser(filePathCallback,callingActivity);
                }
                return true;
            }
        });
    }
    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    // Create an image file
    public File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_"+timeStamp+"_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        /**save and return file from temp directory.*/
        return File.createTempFile(imageFileName,".jpg",storageDir);
    }
    public  void setActivityResult(int requestCode, int resultCode, Intent data){
        if(Build.VERSION.SDK_INT >= 21){
            Uri[] results = null;
            //Check if response is positive
            if(resultCode== Activity.RESULT_OK){
                if(requestCode == FCR){
                    if(null == mUMA){
                        return;
                    }
                    if(data == null || data.getData() == null){
                        //Capture Photo if no image available
                        if(mCM != null){
                            results = new Uri[]{Uri.parse(mCM)};
                        }
                    }else{
                        String dataString = data.getDataString();
                        if(dataString != null){
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }
            }else {
                listner.cancelOpenChooser();
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        }else{
            if(requestCode == FCR){
                if(null == mUM) return;
                Uri result = data == null || resultCode != Activity.RESULT_OK ? null : data.getData();
                mUM.onReceiveValue(result);
                mUM = null;
            }
        }
        return;
    }
    public void setRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.e("dooth-permission","onRequestPermissionsResult 2");
        //openChooser();
        switch (requestCode) {

            case 100: {//chat,mail permission.
                Log.e("dooth-permission","onRequestPermissionsResult 3");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("dooth-permission","onRequestPermissionsResult");
                    openChooser();
                   /* if (currentApp=="CHAT"){
                        ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.chatfragmentcontainer);
                        chatFragment.openChooser();
                    }else if (currentApp=="MAIL"){
                        MailFragment mailFragment = (MailFragment) getSupportFragmentManager().findFragmentById(R.id.chatfragmentcontainer);
                        mailFragment.openChooser();
                    }*/

                } else {
                    initChooser(webView, callingActivity, listner);
                   /* if (currentApp=="CHAT"){
                        ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.chatfragmentcontainer);
                        chatFragment.initOpenChooser();
                    }else if (currentApp=="MAIL"){
                        MailFragment mailFragment = (MailFragment) getSupportFragmentManager().findFragmentById(R.id.chatfragmentcontainer);
                        mailFragment.initOpenChooser();
                    }*/
                }
            }
        }
    }

    public void openChooser() {
        if(mUMA != null){
            mUMA.onReceiveValue(null);
        }
        mUMA =filePathCallback;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(callingActivity.getPackageManager()) != null){
            File photoFile = null;
            try{
                photoFile = createImageFile();
                takePictureIntent.putExtra("PhotoPath", FileUploader.mCM);
            }catch(IOException ex){
                //Log.e(TAG, "Image file creation failed", ex);
            }
            if(photoFile != null){
                FileUploader.mCM = "file:" + photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            }else{
                takePictureIntent = null;
            }
        }
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("*/*");
        Intent[] intentArray;
        if(takePictureIntent != null){
            intentArray = new Intent[]{takePictureIntent};
        }else{
            intentArray = new Intent[0];
        }

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        callingActivity.startActivityForResult(chooserIntent, FCR);
    }

    /**add listeners for call back */
    public interface callBackListner{
        void cancelOpenChooser();
        //void onOpenedChooser(ValueCallback<Uri[]> filePathCallback, Activity callingActivity);
        //void pendingOpenedChooserForPermission(ValueCallback<Uri[]> filePathCallback, Activity callingActivity);
    }

}
