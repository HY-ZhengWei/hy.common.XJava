#!/bin/sh

cd ./bin


rm -R ./org/hy/common/xml/junit


jar cvfm xjava.jar MANIFEST.MF META-INF org

cp xjava.jar ..
rm xjava.jar
cd ..





cd ./src
jar cvfm xjava-sources.jar MANIFEST.MF META-INF org 
cp xjava-sources.jar ..
rm xjava-sources.jar
cd ..
