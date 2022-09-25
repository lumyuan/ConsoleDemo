package com.example.console.demo.console;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ConsoleModel {

    public static final String CONSOLE_PERMISSION_SHELL = "sh";
    public static final String CONSOLE_PERMISSION_SU = "su";

    public static final int CONSOLE_TYPE_SUCCESS = 0;
    public static final int CONSOLE_TYPE_ERROR = 1;

    private final MutableData<String> successData = new MutableData<>();
    private final MutableData<String> errorData = new MutableData<>();

    private final List<LogcatBody> list = new ArrayList<>();

    public void observeSuccess(MutableData.Observer<String> observer){
        successData.observe(observer);
    }

    public void observeError(MutableData.Observer<String> observer){
        errorData.observe(observer);
    }

    private Process process;
    private static volatile ConsoleModel consoleModel;

    private ConsoleModel(String cmd) throws IOException {
        process = initProcess(cmd);
    }

    /**
     * 单例模式
     * @param cmd 指令
     * @return 控制台
     * @throws IOException IO异常
     */
    public static ConsoleModel newInstance(String cmd) throws IOException {
        if (consoleModel == null){
            synchronized (ConsoleModel.class){
                if (consoleModel == null){
                    consoleModel = new ConsoleModel(cmd);
                }
            }
        }
        return consoleModel;
    }

    private Process initProcess(String cmd) throws IOException {
        Process process = Runtime.getRuntime().exec(cmd);
        {//成功时的log读取
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            //写入log
            readLine(CONSOLE_TYPE_SUCCESS, bufferedReader);
        }
        {//失败时的log读取
            InputStream inputStream = process.getErrorStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            //写入log
            readLine(CONSOLE_TYPE_ERROR, bufferedReader);
        }
        return process;
    }

    public void restart(String cmd) throws IOException {
        destroy();
        process = initProcess(cmd);
    }

    public void exec(String cmd) throws IOException {
        OutputStream outputStream = process.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeBytes(cmd);
        dataOutputStream.writeBytes("\n");
        dataOutputStream.flush();
    }

    public String getLogcat(){
        StringBuilder stringBuilder = new StringBuilder();
        for (LogcatBody logcatBody : list) {
            stringBuilder
                    .append(logcatBody.getMessage())
                    .append("\n");
        }
        return stringBuilder.toString();
    }

    public List<LogcatBody> getLogcatList(){
        return new ArrayList<>(list);
    }

    private void readLine(int type, BufferedReader bufferedReader){
        new Thread(()-> {
            try {
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    if (type == CONSOLE_TYPE_SUCCESS){
                        successData.setValue(line);
                    }else {
                        errorData.setValue(line);
                    }
                    list.add(new LogcatBody(type, line));
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 退出程序
     */
    public void destroy(){
        list.clear();
        successData.clean();
        errorData.clean();
        try {
            process.getOutputStream().close();
            process.getInputStream().close();
            process.getErrorStream().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        process.destroy();
        consoleModel = null;
    }

    public static class LogcatBody{
        private int type;
        private String message;

        public LogcatBody() {
        }

        public LogcatBody(int type, String message) {
            this.type = type;
            this.message = message;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString();
        }
    }
}
