import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.*;


public class ClientFor_login  implements ActionListener
{

	//---Loading GUI objects 
	private JFrame loginWindow = new JFrame("A Chat Room---Guangsen Ji@2020 ");
	
	private JTextArea		logInTextArea		= new JTextArea("Stay online, Stay connected, Stay together");
	private JPanel			loginPanel			= new JPanel();
	private JTextField		idTextField			= new JTextField();
	private JPasswordField	passwordTextField	= new JPasswordField();
	private JPanel			buttonPanel			= new JPanel();
	private JButton			loginButton			= new JButton("Log In");
	private JButton			newUserButton		= new JButton("New to here? Sign Up !");
	
	private JButton			backButton			= new JButton("Back to Log In");
	private JPanel			signUpPanel			= new JPanel();
	private JTextField		NameTextField		= new JTextField();
	private JPasswordField	newPw1Field			= new JPasswordField();
	private JPasswordField	newPw2Field			= new JPasswordField();
	private JButton			signUpButton		= new JButton("Sign Up");
	private JTextArea		newUserTextArea		= new JTextArea();
	
	//----Build Connection
	private Socket				s;
	private ObjectOutputStream	oos;
	private ObjectInputStream	ois;
	private Dimension			dimension = Toolkit.getDefaultToolkit().getScreenSize();
	
	//Information for log in
	private int		account_number;
	private String	password;
	
	//Load Main Client for chat
	ClientFor_chat Client_main;
	
	
	
	public ClientFor_login(String serverAddress, int portNumber)
	{
		
		try
		{
			s = new Socket(serverAddress, portNumber); // wait for server connect
			oos = new ObjectOutputStream(s.getOutputStream());
			System.out.println("Build connection with server successfully");
		} catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("failed to build socket connection");
			return;
		}
		//build login panel
		loginPanel.setLayout(new GridLayout(5, 1));
		loginPanel.add(new JLabel("Enter Your User ID: "));
		loginPanel.add(idTextField);
		loginPanel.add(new JLabel("Enter Your Password: "));
		loginPanel.add(passwordTextField);
		loginPanel.add(new JLabel("Any Questions? Get in touch with the author: gji@ncsu.edu "));
		
		buttonPanel.setLayout(new GridLayout(2, 1));
		buttonPanel.add(loginButton);
		buttonPanel.add(newUserButton);
		
		signUpPanel.setLayout(new GridLayout(6, 1));
		signUpPanel.add(new JLabel("Enter Your Nick Name here! "));
		signUpPanel.add(NameTextField);
		signUpPanel.add(new JLabel("Enter Your Password here! "));
		signUpPanel.add(newPw1Field);
		signUpPanel.add(new JLabel("Enter Your Password one more time! "));
		signUpPanel.add(newPw2Field);
		
		// Setup Text Area
		logInTextArea.setEditable(false);
		logInTextArea.setEnabled(false);
		newUserTextArea.setEditable(false);
		
		// Add Listener
		loginButton.addActionListener(this);
		newUserButton.addActionListener(this);
		backButton.addActionListener(this);
		signUpButton.addActionListener(this);
		
		loadLogin();
		
