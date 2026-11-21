@echo off
cd /d "%~dp0"
echo Instalando dependencias caso necessario...
python -m pip install -r requirements.txt
echo Iniciando aplicativo...
python app.py
pause
