package com.janvollmuth.smartmirroros;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

public class YoutubePlayerFragment extends YouTubeBaseActivity {

    private static final String TAG = "YoutubePlayerFragment";


    private HomeActivity homeActivity;
    YouTubePlayerView mYoutubePlayerView;
    Button playButton;
    YouTubePlayer.OnInitializedListener mOnInitializedListener;

    public YoutubePlayerFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}