		// Set Main Window Size
		loginWindow.setSize(550,300);
		loginWindow.setResizable(false);
		loginWindow.setLocation(dimension.width/2-loginWindow.getSize().width/2, dimension.height/2-loginWindow.getSize().height/2);
		loginWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		loginWindow.setVisible(true);
	
		
	}
	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == loginButton) 
		{
			if (idTextField.getText().isBlank() || passwordTextField.getPassword().length == 0)
			{
				errorWindow(dimension, "Please enter you ID and password");
				return;
			}
			try
			{
				account_number = Integer.parseInt(idTextField.getText().trim());
			} catch (Exception e)
			{
				 errorWindow(dimension, "Invalid User ID " + e.getMessage() + " ID Must be Numbers");
				return;
			}
			password = new String(passwordTextField.getPassword());
			
			System.out.println(account_number + " " + password);
		
		
		//--Send Join request to server for verification
			String request = "login:"+ account_number +" "+password;
			String name = "";
			try
			{
				oos.writeObject(request);
				if(ois == null) ois = new ObjectInputStream(s.getInputStream());
				Object reply = ois.readObject();
				String msg = (String)reply;
				int offsetForLogin = msg.indexOf(":");
				String msgType = msg.substring(0,offsetForLogin);
				String realMsg = msg.substring(offsetForLogin+1); // the information sent by server after ":";
				
				if (msgType.equals("true"))
				{
					System.out.println("log in succeed");
					name = realMsg;
					
				}
				else if(msgType.equals("false"))
				{
					errorWindow(dimension, "log in failed");
					System.out.println("log in failed");
					return;
				}
				else 
				{
					errorWindow(dimension, "didn't recongized msgType"+msgType);
					System.out.println("didn't recongized msgType"+msgType);
					return; 
				}
				
			}
			
			catch (IOException e)
			{
				errorWindow(dimension, e.getMessage());
				return;
			} catch (ClassNotFoundException e)
			{
				errorWindow(dimension, e.getMessage());
				return;
			}
			System.out.println("log in succeed and will load the second window");
			
			Client_main = new ClientFor_chat();
			Client_main.AfterLogin(s, oos, ois, account_number, name);
			
			loginWindow.dispatchEvent(new WindowEvent(loginWindow, WindowEvent.WINDOW_CLOSING));	
		}
		else if (ae.getSource() == newUserButton) // Load Sign Up Panel
		{
			loadSignUp();
		}
		else if (ae.getSource() == backButton) // Load Login Panel
		{
			loadLogin();
		}
		else if(ae.getSource() == signUpButton)
		{
			
			String name = NameTextField.getText().trim();
			name = checkNickNameFormat(name);
			if (name == null) return;
			String pw1 = new String(newPw1Field.getPassword());
			String pw2 = new String(newPw2Field.getPassword());
			String pw = checkPasswordFormat(pw1, pw2);
			if (pw == null) return;
			System.out.println(name + " " + pw1 + " " + pw2 + pw);
			
			String request = "signUp:"+ name +" "+ pw;
			try
			{
				oos.writeObject(request);
				if(ois == null) ois = new ObjectInputStream(s.getInputStream());
				Object reply = ois.readObject();
				String msgType = (String)reply;
				int OffsetForSignUp = msgType.indexOf(":");
				String Type = msgType.substring(0, OffsetForSignUp);
				String id = msgType.substring(OffsetForSignUp+1);
				if (Type.equals("true"))
				{
					System.out.println("sign up succeed");
					account_number = Integer.parseInt(id);
					NameTextField.setText("");
					newPw1Field.setText("");
					newPw2Field.setText("");
					loadNewUser();
					
				}
				else if(Type.equals("false"))
				{
					errorWindow(dimension, "sign up failed");
					System.out.println("sign up failed");
					return;
				}
				else 
				{
					errorWindow(dimension, "didn't recongized msgType"+msgType);
					System.out.println("didn't recongized msgType"+msgType);
					return; 
				}

			}
			catch (Exception e)
			{
				errorWindow(dimension, e.getMessage());
				return;
			}
		}
	
		
	}
	
	public void errorWindow(Dimension dimension, String msg)
	{
		JFrame        errorWindow 	= new JFrame("Chat Room - Error Message"); 
		JTextArea 	errorTextArea 	= new JTextArea();
		errorWindow.add(errorTextArea);
		errorTextArea.setEditable(false);
		errorTextArea.setText(msg);
		errorTextArea.setBackground(Color.pink);
		errorTextArea.setFont(new Font("default", Font.PLAIN, 20));
		errorTextArea.setLineWrap(true);
		errorTextArea.setWrapStyleWord(true);
		errorWindow.setSize(300,200);
		errorWindow.setLocation(dimension.width/2-errorWindow.getSize().width/2, dimension.height/2-errorWindow.getSize().height/2);
		errorWindow.setResizable(false);
		errorWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		errorWindow.setVisible(true);
		return;
		
	}
	
	
	//---check eligibility of name
	public String checkNickNameFormat(String nickName)
	{
		nickName = nickName.trim();
		if (nickName.length() == 0)
		{
			errorWindow(dimension, "Please Enter Nick Name" + account_number);
			return null;
		} else if (nickName.length() > 30)
		{
			errorWindow(dimension, "Nick Name Cannot Exceed 30 Characters"+account_number);
			return null;
		}
		return nickName;
	}
	
	public String checkPasswordFormat(String pw1, String pw2)
	{
		if (pw1.length() < 6 || pw1.length() > 30)
		{
			errorWindow(dimension, "Password Length Must Be 6-30 Characters"+account_number);
			return null;
		} else if (!pw1.equals(pw2))
		{
			errorWindow(dimension, "Passwords are Not Consistent" + account_number);
			return null;
		}
		return pw1;
	}
	
	
	//----main function
	public static void main(String[] args)
	{
		String ip;
		int serverPort = 5555;
		try
		{
			ip = InetAddress.getLocalHost().getHostAddress();
			ClientFor_login cli = new ClientFor_login(ip, serverPort);
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
			return;
		}
		

	}
	
	// Load Login Window
		private void loadLogin()
		{
			loginWindow.getContentPane().removeAll();
			
			loginWindow.getContentPane().add(logInTextArea, "North");
			loginWindow.getContentPane().add(loginPanel, "Center");
			loginWindow.getContentPane().add(buttonPanel,"South");
			// Pre-load user_id
			if (account_number != 0) idTextField.setText(String.valueOf(account_number));
			
			loginWindow.getRootPane().setDefaultButton(loginButton);
			
			loginWindow.revalidate();
			loginWindow.getContentPane().repaint();
		}
		
	// Load Sign Up Window
		private void loadSignUp()
		{
			loginWindow.getContentPane().removeAll();
			
			loginWindow.getContentPane().add(backButton,"North");
			loginWindow.getContentPane().add(signUpPanel, "Center");
			loginWindow.getContentPane().add(signUpButton,"South");
			
			loginWindow.getRootPane().setDefaultButton(signUpButton);
			
			loginWindow.revalidate();
			loginWindow.getContentPane().repaint();
		}
	// Load after sign up 	
		private void loadNewUser()
		{
			loginWindow.getContentPane().removeAll();
			
			String newLine = System.lineSeparator();
			newUserTextArea.setText("success in sign up" + newLine + "Your User ID is: " + account_number);
			newUserTextArea.setFont(new Font("default", Font.PLAIN, 20));
			loginWindow.getContentPane().add(backButton,"North");
			loginWindow.getContentPane().add(newUserTextArea, "Center");
			
			loginWindow.revalidate();
			loginWindow.getContentPane().repaint();
		}
		
		
		
		
		

}
