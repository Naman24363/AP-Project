@echo off
REM University ERP - Windows Compilation Script

echo ======================================
echo University ERP - Build Script (Windows)
echo ======================================
echo.

REM Check if bin directory exists
if not exist "bin" (
    echo Creating bin directory...
    mkdir bin
)

echo Compiling all Java files...
echo.

REM Compile all source files
javac -d bin ^
    src\edu\univ\erp\Main.java ^
    src\edu\univ\erp\util\*.java ^
    src\edu\univ\erp\auth\*.java ^
    src\edu\univ\erp\data\*.java ^
    src\edu\univ\erp\access\*.java ^
    src\edu\univ\erp\domain\*.java ^
    src\edu\univ\erp\service\*.java ^
    src\edu\univ\erp\ui\auth\*.java ^
    src\edu\univ\erp\ui\admin\*.java ^
    src\edu\univ\erp\ui\instructor\*.java ^
    src\edu\univ\erp\ui\student\*.java ^
    src\edu\univ\erp\ui\common\*.java 2>&1

REM Check compilation result
if %errorlevel% equ 0 (
    echo.
    echo Compilation successful!
    echo.
    echo To run the application:
    echo   cd bin
    echo   java -cp .;postgresql-VERSION.jar edu.univ.erp.Main
    echo.
    echo Copying resources to bin for runtime classpath...
    if exist "src\main\resources\images" (
        xcopy /E /I /Y "src\main\resources\images" "bin\images" >nul
    )
    if exist "resources\login_bg.jpg" (
        xcopy /Y "resources\login_bg.jpg" "bin\login_bg.jpg" >nul
    )
) else (
    echo.
    echo Compilation failed!
    echo Please check errors above.
    exit /b 1
)

echo ======================================
echo Build Complete
echo ======================================
pause
