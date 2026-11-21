@echo off
cd /d "%~dp0"
echo Instalando PyInstaller e dependencias...
python -m pip install -r requirements.txt
echo Gerando executavel (.exe)...
pyinstaller --noconsole --onefile --name "CustomApp" --icon "icon.ico" app.py
if exist config.json copy /y config.json dist\config.json
if exist icon.ico copy /y icon.ico dist\icon.ico
if exist icon.png copy /y icon.png dist\icon.png
if exist custom.css copy /y custom.css dist\custom.css
if exist custom.js copy /y custom.js dist\custom.js
echo.
echo Executavel gerado na pasta 'dist/CustomApp.exe'!
echo Lembre-se de manter o arquivo 'config.json' na mesma pasta do executavel para poder alterar a URL quando quiser.
pause
