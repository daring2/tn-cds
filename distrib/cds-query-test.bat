@echo off

cd /d %~dp0
call cds-test.bat -Dcds.test.enabledTests="LongQueryTest,ParallelQueryTest" %*