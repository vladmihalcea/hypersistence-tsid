@echo off

mvn362 -P release -DignoreSnapshots=true release:clean release:prepare
goto:eof