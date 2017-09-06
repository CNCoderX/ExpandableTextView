package com.cncoderx.test.expandabletextview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cncoderx.expandabletextview.ExpandableTextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ExpandableTextView tv = (ExpandableTextView) findViewById(R.id.text);
        try {
            String text = readString(getAssets().open("1.txt"));
            tv.getTextView().setText(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readString(InputStream stream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuffer stringBuffer = new StringBuffer();
        String str;
        try {
            while ((str = reader.readLine()) != null) {
                stringBuffer.append(str).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int length = stringBuffer.length();
        return length == 0 ? stringBuffer.toString() :
                stringBuffer.subSequence(0, length - 1).toString();
    }
}
