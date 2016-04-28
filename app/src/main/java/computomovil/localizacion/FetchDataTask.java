package computomovil.localizacion;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Romarin on 15/03/2016.
 */
public class FetchDataTask extends AsyncTask<String, Void, String> {

    private TextView label;
    public static final String URL_TAG = "URL_TAG";

    public FetchDataTask(TextView label) {
        this.label = label;
    }

    @Override
    protected String doInBackground(String... params) {

        String result = "No hay Resultados";
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {

            final String BASE_URL = "http://hmkcode.appspot.com/rest/controller/get.json";
            Uri buildUri = Uri.parse(BASE_URL).buildUpon().build();
            URL url = new URL(buildUri.toString());
            Log.d(URL_TAG, buildUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            result = buffer.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("URL_TAG", "Error closing stream", e);
                }
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        label.setText(s);
    }
}