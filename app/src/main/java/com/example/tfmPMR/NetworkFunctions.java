package com.example.tfmPMR;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Class for doing querys to opendata
 */
public class NetworkFunctions extends Thread {
    public String resultRequest;
    private String paramURL;
    private String headerURL = "http://opendata.caceres.es/sparql/?default-graph-uri=";

    /**
     * Parametric constructor for the class
     *
     * @param url
     * @throws IOException
     */
    NetworkFunctions(String url) throws IOException {
        this.paramURL = headerURL.concat(url);
    }

    /**
     * Main method
     */
    @Override
    public void run() {
        super.run();
        try {
            resultRequest = doGetRequest(this.paramURL);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Function to do a request using OkHttpClient
     *
     * @param url
     * @return
     * @throws IOException
     */
    private String doGetRequest(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();

        return response.body().string();
    }


}