@echo off
cd /d "%~dp0"
java -Duser.timezone=America/Bogota -jar ejecutable/tecnoturno-1.0.0.jar --spring.profiles.active=demo
pause
