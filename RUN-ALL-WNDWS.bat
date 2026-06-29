@echo off

echo Building Maven projects...
call mvn clean package

echo Stopping previous containers...
docker compose down -v

echo Starting all components...
docker compose up --build

pause