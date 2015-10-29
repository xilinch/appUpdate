package xilinch.cc.org.appupdate;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
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
import java.net.URL;
import java.net.URLConnection;

import xilinch.cc.org.appupdate.multdownload.SubDownloadThread;

/**
 * @author xilinch on 2015/10/19.
 * @version 1.2.0
 * @modifier xilinch 2015/10/19.
 * @description
 */
public class UpdateUtil implements SubDownloadThread.ErrorListener{
    private Context context;

    /**
     * http://www.baidu.com/zz.apk
     */
    private String downloadUrl;
    /**
     * /mnt/sdcard/qlk/
     */
    private String saveFilePath;
    /**
     * 1.更新xx
     * 2.升级xx
     * 3.修复xx
     */
    private String content;

    /**
     * 是否可以取消
     */
    private boolean cancel = false;

    /**
     *  3.4M
     */
    private String size;
    /**
     * 1.1.1
     */
    private String version;

    /**
     * 文件长度
     */
    private long fileLength;
    /**
     * 线程数目
     */
    private int defaultThreadCount = 1;

    /**
     * 默认升级对话框
     */
    private Dialog defaultUpgradeDialog;
    /**
     *  默认升级对话框视图
     */
    private View defaultDialogLayout;

    private CustomProgressWithPercentView xc_id_dialog_fl_percent;
    private LinearLayout xc_id_dialog_ll_percent_btns;

    /**
     * 自定义升级对话框
     */
    private Dialog customUpgradeDialog;

    /**
     * 自定义升级对话框事件
     */
    private DialogCallBack dialogCallBack;

    /**
     * 下载进度
     */
    private DownloadListener downloadListener;


    private DialogInterface.OnDismissListener onDismissListener;

    private int notificationDrawableId = R.mipmap.ic_launcher;
    private String notificationDrawableText = "开始下载";
    private Notification notification;
    private NotificationManager notificationManager;


    public static final int S_ERROR_CODE = 1;
    private static final int NOTIFICATION_ID = 0x66;
    public static final String S_DOWNLOADURL = "S_DOWNLOADURL";
    public static final String S_SAVEFILEPATH = "S_SAVEFILEPATH";
    public static final String S_DEFAULTTHREADCOUNT = "S_DEFAULTTHREADCOUNT";
    public static final String S_CONTENT = "S_CONTENT";
    public static final String S_VERSION = "S_VERSION";
    public static final String S_SIZE = "S_SIZE";
    public static final String S_CANCEL = "S_CANCEL";
    public static final String S_CACHE_DIRECTORY = "qlk";
    public static final String S_NOTIFICATIONDRAWABLEID = "notificationDrawableId";
    public static final String S_NOTIFICATIONDRAWABLETEXT = "notificationDrawableText";

    public static final int MSG_RENEW = 0;
    public static final int MSG_ERROR = 1;
    public static final int MSG_SECONDPROCESS = 3;


