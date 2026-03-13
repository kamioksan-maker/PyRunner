# PyRunner - Android Python脚本运行器

一个功能完整的Android应用，可以在手机上直接运行Python脚本。

**GitHub仓库**: https://github.com/kamioksan-maker/PyRunner

## 功能特性

- **Python 3.11 运行环境** - 基于Chaquopy提供完整的Python支持
- **代码编辑器** - 带行号显示的代码编辑区域
- **脚本管理** - 保存、打开、删除Python脚本
- **示例代码** - 内置多种Python示例
- **输出显示** - 实时显示脚本执行结果
- **常用库支持** - 预装numpy、requests等常用库

## 项目结构

```
PyRunner/
├── app/
│   ├── build.gradle              # 应用级Gradle配置
│   ├── proguard-rules.pro        # 混淆规则
│   └── src/main/
│       ├── AndroidManifest.xml   # 应用清单
│       ├── java/com/pyrunner/app/
│       │   ├── MainActivity.java      # 主Activity
│       │   ├── PythonExecutor.java    # Python执行引擎
│       │   ├── FileAdapter.java       # 文件列表适配器
│       │   └── ScriptFile.java        # 脚本文件模型
│       ├── python/
│       │   ├── __init__.py
│       │   └── pyrunner_helper.py     # Python辅助模块
│       └── res/
│           ├── layout/           # 布局文件
│           ├── values/           # 资源值
│           ├── menu/             # 菜单
│           └── drawable/         # 图标
├── build.gradle                 # 项目级Gradle配置
├── settings.gradle              # 项目设置
└── gradle.properties            # Gradle属性
```

## 构建要求

- **Android Studio**: Hedgehog (2023.1.1) 或更高版本
- **JDK**: 17
- **Android SDK**: API 34 (Android 14)
- **最低支持**: API 24 (Android 7.0)
- **Gradle**: 8.5

## 通过GitHub自动构建APK

本项目已配置GitHub Actions，可以自动构建APK：

### 方法一：下载Release版本

1. 将代码推送到GitHub仓库的 `main` 或 `master` 分支
2. GitHub Actions会自动构建并创建Release
3. 在仓库的 **Releases** 页面下载APK文件

### 方法二：下载Artifacts

1. 进入仓库的 **Actions** 标签页
2. 选择最新的工作流运行
3. 在页面底部 **Artifacts** 区域下载：
   - `PyRunner-Debug`: 调试版本APK
   - `PyRunner-Release`: 发布版本APK

### 手动触发构建

1. 进入 **Actions** 标签页
2. 选择 **Build APK** 工作流
3. 点击 **Run workflow** 按钮

## 本地构建步骤

1. **打开项目**
   ```bash
   # 使用Android Studio打开PyRunner目录
   ```

2. **同步Gradle**
   - Android Studio会自动提示同步Gradle
   - 或手动点击 "File > Sync Project with Gradle Files"

3. **构建APK**
   ```bash
   # Debug版本
   ./gradlew assembleDebug
   
   # Release版本
   ./gradlew assembleRelease
   ```

4. **安装到设备**
   ```bash
   # 通过USB连接设备后
   ./gradlew installDebug
   ```

## 使用说明

### 运行Python代码
1. 在代码编辑区输入Python代码
2. 点击"运行"按钮执行
3. 在输出区域查看结果

### 保存脚本
1. 点击"保存"按钮
2. 输入文件名（自动添加.py后缀）
3. 脚本将保存到应用私有目录

### 打开脚本
1. 点击"打开"按钮
2. 从列表中选择已保存的脚本
3. 代码将加载到编辑区

### 示例代码
1. 点击菜单 → "示例代码"
2. 选择需要的示例类型
3. 示例代码将加载到编辑区

## 技术栈

- **Chaquopy**: Android Python运行环境
- **Material Design**: UI设计
- **ViewBinding**: 视图绑定
- **RecyclerView**: 文件列表
- **ExecutorService**: 异步任务执行

## 扩展Python库

在 `app/build.gradle` 的 `chaquopy` 配置中添加更多库：

```groovy
chaquopy {
    defaultConfig {
        pip {
            install "numpy"
            install "requests"
            // 添加更多库
            install "pandas"
            install "matplotlib"
        }
    }
}
```

## 注意事项

1. **首次运行**: 首次运行时会初始化Python环境，可能需要几秒钟
2. **存储权限**: 需要授予存储权限才能访问外部文件
3. **性能**: 复杂计算可能需要较长时间，请耐心等待
4. **网络**: 网络请求功能需要网络权限

## 许可证

MIT License
