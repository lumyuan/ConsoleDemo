package com.example.console.demo;

import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;

import com.example.console.demo.console.ConsoleModel;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private EditText inputView;
    private TextView logcatTextView;

    private ConsoleModel consoleModel;
    private boolean isInput = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputView = findViewById(R.id.inputLayout);
        Button execButton = findViewById(R.id.execButton);
        Button restartButton = findViewById(R.id.restartButton);
        logcatTextView = findViewById(R.id.logcatTextView);
        try {
            initConsole();
        } catch (IOException e) {
            e.printStackTrace();
            toast(e.toString());
        }

        //执行指令
        execButton.setOnClickListener(view -> {
            String cmd = inputView.getText().toString();
            if (consoleModel != null){
                try {
                    isInput = true;
                    showInput(cmd);
                    inputView.setText("");
                    consoleModel.exec(cmd);
                } catch (IOException e) {
                    e.printStackTrace();
                    toast(e.toString());
                    showErrorLog(e.toString());
                }
            }else {
                toast("执行失败！控制台对象为空");
            }
        });

        //重启进程
        restartButton.setOnClickListener(view -> {
            try {
                consoleModel.restart("sh");
            } catch (IOException e) {
                e.printStackTrace();
                toast(e.toString());
            }
        });
    }

    private void toast(CharSequence charSequence){
        Toast.makeText(this, charSequence, Toast.LENGTH_SHORT).show();
    }

    private void initConsole() throws IOException {
        consoleModel = ConsoleModel.newInstance("sh");
        //解决重载后log消失的情况
        for (ConsoleModel.LogcatBody logcatBody : consoleModel.getLogcatList()) {
            logcatTextView.append(Html.fromHtml(setColor(logcatBody.getMessage(), logcatBody.getType() == ConsoleModel.CONSOLE_TYPE_SUCCESS ? "green" : "red")));
        }
        //添加成功Log的观察者
        consoleModel.observeSuccess(value ->
                runOnUiThread(()->
                        showSuccessLog(value)
                )
        );
        //添加错误Log的观察者
        consoleModel.observeError(value ->
                runOnUiThread(()->
                        showErrorLog(value)
                )
        );
    }

    @UiThread
    private void showSuccessLog(String log){
        logcatTextView.append(Html.fromHtml(setColor(log, "green")));
    }

    @UiThread
    private void showErrorLog(String log){
        logcatTextView.append(Html.fromHtml(setColor(log, "red")));
    }

    private void showInput(String log){
        logcatTextView.append(Html.fromHtml("input > <font color=\"gray\">" + log + "</font><br/>"));
    }


    private String setColor(String content, String dexColor){
        String html = "<font color=\"" + dexColor + "\" >" + (isInput ? "message > " : "")
                + content + "</font><br/>";
        isInput = false;
        return html;
    }

}