package com.solucioneshr.soft.downloadtest;

import android.os.AsyncTask;
import android.os.Environment;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFile extends AsyncTask<String, Integer, String> {
    private int lengthFile = 0;
    private String nameFolder = "";

    public DownloadFile(String nameFolder) {
        this.nameFolder = nameFolder;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        cancel(true);
    }

    @Override
    protected String doInBackground(String... strings) {
        int count = 0;
        try{
            URL url = new URL(strings[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            lengthFile = connection.getContentLength();

            InputStream downFile = new BufferedInputStream(url.openStream(), 8192);

            String pathSave = Environment.getExternalStorageDirectory().toString() + "/" + nameFolder;
            dirChecker(pathSave);
            String tmp[] = url.getFile().split("/");
            String nameFile = tmp[tmp.length -  1];
            OutputStream saveFile = new FileOutputStream(pathSave + "/" + nameFile);

            byte data[] = new byte[1024];
            long total = 0;

            EventBus.getDefault().postSticky(new MessageInitDownload(getLengthDown(lengthFile)));

            while ((count = downFile.read(data)) != -1){
                total += count;
                saveFile.write(data, 0, count);
                publishProgress((int) total);
                if (isCancelled()){
                    downFile.close();
                    break;
                }
            }

            saveFile.flush();
            saveFile.close();
            downFile.close();

            EventBus.getDefault().postSticky(new MessageDownloadFinish("Ok", pathSave + "/" + nameFile));

        } catch (Exception error){
            EventBus.getDefault().postSticky(new MessageDownloadFinish("Error", error.getMessage()));
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Integer value = (int)((values[0] * 100)/lengthFile);
        String valueDown = getLengthDown(values[0]);
        EventBus.getDefault().postSticky(new MessageProgressValue(value, valueDown));
    }

    private void dirChecker(String filepath) {
        File file = new File(filepath);
        if (!file.isDirectory()) {
            file.mkdir();
        }
    }

    private String getLengthDown (int size){
        String data = "";

        float kb = size/1024;
        float mb = kb/1024;
        float gb = mb/1024;
        float tb = gb/1024;

        if(size < 1024){
            data = size + " Bytes";
        } else if(size >= 1024 && size < 1048576){
            data = String.format("%.2f", kb) + " KB";
        } else if(size >= 1048576 && size < 1073741824){
            data = String.format("%.2f", mb) + " MB";
        } else if(size >= 1073741824 && size < (1073741824 * 1024)){
            data = String.format("%.2f", gb) + " GB";
        } else if(size >= (1073741824 * 1024)){
            data = String.format("%.2f", tb) + " TB";
        }

        return data;
    }
}
