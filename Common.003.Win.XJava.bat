

cd .\bin


rd /s/q .\org\hy\common\xml\junit


jar cvfm xjava.jar MANIFEST.MF META-INF org 

copy xjava.jar ..
del /q xjava.jar
cd ..





cd .\src
jar cvfm xjava-sources.jar MANIFEST.MF META-INF org 
copy xjava-sources.jar ..
del /q xjava-sources.jar
cd ..
