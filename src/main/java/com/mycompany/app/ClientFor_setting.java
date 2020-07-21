import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;





public class ClientFor_setting implements ActionListener
{

	// User Info
		private ClientFor_chat chatClient; 
		private Dimension	dim;
		private int			user_id;

		// Settings GUI Objects
		private JFrame			settingsWindow		= new JFrame("Chat Room - Settings"); 
		private JButton			myInfoButton		= new JButton("To be VIP!");
		private JTextArea		infoTextArea		= new JTextArea();
		private JPanel			resetPanel			= new JPanel();
		private JTextField		nickNameTextField	= new JTextField();
		private JButton			nickNameButton		= new JButton("Set My Nick Name");
		private JPasswordField	oldPwField			= new JPasswordField();
		private JPasswordField	newPw1Field			= new JPasswordField();
		private JPasswordField	newPw2Field			= new JPasswordField();
		private JButton			passwordButton		= new JButton("Set New Password");
		private JButton			removeButton		= new JButton("Delete My Account");
		private JButton			removeButton2		= new JButton("Confirm Delete My Account");
		private JButton			backButton			= new JButton("Back to Settings");
		
	
	public ClientFor_setting(ClientFor_chat chatClient, Dimension dim, int user_id)
	{
		this.chatClient = chatClient;
		this.dim = dim;
		this.user_id = user_id;
		settingsWindow.setTitle("Chat Room - " + user_id + " - Settings");
		
		resetPanel.setLayout(new GridLayout(10, 1));
		resetPanel.setBackground(Color.LIGHT_GRAY);
		
		resetPanel.add(myInfoButton);
		resetPanel.add(new JLabel("Set New Nick Name: "));
		resetPanel.add(nickNameTextField);
		resetPanel.add(nickNameButton);
		resetPanel.add(new JLabel("Enter Current Password: "));
		resetPanel.add(oldPwField);
		resetPanel.add(new JLabel("Enter New Password Twice: "));
		resetPanel.add(newPw1Field);
		resetPanel.add(newPw2Field);
		resetPanel.add(passwordButton);
		
		infoTextArea.setBackground(Color.LIGHT_GRAY);
		infoTextArea.setEditable(false);
		
		// Add Listeners
		myInfoButton.addActionListener(this);
		nickNameButton.addActionListener(this);
		passwordButton.addActionListener(this);
		removeButton.addActionListener(this);
		removeButton2.addActionListener(this);
		backButton.addActionListener(this);
		
		// Load Main Settings Window
		loadMain();
		
		// Set Window
		settingsWindow.setSize(300,400);
		settingsWindow.setResizable(false);
		settingsWindow.setLocation(dim.width/2-settingsWindow.getSize().width/2, dim.height/2-settingsWindow.getSize().height/2);
		settingsWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		settingsWindow.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == backButton) // Back to Main Window
		{
			loadMain();
		}
		else if(ae.getSource() == nickNameButton)
		{
			
			String newName = chatClient.checkNameFormat(nickNameTextField.getText());
			if(newName == null) return;
			System.out.println("100" + newName);
			if(chatClient.setNewName(newName))
			{
				loadInfo("Your new nick name: " + newName + " has been set.");
				nickNameTextField.setText("");
			}
			else
			{
				chatClient.errorWindow(dim, "Error when change user name, please try again");
				
			}
		}
		else if (ae.getSource() == passwordButton)
		{
			String oldPw = new String(oldPwField.getPassword());
			if (oldPw.isEmpty()) 
			{
				chatClient.errorWindow(dim, "You Must Enter Current Password to Set New Password.");
				return;
			}
			String pw1 = new String(newPw1Field.getPassword());
			String pw2 = new String(newPw2Field.getPassword());
			
			String newPw = chatClient.checkPasswordFormat(pw1, pw2);
			if (newPw == null) return;
			if(chatClient.setNewPassword(oldPw, newPw)) loadInfo("Your new password has been set. Please Login with New Password.");
			else
			{
				chatClient.errorWindow(dim, "Error when change your password, please try again");
				
			}
		}
		else if(ae.getSource() == myInfoButton)
		{
			loadInfo("More function is on the way......");
			
		}
		
	}
	
	// Load Main Settings Window
		private void loadMain()
		{
			settingsWindow.getContentPane().removeAll();
			
			settingsWindow.getContentPane().add(resetPanel, "Center");
			settingsWindow.getContentPane().add(removeButton, "South");
			
			settingsWindow.revalidate();
			settingsWindow.getContentPane().repaint();
		}
		// Load myInfo Window
		private void loadInfo(String info)
		{
			settingsWindow.getContentPane().removeAll();
			
			settingsWindow.getContentPane().add(backButton, "North");
			settingsWindow.getContentPane().add(infoTextArea, "Center");
			infoTextArea.setText(info);
			infoTextArea.setFont(new Font("default", Font.PLAIN, 20));
			infoTextArea.setLineWrap(true);
			infoTextArea.setWrapStyleWord(true);
			
			settingsWindow.revalidate();
			settingsWindow.getContentPane().repaint();
		}
	
	

}
