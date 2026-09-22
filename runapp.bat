@echo off
setlocal

rem Capture the absolute workspace directory root path safely
set "BASE_DIR=%~dp0"

echo Checking for locally cached app binaries...

rem Target the precise path confirmed by your folder directory list
set "TARGET_APK=%BASE_DIR%app\build\outputs\apk\debug\app-debug.apk"

if not exist "%TARGET_APK%" (
    echo Error: No pre-compiled app-debug.apk found at:
    echo "%TARGET_APK%"
    echo Please ensure the project workspace hasn't been cleared.
    exit /b
)

echo Found local compiled artifact successfully!

rem --- Push to phone via adb entirely offline ---
echo [1/2] Installing local app binary onto your device...
adb install -r "%TARGET_APK%"
if %errorlevel% neq 0 (
    echo Deployment failed! Please make sure your phone is connected and USB debugging is on.
    exit /b
)

rem --- Launch on screen ---
echo [2/2] Launching app screen...
adb shell monkey -p com.example.fcams -c android.intent.category.LAUNCHER 1 >nul 2>&1
echo Done! Enjoy developing offline!

endlocal
