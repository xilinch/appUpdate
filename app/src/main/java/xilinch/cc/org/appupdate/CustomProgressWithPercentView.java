package xilinch.cc.org.appupdate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * Created by xiilinch on 2015/9/22.
 */
public class CustomProgressWithPercentView extends FrameLayout {

    private int progressAnimationStart = 0;
    private int progressAnimationStartRate = 0;
    private ProgressBar progressBar;
    private TextView tv_percent;
    private Context context;
    private int currentProgress;
    private int anima_process;
    private static int MAX = 100;
    private int width;
    private Thread myRunnable;
    private Runnable updateRunnable;

    public ProgressBar getProgressBar() {
        return this.progressBar;
    }

    public TextView getTextView() {
        return this.tv_percent;
    }

    public int getCurrentProgress(){
        return this.currentProgress;
    }


    public CustomProgressWithPercentView(Context context) {
        this(context, null);
    }

    public CustomProgressWithPercentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomProgressWithPercentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        setWidgets();

        init();


    }

    private void setWidgets(){
        progressBar = new ProgressBar(context,null,R.style.customProgressBar);
        progressBar.setMax(MAX);
        progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.dd_color_progressbar));
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                UtilImage.dip2px(context,40));
        addView(progressBar,layoutParams);

        tv_percent = new TextView(context);
        tv_percent.setGravity(Gravity.CENTER_VERTICAL);
        tv_percent.setText("");
        tv_percent.setTextColor(getResources().getColor(R.color.c_white_ffffff));
        LayoutParams layoutParams1 = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams1.gravity = Gravity.CENTER_VERTICAL;

        addView(tv_percent, layoutParams1);

    }


    private void init() {
        currentProgress = 0;

        progressAnimationStart = currentProgress * progressAnimationStartRate / 10;
        if (progressBar != null) {
            progressBar.setProgress(0);
            progressBar.setMax(MAX);
        }
        setPercentFormat(0);

    }


    /**
     * show progress
     *
     * @param progress
     */
    public void setProgress(int progress) {
        currentProgress = progress;
        progressAnimationStart = currentProgress * progressAnimationStartRate /10;
        setPercentFormat(progress);
        progressBar.setSecondaryProgress(progress);

    }


    private void initUpdateRunnable(){
        if(updateRunnable == null){
            updateRunnable = new Runnable() {
                @Override
                public void run() {
                    if(progressBar != null){
                        progressBar.setProgress(anima_process);

                    }
                }
            };
        }
    }


    private void startRepeatAnimationRunnable() {
        if (myRunnable == null) {
            myRunnable = new Thread() {
                @Override
                public void run() {
                    //动画
                    while (anima_process != 100) {
                        if(anima_process == 0){
                            anima_process = progressAnimationStart ;
                        }

                        anima_process = ((++anima_process) % (currentProgress + 1));
                        initUpdateRunnable();
                        post(updateRunnable);

                        try {
                            sleep(1500 / (currentProgress + 1));
                        } catch (Exception e) {
                            e.printStackTrace();

                        }


                    }
                }
            };

            myRunnable.start();
        }
    }

    /**
     * show progress
     *
     * @param progress
     */
    public void setProgress(String progress) {
        try {
            int int_progress = Integer.valueOf(progress);
            setProgress(int_progress);


        } catch (Exception e) {

        } finally {

        }

    }

    /**
     * set percent textview text format
     * ex: 15%
     */
    public void setPercentFormat(int percent) {

        if (tv_percent != null) {
            tv_percent.setText(percent + "%");
            layoutPercentTextView(percent);
        }
    }

    /**
     * set the text of textview  format
     * ex: 15%
     */
    public void setPercentFormat(String percent) {
        if (tv_percent != null) {
            tv_percent.setText(percent + "%");
            try {
                int int_percent = Integer.valueOf(percent);
                layoutPercentTextView(int_percent);

            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    /**
     * calcute textview layout width
     *
     * @param percent
     */
    private void layoutPercentTextView(int percent) {
        startRepeatAnimationRunnable();
        width = getWidth();
//        System.out.println("width:" + width);

        if (width != 0) {
            if (percent < 50) {
                int padding = width * percent / 100 + 10;
                tv_percent.setGravity(Gravity.CENTER_VERTICAL);
                tv_percent.setPadding(padding, 0, 0, 0);
            } else {

                int padding = width - width * percent /100 + 20;
                tv_percent.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                tv_percent.setPadding(0, 0, padding, 0);
            }
        }

    }


}
