// Guangsen Ji
//07/02/2020
//A full stack server
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;

//import firestore lib
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.cloud.firestore.Query.Direction;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

public class Server_Ji implements Runnable
{
	//----- SQL sPOOLatements
	private Connection connection;
	private PreparedStatement AddNewUser;
	private PreparedStatement DeleteUser;
	private PreparedStatement SaveUserMessage;
	private PreparedStatement UpdateUserPassword;
	private PreparedStatement UpdateUserName;
	private PreparedStatement UpdateAccountType;
	private Statement ForExecution;
	//-----Instance Variables
	private final ExecutorService threadPool;
	Firestore db;
	
	
	ServerSocket ss;
	// Collections
	ConcurrentHashMap<Integer, ObjectOutputStream> whosIn    = new ConcurrentHashMap<Integer, ObjectOutputStream>();
	HashSet<Integer> whosNotIn    = new HashSet<Integer>();
	ConcurrentHashMap<String, String>             passwords = new ConcurrentHashMap<String, String>();
	ConcurrentHashMap<String, Vector<Object>>  savedMessages = new ConcurrentHashMap<String, Vector<Object>>(); 
	
	
	//main function to load constructor
	public static void main(String[] args)
	{
		if (args.length != 0) System.out.println("Guangsen Ji Entered command line parameters are being ignored.");
		try {
			new Server_Ji();
		}
		catch(Exception e) 				//save system message in e
		{								//always use: IOexception, illegaleArgumentexception. 
			System.out.println(e.getMessage()); //compile time exception: force to use try catch if you want to compile. 
		}
	}
	
	public Server_Ji() 	throws DataBaseErrorException
	{
	//** */ build connection with firestore
		try
			{
			FirestoreOptions firestoreOptions =
			FirestoreOptions.getDefaultInstance().toBuilder()
				.setProjectId("public-chat-room-283800")
				.setCredentials(GoogleCredentials.getApplicationDefault())
				.build();
			db = firestoreOptions.getService();
			}
		catch(Exception e)
		{
			System.out.println("Failed to build connectio with firebase" + e.getMessage());
			throw new DataBaseErrorException(e.toString());
		}
	
	//Go through every documentation to read user data
	// ApiFuture<QuerySnapshot> query = db.collection("User_Information_Center").get();
	// QuerySnapshot querySnapshot = query.get();
	// List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
	// for (QueryDocumentSnapshot document : documents) {
	// 	System.out.println("User: " + document.getId());
	// 	System.out.println("First: " + document.getString("first"));
	// 	if (document.contains("middle")) {
	// 	  System.out.println("Middle: " + document.getString("middle"));
	// 	}
	// 	System.out.println("Last: " + document.getString("last"));
	// 	System.out.println("Born: " + document.getLong("born"));
	//   }
	

		
	//** build socket connection
		try {
			ss = new ServerSocket(5555);				//serversocket
			System.out.println("GuangsenJi's Lab5SavePrivateChatServer is up at " 
					+ InetAddress.getLocalHost().getHostAddress()
					+ " on port " + ss.getLocalPort());
		}
		catch(Exception e)
		{
			String errorMessage = "Port number " + ss.getLocalPort() + " is not available on this computer. "
					+ "Cancel the app currently using port " + ss.getLocalPort() + " and restart. " + e.getMessage();
			throw new IllegalArgumentException(errorMessage); 	// compile time exception, should catch it before compile
		}
		int numCores = Runtime.getRuntime().availableProcessors();
		threadPool = Executors.newFixedThreadPool(100);
		new Thread(this).start();
	}
	
