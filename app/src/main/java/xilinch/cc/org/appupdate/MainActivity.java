package xilinch.cc.org.appupdate;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author xilinch on 2015/10/8.
 * @version 1.0
 * @description
 */
public class MainActivity extends Activity implements View.OnClickListener,UpdateService.DownloadListener{

    Button btn1,btn2;
    EditText et;
    UpdateService updateService;
    boolean cancel = false;
    public static final String CACHE_DIRECTORY = "qlk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        initListener();
    }

    private void findViews(){
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        et = (EditText) findViewById(R.id.xl_et_url);
    }

    private void initListener(){
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        String downloadUrl = et.getText().toString();
        String saveFilePath = getSaveFile();
        updateService = new UpdateService(MainActivity.this);

        switch (v.getId()){
            case R.id.btn1:

//                Intent intent = new Intent(MainActivity.this,UpdateService.class);
//                intent.putExtra(UpdateService.S_SIZE, "34M");
//                intent.putExtra(UpdateService.S_VERSION, "1.2.0");
//                intent.putExtra(UpdateService.S_CONTENT,"1.增加更多功能;\n2.修复若干bug;\n3.界面优化;");
//                intent.putExtra(UpdateService.S_CANCEL,true);
//                intent.putExtra(UpdateService.S_DOWNLOADURL,downloadUrl);
//                intent.putExtra(UpdateService.S_SAVEFILEPATH, saveFilePath);
//                startService(intent);
                cancel = true;
                Intent intent = new Intent();
                intent.putExtra(UpdateService.S_SIZE, "34M");
                intent.putExtra(UpdateService.S_VERSION, "1.2.0");
                intent.putExtra(UpdateService.S_CONTENT,"1.增加更多功能;\n2.修复若干bug;\n3.界面优化;");
                intent.putExtra(UpdateService.S_CANCEL,cancel);
                intent.putExtra(UpdateService.S_DOWNLOADURL,downloadUrl);
                intent.putExtra(UpdateService.S_SAVEFILEPATH, saveFilePath);
                updateService = new UpdateService(MainActivity.this);

                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.xl_dialog_upgrade);
                dialog.findViewById(R.id.xc_id_dialog_query_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "click confirm", Toast.LENGTH_SHORT).show();
                        //确认按钮
                        if (updateService != null) {
                            updateService.handleDownload();
                        }
                        if (cancel) {
                            dialog.dismiss();
                        }
                    }
                });
                dialog.findViewById(R.id.xc_id_dialog_query_cancle).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "click cancel", Toast.LENGTH_SHORT).show();
                        //取消按钮
                        if(cancel){
                            dialog.dismiss();
                        }
                    }
                });
                updateService.setCustomUpdateDialog(dialog);
                updateService.setDownloadListener(MainActivity.this);
                updateService.start(intent);

                break;
            case R.id.btn2:

//                Intent intent = new Intent(MainActivity.this,UpdateService.class);
//                intent.putExtra(UpdateService.S_SIZE, "34M");
//                intent.putExtra(UpdateService.S_VERSION, "1.2.0");
//                intent.putExtra(UpdateService.S_CONTENT,"1.增加更多功能;\n2.修复若干bug;\n3.界面优化;");
//                intent.putExtra(UpdateService.S_CANCEL,false);
//                intent.putExtra(UpdateService.S_DOWNLOADURL,downloadUrl);
//                intent.putExtra(UpdateService.S_SAVEFILEPATH,saveFilePath);
//                startService(intent);
                cancel = false;
                Intent intent1 = new Intent();
                intent1.putExtra(UpdateService.S_SIZE, "34M");
                intent1.putExtra(UpdateService.S_VERSION, "1.2.0");
                intent1.putExtra(UpdateService.S_CONTENT,"1.增加更多功能;\n2.修复若干bug;\n3.界面优化;");
                intent1.putExtra(UpdateService.S_CANCEL,cancel);
                intent1.putExtra(UpdateService.S_DOWNLOADURL,downloadUrl);
                intent1.putExtra(UpdateService.S_SAVEFILEPATH, saveFilePath);
                updateService.start(intent1);
                break;
        }
    }

    @Override
    public void onProgress(int progress) {
        Log.i("my","onProgress:" + progress);
    }

    @Override
    public void onError(int errorCode) {
        Log.i("my","onError:" + errorCode);
    }

    private String getSaveFile(){

        return Environment.getExternalStorageDirectory().getAbsolutePath() + System.getProperty("file.separator") +
                CACHE_DIRECTORY + "/apk";
    }


}
