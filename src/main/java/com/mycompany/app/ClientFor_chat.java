//Guangsen Ji
//Client for chat



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;




public class ClientFor_chat implements ActionListener, ListSelectionListener, FocusListener, Runnable
{

//---Instance Variables
	private Socket				s;
	private ObjectInputStream	ois;
	private ObjectOutputStream	oos;
	private int		account_number;
	private String	name;
	private HashMap<Integer, String>	onlineUsers;
	private HashMap<Integer, String>	offlineUsers;
	private List<Integer>				onlineIDs;
	private List<Integer>				offlineIDs;
	private String[] 					onlineArray;			//Updated online user list
	private String[]					offlineArray;			//Updated offline user list
	private boolean						reallyChangeName;
	private boolean						reallyChangePassword;
	private int[] 						onlineIDarray;
	private int[]						offlineIDarray;
	
	
	private Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
//---Load GUI objects
	private JButton			settingsButton		= new JButton("Settings");
	private JButton			clearButton			= new JButton("Clear Selections");
	private JPanel			leftPanel			= new JPanel();
	private JLabel			onlineLabel			= new JLabel("Online Users");
	private JLabel			offlineLabel		= new JLabel("Offline Users");
	private JPanel			listPanel			= new JPanel();
	private JList<String>	onlineList			= new JList<String>();
	private JList<String>	offlineList			= new JList<String>();
	private JScrollPane		onlineScrollPane	= new JScrollPane(onlineList);
	private JScrollPane		offlineScrollPane	= new JScrollPane(offlineList);
	
	
	private JPanel		chatPanel			= new JPanel();
	private JTextArea	messageTextArea		= new JTextArea();
	private JScrollPane	messageScrollPane	= new JScrollPane(messageTextArea);
	private String		messageTextFieldStr	= "Send Messages to Everyone"; // Instruction message
	private String		messageTextStr		= ""; // User-typed message
	private JTextField	messageTextField	= new JTextField(messageTextFieldStr);
	private JButton		sendButton			= new JButton("Send");
	
	private JFrame		mainWindow	= new JFrame("A Chat Room---Guangsen Ji@2020"); 
	private JSplitPane	splitPane	= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, chatPanel);
	private String		newLine		= System.lineSeparator();
	

