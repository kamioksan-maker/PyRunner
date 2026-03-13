package com.pyrunner.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pyrunner.app.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements FileAdapter.OnFileItemClickListener {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String SCRIPTS_DIR = "scripts";
    private static final String METADATA_FILE = "metadata.json";

    private ActivityMainBinding binding;
    private ExecutorService executorService;
    private Handler mainHandler;
    private String currentFileName = "untitled.py";
    private boolean hasUnsavedChanges = false;
    private File scriptsDir;
    private List<ScriptFile> scriptFiles;
    private FileAdapter fileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        setupToolbar();
        setupButtons();
        setupCodeEditor();
        setupScriptsDirectory();
        checkPermissions();

        loadDefaultCode();
    }

    private void setupToolbar() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
    }

    private void setupButtons() {
        binding.btnRun.setOnClickListener(v -> runPythonCode());
        binding.btnNew.setOnClickListener(v -> createNewFile());
        binding.btnOpen.setOnClickListener(v -> showOpenDialog());
        binding.btnSave.setOnClickListener(v -> showSaveDialog());
        binding.btnClearOutput.setOnClickListener(v -> binding.tvOutput.setText(""));
    }

    private void setupCodeEditor() {
        binding.etCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                hasUnsavedChanges = true;
                updateLineNumbers();
            }
        });
    }

    private void updateLineNumbers() {
        String code = binding.etCode.getText().toString();
        int lineCount = code.split("\n", -1).length;
        StringBuilder lineNumbers = new StringBuilder();
        for (int i = 1; i <= lineCount; i++) {
            lineNumbers.append(i).append("\n");
        }
        binding.tvLineNumbers.setText(lineNumbers.toString().trim());
    }

    private void setupScriptsDirectory() {
        scriptsDir = new File(getFilesDir(), SCRIPTS_DIR);
        if (!scriptsDir.exists()) {
            scriptsDir.mkdirs();
        }
        scriptFiles = new ArrayList<>();
        fileAdapter = new FileAdapter(this);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadDefaultCode() {
        String defaultCode = "# PyRunner - Python脚本运行器\n" +
            "# 在此输入您的Python代码\n\n" +
            "print('Hello, PyRunner!')\n\n" +
            "# 示例: 数学计算\n" +
            "import math\n" +
            "print(f'圆周率: {math.pi:.4f}')\n" +
            "print(f'2的10次方: {2**10}')\n\n" +
            "# 示例: 列表操作\n" +
            "numbers = [1, 2, 3, 4, 5]\n" +
            "print(f'列表: {numbers}')\n" +
            "print(f'求和: {sum(numbers)}')\n";
        
        binding.etCode.setText(defaultCode);
        updateLineNumbers();
    }

    private void runPythonCode() {
        String code = binding.etCode.getText().toString();
        if (code.trim().isEmpty()) {
            Toast.makeText(this, "请输入代码", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnRun.setEnabled(false);
        binding.tvOutput.setText("正在执行...\n");

        executorService.execute(() -> {
            try {
                PythonExecutor executor = new PythonExecutor(this);
                final String output = executor.execute(code);
                
                mainHandler.post(() -> {
                    binding.tvOutput.setText(output);
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnRun.setEnabled(true);
                    scrollToBottom();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    binding.tvOutput.setText("执行错误:\n" + e.getMessage());
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnRun.setEnabled(true);
                });
            }
        });
    }

    private void scrollToBottom() {
        binding.outputScrollView.post(() -> 
            binding.outputScrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void createNewFile() {
        if (hasUnsavedChanges) {
            new MaterialAlertDialogBuilder(this)
                .setTitle("未保存的更改")
                .setMessage("当前文件有未保存的更改，是否保存？")
                .setPositiveButton("保存", (dialog, which) -> {
                    showSaveDialog();
                    resetEditor();
                })
                .setNegativeButton("不保存", (dialog, which) -> resetEditor())
                .setNeutralButton("取消", null)
                .show();
        } else {
            resetEditor();
        }
    }

    private void resetEditor() {
        binding.etCode.setText("");
        currentFileName = "untitled.py";
        binding.tvFileName.setText(currentFileName);
        hasUnsavedChanges = false;
        updateLineNumbers();
    }

    private void showOpenDialog() {
        loadScriptFiles();
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_file_list, null);
        RecyclerView rvFiles = dialogView.findViewById(R.id.rvFiles);
        TextView tvNoFiles = dialogView.findViewById(R.id.tvNoFiles);
        
        rvFiles.setLayoutManager(new LinearLayoutManager(this));
        rvFiles.setAdapter(fileAdapter);
        
        if (scriptFiles.isEmpty()) {
            rvFiles.setVisibility(View.GONE);
            tvNoFiles.setVisibility(View.VISIBLE);
        } else {
            rvFiles.setVisibility(View.VISIBLE);
            tvNoFiles.setVisibility(View.GONE);
            fileAdapter.setFiles(scriptFiles);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void loadScriptFiles() {
        scriptFiles.clear();
        
        File metadataFile = new File(scriptsDir, METADATA_FILE);
        JSONObject metadata = new JSONObject();
        
        if (metadataFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(metadataFile);
                byte[] data = new byte[(int) metadataFile.length()];
                fis.read(data);
                fis.close();
                metadata = new JSONObject(new String(data));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File[] files = scriptsDir.listFiles((dir, name) -> name.endsWith(".py"));
        if (files != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            for (File file : files) {
                ScriptFile sf = new ScriptFile();
                sf.file = file;
                sf.name = file.getName();
                sf.lastModified = sdf.format(new Date(file.lastModified()));
                
                try {
                    if (metadata.has(sf.name)) {
                        JSONObject fileMeta = metadata.getJSONObject(sf.name);
                        sf.description = fileMeta.optString("description", "");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                
                scriptFiles.add(sf);
            }
        }
        
        scriptFiles.sort((a, b) -> Long.compare(b.file.lastModified(), a.file.lastModified()));
    }

    private void showSaveDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_save_file, null);
        EditText etFileName = dialogView.findViewById(R.id.etFileName);
        
        String nameWithoutExt = currentFileName.replace(".py", "");
        etFileName.setText(nameWithoutExt);
        etFileName.selectAll();

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String fileName = etFileName.getText().toString().trim();
            if (fileName.isEmpty()) {
                Toast.makeText(this, "请输入文件名", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!fileName.endsWith(".py")) {
                fileName += ".py";
            }
            
            saveFile(fileName);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveFile(String fileName) {
        String code = binding.etCode.getText().toString();
        File file = new File(scriptsDir, fileName);
        
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(code.getBytes());
            fos.close();
            
            currentFileName = fileName;
            binding.tvFileName.setText(fileName);
            hasUnsavedChanges = false;
            
            updateMetadata(fileName);
            
            Toast.makeText(this, R.string.file_saved, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, R.string.error_saving, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void updateMetadata(String fileName) {
        File metadataFile = new File(scriptsDir, METADATA_FILE);
        JSONObject metadata = new JSONObject();
        
        if (metadataFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(metadataFile);
                byte[] data = new byte[(int) metadataFile.length()];
                fis.read(data);
                fis.close();
                metadata = new JSONObject(new String(data));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        try {
            JSONObject fileMeta = new JSONObject();
            fileMeta.put("lastSaved", System.currentTimeMillis());
            metadata.put(fileName, fileMeta);
            
            FileOutputStream fos = new FileOutputStream(metadataFile);
            fos.write(metadata.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFileClick(ScriptFile file) {
        loadFile(file.file);
    }

    @Override
    public void onFileDelete(ScriptFile file) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("删除文件")
            .setMessage("确定要删除 " + file.name + " 吗？")
            .setPositiveButton("删除", (dialog, which) -> {
                if (file.file.delete()) {
                    Toast.makeText(this, "文件已删除", Toast.LENGTH_SHORT).show();
                    loadScriptFiles();
                    fileAdapter.setFiles(scriptFiles);
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void loadFile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            
            String code = new String(data);
            binding.etCode.setText(code);
            currentFileName = file.getName();
            binding.tvFileName.setText(currentFileName);
            hasUnsavedChanges = false;
            updateLineNumbers();
            
            Toast.makeText(this, R.string.file_loaded, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, R.string.error_loading, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_examples) {
            showExamplesDialog();
            return true;
        } else if (id == R.id.action_settings) {
            showSettingsDialog();
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showExamplesDialog() {
        String[] examples = {
            "Hello World",
            "数学计算",
            "列表操作",
            "字典操作",
            "文件操作",
            "网络请求"
        };

        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.examples)
            .setItems(examples, (dialog, which) -> {
                String code = getExampleCode(which);
                binding.etCode.setText(code);
                updateLineNumbers();
                hasUnsavedChanges = true;
            })
            .show();
    }

    private String getExampleCode(int index) {
        switch (index) {
            case 0:
                return "# Hello World 示例\nprint('Hello, World!')\nprint('欢迎使用 PyRunner!')\n";
            case 1:
                return "# 数学计算示例\nimport math\n\nprint('=== 数学计算 ===')\nprint(f'圆周率: {math.pi}')\nprint(f'自然对数: {math.e}')\nprint(f'平方根(16): {math.sqrt(16)}')\nprint(f'2^10 = {2**10}')\nprint(f'sin(90°) = {math.sin(math.radians(90))}')\n";
            case 2:
                return "# 列表操作示例\nnumbers = [1, 2, 3, 4, 5]\n\nprint('=== 列表操作 ===')\nprint(f'原始列表: {numbers}')\nprint(f'求和: {sum(numbers)}')\nprint(f'最大值: {max(numbers)}')\nprint(f'最小值: {min(numbers)}')\nprint(f'平均值: {sum(numbers)/len(numbers)}')\n\nsquares = [x**2 for x in numbers]\nprint(f'平方: {squares}')\n\nevens = [x for x in numbers if x % 2 == 0]\nprint(f'偶数: {evens}')\n";
            case 3:
                return "# 字典操作示例\nperson = {\n    'name': '张三',\n    'age': 25,\n    'city': '北京'\n}\n\nprint('=== 字典操作 ===')\nprint(f'个人信息: {person}')\nprint(f'姓名: {person[\"name\"]}')\nprint(f'年龄: {person[\"age\"]}')\n\nfor key, value in person.items():\n    print(f'{key}: {value}')\n";
            case 4:
                return "# 文件操作示例\nimport os\n\nprint('=== 文件操作 ===')\n\n# 获取当前工作目录\ncwd = os.getcwd()\nprint(f'当前目录: {cwd}')\n\n# 列出当前目录文件\nfiles = os.listdir('.')\nprint(f'\\n目录内容:')\nfor f in files[:10]:\n    print(f'  {f}')\n";
            case 5:
                return "# 网络请求示例\nimport urllib.request\nimport json\n\nprint('=== 网络请求 ===')\n\ntry:\n    url = 'https://api.github.com'\n    response = urllib.request.urlopen(url)\n    data = json.loads(response.read())\n    print(f'GitHub API 响应:')\n    print(json.dumps(data, indent=2))\nexcept Exception as e:\n    print(f'请求失败: {e}')\n";
            default:
                return "";
        }
    }

    private void showSettingsDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings)
            .setMessage("设置功能开发中...")
            .setPositiveButton("确定", null)
            .show();
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.about)
            .setMessage("PyRunner v1.0\n\n一个在Android设备上运行Python脚本的工具。\n\n支持功能:\n• Python 3.11 运行环境\n• 代码编辑与语法高亮\n• 脚本保存与管理\n• 常用Python库支持")
            .setPositiveButton("确定", null)
            .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public void onBackPressed() {
        if (hasUnsavedChanges) {
            new MaterialAlertDialogBuilder(this)
                .setTitle("未保存的更改")
                .setMessage("当前文件有未保存的更改，是否保存？")
                .setPositiveButton("保存", (dialog, which) -> {
                    showSaveDialog();
                    super.onBackPressed();
                })
                .setNegativeButton("不保存", (dialog, which) -> super.onBackPressed())
                .setNeutralButton("取消", null)
                .show();
        } else {
            super.onBackPressed();
        }
    }
}
