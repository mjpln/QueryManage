/**  
 * @Project: JfinalDemo 
 * @Title: LogQueueListener.java
 * @Package com.knowology.common.log
 * @author c_wolf your emai address
 * @date 2014-11-19 下午1:08:10
 * @Copyright: 2014 www.knowology.cn Inc. All rights reserved.
 * @version V1.0   
 */
package com.knowology.common.log;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

/**
 * @内容摘要 ：
 * <p>Company: knowology </p>
 * @author c_wolf your emai address
 * @date 2014-11-19 下午1:08:10
 */

public class LogQueueListener implements MessageListener,ServletContextListener{

	public static Logger logger = Logger.getLogger(LogQueueListener.class);  
	
	public static LogQueueListener lis;
    
 
    public void onMessage( final Message message)  
    {  
        if ( message instanceof ObjectMessage )  
        {  
            try{
            	Thread.sleep(3000);
                final LoggingEventWrapper loggingEventWrapper = (LoggingEventWrapper)((ObjectMessage) message).getObject();  
                System.out.println(loggingEventWrapper.getIpAddress());
                message.acknowledge();
            }  
            catch (final JMSException e)  
            {  
                logger.error(e.getMessage(), e);  
            } catch (Exception e) {  
            logger.error(e.getMessage(),e);  
        }  
        }  
    }



	public void contextInitialized(ServletContextEvent servletcontextevent) {
		//initMq();
	}



	public void contextDestroyed(ServletContextEvent servletcontextevent) {
		// TODO Auto-generated method stub
		
	}  
	public static void initMq() {
		lis = new LogQueueListener();
		// ConnectionFactory ：连接工厂，JMS 用它创建连接
		// Connection ：JMS 客户端到JMS Provider 的连接
		try {
		Connection connection = MqConnPool.getConn();
		connection.start();
		// Session： 一个发送或接收消息的线程
           // 获取操作连接
          Session session;
		
		session = connection.createSession(Boolean.FALSE,
			           Session.AUTO_ACKNOWLEDGE);
		
           // 获取session注意参数值xingbo.xu-queue是一个服务器的queue，须在在ActiveMq的console配置
          Destination destination = session.createQueue("foo.bar");
          MessageConsumer  consumer = session.createConsumer(destination);
          consumer.setMessageListener(lis);
        // connection.close();
       }catch (JMSException e) {

    	   e.printStackTrace();
   	}
	} 
	}


