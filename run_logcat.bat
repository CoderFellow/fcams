@echo off
title Android Logcat Viewer
echo Checking if your device is connected...
adb devices

echo.
echo Searching for process ID of com.example.fcams...
echo (Make sure your app is open on the device!)
echo.

:: Loop through the PID. Note the double %%i used for batch execution
for /f "tokens=*" %%i in ('adb shell pidof -s com.example.fcams') do (
    echo [SUCCESS] Found active process ID: %%i
    echo Starting colorized logcat stream...
    echo Press Ctrl+C to stop.
    echo ----------------------------------------------------
    adb logcat --pid=%%i -v color
    goto :end
)

:: If the loop didn't execute, the app isn't running. Fall back to global filter.
echo [WARNING] App process 'com.example.fcams' is not currently active on screen.
echo Falling back to keyword search mode...
echo ----------------------------------------------------
adb logcat -v color | findstr com.example.fcams

:end
pause
