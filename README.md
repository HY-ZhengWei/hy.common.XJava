# XJava



* XJava
	* 标记
		* [import 引类](#import)
		* [constructor 构造器](#constructor)
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
写在XML文件的最外层节点之后，其它之前。
	
基本语法：
```xml
	<import name="节点名称"  class="Java类型" />
```
举例说明：
```xml
	<import name="xconfig"  class="java.util.ArrayList" />     <!-- 定义一个List集合的节点名称 -->
	<import name="xparam"   class="java.util.Hashtable" />     <!-- 定义一个Map集合的节点名称 -->
	<import name="item"     class="org.hy.common.app.Param" /> <!-- 定义一个自定义对象类型的节点名称 -->
```



constructor
------
Java对象的复杂构造器在创建实例时使用。
写在Java对象节点名称之后，对象实例操作之前。

基本语法：
```xml
	<Java对象的节点名称>
		<constructor>
			<参数类型01>参数值01</参数类型01>
			<参数类型02>参数值02</参数类型02>
			<...>...</...>
		</constructor>
	</Java对象的节点名称>
```