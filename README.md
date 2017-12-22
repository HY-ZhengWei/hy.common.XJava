# XJava



* XJava
	* 系统标记
		* [import 引用类名](#系统标记import)
		* [constructor 构造器](#系统标记constructor)
		* [call 执行方法](#系统标记call)
	* 通用标记属性
		* [id 定义变量名](#属性id)
		* [class 标记的Java类型](#属性class)
		* [ref 引用](#属性ref)
		* [setter 容器添加子对象](#属性setter)
		* [key Map集合添加子元素的Key值](#属性key)
		* [submit 打包提交](#属性submit)
		* [new 是否为单例](#属性new)
	* 关键字
		* [this 对象实例自已](#关键字this)
		* [classpath XML同级包的路径](#关键字classpath)
		* [classhome Java工程的根路径](#关键字classhome)
		* [webhome Web工程的根路径](#关键字webhome)
	* 逻辑判断
		* [if 真值才解释](逻辑判断if)
		* [ifnot 假值才解释](逻辑判断ifnot)
	* 注解
		* [@Xjava 依赖注入](src/org/hy/common/xml/annotation/Xjava.java)
		* [@Xsql 映射SQL配置文件，无须持久层实体类](src/org/hy/common/xml/annotation/Xsql.java)
		* [@Xparam 方法参数映射SQL占位符及非空检查](src/org/hy/common/xml/annotation/Xparam.java)
		* [@XRequest 定义请求接口](src/org/hy/common/xml/annotation/XRequest.java)
		* [@Doc 将注释保留在编译后的二进制程序中](src/org/hy/common/xml/annotation/Doc.java)
* [XSQL 轻量级持久层](#轻量级持久层xsql)
* [XSQLGroup 轻量级ETL](#轻量级etl)
* [XSQLPaging 多种类数据库通用分页](src/org/hy/common/xml/XSQLPaging.xml)
* [XJSON Json与Java对象的转换](src/org/hy/common/xml/XJSON.java)
* [XHttp Http访问](src/org/hy/common/xml/XHttp.java)
* [XWebService 接口服务](src/org/hy/common/xml/XWebService.java)



XJava
------
* 描述：描述应用程序的特性，如参数配置。
* 定义：实例化对象，定义变量。
* 控制：控制对象间的关系及执行逻辑。
* 执行：执行Java方法。



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



轻量级持久层xsql
------
XSQL支持普通SQL、高级SQL、动态SQL、存储过程、应用层SQL触发器和数据库与Java对象映射的持久层轻量级框架。与XJava融合，用XML文件保存SQL语句配置信息，构建SQL对象池。

特点：

	*  1.易于上手和掌握。
	
	*  2.SQL语句写在XML里，解除SQL与程序代码的耦合，便于统一管理和优化XML配置。
	
	*  3.最佳SQL可读性。配置的SQL(或叫SQL模板)与运行时的执行SQL几乎一样。
	
	*  4.简单实用的对象关系映射。通过SQL语句的别名即能映射为对象属性。
	
	*  5.支持动态SQL。配置一个SQL模板，自动根据入参信息的变化，可生成不同情况的执行SQL。并且依然保持良好的SQL可读性

	*  6.简单的DAO持久层，只定义接口类，其实体类无须编写，通过一行@Xsql注解即可完成。
	
	*  7.复杂的DAO持久层，也可有真正的DAO实体类，编写更为强大的持久层。与XSQL组(XSQLGropu)配合，可轻松的实现跨服务的事务处理。
	
	*  8.支持SQL重载。通过定义重复的XML里标签ID，方便的实现SQL重载功能。
	
	*  9.数据库移植方面：只须针对有数据库特性的SQL定义多个Content即可实现移植及数据库类型动态切换。
	
	* 10.实用的SQL缓存。@Xsql(cacheID)、@Xsql(updateCacheID) + Job(定时任务)，全部配置化即可实现，无须代码。
	
	* 11.强大的SQL统计(http://IP:Port/服务名/analyses/analyseDB)。异常SQL、SQL组即时发现。当配合集群功能时，可集群统计及监控。
	
	* 12.热部署SQL配置XML文件。可在服务不重启的情况下，动态加载有更新变化的XML配置文件并及时生效。同时能保证不影响正在运行的SQL。方便及时修复Bug。

两种加载SQL配置的方式：

	* 1.启动加载：常规情况下，应用程序在首次启动时，按SQL配置文件初始化SQL对象，并已单例的形式保存在内存中。
	
	* 2.动态加载：同时，也支持应用程序运行过程中（应用程序不停机），动态重新加载SQL配置文件。可局部加载或全量加载。

两种数据源：

	* 1.数据库连接池：默认情况XSQL须与数据库连接池对象关联。每个XSQL对应一个数据库连接池对象，多个XSQL也可共用一个数据库连接池对象。
	
	* 2.无数据库连接池：特殊情况下，如手机App这样的微型应用，一个单例的数据库连接就可以满足了，不需要庞大的数据库连接池。
	
两种入参类型：

	* 1.可按对象填充占位符SQL; 同时支持动态SQL，动态标识 <[ ... ]>
	
	* 2.可按Map集合填充占位符SQL。同时支持动态SQL，动态标识 <[ ... ]>
	
动态SQL的形式：

	* 1.占位符：由一个冒号+变量名组成。如，:name。
	
	* 2.占位符支持面向对象：占位符可以为xxx.yyy.www(或getXxx.getYyy.getWww)全路径的解释。如，:shool.BeginTime。
	
	* 3.占位符可以写在SQL语句的任何位置。如，表名称后，表示动态表；Where条件后，表示动态条件。
	
	* 4.动态SQL标记区 <[ ... ]>。当区域内的占位符有值时(不为null)，动态SQL标记区生效，将参与到最终SQL的执行中。

基本语法：
```xml
<import name="sql" class="org.hy.common.xml.XSQL" />

<sql>

	<dataSourceGroup>...</dataSourceGroup>	 <!-- 定义所使用的数据库连接池 -->
	
	<content>...</content>	                 <!-- 定义执行的SQL语句 -->  
	
	<result>	                             <!-- 当为查询SQL语句时，定义查询结果集映射的Java对象及映射的方式 -->
		<table>java.util.ArrayList</table>   <!-- 表级的对象类型 -->
		<fill>add(row)</fill>                <!-- 行级对象填充到表级对象的填充方法名 -->
		<row>java.util.ArrayList</row>       <!-- 行级的对象类型 -->
		<cfill>add(colValue)</cfill>         <!-- 列级对象填充到行级对象的填充方法名 -->
		<relationKeys>...</relationKeys>     <!-- 一对多关系时，识别出属于同一对象的主键信息（多个属性间用逗号分隔） -->
	</result>                     
	
	<trigger>...</trigger>	                 <!-- 定义触发器。类似于数据库的After触发器。可选 -->
	
</sql>
```

查询SQL举例说明：
```xml
<import name="sql" class="org.hy.common.xml.XSQL" />

<sql> 

	<dataSourceGroup ref="DSG" />
			
	<content>
		<![CDATA[
		SELECT <[ TOP :pagePerSize ]>              <!-- 动态区。当pagePerSize不为null时有效，表示只查询前多少行记录 -->
		        A.modelID                          <!-- 普通列值映射。映射到行级对象的setModelID(...)方法 -->
		       ,A.only_Code  AS onlyCode           <!-- 列传重命名映射。映射到行级对象的setOnlyCode(...)方法 -->
		       ,A.projectID  AS "project.modelID"  <!-- 一对一关系映射。映射到行级对象的getProject().setModelID(...)方法 -->
		  FROM  Product:tableDate  A               <!-- 动态表查询 -->
		 WHERE  A.projectID  = ':modelID'          <!-- 固定的查询条件 -->
		<[ AND  A.curUserId IN (:curUserId)  ]>    <!-- 动态的查询条件。当curUserId不为null时有效 -->
		 ORDER  BY A.orderNum
		]]>
	</content>
	
	<result>	                                   <!-- 查询结果集转为Java对象的结构为：Set<Product> -->
		<table>java.util.LinkedHashSet</table>
		<row>xx.xx.Product</row>
		<cfill>setter(colValue)</cfill>            <!-- 使用setter方法的形式填充对象属性 -->
	</result>
	
</sql>
```

插入SQL举例说明：
```xml
<import name="sql" class="org.hy.common.xml.XSQL" />

<sql> 

	<dataSourceGroup ref="DSG" />
			
	<contentDB>
		<keyReplace>true</keyReplace>  <!-- 替换数据库关键字。如，单引号替换成两个单引号。默认为：false，即不替换 -->
	</contentDB>
	
	<content>
	<![CDATA[
        INSERT  INTO TLog_:sysID       <!-- 动态表写数据 -->
               (
                id
               ,logID
               ,logType
               ,logClass
               ,logContent
               ,logInfo
               ,operatorNo
               ,operationType
               ,operationRemark
               ,operationTime
               ,waitTime
               )
        VALUES (
                ':id'
               ,':logID'
               ,':logType'
               ,':logClass'
               ,':logContent'
               ,':logInfo'
               ,':operatorNo'
               ,':operationType'
               ,':operationRemark'
               ,':operationTime'
               ,':waitTime'
               )
	]]>
	</content>
			
</sql>
```

DDL举例说明：
```xml
<import name="sql" class="org.hy.common.xml.XSQL" />

<sql> 

	<dataSourceGroup ref="DSG" />
			
	<content>
	<![CDATA[
		CREATE TABLE TLog_:sysID       <!-- 动态表创建 --> 
		(
		  id                           VARCHAR(64)        NOT NULL
		 ,logID                        VARCHAR(64)
		 ,logType                      VARCHAR(1000)
		 ,logClass                     VARCHAR(1000)
		 ,logContent                   TEXT
		 ,logInfo                      TEXT
		 ,operatorNo                   VARCHAR(64)
		 ,operationType                VARCHAR(64)
		 ,operationRemark              VARCHAR(1000)
		 ,operationTime                DATETIME          NOT NULL 
		 ,waitTime                     INT               NOT NULL
		 ,PRIMARY KEY (id)
		)
	]]>
	</content>
			
</sql>
```

多种类数据库兼容举例说明：
```xml
<import name="sql" class="org.hy.common.xml.XSQL" />

<sql> 

	<dataSourceGroup ref="DSG" />
			
	<content if="MYSQL == DSG.getDbProductType">
	<![CDATA[
		ALTER TABLE TLog_:sysID ADD INDEX (logID)
	]]>
	</content>
	
	<content if="SQLSERVER == DSG.getDbProductType">
	<![CDATA[
		CREATE NONCLUSTERED INDEX IX_TLog_LogID_:sysID ON TLog_:sysID (logID)
	]]>
	</content>
			
</sql>
```



轻量级etl
------
组合一组XSQL，有顺序，有参数关联的执行。

特点：

	* 特点01：跨数据库位置及数据库种类。每个XSQL节点，可以执行（或查询）不同数据库的SQL语言。
	* 特点02：先后执行顺序的执行，并且前一个查询SQL的结果，将是下一个（乃至其后）XSQL节点的执行参数（或查询参数）。
	* 特点03：前一个查询XSQL节点的查询结果行数，决定着下一个XSQL节点的执行（或查询）次数，并且依次类推（除了 lastOnce=true 的节点）。
	* 特点04：当节点 lastOnce=true 时，表示本节点为收尾操作之一。在整个组合XSQLGroup的最后执行，并只执行一次。就算查询节点查无数据(无检查条件的情况下)，也会被执行。
	* 特点05：每个XSQL节点，都一个检查点，只有当检查通过时，才能执行本XSQL节点。
	* 特点06：当XSQL节点检查不通过时，通过noPassContinue属性决定是否继续执行其后的XSQL节点。默认检查只影响自己的节点，不影响其它及其后XSQL节点的执行。
	* 特点07：任何一个XSQL节点执行异常，都将全部终止执行（或查询）。
	* 特点08：可打印出执行轨迹，显示每一步执行的SQL语句。
	* 特点09：记录每个XSQL节点的上执行SQL（Insert、Update、Delete）影响的行数，可作为其后所有节点的执行参数（查询参数）。如变量EXECOUNT2，表示第二个步骤执行后影响的行数。但，不记录查询SQL的行数。
	* 特点10：支持统一提交、统一回滚的事务功能。不做特别设置的时，默认情况下，在所有操作成功后，统一提交。或在出现异常后，统一回滚。
	* 特点11：有三种节点提交类型：
		* 1. beforeCommit   操作节点前提交；
		* 2. afterCommit    操作节点后提交；
		* 3. perAfterCommit 每获取结果集一条记录前提交。
	* 特点12： 当未更新任何数据（操作影响的数据量为0条）时，可控制是否执行事务统一回滚操作XSQLNode.isNoUpdateRollbacks()
	* 特点13： 由外界决定是否提交、是否回滚的功能。
	* 特点14：支持简单的统计功能（请求整体执行的次数、成功次数、成功累计用时时长）。
	* 特点15：支持组嵌套组的执行：实现XSQLGroup组中嵌套另一个XSQLGroup组嵌套执行的功能。
	* 特点16：支持返回多个查询结果集。returnID标记的查询XSQL节点，一次性返回所有记录，并按XSQLResult定义的规则生成一个结果集对象。
	* 特点17：支持查询结果当作其后节点的SQL入参的同时，还返回查询结果。将查询XSQL节点每次循环遍历出的每一行记录，用 PartitionMap<String ,Object> 类型的行转列保存的数据结构，并返回查询结果集。
	* 特点18：支持执行Java代码，对查询结果集进行二次加工处理等Java操作。
	* 特点19：查询SQL语句有两种使用数据库连接的方式
		* 1. 读写分离：每一个查询SQL均占用一个新的连接，所有的更新修改SQL共用一个连接。this.oneConnection = false，默认值。
		* 2. 读写同事务：查询SQL与更新修改SQL共用一个连接，做到读、写在同一个事务中进行。


[举例说明](https://github.com/HY-ZhengWei/Stock/blob/master/src/com/hy/stock/config/db/db.StockInfoDay.CalcKDJ.xml)




---
#### 本项目引用Jar包，其源码链接如下
引用 https://github.com/HY-ZhengWei/hy.common.base 类库

引用 https://github.com/HY-ZhengWei/hy.common.db 类库

引用 https://github.com/HY-ZhengWei/hy.common.file 类库

引用 https://github.com/HY-ZhengWei/hy.common.net 类库

引用 https://github.com/HY-ZhengWei/hy.common.tpool 类库
