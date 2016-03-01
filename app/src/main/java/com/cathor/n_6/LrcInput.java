package com.cathor.n_6;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.support.percent.PercentRelativeLayout;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Cathor on 2015/9/18.
 */
public class LrcInput extends Activity {
    private static List<LrcItem> lrclist;
    private static String title;
    private static String author;
    private CustomeProgressDialog dialog;

    private android.os.Handler handler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 101:
                    Toast.makeText(LrcInput.this, "搜索失败(〃｀ 3′〃)", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PercentRelativeLayout relativ = (PercentRelativeLayout)getLayoutInflater().inflate(R.layout.lyricinput, null);
        setContentView(relativ);
        final TextInputLayout lrctitle = (TextInputLayout)relativ.findViewById(R.id.mtitle);
        lrctitle.getEditText().setText(MyService.getInstance().getNowPlayTitle());
        final TextInputLayout lrcauthor = (TextInputLayout)relativ.findViewById(R.id.mauthor);
        lrcauthor.getEditText().setText(MyService.getInstance().getNowPlayAuthor());
        TextView button = (TextView)relativ.findViewById(R.id.msearch);
        button.setClickable(true);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = lrctitle.getEditText().getText().toString();
                author = lrcauthor.getEditText().getText().toString();
                dialog = CustomeProgressDialog.Companion.show(LrcInput.this, "正在搜索歌词");
                Thread threa = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            lrclist = LrcSearcher.search(title, author);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("lrc", (Serializable)lrclist);
                            Intent intent = new Intent(LrcInput.this, MainActivity.class);
                            intent.putExtras(bundle);
                            LrcInput.this.setResult(RESULT_OK, intent);
                            LrcInput.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.sendEmptyMessage(101);
                        }
                        finally {
                            dialog.cancel();
                        }
                    }
                });
                threa.start();
            }
        });


    }
}
