package xilinch.cc.org.appupdate.multdownload;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import xilinch.cc.org.appupdate.R;


/**
 * @author xilinch on 2015/10/8.
 * @version 1.0
 * @description 下载子线程，用于多线程下载
 */
public class SubDownloadThread extends Thread {

    private long startIndex;
    private long endIndex;
    private String downloadUrl;
    private String saveFilePath;
    private int threadId;

    private ErrorListener errorListener;


    public SubDownloadThread(int threadId, long startIndex, long endIndex, String url, String saveFilePath) {

        this.threadId = threadId;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.downloadUrl = url;
        this.saveFilePath = saveFilePath;
        printi("threadId:" + threadId
                + "startIndex:" + startIndex
                + "endIndex:" + endIndex
                + "downloadUrl:" + downloadUrl
                + "saveFilePath:" + saveFilePath);

    }


    @Override
    public void run() {
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
            int responseCode = httpURLConnection.getResponseCode();
            printi("responseCode:" + responseCode);
            //成功返回206
            if (responseCode == 206) {
                RandomAccessFile randomAccessFile = new RandomAccessFile(saveFilePath, "rwd");
                File saveFile = new File(saveFilePath);
                randomAccessFile.seek(startIndex);
                InputStream inputStream = httpURLConnection.getInputStream();
                byte[] buffer = new byte[1 * 1024];
                int len = 0;
                while ((len = inputStream.read(buffer)) != -1) {

                    randomAccessFile.write(buffer, 0, len);
                    long fileLength = saveFile.length();

                }
                inputStream.close();
                randomAccessFile.close();
                printi("线程" + threadId + "下载完成----");
            } else {
                printi("服务器出错---");
            }

        } catch (Exception e) {
            printi(e.getMessage());
            if(errorListener != null){
                errorListener.onError();
            }
        } finally {

        }

    }

    public void setErrorListener(ErrorListener errorListener){
        this.errorListener = errorListener;
    }

    private void printi(String msg) {
        Log.i("my", msg);

    }

    private void printi(String tag, String msg) {
        Log.i(tag, msg);

    }

    public interface ErrorListener{
        void onError();
    }

}
