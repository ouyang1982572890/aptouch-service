@echo off
chcp 65001 >nul
echo ================================================
echo    GitHub Upload & Auto Build APK Script
echo ================================================
echo.

set "REPO_DIR=%~dp0"
set "REPO_NAME=aptouch-service-android"

echo [1/6] Checking Git status...
cd "%REPO_DIR%"
if not exist ".git" (
    echo Git repository not found. Creating new one...
    git init
    git add .
    git commit -m "Initial commit: APK Touch Service"
    echo.
    echo Please run these commands manually to upload to GitHub:
    echo.
    echo   git remote add origin https://github.com/YOUR_USERNAME/%REPO_NAME%.git
    echo   git push -u origin main
    echo.
    echo Then go to GitHub Actions to download the APK.
    pause
    exit /b
)

echo [2/6] Adding all files...
git add -A >nul

echo [3/6] Creating commit...
set "MSG=Update: Auto build APK - %date% %time%"
git commit -m "%MSG%" >nul 2>&1

echo [4/6] Pushing to GitHub...
git push origin main

echo [5/6] Triggering GitHub Actions...
echo.
echo Build triggered! Please:
echo   1. Open: https://github.com/YOUR_USERNAME/%REPO_NAME%/actions
echo   2. Wait for build to complete (~5 minutes)
echo   3. Download APK from Artifacts

echo [6/6] Done!
echo.
echo ================================================
echo Next Steps:
echo   1. Go to https://github.com/YOUR_USERNAME/%REPO_NAME%/actions
echo   2. Wait for "Build APK" workflow to finish
echo   3. Click on the workflow run
echo   4. Download APK from Artifacts section
echo ================================================
echo.
pause
