# XJava



* XJava
	* 标记
		* [import](#import)
		* constructor
		* call
	* 通用标记属性
		* id
		* class
		* ref
		* setter
		* submit
	* 关键字
		* this
		* classpath
		* classhome
		* webhome
* XSQL
	* XSQLGroup
	* XSQLPaging
* XJSON
* XHttp
* XWebService



import
------
类似于Java语言中的引包功能。定义XML文件中节点对应的Java类型。
```xml
    <import name="节点名称"  class="Java类型" />
	<import name="xconfig"  class="java.util.ArrayList" />
	<import name="xparam"   class="java.util.Hashtable" />
	<import name="item"     class="org.hy.common.app.Param" />
```