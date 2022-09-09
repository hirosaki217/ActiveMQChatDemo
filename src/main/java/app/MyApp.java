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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.BasicConfigurator;

import com.google.gson.Gson;

import data.User;

public class MyApp extends JFrame implements ActionListener, MessageListener {

	private static final String APP_TOPIC = "thanthidet";
	private static final String DEFAULT_BROKER_NAME = "tcp://localhost:61616";

	private javax.jms.Connection connect = null;
	private javax.jms.Session pubSession = null;
	private javax.jms.Session subSession = null;
	private javax.jms.MessageProducer publisher = null;

	private JTextArea textArea;
	private JTextField username;
	private JTextField password;
	private JTextField textField;
	private Gson gson;
	private String usn, ps;
	private User user;
	private JButton b, cnn;
	private static boolean login = false;
	private void chatter(String broker, String username, String password) {
		try {
			javax.jms.ConnectionFactory factory;
			factory = new ActiveMQConnectionFactory(username, password, broker);
			connect = factory.createConnection(username, password);
			pubSession = connect.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
			subSession = connect.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
		} catch (javax.jms.JMSException jmse) {
			System.err.println("error: Cannot connect to Broker - " + broker);
			jmse.printStackTrace();
			System.exit(1);
		}
		try {
			javax.jms.Topic topic = pubSession.createTopic(APP_TOPIC);
			javax.jms.MessageConsumer subscriber = subSession.createConsumer(topic);
			subscriber.setMessageListener(this);
			publisher = pubSession.createProducer(topic);
			// Now that setup is complete, start the Connection
			connect.start();
		} catch (javax.jms.JMSException jmse) {
			jmse.printStackTrace();
		}
	}

	MyApp() throws Exception {

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
		pHeader.add(pHeader2, BorderLayout.WEST);
		cnn = new JButton("connect");
		pHeader.add(cnn, BorderLayout.EAST);
		cnn.addActionListener(this);
		// bottom

		JPanel pBottom = new JPanel();
		pBottom.setLayout(new BorderLayout());
		textField = new JTextField();
		pBottom.add(textField, BorderLayout.CENTER);

		b = new JButton(new ImageIcon());
		b.setText("Send");
		b.addActionListener(this);
		gson = new Gson();
		user = new User(usn, null);

		b.setBounds(100, 100, 100, 40);
		pBottom.add(b, BorderLayout.EAST);

		this.add(pHeader, BorderLayout.NORTH);
		this.add(textArea, BorderLayout.CENTER);
		this.add(pBottom, BorderLayout.SOUTH);
		this.setSize(300, 400);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		chatter(DEFAULT_BROKER_NAME, usn, ps);
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == b) {
			if(!login) {
				textArea.append("please enter data user connect\n");
				return;
			}
			String msg = textField.getText().trim();
			if (usn.length() > 0 && ps.length() > 0 && msg.length() > 0) {
				user.setName(usn);
				user.setMessage(msg);
				Message obj1;

				try {
					obj1 = pubSession.createObjectMessage(gson.toJson(user));
					textArea.append("\n" +user.getName()+": " + user.getMessage());
					publisher.send(obj1);
					textField.setText("");
					textArea.requestFocus();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		}
		else if(o == cnn) {
			usn = username.getText().trim();

			ps = password.getText().trim();
			
			if(usn.length() > 0 && ps.length() >0) {
				user.setName(usn);
				login = true;
				textArea.append("user ' "+usn+ "' connected\n");
			}else {
				textArea.append("invalid connect\n");
			}
		}
	}

	public void onMessage(Message msg) {
		try {
			if (msg instanceof TextMessage) {
				TextMessage tm = (TextMessage) msg;
				String txt = tm.getText();
				System.out.println("Nhận được " + txt);
				msg.acknowledge();// gửi tín hiệu ack
			} else if (msg instanceof ObjectMessage) {
				ObjectMessage om = (ObjectMessage) msg;

				User user1 = gson.fromJson(om.getObject().toString(), User.class);
				String msg1 = user1.getName() + ": " + user1.getMessage();
				System.out.print(user.toString());
				if (user.getName()!=null && !user.getName().equals(user1.getName())) {
					String txt = textArea.getText();
					textArea.setText(txt + "\n" + msg1);
				}
			}
			// others message type....
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}