package com.pyrunner.app;

import android.content.Context;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PythonExecutor {
    
    private Context context;
    private Python python;
    private boolean initialized;
    
    public PythonExecutor(Context context) {
        this.context = context;
        this.initialized = false;
        initPython();
    }
    
    private void initPython() {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(context));
        }
        python = Python.getInstance();
        initialized = true;
    }
    
    public String execute(String code) {
        if (!initialized) {
            return "Python环境未初始化";
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        
        try {
            System.setOut(printStream);
            System.setErr(printStream);
            
            PyObject pyObject = python.getModule("__main__");
            pyObject.exec(code);
            
            String output = outputStream.toString();
            if (output.isEmpty()) {
                output = "代码执行完成，无输出。";
            }
            return output;
            
        } catch (Exception e) {
            return "执行错误:\n" + e.getMessage() + "\n\n" + outputStream.toString();
        } finally {
            System.setOut(oldOut);
            System.setErr(oldErr);
            try {
                printStream.close();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public String executeFile(String filePath) {
        try {
            java.io.File file = new java.io.File(filePath);
            if (!file.exists()) {
                return "文件不存在: " + filePath;
            }
            
            java.io.FileInputStream fis = new java.io.FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            
            String code = new String(data);
            return execute(code);
            
        } catch (Exception e) {
            return "文件执行错误:\n" + e.getMessage();
        }
    }
}
