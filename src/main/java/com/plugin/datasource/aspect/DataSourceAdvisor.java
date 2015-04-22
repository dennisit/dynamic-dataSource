//--------------------------------------------------------------------------
// Copyright (c) 2010-2020, En.dennisit or Cn.苏若年
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
// Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
// Neither the name of the dennisit nor the names of its contributors
// may be used to endorse or promote products derived from this software
// without specific prior written permission.
// Author: dennisit@163.com | dobby | 苏若年
//--------------------------------------------------------------------------
package com.plugin.datasource.aspect;

import com.plugin.datasource.annotation.DataSource;
import com.plugin.datasource.annotation.DynamicDataSource;
import com.plugin.datasource.context.DataSourceHolder;
import com.plugin.datasource.type.DataSourceType;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Description: function dependency on annotation scan
 * @author dennisit@163.com
 * @version 1.0
 */
@Component
public class DataSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor implements MethodInterceptor {

    public static final Logger LOG = Logger.getLogger(DataSourceAdvisor.class);

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // handle annotation dynamic data source
        Class<?> clazz = invocation.getThis().getClass();
        Method method = invocation.getMethod();
        Method targetMethod = clazz.getMethod(method.getName(), method.getParameterTypes());
        if ( null != targetMethod && targetMethod.isAnnotationPresent(DataSource.class)) {
            DataSource dynamicDataSource = targetMethod.getAnnotation(DataSource.class);
            // if use annotation define @DynamicDataSource(name="slave") , inject slave data source, else inject master data source
            if(DataSourceType.SLAVE == dynamicDataSource.type()){
                DataSourceHolder.setDataSourceType(DataSourceType.SLAVE);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("before invoke:" + targetMethod.getClass().getName() + "#" + targetMethod.getName() + " using " + DataSourceType.SLAVE.getType() + " datasource");
                }
            }else{
                DataSourceHolder.setDataSourceType(DataSourceType.MASTER);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("before invoke:" + targetMethod.getClass().getName() + "#" + targetMethod.getName() + " using " + DataSourceType.MASTER.getType() + " datasource");
                }
            }
        }
        try {
            return invocation.proceed();
        } catch (Exception e) {
            LOG.error("before invoke inject dataSource type error, " + e.getMessage() ,e);
            throw new RuntimeException(e);
        } finally {
            stopWatch.stop();
            if (stopWatch.getTime() > 100) {
                LOG.error("DataSourceAdvisor execute time:" + stopWatch.getTime() + " ms.");
            }
        }
    }

    @Override
    public Pointcut getPointcut() {
        // param DynamicDataSource annotation on class, DataSource annotation on method, type rely on DataSource
        return new AnnotationMatchingPointcut(DynamicDataSource.class, DataSource.class);
    }

    @Override
    public Advice getAdvice() {
        return this;
    }
}
