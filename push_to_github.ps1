# PyRunner GitHub推送脚本
# 用户名: kamioksan-maker

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  PyRunner - GitHub推送脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查Git是否安装
try {
    $gitVersion = git --version
    Write-Host "[OK] Git已安装: $gitVersion" -ForegroundColor Green
} catch {
    Write-Host "[错误] Git未安装!" -ForegroundColor Red
    Write-Host "请先安装Git: https://git-scm.com/download/win" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "按Enter键退出"
    exit 1
}

Write-Host ""
Write-Host "正在初始化Git仓库..." -ForegroundColor Yellow

# 初始化Git仓库
git init
Write-Host "[OK] Git仓库初始化完成" -ForegroundColor Green

# 添加所有文件
Write-Host "正在添加文件..." -ForegroundColor Yellow
git add .
Write-Host "[OK] 文件添加完成" -ForegroundColor Green

# 提交
Write-Host "正在提交..." -ForegroundColor Yellow
git commit -m "Initial commit: PyRunner Android App"
Write-Host "[OK] 提交完成" -ForegroundColor Green

# 添加远程仓库
Write-Host "正在添加远程仓库..." -ForegroundColor Yellow
git remote add origin https://github.com/kamioksan-maker/PyRunner.git
Write-Host "[OK] 远程仓库添加完成" -ForegroundColor Green

# 设置主分支
git branch -M main
Write-Host "[OK] 分支设置为main" -ForegroundColor Green

# 推送
Write-Host ""
Write-Host "正在推送到GitHub..." -ForegroundColor Yellow
Write-Host "注意: 如果需要登录GitHub，请输入您的凭据" -ForegroundColor Yellow
Write-Host ""

git push -u origin main

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  推送成功!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "仓库地址: https://github.com/kamioksan-maker/PyRunner" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "下一步:" -ForegroundColor Yellow
    Write-Host "1. 访问 Actions 页面查看构建进度" -ForegroundColor White
    Write-Host "2. 构建完成后在 Releases 页面下载APK" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "[错误] 推送失败!" -ForegroundColor Red
    Write-Host "请检查:" -ForegroundColor Yellow
    Write-Host "1. GitHub仓库是否已创建: https://github.com/new" -ForegroundColor White
    Write-Host "2. 用户名是否正确: kamioksan-maker" -ForegroundColor White
    Write-Host "3. 网络连接是否正常" -ForegroundColor White
    Write-Host ""
}

Read-Host "按Enter键退出"
