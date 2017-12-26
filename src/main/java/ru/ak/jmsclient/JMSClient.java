package ru.ak.jmsclient;

import org.apache.activemq.ActiveMQConnectionFactory;
import ru.ak.jmsclient.model.Response;

import javax.jms.*;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.logging.Level;

/**
 * Web-сервис для реализации интерфейса взаимодействия с брокером Apache ActiveMQ
 * @author akakushin
 */
@WebService(name = "JMSClient", serviceName = "JMSClient", portName = "JMSClientPort")
public class JMSClient {

    /**
     * Регистрация в брокере сообщений
     * @param queue имя очереди
     * @param brokerURL адрес брокера
	 * @param user пользователь
	 * @param password пароль
     * @param textMessage текстовое сообщение
     *            
     * @return response сущность "Ответ"
     */
	@WebMethod
    public Response produce(
    		@WebParam(name = "queue") String queue, 
    		@WebParam(name = "brokerURL") String brokerURL,
    		@WebParam(name = "user") String user,
    		@WebParam(name = "password") String password,
    		@WebParam(name = "textMessage") String textMessage) {

		Response response = new Response();

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, brokerURL);
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection(user, password);
        	connection.start();
        	
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(queue);
            MessageProducer producer = session.createProducer(destination);
            TextMessage message = session.createTextMessage(textMessage);

            producer.send(message);
            response.setMessageId(message.getJMSMessageID());

        } catch (JMSException ex) {
            MainClass.getInstanceLogger().log(Level.WARNING, "Produce : fall; {0}", ex);
            response.setError(true);
            response.setDescription(ex.getLocalizedMessage());
        } finally {
        	if (connection != null) {
        		try {
					connection.close();
				} catch (JMSException ex) {
					MainClass.getInstanceLogger().log(Level.WARNING, "Produce : fall; {0}", ex);
				}
        	}
		}
        return response;
    }

	/**
	 * Получение сообщения из брокера
	 * @param queue имя очереди
	 * @param brokerURL адрес брокера
	 * @param user пользователь
	 * @param password пароль
	 *
	 * @return response сущность "Ответ"
	 */
	@WebMethod
	public Response consume(
			@WebParam(name = "queue") String queue, 
			@WebParam(name = "brokerURL") String brokerURL,
			@WebParam(name = "user") String user,
			@WebParam(name = "password") String password) {
		
		Response response = new Response();
		
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, brokerURL);
		Connection connection = null;
        try {
        	connection = connectionFactory.createConnection(user, password);
        	connection.start();
        	
        	Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        	Destination destination = session.createQueue(queue);
        	        	
        	QueueBrowser queueBrowser = session.createBrowser((Queue) destination);
        	if (queueBrowser.getEnumeration().hasMoreElements()) {      	
	        	MessageConsumer consumer = session.createConsumer(destination);        	
	        	Message message = consumer.receive();
	        	if (message instanceof TextMessage) {
	        		response.setMessageId(message.getJMSMessageID());
	        		response.setTextMessage(((TextMessage) message).getText());
	        	}
        	}
        	
        } catch (JMSException ex) {
            MainClass.getInstanceLogger().log(Level.WARNING, "Consume : fall; {0}", ex); 
            response.setError(true);
            response.setDescription(ex.getLocalizedMessage());            
        } finally {
        	if (connection != null) {
        		try {
					connection.close();
				} catch (JMSException ex) {
					MainClass.getInstanceLogger().log(Level.WARNING, "Consume : fall; {0}", ex);
				}
        	}
		}
        
        return response;
	}

    /**
     * Получение сообщения из брокера
     * @param brokerURL адрес брокера
     * @param user пользователь
     * @param password пароль
     *
     * @return response сущность "Ответ"
     */
    @WebMethod
    public Response ping(
            @WebParam(name = "brokerURL") String brokerURL,
            @WebParam(name = "user") String user,
            @WebParam(name = "password") String password) {
        
    	Response response = new Response();

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, brokerURL);
		Connection connection = null;
        try {
        	connection = connectionFactory.createConnection(user, password);
        	connection.start();
        } catch (JMSException ex) {
            MainClass.getInstanceLogger().log(Level.WARNING, "Ping : fall; {0}", ex);
            response.setError(true);
            response.setDescription(ex.getLocalizedMessage());            
        } finally {
        	if (connection != null) {
        		try {
					connection.close();
				} catch (JMSException ex) {
					MainClass.getInstanceLogger().log(Level.WARNING, "Ping : fall; {0}", ex);
				}
        	}
		}
        return response;
    }
}
