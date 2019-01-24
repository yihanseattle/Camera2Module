#!/bin/sh
echo "Start install the RokidCamera apk"

rm -rf ./RokidCamera.apk
cp ./app/build/outputs/apk/debug/CameraApp-v*.apk ./RokidCamera.apk

adb root
adb remount
adb push ./RokidCamera.apk /system/app/RokidCamera
#adb reboot

echo "install RokidCamera apk end!"
