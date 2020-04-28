package com.janvollmuth.smartmirroros;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.janvollmuth.smartmirroros.Weather.WeatherData;

/**
 * A helper class to regularly retrieve weather information.
 */
public class Weather extends DataUpdater<WeatherData> {
  private static final String TAG = Weather.class.getSimpleName();

  /**
   * The time in milliseconds between API calls to update the weather.
   */
  private static final long UPDATE_INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(5);

  /**
   * The time in seconds over which to average the precipitation probability.
   */
  private static final long PRECIPITATION_WINDOW_SECONDS = TimeUnit.HOURS.toSeconds(12);

  /**
   * The context used to load string resources.
   */
  private final Context context;

  /**
   * A {@link Map} from OpenWeatherMap's icon code to the corresponding drawable resource ID.
   */
  private final Map<String, Integer> iconResources = new HashMap<String, Integer>(){{
    put("01d", R.drawable.clear_sky);
    put("02d",R.drawable.few_clouds);
    put("03d",R.drawable.scattered_clouds);
    put("04d",R.drawable.broken_clouds);
    put("09d",R.drawable.shower_rain);
    put("10d",R.drawable.rain);
    put("11d",R.drawable.thunderstorm);
    put("13d",R.drawable.snow);
    put("50d",R.drawable.mist);
  }};

  /**
   * The current location, which is assumed to be static.
   */
  private Location location;

  /**
   * The data structure containing the weather information we are interested in.
   */
  public class WeatherData {

    /**
     * The current temperature in degrees Fahrenheit.
     */
    public final double currentTemperature;

    /**
     * The current precipitation probability as a value between 0 and 1.
     */
    public final double currentPrecipitationProbability;

    /**
     * A human-readable summary of the 24-hour forecast.
     */
    public final String daySummary;

    /**
     * The average precipitation probability during the 24-hour forecast as a value between 0 and 1.
     */
    public final double dayPrecipitationProbability;

    /**
     * The resource ID of the icon representing the current weather conditions.
     */
    public final int currentIcon;

    /**
     * The resource ID of the icon representing the weather conditions during the 24-hour forecast.
     */
    public final int dayIcon;

    public WeatherData(double currentTemperature, double currentPrecipitationProbability,
        String daySummary, double dayPrecipitationProbability, int currentIcon, int dayIcon) {

      Log.d(TAG, "New WeatherData");

      this.currentTemperature = currentTemperature;
      this.currentPrecipitationProbability = currentPrecipitationProbability;
      this.daySummary = daySummary;
      this.dayPrecipitationProbability = currentPrecipitationProbability; //TODO: this shoulb be implemented in another way
      this.currentIcon = currentIcon;
      this.dayIcon = dayIcon;

      //Log.d(TAG, "New WeatherData");
    }
  }

  public Weather(Context context, UpdateListener<WeatherData> updateListener) {
    super(updateListener, UPDATE_INTERVAL_MILLIS);
    this.context = context;
  }

  @Override
  protected WeatherData getData() {
    // Lazy load the location.
    if (location == null) {
      // We're using geo location by IP, because many headless Android devices don't return anything
      // useful through the usual location APIs.
      location = GeoLocation.getLocation();
      Log.d(TAG, "Using location for weather: " + location);
    }


    // Get the latest data from the Dark Sky API.
    String requestUrl = getRequestUrl(location);

    // Parse the data we are interested in from the response JSON.
    try {
      JSONObject response = Network.getJson(requestUrl);

      Log.d(TAG, "Response: " + response);

      if (response != null) {
        return new WeatherData(
            parseCurrentTemperature(response),
            parseCurrentPrecipitationProbability(response),
            parseDaySummary(response),
            parseCurrentPrecipitationProbability(response), //TODO: this is hacky (previously: parseDayPrecipitationProbability())
            parseCurrentIcon(response),
            parseDayIcon(response));
      } else {
        return null;
      }
    } catch (JSONException e) {
      Log.e(TAG, "Failed to parse weather JSON.", e);
      return null;
    }
  }

