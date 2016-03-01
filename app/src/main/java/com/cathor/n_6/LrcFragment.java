package com.cathor.n_6;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Cathor on 2015/9/19.
 */
public class LrcFragment extends Fragment {

    private List<String> mLrcs = new ArrayList<String>(); // 存放歌词
    private List<Long> mTimes = new ArrayList<Long>(); // 存放时间

    private long mNextTime = 0l; // 保存下一句开始的时间
    private long mCurrentTime = -1l;
    private int mCurrentLine = 0; // 当前行

    private int mNormalColor = 0xff808080;
    private int mCurrentColor = Color.BLACK;

    private boolean isPrepared = false;
    private boolean isLrc = true;

    private float mTextSize; // 字体
    private int mDividerHeight; // 行间距

    private int maxWidth;

    private Context context;
    private ArrayList<TextView> lyrics;
    private RelativeLayout layout;

    private RelativeLayout parent;

    private ValueAnimator va;

    private static int preMargin;

    public final static String TEXT_SIZE = "textSize";
    public final static String DIVIDER_HEIGHT = "dividerHeight";


    public LrcFragment(){}

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mTextSize = args.getFloat(TEXT_SIZE);
        mDividerHeight = args.getInt(DIVIDER_HEIGHT);
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1111:
                    va.start();
                    break;
                case 1112:
                    int pre = msg.getData().getInt("pre");
                    int now = msg.getData().getInt("now");
                    lyrics.get(pre).setTextColor(mNormalColor);
                    lyrics.get(pre).setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    lyrics.get(now).setTextColor(mCurrentColor);
                    lyrics.get(now).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    break;
            }
        }
    };

    // 解析时间
    private Long parseTime(String time) {
        // 03:02.12
        String[] min = time.split(":");
        String[] sec = min[1].split("\\.");

        long minInt = Long.parseLong(min[0].replaceAll("\\D+", "")
                .replaceAll("\r", "").replaceAll("\n", "").trim());

        long secInt = Long.parseLong(sec[0].replaceAll("\\D+", "")
                .replaceAll("\r", "").replaceAll("\n", "").trim());

        if(sec[1].length() == 2){
            long milInt = Long.parseLong(sec[1].replaceAll("\\D+", "")
                    .replaceAll("\r", "").replaceAll("\n", "").trim());

            return minInt * 60 * 1000 + secInt * 1000 + milInt * 10;
        }
        else{
            long milInt = Long.parseLong(sec[1].replaceAll("\\D+", "")
                    .replaceAll("\r", "").replaceAll("\n", "").trim());

            return minInt * 60 * 1000 + secInt * 1000 + milInt;
        }

    }

    //希尔算法排序
    public static void sortByShell(List<Long> times, List<String> lrcs){
        int size = times.size();
        int span = size / 2;
        while(span > 0){
            for(int i = 0; i < span; i++){
                for(int k = i + span; k < size; ){
                    if(times.get(k - span) > times.get(k)){
                        long temp = times.get(k - span);
                        String lrc = lrcs.get(k - span);
                        times.set(k - span, times.get(k));
                        lrcs.set(k - span, lrcs.get(k));
                        times.set(k, temp);
                        lrcs.set(k, lrc);
                        if(k != i + span){
                            k -= span;
                        }
                        else{
                            k += span;
                        }
                    }
                    else{
                        k += span;
                    }
                }
            }
            span /= 2;
        }
    }

    // 解析每行
    private Map<Long, String> parseLine(String pre) {
        String line = pre;
        Matcher matcher = Pattern.compile(".*\\[\\d+:\\d+\\.\\d+\\].*").matcher(line);
        // 如果形如：[xxx]后面啥也没有的，则return空
        if (!matcher.matches()) {
            Logger.INSTANCE.d("throw s " + line);
            return null;
        }
        // 反感歌词里放[](ノ｀Д)ノ solved
        line = line.replaceAll("\\[(\\d+:\\d+\\.\\d+)\\]", "$1-#_#-");  // 但愿没有歌词里面不会出现这么堆符号凸(艹皿艹 )
        Map<Long, String>resultMap = new HashMap<>();

        Logger.INSTANCE.d(line);
        String[] result = line.split("-#_#-");
        int last = line.lastIndexOf("-#_#-");
        if(last != line.length() - 5) {
            if (result.length == 1) {
                resultMap.put(-1L, result[0]);
            }
            for (int i = 0; i < result.length - 1; i++) {
                resultMap.put(parseTime(result[i]), result[result.length - 1]);
            }

        }
        else{
            for (int i = 0; i < result.length; i++) {
                resultMap.put(parseTime(result[i]), " ");
            }

        }
        return resultMap;
    }

    //设置路径基础上改变颜色
   /* public int setLrcPath(String path, int nColor, int cColor) throws IOException {
        setColors(nColor, cColor);
        return setLrcPath(path);
    }*/

        // 设置lrc的路径
    public int setLrcPath(String path) throws IOException {
        isPrepared = false;

        mLrcs = new ArrayList<>();
        mTimes = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) {
            noLrc();
            return 0;
        }
        if(path.endsWith(".lrc")){
            isLrc = true;
        }
        else{
            isLrc = false;
        }
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        bis.mark(4);
        BufferedReader reader;

        byte[] first3bytes=new byte[3];
