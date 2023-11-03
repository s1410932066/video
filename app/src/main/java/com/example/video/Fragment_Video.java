package com.example.video;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.VideoView;

import java.util.Objects;

public class Fragment_Video extends Fragment {
    private VideoView videoView;
    private OrientationEventListener orientationEventListener;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private Context context;
    private SeekBar seekBar;
    private Handler handler;
    private Runnable updateSeekBarRunnable;
    private boolean isZoomedOut = false;
    private float scaleFactor = 1.0f; // 初始缩放比例
    private float translationX = 0; // X轴平移偏移量
    private float translationY = 0; // Y轴平移偏移量

    public Fragment_Video(Activity activity, SeekBar seekBar, String rtspUrl) {
        context = activity;
        this.seekBar = seekBar; // 将传递进来的SeekBar参数赋值给成员变量

    }
    public VideoView getVideoView() {
        return videoView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 注销屏幕方向监听器
        orientationEventListener.disable();
        handler.removeCallbacks(updateSeekBarRunnable);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MediaController mediaController = new MediaController(context);
        videoView = view.findViewById(R.id.videoView);
//        rtspUrl = "rtsp://localhost:8554/stream"; // 替換為您的RTSP監視器URL

        // 设置视频路径
        videoView.setVideoURI(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.video));
        videoView.setMediaController(null);
        mediaController.setAnchorView(videoView);
        scaleGestureDetector = new ScaleGestureDetector(context, new MyScaleGestureListener());
        gestureDetector = new GestureDetector(context, new MyGestureListener());
        // 获取父容器
        ViewGroup parentContainer = (ViewGroup) view.getParent();
        // 应用放大和缩小手势到父容器
        parentContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });


        // 初始化Handler和Runnable以更新SeekBar
        handler = new Handler();
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                //更新進度條
                updateSeekBar();
                handler.postDelayed(this, 300); // 每0.3秒更新一次
            }
        };
        // 开始更新SeekBar
        handler.postDelayed(updateSeekBarRunnable, 10);

        //屏幕方向監聽
        orientationListener();

    }

    private void orientationListener() {
        // 注册屏幕方向监听器
        orientationEventListener = new OrientationEventListener(context) {
            @Override
            public void onOrientationChanged(int orientation) {
                //取當前屏幕方向
                int screenOrientation = getResources().getConfiguration().orientation;
                if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    // 横向顯示全屏
                    requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                } else if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    // 縱向取消全屏
                    requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                    requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                }
            }
        };
        // 启动屏幕方向监听器
        orientationEventListener.enable();
    }

    private void updateSeekBar() {
        //影片總長(毫秒)
        int duration = videoView.getDuration();
        //當前時間(毫秒)
        int currentPosition = videoView.getCurrentPosition();
//        Log.e("currentPosition: ", String.valueOf(currentPosition));
        int progress = (int) (currentPosition * 100f / duration);
        seekBar.setProgress(progress);
    }

    //處理縮放(雙指)
    private class MyScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(1.0f, Math.min(scaleFactor, 3.0f));

            videoView.setScaleX(scaleFactor);
            videoView.setScaleY(scaleFactor);

            // 检查缩放比例是否小于1.0f，如果是则重置为1.0f
            if (scaleFactor < 1.0f) {
                scaleFactor = 1.0f;
                videoView.setScaleX(scaleFactor);
                videoView.setScaleY(scaleFactor);
            }

            // 判断视频比例是否为1.0f
            // 视频比例不为1.0f，设置isZoomedOut为true
            // 视频比例为1.0f，设置isZoomedOut为false
            isZoomedOut = scaleFactor != 1.0f;

            return true;
        }
    }

    //(單手)
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private float initialX;
        private float initialY;

        //雙擊
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //放大狀態
            if (isZoomedOut) {
                videoView.setScaleX(1.0f);
                videoView.setScaleY(1.0f);
                videoView.setTranslationX(0);
                videoView.setTranslationY(0);
                isZoomedOut = false;
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            initialX = e.getX();
            initialY = e.getY();
            return true;
        }
        /**
         * 拖動事件
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (isZoomedOut) {
                float deltaX = e2.getX() - initialX;
                float deltaY = e2.getY() - initialY;
                initialX = e2.getX();
                initialY = e2.getY();

                translationX += deltaX;
                translationY += deltaY;

                float maxTranslationX = (videoView.getWidth() * videoView.getScaleX() - videoView.getWidth()) / 2;
                float maxTranslationY = (videoView.getHeight() * videoView.getScaleY() - videoView.getHeight()) / 2;

                translationX = Math.max(-maxTranslationX, Math.min(translationX, maxTranslationX));
                translationY = Math.max(-maxTranslationY, Math.min(translationY, maxTranslationY));

                videoView.setTranslationX(translationX);
                videoView.setTranslationY(translationY);
            }
            return true;
        }
    }
}