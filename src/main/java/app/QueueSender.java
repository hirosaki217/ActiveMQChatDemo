package app;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.apache.log4j.BasicConfigurator;

import com.google.gson.Gson;

import data.User;

public class QueueSender {
	public static void main(String[] args) throws Exception {
//config environment for JMS
		BasicConfigurator.configure();
//config environment for JNDI
		Properties settings = new Properties();
		settings.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		settings.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
//create context
		Context ctx = new InitialContext(settings);
//lookup JMS connection factory
		ConnectionFactory factory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
//lookup destination. (If not exist-->ActiveMQ create once)
		Destination destination = (Destination) ctx.lookup("dynamicQueues/thanthidet");
//get connection using credential
		Connection con = factory.createConnection("admin", "admin");
//connect to MOM
		con.start();
//create session
		Session session = con.createSession(/* transaction */false, /* ACK */Session.AUTO_ACKNOWLEDGE);
//create producer
		MessageProducer producer = session.createProducer(destination);
// tạo consumer
		MessageConsumer receiver = session.createConsumer(destination);
//create text message
//		Message msg = session.createTextMessage("hello mesage from ActiveMQ");
//		producer.send(msg);
//		
//		Message obj = session.createTextMessage("{"
//				+ "name: 'hieu'"
//				+ "}");
//		producer.send(obj);
//		Person p = new Person(1001, "Thân Thị Đẹt", new Date());
//		
		final Gson gson = new Gson();
//		
//		msg = session.createTextMessage(gson.toJson(p));
//		producer.send(msg);
		User user = new User("Guest", null);
		// Cho receiver lắng nghe trên queue, chừng có message thì notify - async
		System.out.println(user.getName()+" was listened on queue...");
		receiver.setMessageListener(new MessageListener() {

			// có message đến queue, phương thức này được thực thi
			public void onMessage(Message msg) {// msg là message nhận được
				try {
					if (msg instanceof TextMessage) {
						TextMessage tm = (TextMessage) msg;
						String txt = tm.getText();
						System.out.println("Nhận được " + txt);
						msg.acknowledge();// gửi tín hiệu ack
					} else if (msg instanceof ObjectMessage) {
						ObjectMessage om = (ObjectMessage) msg;

						User user = gson.fromJson(om.getObject().toString(), User.class);

						System.out.println(user.getName() + ": " + user.getMessage());
					}
					// others message type....
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		String m = "";
		Scanner sc = new Scanner(System.in);
		while (true) {
			m = sc.nextLine();
			user.setMessage(m);
			Message obj1 = session.createObjectMessage(gson.toJson(user));
			producer.send(obj1);
		}

//		Person p = new Person(1001, "Thân Thị Đẹt", new Date());
//		String xml = new XMLConvert<Person>(p).object2XML(p);
//		msg = session.createObjectMessage(xml);
//		producer.send(msg);
//shutdown connection
//		session.close();
//		con.close();
//		System.out.println("Finished...");
	}
}