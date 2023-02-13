@echo off

mvn -P release -DignoreSnapshots=true release:clean release:prepare
goto:eof