package com.shiv.testapp1;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static com.shiv.testapp1.GlobalVars.BASE_REQ_URL;
import static com.shiv.testapp1.GlobalVars.DEFAULT_LANG_POS;
import static com.shiv.testapp1.GlobalVars.LANGUAGE_CODES;

public class Main2Activity extends AppCompatActivity {

    public static final String LOG_TAG = Main2Activity.class.getName();
    private static final int REQ_CODE_SPEECH_INPUT = 1;

    private Spinner mSpinnerLanguageFrom;                   //    Dropdown list for selecting base language (From)
    private Spinner mSpinnerLanguageTo;                     //    Dropdown list for selecting translation language (To)
    private String mLanguageCodeFrom = "en";                //    Language Code (From)
    private String mLanguageCodeTo = "en";                  //    Language Code (To)
    private EditText mTextInput;                            //    Input text ( in From language )
    private TextView mTextTranslated;                       //    Output Translated text ( in To language )
    private Dialog process_tts;                             //    Dialog box for Text to Speech Engine Language Switch
    HashMap<String, String> map = new HashMap<>();
    volatile boolean activityRunning;                       //    To track status of current activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        activityRunning = true;
        TextView mEmptyTextView = (TextView) findViewById(R.id.empty_view_not_connected);
        mSpinnerLanguageFrom = (Spinner) findViewById(R.id.spinner_language_from);
        mSpinnerLanguageTo = (Spinner) findViewById(R.id.spinner_language_to);
        Button mButtonTranslate = (Button) findViewById(R.id.button_translate);         //      Translate button to translate text

        mTextInput = (EditText) findViewById(R.id.text_input);
        mTextTranslated = (TextView) findViewById(R.id.text_translated);
        mTextTranslated.setMovementMethod(new ScrollingMovementMethod());
        process_tts = new Dialog(Main2Activity.this);
        process_tts.setContentView(R.layout.dialog_processing_tts);
        process_tts.setTitle(getString(R.string.process_tts));
        TextView title = (TextView) process_tts.findViewById(android.R.id.title);
        // title.setSingleLine(false);

        //  CHECK INTERNET CONNECTION
        if (!isOnline()) {
            mEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyTextView.setVisibility(View.GONE);
            //  GET LANGUAGES LIST
            new GetLanguages().execute();


            //  TRANSLATE
            mButtonTranslate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String input = mTextInput.getText().toString();
                    new TranslateText().execute(input);
                }
            });


            //  SPINNER LANGUAGE FROM
            mSpinnerLanguageFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mLanguageCodeFrom = LANGUAGE_CODES.get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Toast.makeText(getApplicationContext(), "No option selected", Toast.LENGTH_SHORT).show();
                }
            });
            //  SPINNER LANGUAGE TO
            mSpinnerLanguageTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mLanguageCodeTo = LANGUAGE_CODES.get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Toast.makeText(getApplicationContext(), "No option selected", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //  CHECK INTERNET CONNECTION
    public boolean isOnline() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return false;
    }






    //  SUBCLASS TO TRANSLATE TEXT ON BACKGROUND THREAD
    private class TranslateText extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... input) {
            Uri baseUri = Uri.parse(BASE_REQ_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();
            uriBuilder.appendPath("translate")
                    .appendQueryParameter("key", getString(R.string.API_KEY))
                    .appendQueryParameter("lang", mLanguageCodeFrom + "-" + mLanguageCodeTo)
                    .appendQueryParameter("text", input[0]);
            Log.e("String Url ---->", uriBuilder.toString());
            return QueryUtils.fetchTranslation(uriBuilder.toString());
        }

        @Override
        protected void onPostExecute(String result) {
            if (activityRunning) {
                mTextTranslated.setText(result);
            }
        }
    }

    //  SUBCLASS TO GET LIST OF LANGUAGES ON BACKGROUND THREAD
    private class GetLanguages extends AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            Uri baseUri = Uri.parse(BASE_REQ_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();
            uriBuilder.appendPath("getLangs")
                    .appendQueryParameter("key", getString(R.string.API_KEY))
                    .appendQueryParameter("ui", "en");
            Log.e("String Url ---->", uriBuilder.toString());
            return QueryUtils.fetchLanguages(uriBuilder.toString());
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (activityRunning) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(Main2Activity.this, android.R.layout.simple_spinner_item, result);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinnerLanguageFrom.setAdapter(adapter);
                mSpinnerLanguageTo.setAdapter(adapter);
                //  SET DEFAULT LANGUAGE SELECTIONS
                mSpinnerLanguageFrom.setSelection(DEFAULT_LANG_POS);
                mSpinnerLanguageTo.setSelection(DEFAULT_LANG_POS);
            }
        }
    }
}