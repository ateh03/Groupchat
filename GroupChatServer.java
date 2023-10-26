/*
 * Implementation of a groupchat server in Java
 * by Ali Jamil 2023
 */

// Package for I/O related stuff
import java.io.*;

// Package for socket related stuff
import java.net.*;

// Package for list related stuff
import java.util.*;

/*
 * This class does all the group chat server's job
 *
 * It consists of parent thread (code inside main method) which accepts
 * new client connections and then spawns a thread per connection
 *
 * Each child thread (code inside run method) reads messages
 * from its socket and relays the message to the all active connections
 *
 * Since a thread is being created with this class object,
 * this class declaration includes "implements Runnable"
 */
public class GroupChatServer implements Runnable
{
	// Each instance has a separate socket
	private Socket clientSock;

	// The class keeps track of active clients
	private static List<PrintWriter> clientList;

	// For reading messages from the keyboard
   	private BufferedReader fromUserReader;

   	// For writing messages to the socket
   	private static PrintWriter toSockWriter;

   // Constructor sets the socket for the child thread to process
    public GroupChatServer(Socket sock, BufferedReader reader, PrintWriter writer) {
 		fromUserReader = reader;
 		toSockWriter = writer;
    }
	// Constructor sets the socket for the child thread to process
	public GroupChatServer(Socket sock)
	{
		clientSock = sock;
	}

	// Add the given client to the active clients list
	// Since all threads share this, we use "synchronized" to make it atomic
	public static synchronized boolean addClient(PrintWriter toClientWriter)
	{
		return(clientList.add(toClientWriter));
	}

	// Remove the given client from the active clients list
	// Since all threads share this, we use "synchronized" to make it atomic
	public static synchronized boolean removeClient(PrintWriter toClientWriter)
	{
		return(clientList.remove(toClientWriter));
	}

	// Relay the given message to all the active clients
	// Since all threads share this, we use "synchronized" to make it atomic
	public static synchronized void relayMessage(
			PrintWriter fromClientWriter, String mesg)
	{
		// Iterate through the client list and
		// relay message to each client (but not the sender)
        for (PrintWriter client: clientList) {
            if (client != fromClientWriter) {
                client.println(mesg);
            }
        }
	}

	// The child thread starts here
	public void run()
	{	
		//establish clientName users will use 
		String clientName = null;

		// Read from the client and relay to other clients
		try {

			// Prepare to read from socket
            BufferedReader fromSockReader = new BufferedReader(
				new InputStreamReader(clientSock.getInputStream()));

			// Prepare to write to socket with auto flush on
            PrintWriter toSockWriter =
				new PrintWriter(clientSock.getOutputStream(), true);

			// Add this client to the active client list
            addClient(toSockWriter);

			// defines clientName from sock
            clientName = fromSockReader.readLine();

			//entry message
			System.out.println(clientName + " has rolled on in. Welcome!");

			// Keep doing till client sends EOF
			while (true) {
				// Read a line from the client
                String line = fromSockReader.readLine();

				// If we get null, it means client quit, break out of loop
                if (line == null) {
					// Tell user client quit
					System.out.println("*** Client closed connection");
					break;
				}

				// Else, relay the line to all active clients
                else {
                    relayMessage(toSockWriter, line); // comeback to this
                }
			}

			// Done with the client, remove it from client list
            removeClient(toSockWriter);
			//System.out.println(clientName + " has rolled on out. Adios!");
		}
		catch (Exception e) {
			System.out.println(e);
			System.exit(1);
			System.out.println(clientName + " has rolled on out. Adios!");
		}

		try {
			// Prepare to read from socket
			BufferedReader fromSockReader = new BufferedReader(
				new InputStreamReader(clientSock.getInputStream()));

			// Prepare to write to socket with auto flush on
			PrintWriter toSockWriter =
				new PrintWriter(clientSock.getOutputStream(), true);

			// Add this client to the active client list
			addClient(toSockWriter);
// Keep doing till user types EOF (Ctrl-D)
while (true) {
	// Read a line from the user
	String line = fromSockReader.readLine();

	// If we get null, it means EOF, so quit
	if (line == null) {
		System.out.println("*** Server closing connection");
		break;
	}
	// Write the line to the socket
	
	System.out.println("Received: " + line);

}
removeClient(toSockWriter);
}
catch (Exception e) {
System.out.println(e);
System.exit(1);
}

// End the other thread too
System.exit(0);
	}

	/*
	 * The group chat server program starts from here.
	 * This main thread accepts new clients and spawns a thread for each client
	 * Each child thread does the stuff under the run() method
	 */
	public static void main(String args[])
	{
		// Server needs a port to listen on
		if (args.length != 1) {
			System.out.println("usage: java GroupChatServer <server port>");
			System.exit(1);
		}

        clientList = new ArrayList<>();

        int serverPort = Integer.parseInt(args[0]);

		// Be prepared to catch socket related exceptions
		try {
			// Create a server socket with the given port
            ServerSocket serverSocket = new ServerSocket(serverPort);
            
			System.out.println("Waiting for a client ...");

			// Keep accepting/serving new clients
			while (true) {
                System.out.println("Waiting for a client ...");
                // System.out.println(this.myname);

				// Wait to accept another client
                Socket clientSock = serverSocket.accept();


				// Spawn a thread to read/relay messages from this client
                Thread child = new Thread(
					new GroupChatServer(clientSock));
			    child.start();
				//System.out.println(myname + "has joined");
			}
		}
		catch(Exception e) {
			System.out.println(e);
			System.exit(1);
		}
	}
}