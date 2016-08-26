@echo off

cd /d %~dp0
PsExec.exe -s -d %* > nul 2>&1
echo process started