	@Override
	public void run()
	{

		while(true)
		{
			try
			{
				Socket s = ss.accept();
				Running_Thread RT = new Running_Thread(this, s);
				threadPool.submit(RT);
			} 
			
			catch (IOException e)
			{
				System.out.println("server socket broken" + e.getMessage());
				return; 
			}	
			
		}

	}

//-----get the document ID by using account number;
	public String getDocumentID(int account_number) throws Exception
		{
		try
		{
			ApiFuture<QuerySnapshot> future = db.collection("User_Information_Center").whereEqualTo("ID", account_number).get();
			List<QueryDocumentSnapshot> documents = future.get().getDocuments();
			if(documents.size() == 0) throw new Exception("No useful user ID" + account_number);
			return documents.get(0).getId();
		}
		catch(Exception e)
		{
			System.out.println("failed to get document ID" + e.getMessage());
			return null;
		}
		}
	 
	

//--------add get a new account number for user
	public synchronized int getNewAccountNumber(String name, String password) throws DataBaseErrorException
	{
		int account_number = 0; 
		try
		{
			Query query = db.collection("User_Information_Center").orderBy("ID", Direction.DESCENDING).limit(1);
			int MAX_ID = ((Long) query.get().get().getDocuments().get(0).get("ID")).intValue();
			return MAX_ID + 1;
		}
		catch(Exception e)
		{
			System.out.println("Failed to obtain next userID");
			e.printStackTrace();
			return -1;
		}
		
	}

//------add New use to DB; 
	public synchronized int addNewUser(String name, String password)  throws DataBaseErrorException
	{
		int id = 0;
		try
		{
			int account_number = getNewAccountNumber(name, password);
			Map<String, Object> data = new HashMap<>();
			data.put("NAME", name);
			data.put("ID", account_number);
			data.put("PASSWORD", password);
			data.put("MESSAGE", "");
			data.put("ACCOUNT_TYPE", "VIP");
		//	data.put("TIME", 0);
			ApiFuture<DocumentReference> addedDocRef = db.collection("User_Information_Center").add(data);
			id = account_number;
			return account_number;
		}
		catch(Exception e)
		{
			System.out.println("fail to add new User" + e.getMessage());
		}
		System.out.println("A New User has been added to DB" );
		return -1;
	}

	//----get all users's account number and return a vector
	public Vector getAllUserID()
	{
		Vector userList = new Vector();
		try
		{
			ApiFuture<QuerySnapshot> future = db.collection("User_Information_Center").get();
			List<QueryDocumentSnapshot> documents = future.get().getDocuments();
			for (QueryDocumentSnapshot document : documents) {
				int user_id = document.getLong("ID").intValue();
				userList.add(user_id);
			}
			return userList;
		}
		catch(Exception e)
		{
			System.out.println("fail to get all use ID" + e.getMessage());
			return null;
		}
	}
	
	
	
	
//-------- setters & getters for each column in the DB table
	//set user name by using account_number and new_name
	public synchronized boolean SetUserName(int account_number, String new_name)
	{
		try
		{	
			String document_ID = getDocumentID(account_number);
			DocumentReference docRef = db.collection("User_Information_Center").document(document_ID);
			ApiFuture<WriteResult> future = docRef.update("NAME", new_name);
			return true;
		}
		catch(Exception e)
		{
			System.out.println("fail to set user name" + e.getMessage());
			return false;
		}
		
	}
	
	//get user name by using account_number
	public String getUserName(int account_number)
	{
		try
		{
			String name;
			String document_ID = getDocumentID(account_number);
			DocumentReference docRef = db.collection("User_Information_Center").document(document_ID);
			DocumentSnapshot document = docRef.get().get();
			name = document.getString("NAME");
			return name;
			
		} catch (Exception e)
		{
			System.out.println("fail to get user name" + e.getMessage());
			return null;
			
		}
		
	}
	public synchronized String getUserPassword(int account_number)
	{
		try
		{
			String password;
			String document_ID = getDocumentID(account_number);
			DocumentReference docRef = db.collection("User_Information_Center").document(document_ID);
			DocumentSnapshot document = docRef.get().get();
			password = document.getString("PASSWORD");
			return password;
		} catch (Exception e)
		{
			
			System.out.println("fail to get user password" + e.getMessage());
			return null;
		}
		
	}

	//set user password by account_number and password
	public synchronized boolean setUserPassword(int account_number, String password)
	{
		try
		{
			String document_ID = getDocumentID(account_number);
			DocumentReference docRef = db.collection("User_Information_Center").document(document_ID);
			ApiFuture<WriteResult> future = docRef.update("PASSWORD", password);
			return true;
		} catch (Exception e)
		{
			System.out.println("fail to  Update User Password" + e.getMessage());
			return false;
		}
		
		
	
	}
	// check if user type in the right passwod
	public synchronized boolean checkUserPassword(int account_number, String password)
	{
		String passwordDB = getUserPassword(account_number);
		if(!(passwordDB.equals(password))) return false;
		else return true;
		
	}
	
	// update the user passowrd to a new one
	public synchronized boolean updateUserPassword(int account_number, String old_password, String new_password)
	{
		String userPassword = getUserPassword(account_number);
		if(old_password != userPassword ) 
		{
			setUserPassword(account_number, new_password);
			System.out.println("user password has been updated to" + new_password);
			return true;
		}
		System.out.println("old_password might be wrong, check again");
		return false;
		
	}

