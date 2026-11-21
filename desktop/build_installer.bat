@echo off
cd /d "%~dp0"
echo Compilando instalador do Windows com Inno Setup...
set ISCC="C:\Users\rey_l\AppData\Local\Programs\Inno Setup 6\ISCC.exe"
if not exist %ISCC% (
    echo Compilador Inno Setup nao encontrado no caminho padrao.
    pause
    exit /b 1
)

%ISCC% installer.iss
echo.
echo Instalador gerado com sucesso na pasta 'installer_output\AnimeApp_Setup.exe'!
pause
