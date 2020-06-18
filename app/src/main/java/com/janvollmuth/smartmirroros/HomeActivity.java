package com.janvollmuth.smartmirroros;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.janvollmuth.smartmirroros.Body.BodyMeasure;
import com.janvollmuth.smartmirroros.DataUpdater.UpdateListener;
import com.janvollmuth.smartmirroros.Weather.WeatherData;

/**
 * The main {@link Activity} class and entry point into the UI.
 */
public class HomeActivity extends FragmentActivity {

  /**
   * The IDs of {@link TextView TextViews} in {@link R.layout#activity_home} which contain the news
   * headlines.
   */
  private static final int[] NEWS_VIEW_IDS = new int[]{
      R.id.news_1,
      R.id.news_2,
      R.id.news_3,
      R.id.news_4,
      R.id.news_5,
  };

  /**
   * The listener used to populate the UI with weather data.
   */
  private final UpdateListener<WeatherData> weatherUpdateListener =
      new UpdateListener<WeatherData>() {
    @Override
    public void onUpdate(WeatherData data) {
      if (data != null) {

        // Populate the current temperature rounded to a whole number.
        String temperature = String.format(Locale.US, "%d°",
            Math.round(getLocalizedTemperature(data.currentTemperature)));
        temperatureView.setText(temperature);

        // Populate the 24-hour forecast summary, but strip any period at the end.
        String summary = util.stripPeriod(data.daySummary);
        weatherSummaryView.setText(summary);

        //Set the percentage for clouds
        String precipitation =
                String.format(Locale.GERMAN, "%d%%", Math.round(data.dayPrecipitationProbability));
        precipitationView.setText(precipitation);

        // Populate the precipitation probability as a percentage rounded to a whole number.
        /*String precipitation =
            String.format(Locale.US, "%d%%", Math.round(100 * data.dayPrecipitationProbability));
        precipitationView.setText(precipitation);*/

        // Populate the icon for the current weather.
        iconView.setImageResource(data.currentIcon);

        // Show all the views.
        temperatureView.setVisibility(View.VISIBLE);
        weatherSummaryView.setVisibility(View.VISIBLE);
        precipitationView.setVisibility(View.VISIBLE);
        iconView.setVisibility(View.VISIBLE);
      } else {

        // Hide everything if there is no data.
        temperatureView.setVisibility(View.GONE);
        weatherSummaryView.setVisibility(View.GONE);
        precipitationView.setVisibility(View.GONE);
        iconView.setVisibility(View.GONE);
      }
    }
  };

  /**
   * The listener used to populate the UI with news headlines.
   */
  private final UpdateListener<List<String>> newsUpdateListener =
      new UpdateListener<List<String>>() {
    @Override
    public void onUpdate(List<String> headlines) {

      // Populate the views with as many headlines as we have and hide the others.
      for (int i = 0; i < NEWS_VIEW_IDS.length; i++) {
        if ((headlines != null) && (i < headlines.size())) {
          newsViews[i].setText(headlines.get(i));
          newsViews[i].setVisibility(View.VISIBLE);
        } else {
          newsViews[i].setVisibility(View.GONE);
        }
      }
    }
  };

  /**
   * The listener used to populate the UI with body measurements.
   */
  private final UpdateListener<BodyMeasure[]> bodyUpdateListener =
      new UpdateListener<BodyMeasure[]>() {
        @Override
        public void onUpdate(BodyMeasure[] bodyMeasures) {
          if (bodyMeasures != null) {
            bodyView.setBodyMeasures(bodyMeasures);
            bodyView.setVisibility(View.VISIBLE);
          } else {
            bodyView.setVisibility(View.GONE);
          }
        }
      };

  private static final String TAG = HomeActivity.class.getSimpleName();

  private TextView temperatureView;
  private TextView weatherSummaryView;
  private TextView precipitationView;
  private ImageView iconView;
  private TextView[] newsViews = new TextView[NEWS_VIEW_IDS.length];
  private BodyView bodyView;
  private ImageButton microphoneView;
  private TextView microphoneResult;

  private Weather weather;
  private News news;
  private Body body;
  private Util util;
  private YoutubePlayerActivity youtubePlayer;

  private final int SPEECHINTENT_REQ_CODE = 11;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

    ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.RECORD_AUDIO},
            1);

    setContentView(R.layout.activity_home); //youtubeplayer.xml

    temperatureView = (TextView) findViewById(R.id.temperature);
    weatherSummaryView = (TextView) findViewById(R.id.weather_summary);
    precipitationView = (TextView) findViewById(R.id.precipitation);
    iconView = (ImageView) findViewById(R.id.icon);
    for (int i = 0; i < NEWS_VIEW_IDS.length; i++) {
      newsViews[i] = (TextView) findViewById(NEWS_VIEW_IDS[i]);
    }
    bodyView = (BodyView) findViewById(R.id.body);
    microphoneView = (ImageButton) findViewById(R.id.microphone);
    microphoneResult = (TextView) findViewById(R.id.microphoneResult);

    weather = new Weather(this, weatherUpdateListener);
    news = new News(newsUpdateListener);
    body = new Body(this, bodyUpdateListener);
    util = new Util(this);


    microphoneView.setOnClickListener(new View.OnClickListener(){

      @Override
      public void onClick(View view) {
          openYouTubeActivity();
      }
    });

  }

  void openYouTubeActivity(){
    Intent intent = new Intent(this, YoutubePlayerActivity.class);
    startActivity(intent);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         String permissions[], int[] grantResults) {
    switch (requestCode) {
      case 1: {

        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

          // permission was granted, yay! Do the
          // contacts-related task you need to do.
        } else {

          // permission denied, boo! Disable the
          // functionality that depends on this permission.
          Toast.makeText(this, "Permission denied to record audio", Toast.LENGTH_SHORT).show();
        }
        return;
      }

      // other 'case' lines to check for other
      // permissions this app might request
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    weather.start();
    news.start();
    body.start();
  }

  @Override
  protected void onStop() {
    weather.stop();
    news.stop();
    body.stop();
    super.onStop();
  }

  @Override
  protected void onResume() {
    super.onResume();
    util.hideNavigationBar(temperatureView);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    return util.onKeyUp(keyCode, event);
  }

  /**
   * Converts a temperature in degrees Fahrenheit to degrees Celsius, depending on the
   * {@link Locale}.
   */
  private double getLocalizedTemperature(double temperatureKelvin) {
    // First approximation: Kelvin for US and Celsius anywhere else.

    return Locale.US.equals(Locale.getDefault()) ?
        temperatureKelvin : (temperatureKelvin - 273.15);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if(requestCode == SPEECHINTENT_REQ_CODE && resultCode == RESULT_OK){
      ArrayList<String> speechResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

      String finaltext = speechResult.get(0);

      microphoneResult.setText(finaltext);

      if(finaltext.toLowerCase().equals("news ausblenden")){
        for (int i = 0; i < NEWS_VIEW_IDS.length; i++) {
          newsViews[i].setVisibility(View.INVISIBLE);
        }
      }

      if(finaltext.toLowerCase().equals("news anzeigen")){
        for (int i = 0; i < NEWS_VIEW_IDS.length; i++) {
          newsViews[i].setVisibility(View.VISIBLE);
        }
      }

      if(finaltext.toLowerCase().equals("zu youtube wechseln")){
        openYouTubeActivity();
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