	// delete the user document from the data base by using account_number
	public synchronized boolean deleteUser(int account_number)
	{
		
		try
		{
			String document_ID = getDocumentID(account_number);
			ApiFuture<WriteResult> writeResult = db.collection("User_Information_Center").document(document_ID).delete();
			return true;
		} catch (Exception e)
		{
			System.out.println("failed to delete the user " + e.getMessage());
			return false;
		}
		
	}
	public String getAccount_Type(int account_number)
	{ 
		try
		{
			String type;
			String document_ID = getDocumentID(account_number);
			DocumentReference docRef = db.collection("User_Information_Center").document(document_ID);
			DocumentSnapshot document = docRef.get().get();
			type = document.getString("ACCOUNT_TYPE");
			return type;
		}
		catch (Exception e)
		{
			System.out.println("failed to get account type " + e.getMessage());
			return null;
			
		}
	}

	public String setAccountType(int account_number)
	{
		String type;
		if(account_number < 1100) type = "VIP";
		else type = "NORMAL";
		try
		{
			String document_ID = getDocumentID(account_number);
			DocumentReference docRef = db.collection("User_Information_Center").document(document_ID);
			ApiFuture<WriteResult> future = docRef.update("ACCOUNT_TYPE", type);
			return type;
		} catch (Exception e)
		{
			System.out.println(" failed to set account type " + e.getMessage());
			return null;
		}
		
	}
	
	//get the message of account_number
	public String getUserMSGS(int account_number)
	{
		try
		{
			String MSGS;
			String document_ID = getDocumentID(account_number);
			DocumentReference docRef = db.collection("User_Information_Center").document(document_ID);
			DocumentSnapshot document = docRef.get().get();
			MSGS = document.getString("MESSAGE");
			return MSGS;
		} catch (Exception e)
		{
			System.out.println("failed to get messages for user id: " + account_number);
			return null;
		}
		
	}
	
	// save/set user message by using account_number and msg
	public boolean saveMessage(int account_number, String MSG)
	{
		String getMSGS = getUserMSGS(account_number);
		String toBeSaved = getMSGS.concat(MSG).concat("|"); 
		
		try
		{
			String document_ID = getDocumentID(account_number);
			DocumentReference docRef = db.collection("User_Information_Center").document(document_ID);
			ApiFuture<WriteResult> future = docRef.update("MESSAGE", toBeSaved);
			return true;
		} catch (Exception e)
		{
			System.out.println("failed to save messages " + account_number);
			return false;
		}
	}
	
	//get the time in each user's document
	public long getTime()
	{
		long SignUpTime = System.currentTimeMillis();
		return SignUpTime;
		
	}
	
//----check if user is online
	public void UserisOnline(int account_number, ObjectOutputStream oos)
	{
		if(UserisOnline(account_number))
			{ 
			whosIn.replace(account_number, oos); 
			System.out.println("successfully updated whosIn");
			}
		else
		{
			
			whosIn.put(account_number, oos);
			whosNotIn.remove(account_number);
			System.out.println("oneline user has been put in whosIn");
			System.out.println(whosIn);
		}
	}
	public boolean UserisOnline(int account_number)
	{
		if(whosIn.containsKey(account_number)) return true;
		else return false;
	}
//-----send messages 
	public synchronized void sendMessages(Vector<Integer>list, String msgs)
	{
		for(int id : list)
		{
			if(UserisOnline(id)) 
			{
				try
				{
					whosIn.get(id).writeObject(msgs);
				} catch (IOException e)
				{
					System.out.println("failed to send messages");
					return;
				}
				
			}
			else
			{		
				saveMessage(id, msgs);
				
				
			}
		}
		
	}
	
	public synchronized void sendToAll(String msgs)
	{
		for(ObjectOutputStream oos: whosIn.values())
		{
			try
			{
				oos.writeObject(msgs);
			} catch (IOException e)
			{
				System.out.println("failed to send messages");
				return;
			}
		}
		
		if(msgs.startsWith("onlineList:") || msgs.startsWith("offlineList:"))
		{
			return;
		}
		for(int t : whosNotIn)
		{
			saveMessage(t, msgs);
		}
		
	}
	public void sendList()
	{
		String online = "onlineList:";
		String offline = "offlineList:";
		for(int id : whosIn.keySet())
		{
			String oneGroup = id + "," + getUserName(id);
			online = online.concat(oneGroup).concat("|");
			System.out.println(online);
	
		}
		for(int id: whosNotIn)
		{
			String oneGroup = id + "," + getUserName(id);
			offline = offline.concat(oneGroup).concat("|");
			
		}
		System.out.println(offline);
		sendToAll(online);
		sendToAll(offline);
		
	}
	public synchronized void removeOnlineUser (int account_number)
	{
		whosIn.remove(account_number);
		whosNotIn.add(account_number);
		System.out.println("online user has been removed" + account_number);
	}
	
	
}

	




