# dynamic-dataSource

dynamic-dataSource

How to Use
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
		http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
		http://www.springframework.org/schema/aop
		http://www.springframework.org/schema/aop/spring-aop-2.5.xsd" default-autowire="byName">

    <description>数据源及动态数据源配置</description>

    <!--
    <context:component-scan base-package="com.plugin.datasource" />
    读写分离数据源切面扫描
    <aop:aspectj-autoproxy/>
   -->

    <!-- 基本数据源配置  -->
    <bean id="parentDataSource" class="com.alibaba.druid.pool.DruidDataSource"
          init-method="init" destroy-method="close">
        <!-- 配置过滤 -->
        <property name="filters" value="stat" />
        <!-- 配置初始化大小 -->
        <property name="initialSize" value="1" />
        <!-- 配置初始化最大 连接数 -->
        <property name="maxActive" value="20" />
        <!-- 配置初始化最小连接数 -->
        <property name="minIdle" value="3" />
        <!-- 配置获取连接等待超时的时间 -->
        <property name="maxWait" value="60000" />
        <!-- 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒 -->
        <property name="timeBetweenEvictionRunsMillis" value="60000" />
        <!-- 配置一个连接在池中最小生存的时间，单位是毫秒 -->
        <property name="minEvictableIdleTimeMillis" value="300000" />
        <!-- 检测连接是否有效的SQL -->
        <property name="validationQuery" value="SELECT 'x'" />
        <property name="testWhileIdle" value="true" />
        <property name="testOnBorrow" value="false" />
        <property name="testOnReturn" value="false" />
    </bean>

    <!-- 写数据源配置 -->
    <bean id="writerDataSource" parent="parentDataSource">
        <property name="url" value="jdbc:mysql://127.0.0.1:3306/db_test?useUnicode=true&amp;characterEncoding=UTF-8" />
        <property name="username" value="root" />
        <property name="password" value="root" />
    </bean>

    <!-- 读数据源配置 -->
    <bean id="readerDataSource" parent="parentDataSource">
        <property name="url" value="jdbc:mysql://127.0.0.1:3306/db_test?useUnicode=true&amp;characterEncoding=UTF-8" />
        <property name="username" value="root" />
        <property name="password" value="root" />
    </bean>

    <!-- 动态数据源中间件配置 -->
    <bean id="dynamicDataSource" class="com.plugin.datasource.DynamicRoutingDataSource">
        <property name="targetDataSources">
            <map key-type="java.lang.String" value-type="javax.sql.DataSource">
                <entry key="master" value-ref="writerDataSource" />
                <entry key="slave" value-ref="readerDataSource" />
            </map>
        </property>
        <property name="defaultTargetDataSource" ref="writerDataSource"/>
    </bean>


    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dynamicDataSource" />
    </bean>

    ...

</beans>


