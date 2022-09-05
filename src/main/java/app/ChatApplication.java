package app;

public class ChatApplication {
	public static void main(String[] args) throws Exception{
		 java.awt.EventQueue.invokeLater(new Runnable() {
	          public void run() {
	        	  try {
					new MyApp().setVisible(true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};
	          }
	    });
		
	}
}
