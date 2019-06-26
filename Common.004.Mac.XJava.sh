#!/bin/sh

cd ./bin


rm -R ./org/hy/common/xml/junit


jar cvfm hy.common.xjava.jar MANIFEST.MF META-INF org

cp hy.common.xjava.jar ..
rm hy.common.xjava.jar
cd ..





cd ./src
jar cvfm hy.common.xjava-sources.jar MANIFEST.MF META-INF org 
cp hy.common.xjava-sources.jar ..
rm hy.common.xjava-sources.jar
cd ..
