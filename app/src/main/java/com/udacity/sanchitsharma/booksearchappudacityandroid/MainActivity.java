package com.udacity.sanchitsharma.booksearchappudacityandroid;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> bookdata = new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    private ListView books;
    private Button button;
    private TextView textView;
    private ProgressBar spinner;
    private AlertDialog dialog;
    private int noresult = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {

            bookdata = savedInstanceState.getStringArrayList(getString(R.string.AsItIs));
        }

        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);
        textView = (TextView) findViewById(R.id.SearchText);

        books = (ListView) findViewById(R.id.books);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, bookdata);
        books.setAdapter(arrayAdapter);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bookdata.clear();
                String searchText = textView.getText().toString();
                //System.out.println("Search Text " + searchText);

                textView.setText("");
                textView.clearFocus();
                books.setVisibility(View.INVISIBLE);
                if (isNetworkAvailable()) {
                    if (searchText.length() > 0) {
                        Toast.makeText(getApplicationContext(), R.string.WaitAMinute, Toast.LENGTH_LONG).show();
                        spinner.setVisibility(View.VISIBLE);
                        Downloader task = new Downloader();
                        String url = getString(R.string.url1) + searchText + getString(R.string.url2);
                        url = url.replaceAll(getString(R.string.escapechar), getString(R.string.blankstr));
                        task.execute(url);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.aBookHasAName, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.whyNoInternet, Toast.LENGTH_LONG).show();
                    spinner.setVisibility(View.GONE);
                    textView.clearFocus();
                    dialog = new AlertDialog.Builder(
                            MainActivity.this).create();
                    dialog.setTitle(getString(R.string.noInternet));
                    dialog.setMessage(getString(R.string.GottaReConnect));
                    dialog.show();
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(getString(R.string.AsIs), bookdata);
        super.onSaveInstanceState(outState);
    }

    public class Downloader extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String bookdata = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);

                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {
                    char c = (char) data;

                    bookdata += c;

                    data = reader.read();
                }

                return bookdata;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (bookdata == "") {
                noresult = 1;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            if (noresult == 1) {
                Toast.makeText(getApplicationContext(), R.string.nothingToShow, Toast.LENGTH_LONG).show();
            }

            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String books = jsonObject.getString(getString(R.string.items));

                    JSONArray arr = new JSONArray(books);

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject jsonPart = arr.getJSONObject(i);

                        JSONObject volumeInfo = jsonPart.getJSONObject(getString(R.string.vol));

                        String title = volumeInfo.getString(getString(R.string.title));
                        String authors = "";
                        try {
                            JSONArray authorArr = volumeInfo.getJSONArray(getString(R.string.author));
                            for (int x = 0; x < authorArr.length(); x++) {

                                authors += authorArr.get(x) + "    ";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        String book = title + getString(R.string.lineitem) + authors + ".";
                        bookdata.add(book);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), R.string.againNothingToShow, Toast.LENGTH_LONG).show();
                }

                spinner.setVisibility(View.GONE);
                books.setAdapter(arrayAdapter);
                books.setVisibility(View.VISIBLE);


            } else {
                Toast.makeText(getApplicationContext(), R.string.justNothingToShow, Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(result);
        }
    }

}