//----Constructor 	
	public ClientFor_chat()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e)
		{
			errorWindow(dimension, e.toString());
		}
		
		// Build Main Window
		mainWindow.setSize(900,600);
		mainWindow.setMinimumSize(new Dimension(400, 300));
		mainWindow.setLocation(dimension.width/2-mainWindow.getSize().width/2, dimension.height/2-mainWindow.getSize().height/2);
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.getContentPane().add(splitPane);
		
		// Build Split Panel
		splitPane.setBackground(Color.BLACK);	
		splitPane.setDividerLocation(300);
		splitPane.setResizeWeight(0.1);
		splitPane.setOneTouchExpandable(false);
		splitPane.setEnabled(false);
		
		// Build Left Panel
		Color leftPanelBackgroundColor = Color.DARK_GRAY;
		Color leftPanelForegroundColor = Color.WHITE;
		
		leftPanel.setLayout(new BorderLayout());
		leftPanel.setMinimumSize(new Dimension(250, 250));
		leftPanel.setBackground(leftPanelBackgroundColor);
		leftPanel.add(settingsButton, BorderLayout.NORTH);
		leftPanel.add(listPanel, BorderLayout.CENTER);
		leftPanel.add(clearButton, BorderLayout.SOUTH);
		
		
		// Left List Panel
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setBackground(leftPanelBackgroundColor);
		listPanel.add(onlineLabel);
		listPanel.add(onlineScrollPane);
		listPanel.add(offlineLabel);
		listPanel.add(offlineScrollPane);
		
		onlineLabel.setBackground(leftPanelBackgroundColor);
		onlineLabel.setForeground(leftPanelForegroundColor);
		onlineLabel.setOpaque(true);
		onlineLabel.setFont(new Font("default", Font.ITALIC, 20));
		
		onlineScrollPane.setBackground(leftPanelBackgroundColor);
		onlineScrollPane.getVerticalScrollBar().setBackground(leftPanelBackgroundColor);
		onlineScrollPane.getVerticalScrollBar().setOpaque(true);
		onlineScrollPane.getHorizontalScrollBar().setBackground(leftPanelBackgroundColor);
		onlineScrollPane.getHorizontalScrollBar().setOpaque(true);
		
		onlineList.setBackground(leftPanelBackgroundColor);
		onlineList.setForeground(Color.LIGHT_GRAY);
		onlineList.setFont(new Font("default", Font.PLAIN, 20));
		onlineList.setLayoutOrientation(JList.VERTICAL);
		onlineList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		offlineLabel.setBackground(leftPanelBackgroundColor);
		offlineLabel.setForeground(leftPanelForegroundColor);
		offlineLabel.setOpaque(true);
		offlineLabel.setFont(new Font("default", Font.ITALIC, 20));
		
		offlineScrollPane.setBackground(leftPanelBackgroundColor);
		offlineScrollPane.getVerticalScrollBar().setBackground(leftPanelBackgroundColor);
		offlineScrollPane.getVerticalScrollBar().setOpaque(true);
		offlineScrollPane.getHorizontalScrollBar().setBackground(leftPanelBackgroundColor);
		offlineScrollPane.getHorizontalScrollBar().setOpaque(true);
		
		offlineList.setBackground(leftPanelBackgroundColor);
		offlineList.setForeground(Color.LIGHT_GRAY);
		offlineList.setFont(new Font("default", Font.PLAIN, 20));
		offlineList.setLayoutOrientation(JList.VERTICAL);
		offlineList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		// Build Right Panel
		JPanel textFieldPanel = new JPanel();
		
		chatPanel.setBackground(Color.LIGHT_GRAY);
		chatPanel.setLayout(new BorderLayout());
		chatPanel.add(messageScrollPane, BorderLayout.CENTER);
		chatPanel.add(textFieldPanel, BorderLayout.SOUTH);
		
		messageTextArea.setText("Select User(s) to Send Private Messages."+newLine+"Clear Selections to Send Public Messsages."+newLine);
		messageTextArea.setEditable(false);
		messageTextArea.setFont(new Font("default", Font.PLAIN, 15));
		messageTextArea.setLineWrap(true);
		messageTextArea.setWrapStyleWord(true);

		textFieldPanel.setBackground(Color.LIGHT_GRAY);	
		textFieldPanel.setLayout(new BoxLayout(textFieldPanel, BoxLayout.X_AXIS));
		textFieldPanel.add(messageTextField);
		textFieldPanel.add(sendButton);
		
		// Action Listeners
		settingsButton.addActionListener(this);
		clearButton.addActionListener(this);
		sendButton.addActionListener(this);
		messageTextField.addActionListener(this);
		// List Selection Listeners
		onlineList.addListSelectionListener(this);
		offlineList.addListSelectionListener(this);
		// Focus Listeners
		messageTextField.addFocusListener(this);
		mainWindow.setVisible(true);
		
		System.out.println("End of main constructor");
		
	}
	
	
	public void AfterLogin(Socket s, ObjectOutputStream oos, ObjectInputStream ois, int user_id, String name)
	{
		this.s = s;
		this.oos = oos;
		this.ois = ois;
		this.account_number = user_id;
		this.name = name;
		System.out.println(user_id + " " + name);
		new Thread(this).start();
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			try
			{
				String msg = (String) ois.readObject();
				String orgStr = (String) msg;
				int offset = orgStr.indexOf(":");
				String msgType = orgStr.substring(0, offset);		//message type
				String msgStr = orgStr.substring(offset+1);			//real message
				System.out.println("list:?" + msgType);
				if(msgType.equals("onlineList"))
				{
					if(UpdateUserList_online(msgStr)) System.out.println("onlineUser List has been updated!");
					else System.out.println("failed to update onlineUser List");
					
				}
				if(msgType.equals("offlineList"))
				{
					if(UpdateUserList_offline(msgStr)) System.out.println("offlineUser List has been updated!");
					else System.out.println("failed to update offlineUser List");

				}
				if(msgType.equals( "name"))
				{
					if(msgStr.equals("true"))  reallyChangeName = true; 
					else if(msgStr.equals("false"))  reallyChangeName = false; 
					else reallyChangeName = false;
					synchronized(this) {
						notifyAll();
					}
				}
				if(msgType.equals("password"))
				{
					if(msgStr.equals("true")) reallyChangePassword = true;
					else if(msgStr.equals("false")) reallyChangePassword = false;
					else reallyChangePassword = false;
					synchronized(this) {
						notifyAll();
					}
				}
				if(msgType.equals("getName"))
				{
					name = msgStr;
				}
				if(msgType.equals("private_sendMessage"))		//private message
				{
						System.out.println("pri:" + msgStr);
						int offsetForpri = msgStr.indexOf(":");
						String listPri = msgStr.substring(0,offsetForpri);
						String partOfRealMessage = msgStr.substring(offsetForpri+1);
						int offsetForname = partOfRealMessage.indexOf(":");
						String realMessage = partOfRealMessage.substring(0,offsetForname);
						String senderName = partOfRealMessage.substring(offsetForname+1);
						String message =senderName + "To:" + "(" + listPri + ")" +  newLine + realMessage + newLine;
						messageTextArea.append(message);
						messageTextArea.setCaretPosition(messageTextArea.getDocument().getLength());

				}
				if(msgType.equals("public_sendMessage"))				//public message
				{
					int offsetForname = msgStr.indexOf(":");
					String realMessage = msgStr.substring(0,offsetForname);
					String senderName = msgStr.substring(offsetForname+1);
					String message = senderName + ":" + newLine + realMessage+ newLine;
					messageTextArea.append(message);
					messageTextArea.setCaretPosition(messageTextArea.getDocument().getLength());
				}
				
			} catch (ClassNotFoundException e)
			{
				errorWindow(dimension, e.getMessage());
				return;
				
			} catch (IOException e)
			{
				errorWindow(dimension, "Internet connectio is distrupted, please log in again");
				return;
			}
			
			
			
			
		}
		
		
		
		
		
	}
	
	@Override
	public void actionPerformed(ActionEvent ae)
	{
		
		if (ae.getSource() == settingsButton) // Open Settings Module
		{
			new ClientFor_setting(this, dimension, account_number);
		}
		else if (ae.getSource() == clearButton) // Clear All User List Selections
		{
			onlineList.clearSelection();
			offlineList.clearSelection();
		}
		 else if (ae.getSource() == sendButton || ae.getSource() == messageTextField) // Send Message
		{
			String msgStr = messageTextStr;
			if (msgStr.isBlank()) return;
			String list = "";
			String request = "sendMessage:" + list + ":" + msgStr;
			if (onlineList.isSelectionEmpty() && offlineList.isSelectionEmpty()) // Public Messages
			{
				request = "sendMessage:" + list + ":" + msgStr;
				try
				{
					oos.writeObject(request);
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			else																//private message
			{
				int[] recipients = getSelectedUsers();
				if (recipients == null || recipients.length == 0) // Error in getSelectedUsers();
				{
					errorWindow(dimension, "Failed to Get Selected Users, Please Try Again.");
					return;
				}
				if (recipients.length == 1 && recipients[0] == account_number) return; // If only myself is selected
				for(int temp : recipients)
				{
					list = list.concat(String.valueOf(temp)).concat(",");
				}
				list = list.concat(String.valueOf(account_number)).concat(",");
				request = "sendMessage:" + list + ":" + msgStr;
				try
				{
					oos.writeObject(request);
					messageTextStr = "";
				} catch (IOException e)
				{
					errorWindow(dimension, "fail to send message, please try again");
				}
			} 
			 
			 
		}
		
	}
	

	@Override
	public void focusGained(FocusEvent fe)
	{
		if (fe.getSource() == messageTextField) // Text Field is Focused
		{
			messageTextField.setText(messageTextStr);
		}
		
	}


	@Override
	public void focusLost(FocusEvent fe)
	{
		if (fe.getSource() == messageTextField) // Text Field Lost Focus
		{
			// Save User-typed message
			if (!messageTextField.getText().equals(messageTextFieldStr)) messageTextStr = messageTextField.getText();
			messageTextField.setText(messageTextFieldStr);
		}
		
	}


	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		
		if (lse.getSource() == onlineList || lse.getSource() == offlineList) // Set instruction string on messageTextField
		{
			// Save User-typed message
			if (!messageTextField.getText().equals(messageTextFieldStr)) messageTextStr = messageTextField.getText();
			
			if (onlineList.isSelectionEmpty() && offlineList.isSelectionEmpty()) // Public Messages
			{
				messageTextFieldStr = "Send Messages to Everyone";
			} else // Private Messages
			{
				int[] recipients = getSelectedUsers();
				if (recipients == null || recipients.length == 0) // Error in getSelectedUsers();
				{
					errorWindow(dimension, "Failed to Get Selected Users, Please Try Again." );
					return;
				}
				if (recipients.length == 1 && recipients[0] == account_number) // If only self is selected
				{
					messageTextFieldStr = "Cannot Send Messages to Yourself";
				} else
				{
					messageTextFieldStr = "Send Messages to: " + Arrays.toString(recipients);
				}
			}
			messageTextField.setText(messageTextFieldStr);
		}
		
	}

	
//----Some API 
	
	
	public void getName()
	{
		String request = "getName:" + account_number;
		try
		{
			oos.writeObject(request);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	//get selected user list; 
	private int[] getSelectedUsers()
	{		
		int[] onlineIdx = onlineList.getSelectedIndices();
		int[] offlineIdx = offlineList.getSelectedIndices();
		int[] selectedIDs = new int[onlineIdx.length + offlineIdx.length];
		int i = 0;
		try {
			for (int idx : onlineIdx) selectedIDs[i++] = onlineIDarray[idx];
			for (int idx : offlineIdx) selectedIDs[i++] = offlineIDarray[idx];
		} catch (Exception e)
		{
			return null;
		}
		return selectedIDs;
	}
	
	
	// Check Format of new  name API
		public String checkNameFormat(String nickName)
		{
			nickName = nickName.trim();
			if (nickName.length() == 0)
			{
				errorWindow(dimension, "Please Enter Nick Name" + account_number);
				return null;
			} else if (nickName.length() > 30)
			{
				errorWindow(dimension, "Nick Name Cannot Exceed 30 Characters" + account_number);
				return null;
			}
			return nickName;
		}
		
	//Send name information to the server
		public boolean setNewName(String newName)
		{
			String request = "changeName:" + newName;
			try
			{
				oos.writeObject(request);
				try
				{
					synchronized(this) {
						wait();
					}
					
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				System.out.println("main:" + reallyChangeName);
				if(reallyChangeName) return true;
				else return false;
				
			} 
			catch (IOException e)
			{
				e.printStackTrace(); return false;
			} 
		}
	//Send password information to the server
		public boolean setNewPassword(String oldPassword, String newPassword)
		{
			String request = "changePassword:" + oldPassword + ":" + newPassword;
			try
			{
				oos.writeObject(request);
				try
				{
					synchronized(this) {
						wait();
					}
					
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				if(reallyChangePassword) return true;
				else return false;
				
			}
			catch (IOException e)
			{
				e.printStackTrace(); return false;
			} 
		}
		// Check format of new password API
		public String checkPasswordFormat(String pw1, String pw2)
		{
			if (pw1.length() < 6 || pw1.length() > 30)
			{
				errorWindow(dimension, "Password Length Must Be 6-30 Characters");
				return null;
			} else if (!pw1.equals(pw2))
			{
				errorWindow(dimension, "Passwords are Not Consistent");
				return null;
			}
			return pw1;
		}
		
		//--Update user List online
		public boolean UpdateUserList_online(String msgStr)
		{
			
		if(msgStr.isBlank())
		{
			onlineArray = new String[0];
			onlineList.setListData(onlineArray);
			return true;
		}
			
		try
		{	// String[] to be added to JList
			String[] onlineArray = msgStr.split("\\|");
			onlineIDarray = new int[onlineArray.length];
			int i=0;
			System.out.println("498" + Arrays.toString(onlineArray));
			for(String temp : onlineArray)
			{
				System.out.println("500" + temp);
				int offset = temp.indexOf(",");
				String id = temp.substring(0, offset);
				int user_id = Integer.parseInt(id);
				onlineIDarray[i++] = user_id;
				
				
		}
			// Update JList
			onlineList.setListData(onlineArray);
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
			
		}
		
		//--Update user List offline
		public boolean UpdateUserList_offline(String msgStr)
		{
			if(msgStr.isBlank())
			{
				offlineArray = new String[0];
				offlineList.setListData(offlineArray);
				return true;
			}
		try 
		{	
			String[] offlineArray = msgStr.split("\\|");
			offlineIDarray = new int[offlineArray.length];
			int i = 0;
			for(String temp : offlineArray)
			{
				int offset = temp.indexOf(",");
				String id = temp.substring(0, offset);
				int user_id = Integer.parseInt(id);
				offlineIDarray[i++] = user_id;
			}
			System.out.println("587:" + Arrays.toString(offlineIDarray));
			offlineList.setListData(offlineArray);
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		}
		
		
	//----The Error Window	
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
	
	
	
	


}
