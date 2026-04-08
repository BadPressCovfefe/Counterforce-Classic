@echo off
setlocal

:: Define the path to your git repository
set "REPO_PATH=C:\Users\Corbin\Documents\GitHub\Counterforce-Final-Version"

:: Define the path to your .bat file that starts the .jar program
set "BAT_FILE_PATH=C:\Users\Corbin\Documents\GitHub\Counterforce-Final-Version\Final Version\LaunchServer\Start Server.bat"

:: Define the path to your .jar program
set "JAR_PATH=C:\Users\Corbin\Documents\GitHub\Counterforce-Final-Version\Final Version\LaunchServer\LaunchServer.jar"

:: Define the command to send to the Java process
set "QUIT_COMMAND=quit"

:: Navigate to the git repository directory
cd "%REPO_PATH%"

:: Check for changes to LaunchServer.jar in the git repository
git fetch origin
git diff --name-only HEAD@{1} HEAD | findstr /i /c:"Final Version\LaunchServer\LaunchServer.jar" > nul
if %errorlevel%==0 (
    echo Changes detected in LaunchServer.jar. Pulling repository...
    git pull origin main
    
    :: Send "quit" command to the Java process running the .jar program
    echo Sending quit command to Java process...
    echo %QUIT_COMMAND% | tasklist | findstr "java.exe" | for /f "tokens=2" %%a in ('findstr /i /r /c:"[0-9]* java\.exe"') do (
        echo %QUIT_COMMAND% | taskkill /pid %%a
    )
    
    :: Wait for a moment to ensure the program has time to save and quit
    timeout /t 5 /nobreak
    
    :: Restart the .bat file to start the .jar program
    start "" "%BAT_FILE_PATH%"
) else (
    echo No changes detected in LaunchServer.jar. Exiting...
)

endlocal
