@echo off

call release-prepare.bat &if not errorlevel 1 call release-perform.bat

goto:eof
