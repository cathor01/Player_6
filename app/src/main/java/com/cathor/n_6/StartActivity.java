package com.cathor.n_6;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;


/**
 * Created by Cathor on 2015/10/29.
 */
public class StartActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View view = View.inflate(this, R.layout.start_img, null);
        setContentView(view);
        //渐变展示启动屏
        AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
        aa.setDuration(2000);
        view.startAnimation(aa);
        aa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                AsyncTask<Void, Void, Void> async = new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected Void doInBackground(Void...params) {
                        System.out.println("Hello");
                        DataManager data_manager = ((MyApplication)getApplication()).GetDataManager();
                        MainActivity.album_list = data_manager.GetAlbumData();
                        MainActivity.author_list = data_manager.GetAuthorData();
                        MainActivity.folder_list = data_manager.GetFolderData();
                        MainActivity.play_list = data_manager.GetListData();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        StartActivity.this.preloadResource();
                    }
                };
                async.execute();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void preloadResource() {
        MainActivity.nowType = 0;
        MyService.setMarray(MainActivity.getNowPlay());
        redirectTo();
    }

    /**
     * 跳转到...
     */
    private void redirectTo(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }



}
