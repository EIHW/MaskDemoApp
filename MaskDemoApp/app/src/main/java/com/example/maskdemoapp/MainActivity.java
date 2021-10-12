// Author: Adria Mallol-Ragolta, 2021
// Chair of Embedded Intelligence for Health Care and Wellbeing, University of Augsburg, Germany

package com.example.maskdemoapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private MediaRecorder audioRecorder;
    private String outputFile;

    private AnimationDrawable micGif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions(permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        audioRecorder = null;

    }

    public void recordClick(android.view.View view)
    {

        String userID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        File dataPath = new File(getApplicationContext().getFilesDir().getAbsolutePath());

        ImageButton record = (ImageButton) findViewById(R.id.button_speech_record);
        final TextView screenMessage = (TextView) findViewById(R.id.text_speech_record);

        if (audioRecorder == null){
            long timestamp = Calendar.getInstance().getTimeInMillis();
            outputFile = dataPath + "/" + userID + "_" +  timestamp + "_recording.mp4";

            audioRecorder = new MediaRecorder();

            audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
            audioRecorder.setAudioChannels(2);
            audioRecorder.setAudioSamplingRate(44100);
            audioRecorder.setAudioEncodingBitRate(1411000);
            audioRecorder.setOutputFile(outputFile);

            try {
                audioRecorder.prepare();
                audioRecorder.start();

                screenMessage.setText(getResources().getString(R.string.stopRecInfo));
                record.setImageResource(R.drawable.trans);

                record.setBackgroundResource(R.drawable.gif_micon);
                micGif=(AnimationDrawable) record.getBackground();
                micGif.start();

            } catch (IOException e) {
                Log.e("Error", "prepare() failed");
                audioRecorder = null;
            }

        }
        else {
            audioRecorder.stop();
            audioRecorder.release();
            audioRecorder = null;
            micGif.stop();
            record.setImageResource(R.drawable.mic_000);
            screenMessage.setText(getResources().getString(R.string.waitRecInfo));

            byte[] byteRep = null;
            try {
                byteRep = Files.readAllBytes(Paths.get(outputFile));
            } catch (IOException e) {
                e.printStackTrace();
            }

            final String postUrl= "https://fdd6-94-216-47-127.ngrok.io/register"; // SERVER ENDPOINT URL
            final OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("uploadfile", outputFile.substring(outputFile.lastIndexOf("/")+1, outputFile.length()), RequestBody.create(MediaType.parse("audio/mpeg"), byteRep))
                    .build();

            Request request = new Request.Builder().url(postUrl)
                    .post(requestBody).build();

            final Context context;
            context = this;

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Cancel the post on failure.
                    Log.i("Info", e.toString());
                    call.cancel();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String serverResponse = response.body().string();
                                Log.i("Info", serverResponse);

                                File tmpFile = new File (outputFile);
                                if (tmpFile != null) {
                                    Log.i("Info Server", "File deleted from the system");
                                    tmpFile.delete ();
                                }

                                Intent intentEnd = new Intent(context, resultsScreen.class);

                                if (serverResponse.equals("yes")){
                                    intentEnd.putExtra("model_prediction", 1) ;
                                }
                                else if (serverResponse.equals("no")){
                                    intentEnd.putExtra("model_prediction", 0);
                                }

                                startActivity(intentEnd);

                                screenMessage.setText(getResources().getString(R.string.startRecInfo));

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

        }
    }
}