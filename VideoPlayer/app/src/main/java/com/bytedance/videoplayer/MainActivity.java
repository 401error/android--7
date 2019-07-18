package com.bytedance.videoplayer;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer player = new MediaPlayer();
    private SeekBar seekbar;
    public static final int UPDATE_UI = 1;
    private TextView textView;
    private  SurfaceView surfaceView;
    private  int contxt;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ImageView imageView = findViewById(R.id.imageView);
//        String url = "https://s3.pstatp.com/toutiao/static/img/logo.271e845.png";
//        Glide.with(this).load(url).into(imageView);

         textView =findViewById(R.id.textView);
        seekbar = findViewById(R.id.seek);
        SurfaceHolder holder;
        final Button playbutton= findViewById(R.id.playbutton);
         surfaceView = findViewById(R.id.surfaceView);

        try {
            player.setDataSource(getResources().openRawResourceFd(R.raw.bytedance));
            holder = surfaceView.getHolder();
            holder.addCallback(new PlayerCallBack());
            player.prepare();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {

                    player.start(); // 初始化准备好，立刻播放
                    player.setLooping(true); // 循环播放
                    seekbar.setMax(player.getDuration());
                    handler.sendEmptyMessage(UPDATE_UI);
                    if (player.isPlaying()) {
                        playbutton.setVisibility(View.INVISIBLE);
                    }
                }

            });

//            player.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
//                @Override
//                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//                    changeVideoSize(mp);
//                }
//            });

        } catch (IOException e) {
            e.printStackTrace();
        }


        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int process = seekBar.getProgress();
                if(player != null && player.isPlaying()) {
                    player.seekTo(process);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {


            }
        });


        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                player.pause();
                playbutton.setVisibility(View.VISIBLE);

            }
        });

        playbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                player.start();
                playbutton.setVisibility(View.INVISIBLE);
            }

        });


    }

    private class PlayerCallBack implements
            SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            player.setDisplay(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int
                format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_UI:
                    try {
                        seekbar.setProgress(player.getCurrentPosition());
                        contxt=player.getCurrentPosition();
                        updateTime(textView,player.getCurrentPosition());
                        Log.d("handler", "handleMessage() called with: msg = [" + msg + "]");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d("xxxxxx", "handleMessage() called with: msg = [" + msg + "]");
                    handler.sendEmptyMessageDelayed(UPDATE_UI, 500);
                    break;
            }
        }
    };

    private void updateTime(TextView textView, int millisecond){
        int second = millisecond/1000;
        int hh = second / 3600;
        int mm = second % 3600 / 60;
        int ss = second % 60;

        String str = null;
        if(hh!=0){
            str = String.format("%02d:%02d:%02d",hh,mm,ss);
        }else {
            str = String.format("%02d:%02d",mm,ss);
        }

        textView.setText(str);

    }

    public void changeVideoSize(MediaPlayer mediaPlayer) {
        int surfaceWidth = surfaceView.getWidth();
        int surfaceHeight = surfaceView.getHeight();

        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();

        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。
        float max;
        if (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //竖屏模式下按视频宽度计算放大倍数值
            max = Math.max((float) videoWidth / (float) surfaceWidth, (float) videoHeight / (float) surfaceHeight);
        } else {
            //横屏模式下按视频高度计算放大倍数值
            max = Math.max(((float) videoWidth / (float) surfaceHeight), (float) videoHeight / (float) surfaceWidth);
        }

        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        videoWidth = (int) Math.ceil((float) videoWidth / max);
        videoHeight = (int) Math.ceil((float) videoHeight / max);

        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        surfaceView.setLayoutParams(new ConstraintLayout.LayoutParams(videoWidth, videoHeight));
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.stop();
            player.release();
        }
    }

    private boolean portrait;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        portrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        tryFullScreen(!portrait);
    }

    private void tryFullScreen(boolean fullScreen) {
        if (MainActivity.this instanceof AppCompatActivity) {
            ActionBar supportActionBar = ((AppCompatActivity) MainActivity.this).getSupportActionBar();
            if (supportActionBar != null) {
                ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();

                if (fullScreen) {
                    supportActionBar.hide();
                    layoutParams.width =layoutParams.width;
                    layoutParams.height =WindowManager.LayoutParams.WRAP_CONTENT;

                } else {
                    layoutParams.width =layoutParams.width;
                    layoutParams.height =((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                            250,
                            getResources().getDisplayMetrics()));
                    supportActionBar.show();
                }
               player.seekTo(contxt);
            }
        }
        setFullScreen(fullScreen);
    }


    private void setFullScreen(boolean fullScreen) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullScreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(attrs);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

    }




}
