package com.janvollmuth.smartmirroros;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.Locale;

/**
 * 
 */
public class Assistant {

    private static final String TAG = Assistant.class.getSimpleName();

    private ImageButton microphoneButton;
    private EditText edittext;
    public final int SPEECHINTENT_REQ_CODE = 11;

    private final Context context;
    private final HomeActivity mActivity;
    
    public Assistant(HomeActivity mainActivity, ImageButton microphoneButton){
        this.context = mainActivity.getApplicationContext();
        this.microphoneButton = microphoneButton;
        this.edittext = edittext;
        this.mActivity = mainActivity;

        microphoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Clicked Button.");

                Intent speechRecognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString());

                mActivity.startActivityForResult(speechRecognitionIntent, SPEECHINTENT_REQ_CODE);
            }
        });
    }
}