    private Handler handler = new Handler() {
        boolean isError = false;
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

                    int percent = getFileLength(getSaveFileName(downloadUrl));
                    printi("handleMessage -percent "+ percent);
                    if(percent >= 0 && percent <= 100){

                        renewDialog(percent, cancel);
                        updateNotifycation(percent, isError);

                    } else{
                        //文件不存在
                        shortToast("文件不存在.");
                    }

                    break;
                case MSG_ERROR:
                    if (notificationManager != null) {
                        notificationManager.cancel(NOTIFICATION_ID);
                    }
                    isError = true;
                    shortToast("网络连接错误,下载失败!");
                    break;
                case MSG_SECONDPROCESS:

//                    xc_id_dialog_progressBar_percent.setProgress(anima_process);
                    break;
                default:

            }

        }
    };

    public UpdateUtil(Context context){
        this.context = context;
//        this.downloadUrl = downloadUrl;
//        this.saveFilePath = saveFilePath;
    }

    public void setDefaultThreadCount (int defaultThreadCount){
        if(defaultThreadCount > 0){

            this.defaultThreadCount = defaultThreadCount;
        }
    }
    public void setSaveFilePath (String saveFilePath){
        if(!TextUtils.isEmpty(saveFilePath)){

            this.saveFilePath = saveFilePath;

        }
    }

    /**
     * 自定义对话框
     * @param dialog @Nullable
     */
    public void setCustomUpdateDialog(Dialog dialog){
        if(dialog != null ){
            this.customUpgradeDialog = dialog;

        } else{
            //do nothing
        }
    }

    /**
     * 自定义对话框
     * @param dialog @Nullable
     * @param dialogCallBack
     */
    public void setCustomUpdateDialog(Dialog dialog, DialogCallBack dialogCallBack){
        if(dialog != null ){
            this.customUpgradeDialog = dialog;
            this.dialogCallBack = dialogCallBack;
        } else{
            //do nothing
        }
    }

    public void setDownloadListener(DownloadListener downloadListener){
        if(downloadListener != null){
            this.downloadListener = downloadListener;
        }
    }
    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener){
        if(onDismissListener != null){
            this.onDismissListener = onDismissListener;
        }
    }



    public void start(Intent intent) {
        handlerIntent(intent);
    }


    private void handlerIntent(Intent intent){
        if(intent != null){
            //传值，指定 下载地址，存放路径，线程个数等等
            this.downloadUrl = intent.getStringExtra(S_DOWNLOADURL);
            this.saveFilePath = intent.getStringExtra(S_SAVEFILEPATH);
            this.cancel = intent.getBooleanExtra(S_CANCEL, true);
            this.content = intent.getStringExtra(S_CONTENT);
            this.version = intent.getStringExtra(S_VERSION);
            this.size = intent.getStringExtra(S_SIZE);
            this.notificationDrawableId = intent.getIntExtra(S_NOTIFICATIONDRAWABLEID, notificationDrawableId);
            String text = intent.getStringExtra(S_NOTIFICATIONDRAWABLETEXT);
            if(TextUtils.isEmpty(text)){
                this.notificationDrawableText = text;
            }
            this.defaultThreadCount = intent.getIntExtra(S_DEFAULTTHREADCOUNT, defaultThreadCount);

            if(TextUtils.isEmpty(saveFilePath)){
                //默认存放路径
                this.saveFilePath = getDefaultSaveFileName(downloadUrl);
            }

            if (URLUtil.isValidUrl(downloadUrl)) {
                //网址合法弹窗
                showUpgradeDialog(cancel,downloadUrl,content,version,size);

            } else{
                //下载出错
                shortToast("程序下载链接错误!");
            }


        }
    }

    /**
     * 处理下载
     */
    public void handleDownload(){
        //如果是可选升级，则需要将弹窗掩藏掉
        if (cancel) {
            if(defaultUpgradeDialog != null && defaultUpgradeDialog.isShowing()){

                defaultUpgradeDialog.dismiss();

            } else if (customUpgradeDialog != null && customUpgradeDialog.isShowing()){

                customUpgradeDialog.dismiss();
            }
        }

        if(URLUtil.isValidUrl(this.downloadUrl)){
            //合法地址
            if(this.downloadUrl.endsWith(".apk")){
                //以apk结尾的进入下载
                //首先创建下载文件夹
                boolean success = createFile(downloadUrl);
                //创建成功以后进入下载
                if(success ){
                    startDownload();
                } else{
                    shortToast("文件创建失败");
                }

            } else{
                //否则打开网页
                go2Url();
            }
        }
    }

    /**
     * 开始下载
     */
    private void startDownload(){
        new Thread(){
            @Override
            public void run() {
                String text = downloadUrl;
                printi("text:" + text);
                if(URLUtil.isValidUrl(text)){
                    //如果是合法地址，进入下载
                    printi("isValidUrl----");
                    try {
                        URL url = new URL(text);
                        URLConnection connection = url.openConnection();

                        connection.setConnectTimeout(5000);
                        fileLength = connection.getContentLength();
                        if(fileLength > 0){
                            //连接成功
                            createFile(text);
                            String filePath = getSaveFileName(text);

                            long blocks = fileLength / defaultThreadCount;
                            printi("fileLength:" + fileLength + " blocks:" + blocks);
                            for(int i = 0 ; i < defaultThreadCount; i++){
                                long startIndex = blocks * i;
                                long endIndex = 0;
                                if(i < defaultThreadCount-1){
                                    endIndex = blocks * (i +1) - 1;
                                } else if(i == (defaultThreadCount - 1)){
                                    endIndex = fileLength;
                                }
                                printi("start download");
                                SubDownloadThread subDownloadThread = new SubDownloadThread(i,startIndex,endIndex
                                        ,text,filePath);
                                subDownloadThread.setErrorListener(UpdateUtil.this);
                                subDownloadThread.start();
                            }
                        }

                        //进度更新
                        Message msg = handler.obtainMessage();
                        msg.what = MSG_RENEW;
                        handler.sendMessage(msg);
                        printi("send MSG_RENEW");
                    } catch(Exception e) {
                        String msg = e.getMessage();
                        System.out.println(msg);
                    } finally{

                    }

                }
            }
        }.start();
    }

    @Override
    public void onError() {
        printi("download onError");
        if(downloadListener != null){

            downloadListener.onError( S_ERROR_CODE );

        }
        //下载失败，
        if(handler != null){
            Message msg = handler.obtainMessage();
            msg.what = MSG_ERROR;
            handler.sendMessage(msg);
        }
        notification.contentView.setTextViewText(R.id.xl_remoteview_percent, "下载失败");
        //停止服务、以及释放内存
        stopService();
    }


    /**
     * 停止服务、以及释放内存
     */
    public void stopService(){

        //释放内存
//        context = null;
//        notificationManager = null;
//        customUpgradeDialog = null;
//        defaultUpgradeDialog = null;
//        defaultDialogLayout = null;
//        xc_id_dialog_fl_percent = null;
    }

    /**
     * 跳转到网址
     */
    private void go2Url(){
        //跳转到指定网址
        Uri uri = Uri.parse(downloadUrl);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void initNotification() {

        notificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
        notification = new Notification(notificationDrawableId, notificationDrawableText, NOTIFICATION_ID);
        notification.icon = notificationDrawableId;
        notification.contentView = new RemoteViews(context.getPackageName(), R.layout.xl_remoteview_notification);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,new Intent(),PendingIntent.FLAG_UPDATE_CURRENT);
        notification.contentIntent = pendingIntent;

        notification.contentView.setProgressBar(R.id.xl_remoteview_progress_percent, 100, 0, false);
        notification.contentView.setImageViewResource(R.id.xl_remoteview_iv, R.mipmap.ic_launcher);
        notification.contentView.setTextViewText(R.id.xl_remoteview_percent, 0 + "");

//        notificationManager.notify(NOTIFICATION_ID,notification);
    }

    private void showUpgradeDialog(final boolean canCancel, final String url, final String content
            , final String version, final String size) {
        if(customUpgradeDialog == null){
            if (defaultUpgradeDialog == null) {
                defaultUpgradeDialog = new Dialog(context, R.style.xc_s_dialog);

                defaultDialogLayout = LayoutInflater.from(context)
                        .inflate(R.layout.xl_dialog_upgrade, null);

                defaultUpgradeDialog.setContentView(defaultDialogLayout);
                defaultUpgradeDialog.setCanceledOnTouchOutside(true);
                Window window = defaultUpgradeDialog.getWindow();
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
            TextView xc_id_dialog_query_size = (TextView) defaultDialogLayout.findViewById(R.id.xc_id_dialog_query_size);
            xc_id_dialog_query_size.setText("新版本大小:      " + size);
            TextView xc_id_dialog_query_version = (TextView) defaultDialogLayout.findViewById(R.id.xc_id_dialog_query_version);
            xc_id_dialog_query_version.setText("最新版本:       " + version);
            TextView xc_id_dialog_query_content = (TextView) defaultDialogLayout.findViewById(R.id.xc_id_dialog_query_content);
            xc_id_dialog_query_content.setText(content);

            Button cancle = (Button) defaultDialogLayout.findViewById(R.id.xc_id_dialog_query_cancle);
            Button confirm = (Button) defaultDialogLayout.findViewById(R.id.xc_id_dialog_query_confirm);

            xc_id_dialog_ll_percent_btns = (LinearLayout) defaultDialogLayout.findViewById(R.id.xc_id_dialog_ll_percent_btns);
            xc_id_dialog_fl_percent = (CustomProgressWithPercentView) defaultDialogLayout.findViewById(R.id.xc_id_dialog_fl_percent);
            xc_id_dialog_fl_percent.setVisibility(View.GONE);


            initNotification();
            cancle.setText("取消");
            if (canCancel) {
                cancle.setVisibility(View.VISIBLE);

            } else {
                cancle.setVisibility(View.GONE);
            }
            confirm.setText("立即更新");
            defaultUpgradeDialog.setCancelable(canCancel);
            if(onDismissListener != null){
                defaultUpgradeDialog.setOnDismissListener(this.onDismissListener);
            }
            if (canCancel) {
                cancle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        defaultUpgradeDialog.dismiss();
                    }
                });
            } else {
                cancle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        defaultUpgradeDialog.dismiss();
                        //终结所有activity
//                    finishAllActivity();

                    }
                });
            }

            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    handleDownload();

                }
            });
