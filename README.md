# XJava



* XJava
	* 系统标记
		* [import 引类](#系统标记import)
		* [constructor 构造器](#系统标记constructor)
		* [call 执行方法](#系统标记call)
	* 通用标记属性
		* [id 定义变量名](#属性id)
		* [class 标记的Java类型](#属性class)
		* [ref 引用](#属性ref)
		* [setter 容器添加子对象](#属性setter)
		* [key Map集合添加子元素的Key值](#属性key)
		* [submit 打包提交](#属性submit)
	* 关键字
		* [this 对象实例自已](#关键字this)
		* [classpath XML同级包的路径](#关键字classpath)
		* [classhome Java工程的根路径](#关键字classhome)
		* [webhome Web工程的根路径](#关键字webhome)
* [XSQL 轻量级持久层](#XSQL轻量级持久层)
	* XSQLGroup
	* XSQLPaging
* XJSON
* XHttp
* XWebService



系统标记import
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



系统标记constructor
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



系统标记call
------
执行Java方法。写在任一XML节点名称之内，即表示执行主体为本节点名称对应的Java对象实例。

call:name属性：为执行方法的名称。同时支持 xx.yy.zz 的形式(详见举例说明2)。

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



属性id
------
类似于Java中的变量名，是对节点构造对象实例的唯一标识。ID变量名区分大小写。
ID属性不是必须声明的属性，即没有变更名的匿名实例，如：new Date()。

定义ID变量名后，可以在其后的XML文件中引用。同时，在Java代码中也可被引用。
在Java代码中引用的方法为：XJava.getObject("对象实例的变量名");

允许在XML文件中定义两个相同的ID变量名，但后面ID对应的实例将覆盖前面的。有了这个好处后，就能做很多事情了，比如说版本升级，ID变量名可以保持不变，
但业务逻辑可以重新定义的同时也用不覆盖老版本的Java代码和老版本的XML文件。

基本语法：
```xml
<Java对象的节点名称 id="对象实例的变量名">
</Java对象的节点名称>
```
举例说明：
```xml
<import name="dataSourceGroup" class="org.hy.common.db.DataSourceGroup" />

<xconfig>
	<!-- 数据库连接池组 -->
	<dataSourceGroup id="dsg" />
</xconfig>
```
举例翻译成Java代码：
```java
import org.hy.common.db.DataSourceGroup

DataSourceGroup dsg = new DataSourceGroup();
```



属性class
------
表示XML文件中属性class所在节点的的Java类型。当被说明的XML节点使用次数少，如只出现一次时，可不用<import>引类，而用class属性说明。
基本语法：
```xml
<Java对象的节点名称 class="Java类的全路径">
</Java对象的节点名称>
```
举例说明：
```xml
<xconfig>
	<!-- 数据库连接池组 -->
	<dataSourceGroup class="org.hy.common.db.DataSourceGroup" />
</xconfig>
```
举例翻译成Java代码：
```java
new org.hy.common.db.DataSourceGroup();
```



属性ref
------
表示引用的对象实例是谁，用于对XML节点对象赋值，与XJava:id属性配合使用。

属性ref不只是简单的引用对象实例，还可以引用对象实例中某个方法的返回值。如：date.getFull()方法的返回值对象。

属性ref除了引用其它对象实例外，还引用自己或自己中某个方法的返回值(详见关键字this)，多用于引用Java类中定义的常量值。

基本语法：
```xml
<Java对象的节点名称 ref="实例ID变量名">
</Java对象的节点名称>
```
举例说明：
```xml
<import name="envConfig" class="com.sleepycat.je.EnvironmentConfig" />
<import name="dbConfig"  class="com.sleepycat.je.DatabaseConfig" />
<import name="berkeley"  class="org.hy.common.berkeley.Berkeley" />

<xconfig>
	<!-- 嵌入式数据库的配置参数信息 -->
	<envConfig id="BEnvConfig">
		<allowCreate>true</allowCreate>
		<transactional>false</transactional>
		<locking>false</locking>
		<cacheSize>104857600</cacheSize> <!-- 100 * 1024 * 1024 -->
	</envConfig>
	
	<dbConfig id="BDBConfig">
		<allowCreate>true</allowCreate>
		<deferredWrite>true</deferredWrite>
	</dbConfig>
	
	<berkeley id="Berkeley">
		<environmentConfig ref="BEnvConfig" />  <!-- 引用ID=BEnvConfig -->
		<databaseConfig    ref="BDBConfig"  />  <!-- 引用ID=BDBConfig -->
	</berkeley>
</xconfig>
```
举例翻译成Java代码：
```java
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.DatabaseConfig;
import org.hy.common.berkeley.Berkeley;

EnvironmentConfig v_BEnvConfig = new EnvironmentConfig();
v_BEnvConfig.setAllowCreate(true);
v_BEnvConfig.setTransactional(false);
v_BEnvConfig.setLocking(false);
v_BEnvConfig.setCacheSize(104857600);
 
DatabaseConfig v_DatabaseConfig = new DatabaseConfig();
v_DatabaseConfig.setAllowCreate(true);
v_DatabaseConfig.setDeferredWrite(true);

Berkeley v_Berkeley = new Berkeley();
v_Berkeley.setEnvironmentConfig(v_BEnvConfig);
v_Berkeley.setDatabaseConfig(v_DatabaseConfig);
```



属性setter
------
只用于容器对象、List集合、Set集合对象。表示XML中父节点添加子节点的Java方法名称。入参数量只能是一个。

当XML中节点为java.util.List类型时，默认XML节点的setter属性值为:add，不用标明在XML节点中。

基本语法：
```xml
<Java容器、集合对象的节点名称 setter="方法名称">
</Java容器、集合对象的节点名称>
```
举例说明：
```xml
<import name="xconfig"     class="java.util.ArrayList" />
<import name="XPanel"      class="javax.swing.JPanel" />
<import name="XLabel"      class="javax.swing.JLabel" />
<import name="XTextField"  class="javax.swing.JTextField" />
<import name="flowLayout"  class="java.awt.FlowLayout" />

<xconfig>	                           <!-- 因为xconfig节点是List集合对象，默认setter="add" -->
	<XPanel id="panel" setter="add">   <!-- 容器添加子对象的方法为: add -->
		<layout>
			<flowLayout>
				<alignment ref="this.LEFT" />
			</flowLayout>
		</layout>
		
		<XLabel>
			<text>标题：</text>
		</XLabel>
		
		<XTextField id="txtTitle">
			<columns>20</columns>
			<toolTipText>请输入标题</toolTipText>
		</XTextField>
	</XPanel>
</xconfig>
```
举例翻译成Java代码：
```java
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;

FlowLayout v_FlowLayout = new FlowLayout();
v_FlowLayout.setAlignment(FlowLayout.LEFT);

JLabel v_Label = new JLabel();
v_Label.setText("标题");

JTextField v_TxtTitle = new JTextField();
v_TxtTitle.setColumns(20);
v_TxtTitle.setToolTipText("请输入标题");

JPanel v_Panel = new JPanel();
v_Panel.setLayout(v_FlowLayout);

v_Panel.add(v_Label);               // 容器添加子对象
v_Panel.add(v_TxtTitle);            // 容器添加子对象

ArrayList v_List = new ArrayList();
v_List.add(v_Panel);                // 集合添加子元素
```



属性key
------
只用于Map集合对象。表示Map集合通过Map.put(key ,value)方法添加子元素时，Map.Key的取值方法的名称(子节点对象的方法名称)。Map.Value默认为XML子节点中实例的对象本身。

基本语法：
```xml
<Map集合对象的节点名称 key="子节点的方法名称">
</Map集合对象的节点名称>
```
举例说明：
```xml
<import name="xconfig"  class="java.util.ArrayList" />
<import name="xparam"   class="java.util.Hashtable" />
<import name="item"     class="org.hy.common.app.Param" />

<xconfig>
	
    <xparam id="SYSParam" key="name">  <!-- 子节点对象的getName()方法的返回值为Map.key值 -->
    	<item id="Param01">
            <name>参数名称01</name>
        </item>
		
		<item id="Param02">
            <name>参数名称02</name>
        </item>
	</xparam>
	
</xconfig>
```
举例翻译成Java代码：
```java
import java.util.ArrayList;
import java.util.Hashtable;
import org.hy.common.app.Param;

Param v_Param01 = new Param();
Param v_Param02 = new Param();

v_Param01.setName("参数名称01");
v_Param02.setName("参数名称02");

Hashtable v_SYSParam = new Hashtable();
v_SYSParam.put(v_Param01.getName() ,v_Param01);  // Map集合添加子元素
v_SYSParam.put(v_Param02.getName() ,v_Param02);  // Map集合添加子元素
```



属性submit
------
Submit属性值是一个被XJava定义过的XJava:id变量名称。
将此变量名称下对应的所有子节点中有XJava:id属性的节点实例化的对象，保存在java.util.Map集合中，此集合的Map.key为XJava:id属性值，Map.value 为节点实例化的对象。
然后将Map集合通过 Setter 方法赋值传递给指定的对象。

因此，submit属性所在的XML节点是对象的方法名称，并且方法的入参为：只有一个类型为Map集合的参数。

基本语法：
```xml
<Java对象的节点名称>
	<对象方法的名称 submit="ID变量名" />
</Java对象的节点名称>
```
举例说明：
```xml

```
举例翻译成Java代码：
```java

```



关键字this
------
此为一个关键字，它即不xml文件中的节点，也不是节点的属性。而是用在节点属性值的关键字。
它表示所在节点的父节点对应的Java类实例的引用。同Java语言中的this关键同样的含义。

基本语法：
```xml
<Java对象的节点名称>
	<对象方法的名称 属性="this.xxx" />
</Java对象的节点名称>
```
举例说明：
```xml
<import name="job" class="org.hy.common.thread.Job" />

<xconfig>
	
    <job id="JOB01">
    	<name>间隔1分钟执行的任务</name>
    	<intervalType ref="this.$IntervalType_Minute"/>  <!-- 取对象中定义的常量值 -->
    	<intervalLen>1</intervalLen>
    </job>
</xconfig>
```
举例翻译成Java代码：
```java
import org.hy.common.thread.Job;

Job v_JOB01 = new Job();

v_JOB01.setName("间隔1分钟执行的任务");
v_JOB01.setIntervalType(Job.$IntervalType_Minute);
v_JOB01.setIntervalLen(1);
```



关键字classpath
------
此为一个关键字，它即不xml文件中的节点，也不是节点的属性。而是用在节点属性值的关键字。
它表示当前XML文件在Java工程中的包路径。通过它，可获取同包下的其它资源或是上级包、下级包等相对包路径中的资源信息。

在使用时，classpath后面会后缀一个冒号。

基本语法：
```xml
<Java对象的节点名称>
	<对象方法的名称>classpath:/xxx.xxx</对象方法的名称>
</Java对象的节点名称>
```
举例说明：
```xml
<import name="XImageIcon" class="javax.swing.ImageIcon" />
<import name="XURL"       class="java.net.URL" />

<xconfig>
	<XImageIcon id="xiconSubmit">
		<constructor>
			<XURL>
				<constructor>
					<String>classpath:Ubuntu.png</String>  <!-- 图片与XML类放在同一包下 -->
				</constructor>
			</XURL>
		</constructor>
	</XImageIcon>
</xconfig>
```



关键字classhome
------
此为一个关键字，它即不xml文件中的节点，也不是节点的属性。而是用在节点属性值的关键字。
它表示Java工程中的根路径。通过它，可获取资源信息。

classhome的父目录是：Java工程的bin目录

在使用时，classhome后面会后缀一个冒号。

基本语法：
```xml
<Java对象的节点名称>
	<对象方法的名称>classhome:/xxx.xxx</对象方法的名称>
</Java对象的节点名称>
```
举例说明：
```xml
<import name="XImageIcon" class="javax.swing.ImageIcon" />
<import name="XURL"       class="java.net.URL" />

<xconfig>
	<XImageIcon id="xiconSubmit">
		<constructor>
			<XURL>
				<constructor>
					<String>classhome:Ubuntu.png</String>  <!-- 图片放在Java工程的根路径 -->
				</constructor>
			</XURL>
		</constructor>
	</XImageIcon>
</xconfig>
```



关键字webhome
------
此为一个关键字，它即不xml文件中的节点，也不是节点的属性。而是用在节点属性值的关键字。
它表示Web工程中的根路径。通过它，可获取资源信息。

webhome的父目录是：Web工程的名称

在使用时，webhome后面会后缀一个冒号。

基本语法：
```xml
<Java对象的节点名称>
	<对象方法的名称>webhome:/xxx.xxx</对象方法的名称>
</Java对象的节点名称>
```
举例说明：
```xml
<import name="XImageIcon" class="javax.swing.ImageIcon" />
<import name="XURL"       class="java.net.URL" />

<xconfig>
	<XImageIcon id="xiconSubmit">
		<constructor>
			<XURL>
				<constructor>
					<String>webhome:Ubuntu.png</String>  <!-- 图片放在Web工程的根路径 -->
				</constructor>
			</XURL>
		</constructor>
	</XImageIcon>
</xconfig>
```



XSQL轻量级持久层
------
XSQL支持普通SQL、高级SQL、动态SQL、存储过程、应用层SQL触发器和数据库与Java对象映射的持久层轻量级框架。与XJava融合，用XML文件保存SQL语句配置信息，构建SQL对象池。

两种加载SQL配置的方式：

	* 1.启动加载：常规情况下，应用程序在首次启动时，按SQL配置文件初始化SQL对象，并已单例的形式保存在内存中。
	
	* 2.动态加载：同时，也支持应用程序运行过程中（应用程序不停机），动态重新加载SQL配置文件。可局部加载或全量加载。

两种数据源：

	* 1.数据库连接池：默认情况XSQL须与数据库连接池对象关联。每个XSQL对应一个数据库连接池对象，多个XSQL也可共用一个数据库连接池对象。
	
	* 2.无数据库连接池：特殊情况下，如手机App这样的微型应用，一个单例的数据库连接就可以满足了，不需要庞大的数据库连接池。
	

