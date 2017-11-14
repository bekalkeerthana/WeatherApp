package edu.umb.cs443;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    public final static String DEBUG_TAG = "edu.umb.cs443.MYMSG";
    String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?q=";
    String BASE_ZIP = "http://api.openweathermap.org/data/2.5/weather?zip=";
    String IMG_URL = "http://openweathermap.org/img/w/";
    String APP_ID = "&APPID=f9a0da7858696d1453d0faa23006c2d9";


    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment mFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        mFragment.getMapAsync(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DownloadInfo extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return null;
            }
        }
        @Override
        protected void onPostExecute(String result) {
        String imageUrl;
            try {
               JSONObject jobject = new JSONObject(result);
               JSONObject main = jobject.getJSONObject("main");
               double temperature = main.getDouble("temp");
               temperature = temperature - 273.15;
               TextView text1 = (TextView)findViewById(R.id.textView) ;
               text1.setText(String.format("%.2f",temperature)+ "C");

                JSONObject coord = jobject.getJSONObject("coord");
                double latitude = coord.getDouble("lat");
                double longitude = coord.getDouble("lon");
                CameraUpdate umb= CameraUpdateFactory.newLatLng(new LatLng(latitude,longitude));
                float zoom=(float)Math.random()*10+5;
                CameraUpdate mzoom= CameraUpdateFactory.zoomTo(zoom);
                if (mMap!=null){
                    mMap.moveCamera(umb);
                    mMap.animateCamera(mzoom);
                   Marker marker = mMap.addMarker(new MarkerOptions()
                            .title("hi")
                            .position(new LatLng(latitude,longitude)));
                }
               JSONArray weather = jobject.getJSONArray("weather");
               JSONObject image1 = weather.getJSONObject(0);
               String image2 = image1.getString("icon");
               imageUrl = IMG_URL + image2 +".png";
               new DownloadWebpageTask().execute(imageUrl);
            } catch (Exception e)
            {

            }
        }
    }
    private String downloadUrl(String myurl) throws IOException {

        // params comes from the execute() call: params[0] is the url.
        InputStream is = null;
        String result;
        // TextView text = (TextView) findViewById(R.id.textView);
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.i(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();
            Reader reader = new InputStreamReader(is, "UTF-8");
            char[] buffer = new char[500];
            reader.read(buffer);
            result = new String(buffer);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {

                }
            }
        }
        return result;
    }
    private class DownloadWebpageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadImage(urls[0]);
            } catch (IOException e) {
                return null;
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Bitmap resultB) {
            ImageView img = (ImageView) findViewById(R.id.imageView);
            img.setImageBitmap(resultB);
        }
    }
        private Bitmap downloadImage(String myurlImage) throws IOException {
            InputStream is = null;
            try {
                URL url = new URL(myurlImage);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.i(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();

                Bitmap bitmap = BitmapFactory.decodeStream(is);
                return bitmap;
            } catch (Exception e) {
                Log.i(DEBUG_TAG, e.toString());
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            return null;
        }

        // onPostExecute displays the results of the AsyncTask.

        public void getWeatherInfo(View v) {
            InputStream is = null;
            TextView text1 = (TextView)findViewById(R.id.editText);
            String userInput = text1.getText().toString();
            String userUrl;
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if(userInput.matches("[0-9]+"))
                {
                    userUrl = BASE_ZIP +userInput + ",us" + APP_ID;
                }
                else
                {
                    userUrl = BASE_URL + userInput + APP_ID;
                }
                new DownloadInfo().execute(userUrl);
            }

        }


        @Override
        public void onMapReady(GoogleMap map) {
            this.mMap = map;
        }
    }

