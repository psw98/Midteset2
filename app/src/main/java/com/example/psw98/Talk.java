package com.example.psw98;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class Talk extends AppCompatActivity {
    String baseURL = "https://builder.pingpong.us/api/builder/5cf3d221e4b0c064fb2e228f/integration/v0.2/custom/1";
    String key = "Basic a2V5OmY4ZGY1MGYyZDlkNTNiZTU4ZWRlNWMxMjBjNWRiMTFl";
    TextView text2;
    ArrayList<String> history = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        EditText inputbar = findViewById(R.id.inputMessage);
        Button sendButton = findViewById(R.id.sendButton);
        TextView text1 = findViewById(R.id.text1);
        text2  = findViewById(R.id.text2);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String request = inputbar.getText().toString();
                if(request.equals(""))
                    return;
                text1.setText(request);
                inputbar.setText("");
                new Thread() {
                    String response;
                    @Override
                    public void run() {
                        super.run();
                        response = getResponse(request);
                        Message message = handler.obtainMessage();
                        message.obj = response;
                        handler.sendMessage(message);
                        history.add(request);
                        if(history.size() > 5)
                            history.remove(0);
                    }
                }.start();
            }
        });
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.obj instanceof String)
                text2.setText(msg.obj.toString());
        }
    };

    public String getResponse(String request) {
        try {
            URL url = new URL(baseURL);
            HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
            c.setRequestMethod("POST");
            c.setRequestProperty("Authorization", key);
            c.setRequestProperty("Content-Type", "application/json");
            c.setDoOutput(true); //데이터 보내는 거 설정
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(c.getOutputStream(), "UTF-8"));
            String requestData = "{\"request\": {\"dialog\": [";
            for(String message : history)
                requestData = requestData.concat(String.format("\"%s\", ", message));
            requestData = requestData.concat(String.format("\"%s\"]}}", request));
            writer.write(String.format(requestData, request));
            writer.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
            JSONObject data = new JSONObject(reader.readLine());
            return data.getJSONObject("response").getJSONArray("replies").getJSONObject(0).get("text").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
