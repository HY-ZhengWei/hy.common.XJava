

cd .\bin


rd /s/q .\org\hy\common\xml\junit

del /q  .\org\hy\common\xml\plugins\XJavaFactoryBeanDefinition.class
del /q  .\org\hy\common\xml\plugins\XJavaSpringAnnotationConfigServletWebServerApplicationContext.class
del /q  .\org\hy\common\xml\plugins\XJavaSpringMVCAnnotationConfigServletWebServerApplicationContext.class
del /q  .\org\hy\common\xml\plugins\XJavaSpringMVCDispatcherServlet.class
del /q  .\org\hy\common\xml\plugins\XJavaSpringObjectFactotry.class
del /q  .\org\hy\common\xml\plugins\XJavaStrutsObjectFactory.class


jar cvfm hy.common.xjava.jar MANIFEST.MF META-INF org 

copy hy.common.xjava.jar ..
del /q hy.common.xjava.jar
cd ..





cd .\src
jar cvfm hy.common.xjava-sources.jar MANIFEST.MF META-INF org 
copy hy.common.xjava-sources.jar ..
del /q hy.common.xjava-sources.jar
cd ..
