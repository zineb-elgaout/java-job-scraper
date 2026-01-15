@echo off
setlocal

REM ============================================
REM CONFIGURATION
REM ============================================
set JAVA_HOME=C:\Users\DELL\Downloads\OpenJDK11U-jdk_x64_windows_hotspot_11.0.29_7\jdk-11.0.29+7
set JAVAC="%JAVA_HOME%\bin\javac.exe"
set JAVA="%JAVA_HOME%\bin\java.exe"

set PROJECT_DIR=C:\Users\DELL\Desktop\projet_java\java_scrap
set SRC_DIR=%PROJECT_DIR%\src
set BIN_DIR=%PROJECT_DIR%\bin
set LIB_DIR=%PROJECT_DIR%\lib

REM ============================================
ECHO VERIFICATION DES CHEMINS
echo ============================================
if not exist "%JAVAC%" (
    echo ERREUR: javac.exe non trouve a %JAVAC%
    pause
    exit /b 1
)

if not exist "%SRC_DIR%\ml\*.java" (
    echo ERREUR: Fichiers Java non trouves dans %SRC_DIR%\ml\
    pause
    exit /b 1
)

if not exist "%LIB_DIR%\weka.jar" (
    echo ERREUR: weka.jar non trouve dans %LIB_DIR%\
    dir "%LIB_DIR%\*.jar"
    pause
    exit /b 1
)

echo Configuration OK:
echo - Java: %JAVAC%
echo - Source: %SRC_DIR%\ml\
echo - Classes: %BIN_DIR%
echo - Librairies: %LIB_DIR%
echo.

REM ============================================
echo COMPILATION AVEC ENCODAGE UTF-8
echo ============================================
echo.

REM Creation du dossier bin si necessaire
if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"

REM Nettoyage des anciennes classes
if exist "%BIN_DIR%\ml\*.class" del "%BIN_DIR%\ml\*.class" /q

echo Compilation avec encodage UTF-8...
echo.

REM Compilation avec encodage UTF-8
%JAVAC% -encoding UTF-8 -cp "%LIB_DIR%\weka.jar;%LIB_DIR%\mtj.jar" -d "%BIN_DIR%" ^
    "%SRC_DIR%\ml\MLMain.java" ^
    "%SRC_DIR%\ml\JobDataLoader.java" ^
    "%SRC_DIR%\ml\JobClassifier.java" ^
    "%SRC_DIR%\ml\ModelEvaluator.java" ^
    "%SRC_DIR%\ml\RecommendationService.java"

if errorlevel 1 (
    echo.
    echo ERREUR de compilation.
    echo.
    echo Solutions:
    echo 1. Verifiez les caracteres speciaux dans les fichiers Java
    echo 2. Essayez de les remplacer par des caracteres ASCII simples
    echo 3. Ou corrigez l'encodage des fichiers
    pause
    exit /b 1
)

echo Compilation reussie!
echo.

REM ============================================
ECHO EXECUTION
echo ============================================
echo.

if not exist "%BIN_DIR%\ml\MLMain.class" (
    echo ERREUR: Classe principale non trouvee
    pause
    exit /b 1
)

echo Execution du programme...
echo ============================================
%JAVA% -cp "%BIN_DIR%;%LIB_DIR%\weka.jar;%LIB_DIR%\mtj.jar" ml.MLMain

echo.
pause