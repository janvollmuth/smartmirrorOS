package com.janvollmuth.smartmirroros;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class YoutubePlayerActivity extends YouTubeBaseActivity {

    private static final String TAG = "YoutubePlayerFragment";
    private final int SPEECHINTENT_REQ_CODE = 11;

    YouTubePlayerView mYoutubePlayerView;
    Button playButton;
    YouTubePlayer.OnInitializedListener mOnInitializedListener;

    public YoutubePlayerActivity(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.youtubeplayer);
        Log.d(TAG, "OnCreateView: starting");
        mYoutubePlayerView =  (YouTubePlayerView) findViewById(R.id.youtubePlayer);
        playButton = (Button) findViewById(R.id.playButton);

        mOnInitializedListener = new YouTubePlayer.OnInitializedListener(){

            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                Log.d(TAG, "onClick: Done initializing");
                youTubePlayer.loadPlaylist("PL58xIFEAVt7kWlGF1R19-M2dB-1N-wQqP");
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.d(TAG, "onClick: Failed to initializing");
            }
        };

        playButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Initializing Youtube Player.");
                mYoutubePlayerView.initialize(YoutubeConfig.getAPI_Key(), mOnInitializedListener);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SPEECHINTENT_REQ_CODE && resultCode == RESULT_OK){
            ArrayList<String> speechResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            String finaltext = speechResult.get(0);

            //microphoneResult.setText(finaltext);

            if(finaltext.toLowerCase().equals("video abspielen")){
                Log.d(TAG, "onClick: Initializing Youtube Player.");
                mYoutubePlayerView.initialize(YoutubeConfig.getAPI_Key(), mOnInitializedListener);
            }

            if(finaltext.toLowerCase().equals("news anzeigen")){
            }

            if(finaltext.toLowerCase().equals("zu youtube wechseln")){

            }
        }
    }

    private final List<Integer> blockedKeys = new ArrayList<>(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_POWER));

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            // Close every kind of system dialog
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);

            Log.d(TAG, "Clicked Button.");

            Intent speechRecognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString());

            startActivityForResult(speechRecognitionIntent, SPEECHINTENT_REQ_CODE);
        }
    }

    @Override
    public void onBackPressed() {
        // nothing to do here
        // … really
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (blockedKeys.contains(event.getKeyCode())) {
            return true;
        } else {

            return super.dispatchKeyEvent(event);
        }
    }
}