  /**
   * Creates the URL for a OpenWeatherMap API request based on the specified {@link Location} or
   * {@code null} if the location is unknown.
   */
  private String getRequestUrl(Location location){

    Log.d(TAG, "location: " + location);

    if(location != null){
      return String.format(Locale.US, "https://api.openweathermap.org/data/2.5/onecall?lat=%f&lon=%f&appid=%s",
              location.getLatitude(),
              location.getLongitude(),
              context.getString(R.string.open_weather_map_api_key));
    } else {
      return null;
    }
  }

  /**
   * Reads the current temperature from the API response. API documentation:
   * https://openweathermap.org/api
   */
  private Double parseCurrentTemperature(JSONObject response) throws JSONException {
    JSONObject currently = response.getJSONObject("current");

    Log.d(TAG, "temp: " + currently.getDouble("temp"));

    return currently.getDouble("temp");
  }

  /**
   * Reads the current cloud probability from the API response. API documentation:
   * https://openweathermap.org/api
   */
  private Double parseCurrentPrecipitationProbability(JSONObject response) throws JSONException {

    Log.d(TAG, "clouds...");

    JSONArray currently = response.getJSONArray("daily");
    JSONObject day = currently.getJSONObject(0);

    Log.d(TAG, "Percentage for clouds: " + day.getDouble("clouds"));

    return day.getDouble("clouds");
  }

  /**
   * Reads the 24-hour forecast summary from the API response. API documentation:
   * https://openweathermap.org/api
   */
  private String parseDaySummary(JSONObject response) throws JSONException {

    Log.d(TAG, "daysummary...");

    JSONArray hourly = response.getJSONArray("hourly");
    JSONObject day = hourly.getJSONObject(0);
    JSONArray weather = day.getJSONArray("weather");
    JSONObject detail = weather.getJSONObject(0);

    return detail.getString("description");
  }

  /**
   * Reads the 24-hour forecast precipitation probability from the API response. API documentation:
   * https://darksky.net/dev/docs
   */
/*  private Double parseDayPrecipitationProbability(JSONObject response) throws JSONException {
    long currentTime = response.getJSONObject("currently").getLong("time");
    JSONObject hourly = response.getJSONObject("hourly");
    JSONArray data = hourly.getJSONArray("data");

    // Calculate the average over the coming hours.
    StatsAccumulator stats = new StatsAccumulator();
    for (int i = 0; i < data.length(); i++) {
      JSONObject object = data.getJSONObject(i);
      long time = object.getLong("time");
      if (time >= currentTime && time <= currentTime + PRECIPITATION_WINDOW_SECONDS) {
        double probability = object.getDouble("precipProbability");
        stats.add(probability);
      }
    }
    return stats.mean();
  }*/

  /**
   * Reads the current weather icon code from the API response. API documentation:
   * https://openweathermap.org/api
   */
  private Integer parseCurrentIcon(JSONObject response) throws JSONException {

    Log.d(TAG, "currentIcon...");

    JSONObject currently = response.getJSONObject("current");
    JSONArray weather = currently.getJSONArray("weather");
    JSONObject detail = weather.getJSONObject(0);
    String icon = detail.getString("icon");

    Log.d(TAG, "currentIcon...finished: " + icon);

    if(iconResources.get(icon) == null){
      return iconResources.get("01d");
    }

    return iconResources.get(icon);
  }

  /**
   * Reads the 24-hour forecast weather icon code from the API response. API documentation:
   * https://openweathermap.org/api
   */
  private Integer parseDayIcon(JSONObject response) throws JSONException {

    Log.d(TAG, "dayIcon...");

    JSONArray daily = response.getJSONArray("daily");
    JSONObject day = daily.getJSONObject(0);
    JSONArray weather = day.getJSONArray("weather");
    JSONObject detail = weather.getJSONObject(0);
    String icon = detail.getString("icon");

    if(iconResources.get(icon) == null){
      return iconResources.get("01d");
    }

    return iconResources.get(icon);
  }

  @Override
  protected String getTag() {
    return TAG;
  }
}