//            defaultUpgradeDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
            defaultUpgradeDialog.show();
        } else {

            Button cancle = (Button) customUpgradeDialog.findViewById(R.id.xc_id_dialog_query_cancle);
            Button confirm = (Button) customUpgradeDialog.findViewById(R.id.xc_id_dialog_query_confirm);

            initNotification();
            if(cancle != null  && confirm != null){
                cancle.setText("取消");
                if (canCancel) {
                    cancle.setVisibility(View.VISIBLE);

                } else {
                    cancle.setVisibility(View.GONE);
                }
                confirm.setText("立即更新");
                customUpgradeDialog.setCancelable(canCancel);
                if (canCancel) {
                    cancle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (dialogCallBack != null) {
                                dialogCallBack.cancle();
                            }
                            customUpgradeDialog.dismiss();
                        }
                    });
                } else {
                    cancle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (dialogCallBack != null) {
                                dialogCallBack.cancle();
                            }
                            customUpgradeDialog.dismiss();
                            //终结所有activity
//                    finishAllActivity();
                        }
                    });
                }

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                            if (dialogCallBack != null) {

                                dialogCallBack.confirm();
                            }

                            handleDownload();

                    }
                });
            }

//            customUpgradeDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
            customUpgradeDialog.show();

        }

    }


    private void renewDialog(int percent,boolean canCancel){
        if(customUpgradeDialog == null){

            if (!canCancel && defaultUpgradeDialog != null && defaultUpgradeDialog.isShowing()) {
                if (xc_id_dialog_ll_percent_btns != null) {
                    xc_id_dialog_ll_percent_btns.setVisibility(View.GONE);
                }
                if (xc_id_dialog_fl_percent != null) {
                    xc_id_dialog_fl_percent.setVisibility(View.VISIBLE);
                }
                if (xc_id_dialog_fl_percent != null) {
                    xc_id_dialog_fl_percent.setProgress(percent);

                }

            }
        }

    }

    private void updateNotifycation(int percent,boolean isError){
        printi("updateNotifycation percent:" + percent);
        if(downloadListener != null){
            downloadListener.onProgress(percent);
        }
        if (notification != null) {

            notification.contentView.setProgressBar(R.id.xl_remoteview_progress_percent, 100, percent,
                    false);
            notification.contentView.setTextViewText(R.id.xl_remoteview_percent, "下载进度 "
                    + percent + "%");
            notificationManager
                    .notify(NOTIFICATION_ID, notification);

            if(percent < 100 && !isError){
                //继续更新
                Message message = handler.obtainMessage();
                message.what = MSG_RENEW;
                //延时一秒更新
                handler.sendMessageDelayed(message, 500);
            } else if(percent == 100){
                if(customUpgradeDialog != null){
                    customUpgradeDialog.dismiss();
                } else if(defaultUpgradeDialog != null){
                    defaultUpgradeDialog.dismiss();
                }
                installlApk();
                notification.contentView.setTextViewText(R.id.xl_remoteview_percent, "下载完成");
                notificationManager.notify(NOTIFICATION_ID, notification);

                //停止服务、以及释放内存
                stopService();
            }
        }
    }

    private int getFileLength(String saveFilePath){

        File saveFile = new File(saveFilePath);
        if(saveFile.exists()){
            long saveFileLength = saveFile.length();
            int percent = (int)(saveFileLength * 100 / fileLength);
            printi("fileLength:" + fileLength + "  saveFileLength:" + saveFileLength + " percent:"+ percent);
            return percent;
        }else{
            return -1;
        }

    }

    private void installlApk(){
        //准备拼接新的文件名（保存在存储卡后的文件名）
        String newFilename = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
        //      String newFilename = "dbyz";

        newFilename = saveFilePath + File.separator + newFilename;
        //进入安装
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(newFilename)),
                "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private boolean createFile(String urlStr){
        boolean success = false;
        //准备拼接新的文件名（保存在存储卡后的文件名）
        String newFilename = urlStr.substring(urlStr.lastIndexOf("/") + 1);

        newFilename = saveFilePath + File.separator + newFilename;

        File appFileDetory = new File(saveFilePath);
        if (!appFileDetory.exists()) {

            appFileDetory.mkdirs();
        }
        File file = new File(newFilename);
        //如果目标文件已经存在，则删除。产生覆盖旧文件的效果
        if (file.exists()) {
            //
            file.delete();
        }
        try {
            success = file.createNewFile();
        } catch(Exception e) {
            e.printStackTrace();

        }
        return success;
    }

    private String getSaveFileName(String urlStr){
        String newFilename = urlStr.substring(urlStr.lastIndexOf("/") + 1);

        newFilename = saveFilePath + File.separator + newFilename;
        return newFilename;
    }

    private String getDefaultSaveFileName(String urlStr){
        String newFilename = urlStr.substring(urlStr.lastIndexOf("/") + 1);
        String appFile = Environment.getExternalStorageDirectory().getAbsolutePath() + System.getProperty("file.separator") +
                S_CACHE_DIRECTORY + "/apk";
        newFilename = appFile + File.separator + newFilename;
        return newFilename;
    }

    private void printi(String msg) {
//        Log.e("---------", msg);
        System.out.println(msg);
    }

    private void printi(String tag,String msg){
        Log.e(tag, msg);

    }

    private void shortToast(String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }


    public interface DialogCallBack {
         void confirm();

         void cancle();
    }

    public interface DownloadListener {
         void onProgress(int progress);

         void onError(int errorCode);
    }
}
