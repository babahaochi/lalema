@echo off
echo 正在收集日志...
echo 请确保：
echo 1. 手机已通过USB连接
echo 2. 已开启USB调试
echo 3. 已授权调试权限
echo.
echo 等待设备连接...
"d:\小程序开发\微信web开发者工具\resources\app.asar.unpacked\bin\adb-win\adb.exe" wait-for-device
echo 设备已连接，开始收集日志...
echo.
"d:\小程序开发\微信web开发者工具\resources\app.asar.unpacked\bin\adb-win\adb.exe" logcat -d -s ApiClient:D AuthScreen:D AndroidRuntime:E *:S > app_logs.txt
echo 日志已保存到 app_logs.txt
echo.
pause