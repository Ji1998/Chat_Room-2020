import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

public class Running_Thread implements Runnable
{
	ObjectInputStream ois	= null;
	ObjectOutputStream	oos = null;
	private final Server_Ji server;
	private final Socket s;
	private int account_number = -1;
	
	public Running_Thread(Server_Ji server, Socket s)
	{
	
		this.server = server;
		this.s = s;
	}

	@Override
	public void run()
	{
		try
		{
			ois = new ObjectInputStream(s.getInputStream());
			Object msg = ois.readObject();  
			oos = new ObjectOutputStream(s.getOutputStream());
			while(true)
			{
//----deal with MSG				
				String orgStr = (String) msg;
				int offset = orgStr.indexOf(":");
				String msgType = orgStr.substring(0, offset);		//message type
				String msgStr = orgStr.substring(offset+1);			//real message
				String reply;
				if(msgType.equals("login"))
				{
					int OffsetForJoin = msgStr.indexOf(" ");
					String str1 = msgStr.substring(0, OffsetForJoin);
					String password = msgStr.substring(OffsetForJoin+1);
					account_number = Integer.parseInt(str1);
					if(loginCheck(account_number, password))
					{
						String name = server.getUserName(account_number);
						server.UserisOnline(account_number, oos);
						reply = "true:" + name;
						oos.writeObject(reply);
						System.out.println("successfully log in"); 
						server.sendList();
						String note = server.getUserMSGS(account_number);
						System.out.println("55:" + note);
						String[] noteArray = note.split("\\|");
						for(String s : noteArray)
						{
							oos.writeObject(s);
						}
						
					}
					else
					{
					reply = "false:";
					oos.writeObject(reply);
					System.out.println("log in failed");
					}	
					
				}
				else if(msgType.equals("sendMessage"))
				{
					Vector<Integer> list = new Vector<>();
					int OffsetForMSGS = msgStr.indexOf(":");
					String str1 = msgStr.substring(0, OffsetForMSGS);
					String messages = msgStr.substring(OffsetForMSGS+1);
					if(!str1.isBlank())
					{
						Scanner sc = new Scanner(str1);
						sc.useDelimiter(",");
						while(sc.hasNextInt()) {
							list.add(sc.nextInt());
						}
						sc.close();
						server.sendMessages(list, "private_sendMessage:" + list + ":" + messages + ":"+ server.getUserName(account_number));
					}
					else
					{
						server.sendToAll("public_sendMessage:" + messages + ":" + server.getUserName(account_number));
						
						
					}
						
				}
				else if(msgType.equals("signUp"))
				{
					int OffsetForSignUp = msgStr.indexOf(" ");
					String name = msgStr.substring(0, OffsetForSignUp);
					String password = msgStr.substring(OffsetForSignUp+1);
					try
					{
						int id = server.addNewUser(name, password);
						if(id>0)
						{
							oos.writeObject("true:" + id);
							
						}
						else
						{
							oos.writeObject("false:" + id);
							
						}
					
					
					} catch (DataBaseErrorException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				else if(msgType.equals("changeName"))
				{
					
					String newName = msgStr;
					if(server.SetUserName(account_number, newName))
					{
						oos.writeObject("name:true");
						server.sendList();
						System.out.println("use name has been changed to " + newName);
					}
					else 
					{
						oos.writeObject("name:false");
						System.out.println("failed to change user name" + account_number);
						oos.writeObject("false:");
					}
					
				}
				else if(msgType.equals("changePassword"))
				{
					int OffsetForPassword = msgStr.indexOf(":");
					String oldPassword = msgStr.substring(0,OffsetForPassword);
					String newPassword = msgStr.substring(OffsetForPassword+1);
					if(server.checkUserPassword(account_number, oldPassword))
					{
						if(server.setUserPassword(account_number, newPassword))
						{
							oos.writeObject("password:true");
							System.out.println("successfully changed user password" + account_number);
						}
						else 
						{
							oos.writeObject("password:false");
							System.out.println("failed to change user password" + account_number);
						}
					}
					else
					{
						oos.writeObject("password:false");
						System.out.println("failed to change user password, worng old password" + account_number);
					}
				}
				else if(msgType.equals("OpenVIP"))
				{
					oos.writeObject("More function is on the way......");
				}
				else if(msgType.equals("getName"))
				{
					String name = server.getUserName(Integer.parseInt(msgStr));
					oos.writeObject("getNmae:" + name);
				}
				else System.out.println("Wrong message type has been sent" + account_number);
				
//----Next MSG	
				msg = ois.readObject();  
			}

		}
//---Processing leave
		catch (IOException e)
		{
			if(account_number < 0) return;
			else server.removeOnlineUser(account_number);
			System.out.println(account_number + "has offline");
			server.sendList();
		} 
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	//------- API for Client
		//log in
	private boolean loginCheck(int account_number, String password)
	{
		if(server.checkUserPassword(account_number, password)) return true;
		else return false;
	}
	
	
	

}
