package org.ray.data.mysql.aop;

import org.apache.ibatis.session.SqlSessionException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.ray.data.mysql.annotation.TransactionMulti;
import org.ray.data.mysql.core.DataSourceRouting;
import org.ray.data.mysql.enums.TransactionTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 多数据源事务切面
 * 使用aspectj
 */
@Aspect
public class MultiTransactionManagerAop {


    @Autowired
    DataSourceRouting dataSourceRouting;

    /**
     * 定义切点，带TransactionMulti注解
     */

    @Pointcut("@annotation(org.ray.data.mysql.annotation.TransactionMulti)")
    public void annotationPointcut() {
    }


    /**
     * 使用环绕切
     *
     * @param joinpoint
     * @return
     * @throws Throwable
     */
    @Around("annotationPointcut()")
    public Object roundExecute(ProceedingJoinPoint joinpoint) throws Throwable {
        //获取方法
        MethodSignature methodSignature = (MethodSignature) joinpoint.getSignature();
        Method method = methodSignature.getMethod();
        //获取方法上TransactionMulti注解
        TransactionMulti annotation = method.getAnnotation(TransactionMulti.class);
        //获取注解涉及的多数据源
        String[] values = annotation.dataSourceKey();
        //获取注解定义的隔离级别
        int transactionType = annotation.transactionType();
        //把涉及的数据源连接绑定到当前线程,开启事务,关闭自动提交
        begin(values, transactionType);
        //执行方法
        Object proceed = joinpoint.proceed();
        //方法执行完成后事务提交
        end();
        return proceed;
    }

    @AfterThrowing(pointcut = "annotationPointcut()", throwing = "e")
    public void handleThrowing(JoinPoint joinPoint, Exception e) {
        try {
            //方法执行异常，事务回滚
            dataSourceRouting.rollback();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }


    private void begin(String[] values, int transactionType) throws SQLException {
        //遍历所有涉及的数据源
        for (String value : values) {
            //获取数据源
            DataSource dataSource = dataSourceRouting.getDataSource(value);
            if (dataSource == null) {
                continue;
            }
            //获取数据源连接
            Connection connection = dataSource.getConnection();
            //设置数据源隔离级别
            prepareTransactionalConnection(connection, transactionType);
            //关闭自动提交
            connectBegin(connection);
            //数据源连接绑定到当前线程上
            dataSourceRouting.bindConnection(value, connection);
        }
    }

    /**
     * 方法执行完成后，事务提交
     *
     * @throws SQLException
     */
    private void end() throws SQLException {
        dataSourceRouting.doCommit();
    }

    /**
     * 开启事物的一些准备工作，关闭自动提交
     */
    private void connectBegin(Connection connection) throws SQLException {
        if (connection != null) {
            try {
                if (connection.getAutoCommit()) {
                    connection.setAutoCommit(false);
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }


    /**
     * 设置隔离级别
     *
     * @param con
     * @throws SQLException
     */
    protected void prepareTransactionalConnection(Connection con, int transactionType)
            throws SQLException {
        if (TransactionTypeEnum.isNotDefined(transactionType)) {
            throw new SqlSessionException("当前事物隔离级别未被定义");
        }
        con.setTransactionIsolation(transactionType);
    }


}
