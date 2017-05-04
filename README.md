# XJava



* XJava
	* 系统标记
		* [import 引类](#import)
		* [constructor 构造器](#constructor)
		* [call 执行方法](#call)
	* 通用标记属性
		* [id 定义变量名称](#id)
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
举例说明1：
```xml
<import name="dimension" class="java.awt.Dimension" />

<dimension id="IconSize">   <!-- 实例化组件的宽度和高度 -->
	<constructor>
		<int>40</int>
		<int>36</int>
	</constructor>
</dimension>
```
举例1翻译成Java代码：
```java
	import java.awt.Dimension;
	
	Dimension v_IconSize = new Dimension(40 ,36);
```
举例说明2：
```xml
<import name="databaseMeta"    class="org.pentaho.di.core.database.DatabaseMeta" />
<import name="repositoryMeta"  class="org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta" />

<xconfig>
	<!-- 创建Kettle数据库 -->
	<databaseMeta id="DatabaseMeta" />
		<...>...</...>
	</databaseMeta>
	
	<!-- 创建Kettle资源库 -->
	<repositoryMeta id="RepositoryMeta">
		<constructor>
			<String>Kettle</String>
			<String>Kettle</String>
			<String>Description</String>
			<databaseMeta ref="DatabaseMeta" />
		</constructor>
	</repositoryMeta>
</xconfig>
```
举例2翻译成Java代码：
```java
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;

DatabaseMeta                 v_DatabaseMeta   = new DatabaseMeta();
KettleDatabaseRepositoryMeta v_RepositoryMeta = new KettleDatabaseRepositoryMeta("Kettle" ,"Kettle" ,"Description" ,v_DatabaseMeta);
```



call
------
执行Java方法。写在任一XML节点名称之内，即表示执行主体为本节点名称对应的Java对象实例。

基本语法：
```xml
<Java对象的节点名称>
	<call name="方法名称" returnid="方法返回值的变量名">
		<参数类型01>参数值01</参数类型01>
		<参数类型02>参数值02</参数类型02>
		<...>...</...>
	</call>
</Java对象的节点名称>
```
举例说明1：
```xml
<import name="jobs" class="org.hy.common.thread.Jobs" />

<xconfig>
	<jobs id="jobList">
		<call name="startup" />  <!-- 无入参参数的方法执行 -->
	</jobs>
</xconfig>
```
举例1翻译成Java代码：
```java
import org.hy.common.thread.Jobs

Jobs jobList = new Jobs();
jobList.startup();
```
举例说明2：
```xml
<import name="XImageIcon" class="javax.swing.ImageIcon" />
<import name="XURL"       class="java.net.URL" />

<xconfig>
	<XImageIcon id="xiconSubmit">
		<constructor>
			<XURL>
				<constructor>
					<String>classpath:Ubuntu.png</String>
				</constructor>
			</XURL>
		</constructor>
		
		<!-- 定义组件的大小，通过方法的返回值获取组件尺寸对象 -->
		<call name="this.getImage.getScaledInstance" returnID="ximageSubmit">
			<int>24</int>
			<int>24</int>
			<int>1</int>
		</call>
		
		<image ref="ximageSubmit" />  <!-- 设置图标组件的大小，这里引用方法的返回值 -->
	</XImageIcon>
</xconfig>
```
举例2翻译成Java代码：
```java
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;

ImageIcon xiconSubmit  = new ImageIcon(new URL("Ubuntu.png"));
Image     ximageSubmit = xiconSubmit.getImage().getScaledInstance(24 ,24 ,Image.SCALE_DEFAULT);

xiconSubmit.setImage(ximageSubmit);
```



id
------
类似于变量名，是对节点构造对象实例的唯一标识。定义ID变量名后，可以其后的XML文件中引用。同时，在Java代码中也可被引用。

允许在XML文件中定义两个相同的ID变量名，但后面ID对应的实例将覆盖前面的。有了这个好处后，就能做很多事情了，比如说版本升级，ID变量名可以保持不变，
但业务逻辑可以重新定义的同时也用不覆盖老版本的代码。