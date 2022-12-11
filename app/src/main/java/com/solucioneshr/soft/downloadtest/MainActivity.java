package com.solucioneshr.soft.downloadtest;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.solucioneshr.soft.downloadtest.databinding.ActivityMainBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding mainBinding;

    private DownloadFile downloadFile;
    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View mainView = mainBinding.getRoot();
        setContentView(mainView);

        mainBinding.btnStartDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mainBinding.textURL.getText().toString().isEmpty() && !mainBinding.textNameFolder.getText().toString().isEmpty()){
                    downloadFile = new DownloadFile(mainBinding.textNameFolder.getText().toString());
                    downloadFile.execute(mainBinding.textURL.getText().toString());
                    mainBinding.btnStopDownload.setEnabled(true);
                    mainBinding.btnStartDownload.setEnabled(false);
                }
            }
        });

        mainBinding.btnStopDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFile.onCancelled();
                mainBinding.btnStopDownload.setEnabled(false);
                mainBinding.btnStartDownload.setEnabled(true);
            }
        });

        /************************************************************
        *   Permission
         ************************************************/
        if (checkPermission()){
            Snackbar.make(mainBinding.progressDownload, "Manage External Storage Permission is granted", Snackbar.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }
    }

    // Function to check and request permission.
    private void requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11(R) or above
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            }
            catch (Exception e){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        }
        else {
            //Android is below 11(R)
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    private ActivityResultLauncher<Intent> storageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //here we will handle the result of our intent
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                        //Android is 11(R) or above
                        if (Environment.isExternalStorageManager()){
                            //Manage External Storage Permission is granted
                            Snackbar.make(mainBinding.progressDownload, "Manage External Storage Permission is granted", Snackbar.LENGTH_SHORT).show();
                        }
                        else{
                            //Manage External Storage Permission is denied
                            Snackbar.make(mainBinding.progressDownload, "Manage External Storage Permission is denied", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        //Android is below 11(R)
                    }
                }
            }
    );

    public boolean checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11(R) or above
            return Environment.isExternalStorageManager();
        }
        else{
            //Android is below 11(R)
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            return write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED;
        }
    }

    /*Handle permission request results*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE){
            if (grantResults.length > 0){
                //check each permission if granted or not
                boolean write = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean read = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (write && read){
                    //External Storage permissions granted
                    Snackbar.make(mainBinding.progressDownload, "External Storage permissions granted", Snackbar.LENGTH_SHORT).show();
                }
                else{
                    //External Storage permission denied
                    Snackbar.make(mainBinding.progressDownload, "External Storage permission denied", Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();;
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageInitDownload event) {
        mainBinding.lengthDownload.setText(event.getData());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageProgressValue event) {
        mainBinding.progressDownload.setProgress(event.getValue());
        mainBinding.valueDownload.setText(event.getValueDown());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageDownloadFinish event) {
        if (event.getStatus() != "Ok"){
            Snackbar.make(mainBinding.progressDownload, "Error: " + event.getMsg(), Snackbar.LENGTH_LONG).show();
            downloadFile.onCancelled();
        } else {
            Snackbar.make(mainBinding.progressDownload, "Ruta File: " + event.getMsg(), Snackbar.LENGTH_LONG).show();
        }

        downloadFile = null;

        mainBinding.textLogError.append(event.getMsg() + "\n");

        mainBinding.btnStopDownload.setEnabled(false);
        mainBinding.btnStartDownload.setEnabled(true);
    }
}