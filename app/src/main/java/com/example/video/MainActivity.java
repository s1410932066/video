package com.example.video;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.VideoView;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {
    private SeekBar seekBar;
    private String rtspUrl;
    private Fragment_Video fragment_video;
    private boolean isVideoPlaying = false; // 记录视频是否正在播放的标志
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //MQTT
//        try {
//            MQTT();
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
        //fragment設定
        fragmentSet();
        //控制欄位
        operationBar();

    }

    private void fragmentSet() {
        seekBar = findViewById(R.id.seekBar);
        fragment_video = new Fragment_Video(this,this.seekBar,rtspUrl);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.framelayout, fragment_video).commit();
    }


    private void operationBar() {
        // 跳转10秒按钮的点击事件监听器
        ImageView fast_forwardImage = findViewById(R.id.fast_forward);
        fast_forwardImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = fragment_video.getVideoView().getCurrentPosition();
                int newPosition = currentPosition + 10000; // 跳转10秒
                fragment_video.getVideoView().seekTo(newPosition);
            }
        });

        // 倒退10
        ImageView reverseImage = findViewById(R.id.reverse);
        reverseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //返回當前影片位置
                int currentPosition = fragment_video.getVideoView().getCurrentPosition();
                int newPosition = currentPosition - 10000; // 倒退10秒
                fragment_video.getVideoView().seekTo(newPosition);
            }
        });

        // 暂停
        ImageView pauseImage = findViewById(R.id.pause);
        pauseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVideoPlaying) {
                    fragment_video.getVideoView().pause();
                    isVideoPlaying = false;
                } else {
                    fragment_video.getVideoView().start();
                    isVideoPlaying = true;
                }
            }
        });
        //  進度條
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // 在视频中跳转到所选位置
                    Log.e( "progress: ", String.valueOf(progress));
                    Log.e( "newPosition: ", String.valueOf(fragment_video.getVideoView().getDuration()));
                    int newPosition = (int) (fragment_video.getVideoView().getDuration() * (progress / 100f));
                    fragment_video.getVideoView().seekTo(newPosition);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 当用户开始拖动SeekBar时暂停视频播放
                fragment_video.getVideoView().pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 当用户停止拖动SeekBar时恢复视频播放
                fragment_video.getVideoView().start();
            }
        });
    }

//    private void MQTT() throws MqttException {
//        // MQTT連接
//        String broker = "tcp://mqtt.example.com:1883"; // 替換為您的MQTT代理伺服器地址
//        String clientId = MqttClient.generateClientId();
//        MqttClient mqttClient = new MqttClient(broker, clientId);
//        MqttConnectOptions options = new MqttConnectOptions();
//        options.setCleanSession(true);
//        mqttClient.connect(options);
//
//        // RTSP連接
//        rtspUrl = "rtsp://localhost:8554/stream"; // 替換為您的RTSP監視器URL
//        // 在這裡使用適當的庫或工具來建立RTSP連接，並開始接收視訊流
//
//        // 訂閱MQTT主題以接收指令
//        String topic = "camera/control"; // 替換為您的MQTT訊息主題
//        mqttClient.subscribe(topic, new IMqttMessageListener() {
//            @Override
//            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                String command = new String(message.getPayload());
//                // 在這裡根據接收到的指令進行相應的操作，例如控制監視器的移動、拍攝等
//            }
//        });
//
//    }

}