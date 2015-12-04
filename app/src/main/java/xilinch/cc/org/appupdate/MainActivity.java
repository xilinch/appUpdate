package xilinch.cc.org.appupdate;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import xilinch.cc.upgradelib.UpdateUtil;

/**
 * @author xilinch on 2015/10/8.
 * @version 1.0
 * @description
 */
public class MainActivity extends Activity {

    private Button xl_btn_normalUpdate,xl_btn_forceUpdate;
    private UpdateUtil UpdateUtil;
    private boolean cancel = false;
    private static final String CACHE_DIRECTORY = "qlk";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        initWidgets();
        listeners();

    }

    public void initWidgets() {
        xl_btn_normalUpdate =  (Button) findViewById(R.id.xl_btn_normalUpdate);
        xl_btn_forceUpdate =  (Button) findViewById(R.id.xl_btn_forceUpdate);
    }

    public void listeners() {
        final String downloadUrl = "http://dl.37wan.cn/upload/1_1001655_10004/guailixingbaiwanyasewang_10004.apk";
        final String saveFilePath = getSaveFile();
        UpdateUtil = new UpdateUtil(this);
        xl_btn_normalUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel = true;
                Intent intent = new Intent();
                intent.putExtra(UpdateUtil.S_SIZE, "34M");
                intent.putExtra(UpdateUtil.S_VERSION, "1.2.0");
                intent.putExtra(UpdateUtil.S_CONTENT,"1.增加更多功能;\n2.修复若干bug;\n3.界面优化;");
                intent.putExtra(UpdateUtil.S_CANCEL,cancel);
                intent.putExtra(UpdateUtil.S_DOWNLOADURL,downloadUrl);
                intent.putExtra(UpdateUtil.S_SAVEFILEPATH, saveFilePath);
                intent.putExtra(UpdateUtil.S_NOTIFICATIONDRAWABLEID, R.mipmap.d_upgrade_ic);
                intent.putExtra(UpdateUtil.S_NOTIFICATIONDRAWABLETEXT, "开始下载");
                UpdateUtil.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //强制升级，关闭应用程序,可选升级自己做逻辑出处理

                    }
                });

//                final Dialog dialog = new Dialog(MainActivity.this);
//                dialog.setContentView(R.layout.xl_dialog_upgrade);
//                dialog.findViewById(R.id.xc_id_dialog_query_confirm).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Toast.makeText(MainActivity.this, "click confirm", Toast.LENGTH_SHORT).show();
//                        //确认按钮
//                        if (UpdateUtil != null) {
//                            UpdateUtil.handleDownload();
//                        }
//                        if (cancel) {
//                            dialog.dismiss();
//                        }
//                    }
//                });
//                dialog.findViewById(R.id.xc_id_dialog_query_cancle).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Toast.makeText(MainActivity.this, "click cancel", Toast.LENGTH_SHORT).show();
//                        //取消按钮
//                        if(cancel){
//                            dialog.dismiss();
//                        }
//                    }
//                });
//                UpdateUtil.setCustomUpdateDialog(dialog);
//                UpdateUtil.setDownloadListener(MainActivity.this);
                UpdateUtil.start(intent);
            }
        });

        xl_btn_forceUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel = false;
                Intent intent1 = new Intent();
                intent1.putExtra(UpdateUtil.S_SIZE, "34M");
                intent1.putExtra(UpdateUtil.S_VERSION, "1.2.0");
                intent1.putExtra(UpdateUtil.S_CONTENT, "1.增加更多功能;\n2.修复若干bug;\n3.界面优化;");
                intent1.putExtra(UpdateUtil.S_CANCEL, cancel);
                intent1.putExtra(UpdateUtil.S_DOWNLOADURL, downloadUrl);
                intent1.putExtra(UpdateUtil.S_SAVEFILEPATH, saveFilePath);
                intent1.putExtra(UpdateUtil.S_NOTIFICATIONDRAWABLEID, R.mipmap.d_upgrade_ic);
                intent1.putExtra(UpdateUtil.S_NOTIFICATIONDRAWABLETEXT, "开始下载");
                UpdateUtil.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //强制升级，关闭应用程序,可选升级自己做逻辑出处理
                    }
                });
                UpdateUtil.start(intent1);
            }
        });
    }

    private String getSaveFile(){

        return Environment.getExternalStorageDirectory().getAbsolutePath() + System.getProperty("file.separator") +
                CACHE_DIRECTORY + "/apk";
    }

}
