package app;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.swing.*;

import org.apache.log4j.BasicConfigurator;

import com.google.gson.Gson;

import data.User;

public class MyApp extends JFrame implements ActionListener{
	private JTextArea textArea;
	private JTextField username;
	private JTextField password;
	private JTextField textField;
	private Session session;
	private Gson gson;
	private MessageProducer producer;
	private MessageConsumer receiver;
	private User user;
	private JButton b;

	MyApp() throws Exception {
		
		BasicConfigurator.configure();
		// config environment for JNDI
		Properties settings = new Properties();
		settings.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		settings.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
		// create context
		Context ctx = new InitialContext(settings);
		// lookup JMS connection factory
		ConnectionFactory factory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
		// lookup destination. (If not exist-->ActiveMQ create once)
		Destination destination = (Destination) ctx.lookup("dynamicQueues/thanthidet");
		// get connection using credential
		Connection con = factory.createConnection("Ty", "123");
		// connect to MOM
		
		// create session
		session = con.createSession(/* transaction */false, /* ACK */Session.AUTO_ACKNOWLEDGE);
		// create producer
		producer = session.createProducer(destination);
		// tạo consumer
		receiver = session.createConsumer(destination);
		
		// create text message
//				Message msg = session.createTextMessage("hello mesage from ActiveMQ");
//				producer.send(msg);
//				
//				Message obj = session.createTextMessage("{"
//						+ "name: 'hieu'"
//						+ "}");
//				producer.send(obj);
//				Person p = new Person(1001, "Thân Thị Đẹt", new Date());
//				
//				
//				msg = session.createTextMessage(gson.toJson(p));
//				producer.send(msg);

		// Cho receiver lắng nghe trên queue, chừng có message thì notify - async

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
						String msg1 = user.getName() + ": " + user.getMessage();
						System.out.println(msg1);
						String txt = textArea.getText();
						textArea.setText(txt + "\n" + msg1);
					}
					// others message type....
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
		
		
		
		
		
		
		
		
		
		
		
		this.setLayout(new BorderLayout());
		textArea = new JTextArea();
		textArea.setEditable(false);

		JPanel pHeader = new JPanel();
		JPanel pHeader1 = new JPanel();
		JPanel pHeader2 = new JPanel();
		pHeader.setLayout(new GridLayout());
		pHeader1.setLayout(new BorderLayout());
		pHeader2.setLayout(new BorderLayout());
		username = new JTextField();
		pHeader1.add(username, BorderLayout.CENTER);

		pHeader.add(pHeader1, BorderLayout.WEST);
		password = new JTextField();
		pHeader2.add(password, BorderLayout.CENTER);
		pHeader.add(pHeader2, BorderLayout.EAST);

		// bottom

		JPanel pBottom = new JPanel();
		pBottom.setLayout(new BorderLayout());
		textField = new JTextField();
		pBottom.add(textField, BorderLayout.CENTER);

		b = new JButton(new ImageIcon());
		b.setText("Send");
		b.addActionListener(this);
		gson = new Gson();
		user = new User("Guest" + new Random().nextInt() % 100, null);
		

		b.setBounds(100, 100, 100, 40);
		pBottom.add(b, BorderLayout.EAST);

		this.add(pHeader, BorderLayout.NORTH);
		this.add(textArea, BorderLayout.CENTER);
		this.add(pBottom, BorderLayout.SOUTH);
		this.setSize(300, 400);

		// config environment for JMS
		con.start();
		System.out.println(user.getName() + " was listened on queue...");
	
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o == b) {
			
			String u = username.getText().trim();
			String p = password.getText().trim();
			String msg = textField.getText().trim();
//			if (u.length() > 0 && p.length() > 0 && msg.length() > 0) {

				
				user.setMessage(msg);
				System.out.println(msg);
				Message obj1;
				
					try {
						obj1 = session.createObjectMessage(gson.toJson(user));
						producer.send(obj1);
						String txt = textArea.getText();
						textArea.setText(txt + "\n" + msg);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}


//			}
		}
	}

}