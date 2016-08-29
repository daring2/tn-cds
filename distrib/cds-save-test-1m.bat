@echo off

cd /d %~dp0
set TestOpts=-Dcds.test.SaveTagDataTest.runTime=1m -Dcds.test.QueryTest.runTime=1m
call cds-save-test.bat %TestOpts% %*