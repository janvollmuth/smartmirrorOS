package com.janvollmuth.smartmirroros;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.janvollmuth.smartmirroros.Body.BodyMeasure;
import com.janvollmuth.smartmirroros.Commute.CommuteSummary;
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
  private Commute commute;
  private Util util;
  //private Assistant assistant;
  private YoutubePlayerFragment youtubePlayer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
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
    //assistant = new Assistant(this, microphoneView);


    microphoneView.setOnClickListener(new View.OnClickListener(){

      @Override
      public void onClick(View view) {
          openYouTubeActivity();
      }
    });

  }

  void openYouTubeActivity(){
    Intent intent = new Intent(this, YoutubePlayerFragment.class);
    startActivity(intent);
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
  private double getLocalizedTemperature(double temperatureFahrenheit) {
    // First approximation: Fahrenheit for US and Celsius anywhere else.
    return Locale.US.equals(Locale.getDefault()) ?
        temperatureFahrenheit : (temperatureFahrenheit - 32.0) / 1.8;
  }
/*
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if(requestCode == assistant.SPEECHINTENT_REQ_CODE && resultCode == RESULT_OK){
      ArrayList<String> speechResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

      String finaltext = speechResult.get(0);

      microphoneResult.setText(finaltext);

      if(finaltext.equals("News verstecken")){
        for (int i = 0; i < NEWS_VIEW_IDS.length; i++) {
          newsViews[i].setVisibility(View.INVISIBLE);
        }
      }

      if(finaltext.equals("News anzeigen")){
        for (int i = 0; i < NEWS_VIEW_IDS.length; i++) {
          newsViews[i].setVisibility(View.VISIBLE);
        }
      }
    }
  }*/
}
