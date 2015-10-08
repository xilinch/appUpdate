package xilinch.cc.org.appupdate;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by xilinch on 2015/9/21.
 */
public class UpdateActivity extends Activity implements View.OnClickListener{

    Button btn1;
    Button btn2;

    private Dialog upgradeDialog;
    private View dialog_layout;
    private Notification notification;
    private NotificationManager notificationManager;
    private static final int NOTIFICATION_ID = 0x66;
    private CustomProgressWithPercentView xc_id_dialog_fl_percent;
    private LinearLayout xc_id_dialog_ll_percent_btns;

    public static final String CACHE_DIRECTORY = "update";
    public static final int MSG_RENEW = 0;
    public static final int MSG_ERROR = 1;
    public static final int MSG_SECONDPROCESS = 3;

    private String urlStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        initWidgets();
        listeners();
    }


    public void initWidgets() {
        btn1 = (Button)findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);

    }


    public void listeners() {
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {

        String url = "http://dl.37wan.cn/upload/1_1001655_10004/guailixingbaiwanyasewang_10004.apk";
        String prompt = "测试测试才擦擦擦擦擦擦擦擦擦擦";
        String lastVerNum = 3 + "";
        String lastVerSize = 32 + "";
        switch (v.getId()) {
            case R.id.btn1:
                //可选升级
                showUpgradeDialog(true, url, prompt, lastVerNum, lastVerSize);
                break;
            case R.id.btn2:
                //强制升级
                showUpgradeDialog(false, url, prompt, lastVerNum, lastVerSize);
                break;
        }
    }




    private void initNotification() {

        notificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
        notification = new Notification(R.mipmap.ic_launcher, "开始下载", NOTIFICATION_ID);
        notification.icon = R.mipmap.ic_launcher;
        notification.contentView = new RemoteViews(getPackageName(), R.layout.xl_remoteview_notification);
        notification.contentView.setProgressBar(R.id.xl_remoteview_progress_percent, 100, 0, false);
        notification.contentView.setImageViewResource(R.id.xl_remoteview_iv, R.mipmap.ic_launcher);
        notification.contentView.setTextViewText(R.id.xl_remoteview_percent, 0 + "");
    }

    private void showUpgradeDialog(final boolean canCancel, final String url, final String content
            , final String version, final String size) {
        if (upgradeDialog == null) {
            upgradeDialog = new Dialog(this, R.style.xc_s_dialog);

            dialog_layout = LayoutInflater.from(UpdateActivity.this)
                    .inflate(R.layout.xl_dialog_upgrade, null);

            upgradeDialog.setContentView(dialog_layout);
            upgradeDialog.setCanceledOnTouchOutside(true);
            Window window = upgradeDialog.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            // window .setGravity(Gravity.LEFT | Gravity.TOP);
            // lp.x = 100; // 新位置X坐标
            // lp.y = 100; // 新位置Y坐标
            // lp.width = 300; // 宽度
            // lp.height = 300; // 高度

            lp.alpha = 1.0f;
            lp.dimAmount = 0.3f;

            window.setAttributes(lp);
        }
        TextView xc_id_dialog_query_size = (TextView) dialog_layout.findViewById(R.id.xc_id_dialog_query_size);
        xc_id_dialog_query_size.setText("新版本大小:      " + size);
        TextView xc_id_dialog_query_version = (TextView) dialog_layout.findViewById(R.id.xc_id_dialog_query_version);
        xc_id_dialog_query_version.setText("最新版本:       " + version);
        TextView xc_id_dialog_query_content = (TextView) dialog_layout.findViewById(R.id.xc_id_dialog_query_content);
        xc_id_dialog_query_content.setText(content);

        Button cancle = (Button) dialog_layout.findViewById(R.id.xc_id_dialog_query_cancle);
        Button confirm = (Button) dialog_layout.findViewById(R.id.xc_id_dialog_query_confirm);

        xc_id_dialog_ll_percent_btns = (LinearLayout) dialog_layout.findViewById(R.id.xc_id_dialog_ll_percent_btns);
        xc_id_dialog_fl_percent = (CustomProgressWithPercentView) dialog_layout.findViewById(R.id.xc_id_dialog_fl_percent);
        xc_id_dialog_fl_percent.setVisibility(View.GONE);


        initNotification();
        cancle.setText("取消");
        if (canCancel) {
            cancle.setVisibility(View.VISIBLE);

        } else {
            cancle.setVisibility(View.GONE);
        }
        confirm.setText("立即更新");
        upgradeDialog.setCancelable(canCancel);
        if (canCancel) {
            cancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    upgradeDialog.dismiss();
                }
            });
        } else {
            cancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    upgradeDialog.dismiss();
                    //TODO 终结所有activity
//                    finishAllActivity();
                }
            });
        }

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (URLUtil.isValidUrl(url)) {
                    urlStr = url;

                    //如果是可选升级，则需要将弹窗掩藏掉
                    if (canCancel) {
                        upgradeDialog.dismiss();
                    }

                    downloadApk(url, canCancel);
                } else {
                    shortToast("程序下载链接错误!");

                }


            }
        });
        upgradeDialog.show();
    }


    private void downloadApk(final String urlStr, final boolean canCancel) {

        new AsyncTask<String, String, String>() {

            long tt = 0;

            /**
             * Override this method to perform a computation on a background thread. The
             * specified parameters are the parameters passed to {@link #execute}
             * by the caller of this task.
             * <p/>
             * This method can call {@link #publishProgress} to publish updates
             * on the UI thread.
             *
             * @param params The parameters of the task.
             * @return A result, defined by the subclass of this task.
             * @see #onPreExecute()
             * @see #onPostExecute
             * @see #publishProgress
             */
            @Override
            protected String doInBackground(String... params) {

                //准备拼接新的文件名（保存在存储卡后的文件名）
                String newFilename = urlStr.substring(urlStr.lastIndexOf("/") + 1);
                String appFile = Environment.getExternalStorageDirectory().getAbsolutePath() + System.getProperty("file.separator") +
                         CACHE_DIRECTORY + "/apk";
                newFilename = appFile + File.separator + newFilename;

                File appFileDetory = new File(appFile);
                if (!appFileDetory.exists()) {

                    appFileDetory.mkdirs();
                }
                File file = new File(newFilename);
                //如果目标文件已经存在，则删除。产生覆盖旧文件的效果
                if (file.exists()) {
                    //TODO 判断是否支持断点下载
                    file.delete();
                }

                try {
                    ///创建文件
                    file.createNewFile();
                    // 构造URL
                    URL url = new URL(urlStr);
                    // 打开连接
                    URLConnection con = url.openConnection();
                    //获得文件的长度
                    int contentLength = con.getContentLength();
                    printi("长度 :" + contentLength);
                    if (contentLength <= 0) {
                        //TODO 长度出错了
                        return null;
                    }
                    // 输入流
                    InputStream is = con.getInputStream();
                    // 8K的数据缓冲
                    byte[] bs = new byte[8 * 1024];
                    // 读取到的数据长度
                    int len;
                    long total = 0;
                    // 输出的文件流
                    OutputStream os = new FileOutputStream(newFilename);
                    // 开始读取
                    while ((len = is.read(bs)) != -1) {
                        total = total + len;
                        os.write(bs, 0, len);
                        int progress = (int) (total * 100 / contentLength);

                        long now = System.currentTimeMillis();
                        if (now - tt >= 1500) {
                            publishProgress(progress + "");
                            tt = now;
                            Message msg = handler.obtainMessage();
                            msg.what = MSG_RENEW;
                            msg.arg1 = progress;
                            handler.sendMessage(msg);
                        }


                    }
                    os.flush();
                    // 完毕，关闭所有链接
                    os.close();
                    is.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    return "0";
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                tt = System.currentTimeMillis();


            }

            @Override
            protected void onPostExecute(String aVoid) {
                super.onPostExecute(aVoid);


                //取消notifacation
                if (notificationManager != null) {
                    notificationManager.cancel(NOTIFICATION_ID);
                }
                String result = aVoid;
                if (result == null) {
                    if (canCancel) {
                        //下载完成
                        Message msg = handler.obtainMessage();
                        msg.what = 0;
                        msg.arg1 = 100;
                        handler.sendMessage(msg);
                    } else {
                        if (xc_id_dialog_fl_percent != null) {
                            xc_id_dialog_fl_percent.setProgress(100);
//                            xc_id_dialog_progressBar_percent.setProgress(100);
                        }

                    }


                    //准备拼接新的文件名（保存在存储卡后的文件名）
                    String newFilename = urlStr.substring(urlStr.lastIndexOf("/") + 1);
                    //      String newFilename = "dbyz";
                    String appFile = Environment.getExternalStorageDirectory().getAbsolutePath() + System.getProperty("file.separator") +
                            CACHE_DIRECTORY + "/apk";
                    newFilename = appFile + File.separator + newFilename;
                    //进入安装
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(newFilename)),
                            "application/vnd.android.package-archive");
                    UpdateActivity.this.startActivity(intent);
                } else {
                    handler.sendEmptyMessage(MSG_ERROR);
                }


            }


            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
                //获取文件大小
                String percent_str = values[0];
                int percent = Integer.valueOf(percent_str);
                if (!canCancel && upgradeDialog != null && upgradeDialog.isShowing()) {
                    if (xc_id_dialog_ll_percent_btns != null) {
                        xc_id_dialog_ll_percent_btns.setVisibility(View.GONE);
                    }
                    if (xc_id_dialog_fl_percent != null) {
                        xc_id_dialog_fl_percent.setVisibility(View.VISIBLE);
                    }
                    if (xc_id_dialog_fl_percent != null) {
                        xc_id_dialog_fl_percent.setProgress(percent);

                        //TODO 进度动画,整合进入 view
                        Message msg = handler.obtainMessage();
                        msg.what = MSG_RENEW;
                        msg.arg1 = percent;
                        handler.sendMessage(msg);



                    }

                }


            }
        }.execute();
    }


    private Handler handler = new Handler() {

        int anima_process = 0;

        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;

            switch (what) {
                case MSG_RENEW:
                    if (notification != null) {
                        notification.contentView.setProgressBar(R.id.xl_remoteview_progress_percent, 100, msg.arg1,
                                false);
                        notification.contentView.setTextViewText(R.id.xl_remoteview_percent, "下载进度 "
                                + msg.arg1 + "%");
                        notificationManager
                                .notify(NOTIFICATION_ID, notification);
                    }


                    break;
                case MSG_ERROR:
                    if (notificationManager != null) {
                        notificationManager.cancel(NOTIFICATION_ID);
                    }

                    shortToast("网络连接错误,下载失败!");
                    break;
                case MSG_SECONDPROCESS:

//                    xc_id_dialog_progressBar_percent.setProgress(anima_process);
                    break;
                default:

            }

        }
    };


    private void shortToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    private void printi(String msg){

        System.out.println(msg);
    }

}
