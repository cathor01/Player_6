package com.cathor.n_6;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * Created by Cathor on 2015/9/18.
 */
public class LrcSelect extends Activity {
    private static String path;
    private static List<LrcItem> _list;
    private static int height = 57;
    //private static Activity activity;
    public static void setArray(List<LrcItem> list){
        _list = list;
    }

    private static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 101:
                    if(path.equals("wtf")){
                        System.out.println(path);
                        Toast.makeText(MainActivity.getInstance(), "MB,网易这首歌没歌词23333,可以的话换另一首吧。。。。。", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        TabAdapter.getLrcFragment().setLrcPath(path);
                        MyService.getInstance().getLyric();
            }
                    catch (IOException e){
                        e.printStackTrace();
                    }
            }
        }
    };

    /**
     * 将dp转化为px
     *
     * */

    private static int getPx(float dp) {
        // TODO Auto-generated method stub
        return (int)(dp * MainActivity.getInstance().scale + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyriclist);
        //activity = this;
        RelativeLayout rel = (RelativeLayout)findViewById(R.id.lrel);
        ListView list = (ListView)rel.findViewById(R.id.llist);
        list.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return _list.size();
            }

            @Override
            public Object getItem(int position) {
                return _list.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                RelativeLayout layout = (RelativeLayout) LrcSelect.this.getLayoutInflater().inflate(R.layout.musicadapter, null);
                TextView title = (TextView) layout.findViewById(R.id.title);
                title.setTextColor(Color.rgb(0, 96, 88));
                TextView author = (TextView) layout.findViewById(R.id.author);
                title.setText(_list.get(position).getTitle());
                author.setText(_list.get(position).getAuthor());
                title.setClickable(false);
                author.setClickable(false);
                layout.setClickable(true);
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    path = LrcSearcher.download(_list.get(position).getLink(), MyService.getInstance().getNowPlayTitle(), MyService.getInstance().getNowPlayAuthor(), _list.get(position).getResource());
                                    handler.sendEmptyMessage(101);
                                    Intent intent = new Intent(LrcSelect.this, MainActivity.class);
                                    LrcSelect.this.setResult(RESULT_OK, intent);
                                    LrcSelect.this.finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        thread.start();
                    }
                });
                return layout;
            }
        });
        ViewGroup.LayoutParams params = list.getLayoutParams();
        params.height = getPx(height) * _list.size();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        list.setLayoutParams(params);
    }


}
