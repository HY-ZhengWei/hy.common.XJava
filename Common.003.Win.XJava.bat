

cd .\bin


rd /s/q .\org\hy\common\xml\junit


jar cvfm hy.common.xjava.jar MANIFEST.MF META-INF org 

copy hy.common.xjava.jar ..
del /q hy.common.xjava.jar
cd ..





cd .\src
jar cvfm hy.common.xjava-sources.jar MANIFEST.MF META-INF org 
copy hy.common.xjava-sources.jar ..
del /q hy.common.xjava-sources.jar
cd ..
