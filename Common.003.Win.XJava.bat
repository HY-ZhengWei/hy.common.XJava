

del /Q hy.common.xjava.jar
del /Q hy.common.xjava-sources.jar


call mvn clean package
cd .\target\classes


rd /s/q .\org\hy\common\xml\junit


jar cvfm hy.common.xjava.jar META-INF/MANIFEST.MF META-INF org

copy hy.common.xjava.jar ..\..
del /q hy.common.xjava.jar
cd ..\..





cd .\src\main\java
xcopy /S ..\resources\* .
jar cvfm hy.common.xjava-sources.jar META-INF\MANIFEST.MF META-INF org 
copy hy.common.xjava-sources.jar ..\..\..
del /Q hy.common.xjava-sources.jar
rd /s/q META-INF
cd ..\..\..
