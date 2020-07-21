@echo off
title Testing Client...
start ~debug2.bat
gradlew runClient
timeout 99