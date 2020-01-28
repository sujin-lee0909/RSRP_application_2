package com.example.leesujin.myapplication;


import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    TextView txtRsrp;
    String strSignal;
    int state = 0;
    long start;

    final static String foldername = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Data/";
    //final static String foldername = "sdcard/TestLog";
    final static String filename = "rsrp.txt";

    TelephonyManager telephonyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //버튼클릭
    public void mOnFileWrite(View v){
        state= 1;
        Log.i("button","on");
        Log.i("path",foldername);

        txtRsrp = (TextView)findViewById(R.id.txtRsrp); //xml파일에 추가하기
        //txt파일에 날짜 입력
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        start = System.currentTimeMillis();

        WriteTextFile(foldername, filename, 0 ,now);

        //manager
        telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        if(telephonyManager.getNetworkType()==TelephonyManager.NETWORK_TYPE_LTE){
            Log.i("process","telephony");
            //txt파일에 날짜 입력
            //String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            //String contents = "Log 생성 : "+now+"\n";

            //WriteTextFile(foldername, filename, contents);

            //리스너 등록
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }
    }

    public void mOffFileWrite(View v){
        Log.i("button","off");
        state = 0;
    }

    //리스너
    PhoneStateListener mPhoneStateListener = new PhoneStateListener(){
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            //RSRP (Reference Signal Received Power) - 단위 dBm (절대크기). - 단말에 수신되는 Reference Signal의 Power
            strSignal = signalStrength.toString();
            Log.d("SignalStrength", strSignal);


            txtRsrp.setText("RSRP : "+GetLTE_RSRP(strSignal));
            String content = String.valueOf(GetLTE_RSRP(strSignal));

            if (state==1) {
                Log.i("process","writeTextFile");
                Log.i("data",content);
                long end = System.currentTimeMillis();
                int time = (int) (end - start);
                WriteTextFile(foldername, filename, time, content);
                //start = System.currentTimeMillis();
            }
        }
    };

    //LTE RSRP 구하기
    public int GetLTE_RSRP(String signalStrength){
        String[] arrSignal = signalStrength.split(" ");
        int lteRSRP = 0;
        try{
            if(arrSignal.length>13){
                lteRSRP = Integer.parseInt(arrSignal[9]);
            }
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
        return lteRSRP;
    }

    //텍스트내용을 경로의 텍스트 파일에 쓰기
    public void WriteTextFile(String foldername, String filename, int wtime, String contents){
        try{
            File dir = new File (foldername);
            //디렉토리 폴더가 없으면 생성함
            if(!dir.exists()){
                Log.i("process","mkdir");
                dir.mkdirs();
            }
            //파일 output stream 생성
            FileOutputStream fos = new FileOutputStream(foldername+"/"+filename, true);
            //파일쓰기
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(String.valueOf(wtime)+",");
            writer.write(contents);
            writer.newLine();
            writer.flush();
            writer.close();
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        super.onDestroy();
    }
}


