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
            URL url = new URL(baseURL);//url 만들기
            HttpsURLConnection c = (HttpsURLConnection) url.openConnection();//url 연결과  연결처리 옵젝  저장
            c.setRequestMethod("POST");//포스트방식으로 연결설정
            c.setRequestProperty("Authorization", key);//request요소중 key값 설정
            c.setRequestProperty("Content-Type", "application/json");//받아오는 타입을 json으로 설정
            c.setDoOutput(true); //데이터 보내는 거 설정
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(c.getOutputStream(), "UTF-8"));//데이터 보내는 오브젝트 생성 및 저장
            String requestData = "{\"request\": {\"dialog\": [";
            for(String message : history)//history 보낸 메세지들 저장, message안에 history배열의 요소들이 하나씩 대입
                requestData = requestData.concat(String.format("\"%s\", ", message));//requestData와 보낸 메세지 합침
            requestData = requestData.concat(String.format("\"%s\"]}}", request));//이번에 받은 메세지 넣기 여기까지request문자열 가공
            writer.write(requestData);//작성
            writer.flush();//보내기
            BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));//데이터 받는 오브젝트 생성 및 저장
            JSONObject data = new JSONObject(reader.readLine());//받아온 데이터 json타입으로 저장하기
            return data.getJSONObject("response").getJSONArray("replies").getJSONObject(0).get("text").toString();//response json에서 필요한 값  추출 후 반환
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
