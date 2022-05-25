package com.example.android.test;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

public class MyTTS extends UtteranceProgressListener {
    private final TextToSpeech tts;
    private boolean isSupportCN = true;
    private static MyTTS instance;
    private final StringBuffer stringBuffer = new StringBuffer();

    public static MyTTS getInstance(Context context) {
        if (instance == null) {
            synchronized (MyTTS.class) {
                instance = new MyTTS(context.getApplicationContext());
            }
        }
        return instance;
    }

    private MyTTS(final Context context) {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.CHINA);
                    tts.setPitch(1.0f);
                    tts.setSpeechRate(1.0f);
                    tts.setOnUtteranceProgressListener(MyTTS.this);
                    if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                        isSupportCN = false;
                        stringBuffer.setLength(0);
                        stringBuffer.append("Voice not supported " + " CHINA");
                        Log.e("test", stringBuffer.toString());
                    } else {
                        Log.i("test", "TTS 已开启");
                    }
                }
            }
        });
    }

    @Override
    public void onStart(String utteranceId) {
        Log.d("xulc", "onStart---utteranceId--->" + utteranceId);
    }

    @Override
    public void onDone(String utteranceId) {
        Log.d("xulc", "onDone---utteranceId--->" + utteranceId);
    }

    @Override
    public void onError(String utteranceId) {
        Log.d("xulc", "onError---utteranceId--->" + utteranceId);
    }


    public TextToSpeech getTts() {
        return tts;
    }

    public boolean isSupportCN() {
        return isSupportCN;
    }
}
