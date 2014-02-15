package com.fuetrek.fsr.SampleTypeD;
import java.net.*;
import java.io.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;

import com.fuetrek.fsr.FSRServiceOpen;
import com.fuetrek.fsr.FSRServiceEventListener;
import com.fuetrek.fsr.FSRServiceEnum.*;
import com.fuetrek.fsr.entity.*;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


class SyncObj{
    boolean isDone=false;

    synchronized void wait_(){
        try {
            while(isDone==false){
                wait(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    synchronized void notify_(){
        isDone=true;
        notify();
    }
}


public class MainActivity extends Activity{
    private Handler handler_;
    private Button buttonStart_;
    private ProgressBar progressLevel_;
    private TextView textResult_;
    private fsrController controller_ = new fsrController();

    private static final BackendType backendType_ = BackendType.D;

    private static DecideCommand dc = new DecideCommand();

    // Context
    private Activity activity_ = null;

    public class fsrController extends Thread implements FSRServiceEventListener {
        FSRServiceOpen fsr_;
        SyncObj event_CompleteConnect_ = new SyncObj();
        SyncObj event_CompleteDisconnect_ = new SyncObj();
        SyncObj event_EndRecognition_ = new SyncObj();
        Ret ret_;
        String result_;

        // ここで音声認識後の処理をしている。
        final Runnable notifyFinished = new Runnable() {
            public void run() {
                try {
                    controller_.join();
                } catch (InterruptedException e) {
                }
                textResult_.append("***Result***" + System.getProperty("line.separator"));
                textResult_.append(controller_.result_);
                buttonStart_.setEnabled(true);

            }
        };


        // 実行
        @Override
        public void run() {
            result_ = "";
            try {
                result_=execute();
            } catch (Exception e) {
                result_ = "(error)";
                e.printStackTrace();
            }

            String resultCommand = dc.getCommandType(result);
            // test output
    		System.out.println(result);
    		System.out.println(resultCommand);

            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            if (wifiManager.isWifiEnabled() == false) {
                wifiManager.setWifiEnabled(true);
            }

            String ssid = "EZC_WIFI_5_0019";
            WifiConfiguration config = new WifiConfiguration();
            config.SSID = "\"" + ssid + "\"";
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.preSharedKey = "99999999";
            int networkId = wifiManager.addNetwork(config); // 失敗した場合は-1となります
            wifiManager.saveConfiguration();
            wifiManager.updateNetwork(config);

            SocketClientSample sample = new SocketClientSample();
            sample.SocketConnect();

            handler_.post(notifyFinished);
        }

        /**
         * 音声認識部分
         *
         * @throws Exception
         */
        public String execute() throws Exception {

            try{
                final ConstructorEntity construct = new ConstructorEntity();
                construct.setContext(activity_);

                construct.setApiKey("78316d6d4a506c4c4c3374704434556e5155624b3538556a466d50463332366742636859796a6a37484842");

                // 識別時間2秒に設定 ( デフォルト 10秒 )
                construct.setSpeechTime(2000);
                construct.setRecordSize(240);
                construct.setRecognizeTime(2000);

                // 繧､繝ｳ繧ｹ繧ｿ繝ｳ繧ｹ逕滓�
                // (this縺ｯ FSRServiceEventListener繧段mplements縺励※縺�ｋ縲�)
                if( null == fsr_ ){
                    fsr_ = new FSRServiceOpen(this, this, construct);
                }

                // connect
                fsr_.connectSession(backendType_);
                event_CompleteConnect_.wait_();
                if( ret_ != Ret.RetOk ){
                    Exception e = new Exception("filed connectSession.");
                    throw e;
                }

                // 隱崎ｭ倬幕蟋�

                final StartRecognitionEntity startRecognitionEntity = new StartRecognitionEntity();
                startRecognitionEntity.setAutoStart(true);
                startRecognitionEntity.setAutoStop(true);
                startRecognitionEntity.setVadOffTime((short) 500);
                startRecognitionEntity.setListenTime(0);
                startRecognitionEntity.setLevelSensibility(10);

                // 隱崎ｭ倬幕蟋�
                fsr_.startRecognition(backendType_, startRecognitionEntity);

                // 隱崎ｭ伜ｮ御ｺ�ｾ�■
                // (setAutoStop(true)縺ｪ縺ｮ縺ｧ逋ｺ隧ｱ邨ゆｺ�ｒ讀懃衍縺励※閾ｪ蜍募●豁｢縺吶ｋ)
                event_EndRecognition_.wait_();

                // 隱崎ｭ倡ｵ先棡縺ｮ蜿門ｾ�
                RecognizeEntity recog = fsr_.getSessionResultStatus(backendType_);
                String result="(no result)";
                if( recog.getCount()>0 ){
                    ResultInfoEntity info=fsr_.getSessionResult(backendType_, 1);
                    result = info.getText();
                }

                // 蛻�妙
                fsr_.disconnectSession(backendType_);
                event_CompleteDisconnect_.wait_();

                return result;
            } catch (Exception e) {
                showErrorDialog(e);
                throw e;
            }finally{
                if( fsr_!=null ){
                    fsr_.destroy();
                    fsr_=null;
                }
            }
        }

        @Override
        public void notifyAbort(Object arg0, AbortInfoEntity arg1) {
            Exception e = new Exception("Abort!!");
            showErrorDialog(e);
        }

        @Override
        public void notifyEvent(final Object appHandle, final EventType eventType, final BackendType backendType, Object eventData) {

            switch(eventType){

            case CompleteConnect:
                // 謗･邯壼ｮ御ｺ�
                ret_ = (Ret)eventData;
                event_CompleteConnect_.notify_();
                break;

            case CompleteDisconnect:
                // 蛻�妙螳御ｺ�
                event_CompleteDisconnect_.notify_();
                break;

            case NotifyEndRecognition:
                // 隱崎ｭ伜ｮ御ｺ�
                event_EndRecognition_.notify_();
                break;

            case NotifyLevel:
                // 繝ｬ繝吶Ν繝｡繝ｼ繧ｿ譖ｴ譁ｰ
                int level = (Integer)eventData;
                progressLevel_.setProgress(level);
                break;
            }
        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        handler_ = new Handler();
        buttonStart_ = (Button) findViewById(R.id.button_start);
        progressLevel_ = (ProgressBar) findViewById(R.id.progress_level);
        textResult_ = (TextView) findViewById(R.id.text_result);
        activity_ = this;

        // 繧ｳ繝ｳ繝医Ο繝ｼ繝ｫ蛻晄悄蛹�
        progressLevel_.setMax(100);
        textResult_.setTextSize(28.0f);
    }

    /**
     * 髢句ｧ九�繧ｿ繝ｳ謚ｼ荳�
     *
     * @param view 繝薙Η繝ｼ
     */
    public void onClickStart(final View view) {
        textResult_.setText("");
        buttonStart_.setEnabled(false);
        controller_ = new fsrController();
        controller_.start();
    }


    /**
     * 繧ｨ繝ｩ繝ｼ繝�繧､繧｢繝ｭ繧ｰ繧定｡ｨ遉ｺ縺吶ｋ
     */
    public final void showErrorDialog(Exception e) {
        final Activity activity = this;
        final String text=(e.getCause()!=null)?e.getCause().toString():e.toString();
        final AlertDialog.Builder ad = new AlertDialog.Builder(activity);
        ad.setTitle("Error");
        ad.setMessage(text);
        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int whichButton) {
                activity.setResult(Activity.RESULT_OK);
                activity.finish();
            }
        });
        ad.create();
        ad.show();
    }

    /**
     * 繝医�繧ｹ繝医ｒ陦ｨ遉ｺ縺吶ｋ縲�
     */
    public final void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}










class SocketClientSample {
  /**	ポート番号	*/
  int port_no_ = 8899;
  String hostName_ = "10.10.100.103";

  public void SocketConnect() {
    try{
      // ソケットを生成
      Socket socket = new Socket(hostName_, port_no_);

      // 出力ストリームを取得
      PrintWriter osStr = new PrintWriter(socket.getOutputStream(), true);

    // 入力ストリームを取得
    InputStream is = socket.getInputStream();
    BufferedReader irStr = new BufferedReader(new InputStreamReader(is));

    char pwm[] = {0,0,0,0,0,0};

    char senddata[] = {
            0x55,0x00,0x0b,0x00,   // header
            0x50,                  // accel data
            0x50,                  // handle data
            0x00,0x00,0x00,0x00,   // not use
            0x00};

    for(int i=0;i<256;i++){
        int tmp = 0;

//        pthread_mutex_lock(&mutex);
        senddata[4] = pwm[0];
        senddata[5] = pwm[1];
        senddata[6] = pwm[2];
        senddata[7] = pwm[3];
//        pthread_mutex_unlock(&mutex);

        //　パリティ計算
        for(int j=0;j<10;j++){
          tmp = tmp + senddata[j];
          senddata[10] = (char)tmp;
        }

        //データ送信
        osStr.write(senddata);
        osStr.flush();
    }

    // 入出力ストリームを閉じる
    osStr.close();
    irStr.close();

    // ソケットを閉じる
    socket.close();
  } catch(IOException e) {
    e.printStackTrace();
  }
}
}