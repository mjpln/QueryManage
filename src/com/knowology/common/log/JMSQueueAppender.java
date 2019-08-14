package com.knowology.common.log;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

/**
 * JMSQueue appender is a log4j appender that writes LoggingEvent to a queue.
 * 
 * @author faheem
 * 
 */
public class JMSQueueAppender extends AppenderSkeleton implements Appender {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger("JMSQueueAppender");

	public void close() {

	}

	public boolean requiresLayout() {
		return false;
	}

	@Override
	protected synchronized void append(LoggingEvent event) {
		try {

			Connection connection = MqConnPool.getConn();
			System.out.println(connection.getClientID() + "|"
					+ connection.toString());
			connection.start();

			// Create a Session
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			// Create the destination (Topic or Queue)
			Queue destination = session.createQueue("foo.bar");

			// Create a MessageProducer from the Session to the Topic or Queue
			MessageProducer producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			ObjectMessage message = session
					.createObjectMessage(new LoggingEventWrapper(event, layout));
			// Tell the producer to send the message
			producer.send(message);

			// Clean up
			session.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}