//   Logger.INSTANCE.d("");
        //找到文档的前三个字节并自动判断文档类型。
        bis.read(first3bytes);
        bis.reset();
        if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
                && first3bytes[2] == (byte) 0xBF) {// utf-8
            //Log.v("asasssa","asa");
            reader = new BufferedReader(new InputStreamReader(bis, "utf-8"));

        } else if (first3bytes[0] == (byte) 0xFF
                && first3bytes[1] == (byte) 0xFE) {
            //Log.v("asaaasa","asa");
            reader = new BufferedReader(
                    new InputStreamReader(bis, "unicode"));
        } else if (first3bytes[0] == (byte) 0xFE
                && first3bytes[1] == (byte) 0xFF) {
            //Log.v("asasa","asa");

            reader = new BufferedReader(new InputStreamReader(bis,
                    "utf-16be"));
        } else if (first3bytes[0] == (byte) 0xFF
                && first3bytes[1] == (byte) 0xFF) {
            //Logger.INSTANCE.d("UTF-16le");
            reader = new BufferedReader(new InputStreamReader(bis,
                    "utf-16le"));
        } else {
            //Logger.INSTANCE.d("GBK");
            reader = new BufferedReader(new InputStreamReader(bis, "GBK"));
        }
        if(isLrc) {
            String line = "";
            Map<Long, String> arr;
            int preSize = 0;
            while (null != (line = reader.readLine())) {
                arr = parseLine(line);
                if (arr == null || arr.size() == 0) {
                    continue;
                }
                // 如果解析出来只有一个
                for (Map.Entry<Long, String> entry : arr.entrySet()) {
                    if (-1L == entry.getKey()) {
                        for (int i = 1; i < preSize; i += 1) {
                            String last = mLrcs.remove(mLrcs.size() - i);
                            mLrcs.add(last + entry.getValue());
                        }
                        break;
                    }
                    preSize = arr.size();
                    long time = entry.getKey();
                    int index;
                    if((index = mTimes.indexOf(time)) != -1){
                        mLrcs.set(index, mLrcs.get(index) + "\n" + entry.getValue());
                    }
                    else{
                        mTimes.add(entry.getKey());
                        mLrcs.add(entry.getValue());
                    }

                }
            }
            sortByShell(mTimes, mLrcs);
            reader.close();
            updateView();/*
            int height = preMargin - lyrics.get(0).getMeasuredHeight() / 2;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)layout.getLayoutParams();
            params.setMargins(0, height, 0, 0);
            layout.setLayoutParams(params);
            preMargin = height;*/
            return 1;
        }
        String line ="";
        mLrcs.add("TXT歌词不支持自动滚动");
        while((line =reader.readLine()) != null){
            mLrcs.add(line);
        }
        reader.close();
        updateView();
        return 1;
    }

    // 传入当前播放时间
    public synchronized void changeCurrent(long time) {
        // 如果当前时间小于下一句开始的时间
        // 直接return
        if(isPrepared && isLrc) {
            if (mNextTime > time) {
                if (time > mCurrentTime) {
                    return;
                } else {
                    int i = mCurrentLine - 1;
                    for (; i >= 0; i--) {
                        if (mTimes.get(i) < time && i <= mCurrentLine - 1) {
                            Logger.INSTANCE.d("前换");
                            Bundle bundle = new Bundle();
                            bundle.putInt("pre", mCurrentLine);
                            bundle.putInt("now", i);
                            Message msg = new Message();
                            msg.what = 1112;
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                            int height = lyrics.get(0).getMeasuredHeight() / 2;
                            if(i >= 1){
                                for(int j = 1; j < i; j++) {
                                    height += lyrics.get(j).getMeasuredHeight();
                                }
                                height += lyrics.get(i).getMeasuredHeight() / 2;
                            }
                            else{
                                height = 0;
                            }
                            final int changeHeight = height + mDividerHeight * i;
                            va = ValueAnimator.ofInt(preMargin, parent.getMeasuredHeight() / 2 - changeHeight);
                            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    int value = (int) animation.getAnimatedValue();
                                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)layout.getLayoutParams();
                                    params.setMargins(0,value,0, 0);
                                    layout.setLayoutParams(params);
                                    //layout.setPadding(layout.getPaddingLeft(), value, layout.getPaddingRight(), layout.getPaddingBottom());
                                }
                            });
                            va.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    Logger.INSTANCE.d("paddingTop: " + layout.getPaddingTop());
                                    preMargin = parent.getMeasuredHeight() / 2 - changeHeight;
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)layout.getLayoutParams();
                                    params.setMargins(0,parent.getMeasuredHeight() / 2 - changeHeight,0, 0);
                                    layout.setLayoutParams(params);
                                    preMargin = parent.getMeasuredHeight() / 2 - changeHeight;
                                    /*layout.setPadding(layout.getPaddingLeft(), parent.getMeasuredHeight() / 2 - changeHeight, layout.getPaddingRight(), layout.getPaddingBottom());*/
                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            });
                            mNextTime = mTimes.get(i + 1);
                            mCurrentTime = mTimes.get(i);
                            mCurrentLine = i;
                            va.setDuration((mNextTime - mCurrentTime) / 10);
                            handler.sendEmptyMessage(1111);
                            break;
                        }
                    }
                    if(i < 0 && mCurrentLine != 0){
                        i = 0;
                        Logger.INSTANCE.d("前换");
                        Bundle bundle = new Bundle();
                        bundle.putInt("pre", mCurrentLine);
                        bundle.putInt("now", i);
                        Message msg = new Message();
                        msg.what = 1112;
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                        int height = lyrics.get(0).getMeasuredHeight() / 2;
                        if(i >= 1){
                            for(int j = 1; j < i; j++) {
                                height += lyrics.get(j).getMeasuredHeight();
                            }
                            height += lyrics.get(i).getMeasuredHeight() / 2;
                        }
                        else{
                            height = 0;
                        }
                        final int changeHeight = height + mDividerHeight * i;
                        va = ValueAnimator.ofInt(preMargin, parent.getMeasuredHeight() / 2 - changeHeight);
                        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int value = (int) animation.getAnimatedValue();
                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)layout.getLayoutParams();
                                params.setMargins(0,value,0, 0);
                                layout.setLayoutParams(params);
                                //layout.setPadding(layout.getPaddingLeft(), value, layout.getPaddingRight(), layout.getPaddingBottom());
                            }
                        });
                        va.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                Logger.INSTANCE.d("paddingTop: " + layout.getPaddingTop());
                                preMargin = parent.getMeasuredHeight() / 2 - changeHeight;
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)layout.getLayoutParams();
                                params.setMargins(0,parent.getMeasuredHeight() / 2 - changeHeight,0, 0);
                                layout.setLayoutParams(params);
                                preMargin = parent.getMeasuredHeight() / 2 - changeHeight;
                                    /*layout.setPadding(layout.getPaddingLeft(), parent.getMeasuredHeight() / 2 - changeHeight, layout.getPaddingRight(), layout.getPaddingBottom());*/
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                        mNextTime = mTimes.get(i + 1);
                        mCurrentTime = mTimes.get(i);
                        mCurrentLine = i;
                        va.setDuration((mNextTime - mCurrentTime) / 10);
                        handler.sendEmptyMessage(1111);
                    }
                }
            } else {
                int i = mCurrentLine + 1;
                // 每次进来都遍历存放的时间
                for (; i < mTimes.size() - 1; i++) {
                    // 发现这个时间大于传进来的时间
                    // 那么现在就应该显示这个时间前面的对应的那一行
                    // 每次都重新显示，是不是要判断：现在正在显示就不刷新了
                    if (mTimes.get(i + 1) > time) {
                        Logger.INSTANCE.d("换");
                        Bundle bundle = new Bundle();
                        bundle.putInt("pre", mCurrentLine);
                        bundle.putInt("now", i);
                        Message msg = new Message();
                        msg.what = 1112;
                        msg.setData(bundle);
                        handler.sendMessage(msg);

                        int height = lyrics.get(0).getMeasuredHeight() / 2;
                        if(i >= 1){
                            for(int j = 1; j < i; j ++){
                                height += lyrics.get(j).getMeasuredHeight();
                            }
                            height += lyrics.get(i).getMeasuredHeight() / 2;
                        }
                        else{
                            height = 0;
                        }
                        final int changeHeight = height + mDividerHeight * i;
                        va = ValueAnimator.ofInt(preMargin, parent.getMeasuredHeight() / 2 - changeHeight);

                        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int value = (int) animation.getAnimatedValue();
                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)layout.getLayoutParams();
                                params.setMargins(0,value,0, 0);
                                layout.setLayoutParams(params);
                                /*layout.setPadding(layout.getPaddingLeft(), value, layout.getPaddingRight(), layout.getPaddingBottom());*/
                            }
                        });
                        va.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                Logger.INSTANCE.d("paddingTop : " + layout.getPaddingTop());
                                preMargin = parent.getMeasuredHeight() / 2 - changeHeight;
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)layout.getLayoutParams();
                                params.setMargins(0, parent.getMeasuredHeight() / 2 - changeHeight, 0, 0);
                                preMargin = parent.getMeasuredHeight() / 2 - changeHeight;
                                layout.setLayoutParams(params);
                                //layout.setPadding(layout.getPaddingLeft(), parent.getMeasuredHeight() / 2 - changeHeight, layout.getPaddingRight(), layout.getPaddingBottom());
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                        mCurrentTime = mTimes.get(i);
                        mNextTime = mTimes.get(i + 1);
                        mCurrentLine = i;
                        va.setDuration((mNextTime - mCurrentTime) / 10);

                        handler.sendEmptyMessage(1111);
                        break;
                    }
                }
                if(i >= mTimes.size() - 1 && mCurrentLine != mTimes.size() - 1){
                    i = mTimes.size() - 1;
                    Logger.INSTANCE.d("换");
                    Bundle bundle = new Bundle();
                    bundle.putInt("pre", mCurrentLine);
                    bundle.putInt("now", i);
                    Message msg = new Message();
                    msg.what = 1112;
                    msg.setData(bundle);
                    handler.sendMessage(msg);

                    int height = lyrics.get(0).getMeasuredHeight() / 2;
                    if(i >= 1){
                        for(int j = 1; j < i; j ++){
                            height += lyrics.get(j).getMeasuredHeight();
                        }
                        height += lyrics.get(i).getMeasuredHeight() / 2;
                    }
                    else{
                        height = 0;
                    }
                    final int changeHeight = height + mDividerHeight * i;
                    va = ValueAnimator.ofInt(preMargin, parent.getMeasuredHeight() / 2 - changeHeight);

                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int value = (int) animation.getAnimatedValue();
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
                            params.setMargins(0, value, 0, 0);
                            layout.setLayoutParams(params);
                                /*layout.setPadding(layout.getPaddingLeft(), value, layout.getPaddingRight(), layout.getPaddingBottom());*/
                        }
                    });
                    va.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            Logger.INSTANCE.d("paddingTop: " + layout.getPaddingTop());
                            preMargin = parent.getMeasuredHeight() / 2 - changeHeight;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
                            params.setMargins(0, parent.getMeasuredHeight() / 2 - changeHeight, 0, 0);
                            preMargin = parent.getMeasuredHeight() / 2 - changeHeight;
                            layout.setLayoutParams(params);
                            //layout.setPadding(layout.getPaddingLeft(), parent.getMeasuredHeight() / 2 - changeHeight, layout.getPaddingRight(), layout.getPaddingBottom());
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    mCurrentLine = i;
                    int during = MyService.getInstance().getMaxTime() - MyService.getInstance().getNowPlayTime();
                    va.setDuration(during / 5);

                    handler.sendEmptyMessage(1111);
                }

            }
        }
    }

    public int sp2px(float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private void init(){
        layout.removeAllViews();
        lyrics.clear();
        if(va != null){
            va.end();
        }
        va = null;
        mCurrentLine = 0;
        mNextTime = 0l; // 保存下一句开始的时间
        mCurrentTime = -1l;
    }

    private void updateView(){
        init();
        if(isLrc) {
            for (int i = 0; i < mLrcs.size(); i++) {
                String lyric = mLrcs.get(i);
                TextView lrcview = new TextView(context);
                lrcview.setText(lyric);
                if (i == mCurrentLine) {
                    lrcview.setTextColor(mCurrentColor);
                    lrcview.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                } else {
                    lrcview.setTextColor(mNormalColor);
                }
                lrcview.setId(100100 + i);
                lrcview.setGravity(Gravity.CENTER_HORIZONTAL);
                lrcview.setTextSize(mTextSize);
                lrcview.setMaxWidth(maxWidth);
                lrcview.setWidth(maxWidth);
                lrcview.setPadding(0, 0, 0, 0);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (i != 0) {
                    layoutParams.addRule(RelativeLayout.BELOW, 100100 + i - 1);
                }
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                layoutParams.setMargins(0, 0, 0, mDividerHeight);
                lrcview.setLayoutParams(layoutParams);
                lyrics.add(lrcview);
                layout.addView(lrcview);
            }
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)layout.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.setMargins(0, parent.getMeasuredHeight() / 3, 0, 0);
            preMargin = parent.getMeasuredHeight() / 3;
            layout.setLayoutParams(params);
            //layout.setPadding(layout.getPaddingLeft(), layout.getPaddingTop(), layout.getPaddingRight(), layout.getPaddingBottom());
            isPrepared = true;
        }
        else{
            ListView list = new ListView(context);
            list.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return mLrcs.size();
                }

                @Override
                public Object getItem(int position) {
                    return mLrcs.get(position);
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    RelativeLayout layout = new RelativeLayout(context);
                    TextView text = new TextView(context);
                    text.setTextColor(mCurrentColor);
                    text.setText(mLrcs.get(position));
                    text.setGravity(Gravity.CENTER);
                    text.setTextSize(mTextSize);
                    text.setMaxWidth(maxWidth);
                    text.setWidth(maxWidth);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    layout.addView(text, layoutParams);
                    layout.setClickable(false);
                    layout.setLongClickable(false);
                    layout.setPadding(0, 0, 0, 0);

                    return layout;
                }
            });
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            list.setLayoutParams(params);
            list.setItemsCanFocus(false);
            list.setClickable(false);
            list.setLongClickable(false);
            list.setDivider(new ColorDrawable(Color.TRANSPARENT));
            list.setDividerHeight(mDividerHeight);
            layout.addView(list);
            layout.setPadding(0, 0, 0, 0);
            RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)layout.getLayoutParams();
            params1.setMargins(0, 0, 0, 0);
            layout.setLayoutParams(params1);
        }
    }

    public void noLrc(){
        init();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)layout.getLayoutParams();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.setMargins(0, 0, 0, 0);
        layout.setLayoutParams(params);
        layout.setPadding(0 ,0, 0, 0);
        TextView notf = new TextView(context);
        notf.setText("点击在线搜索");
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params1.addRule(RelativeLayout.CENTER_IN_PARENT);
        notf.setLayoutParams(params1);
        notf.setTextSize(mTextSize);
        notf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().showLrcDialog();
            }
        });
        layout.addView(notf);
    }


   /* public void setColors(int nColor, int cColor){
        mNormalColor = nColor;
        mCurrentColor = cColor;
    }*/

    public void setColor(int cColor){
        mCurrentColor = cColor;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = inflater.getContext();
        lyrics = new ArrayList<>();
        layout = new RelativeLayout(context);
        RelativeLayout.LayoutParams para = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        para.addRule(RelativeLayout.CENTER_HORIZONTAL);
        para.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layout.setLayoutParams(para);
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        maxWidth = size.x * 9 / 10;

        parent = new RelativeLayout(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(5, 5, 5, 5);
        parent.setLayoutParams(params);
        mDividerHeight = size.y / 45;
        parent.addView(layout);
        parent.setBackgroundResource(R.drawable.backgroundradius);
        return parent;
    }
}
