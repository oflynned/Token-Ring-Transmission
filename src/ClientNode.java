import java.io.*;

import javax.swing.*;

import java.net.*;
import java.util.Random;

public class ClientNode extends Thread 
{
	//initialiser
	TokenRing tokenRing = new TokenRing();
	
	//listening socket
	private ServerSocket recv_socket; 
	//declare socket for frame sending
	private Socket send_socket; 
	//flag for token holder
	private boolean flag; 

	//node number
	private Integer this_node_num; 
	//name of node for exceptions
	private String node_name; 
		
	//string to build the input-file-{num} name
	private String f_input;
	//string to build the output-file-{num} name
	private String f_output; 
	
	private Random randomNode = new Random();
	
	//read in files
	private BufferedReader infile_read;
	
	//node constructor
	ClientNode(ServerSocket s_temp, int p_temp, boolean flag) throws IOException
	{
		// initialize client stuff
		this.this_node_num = new Integer(s_temp.getLocalPort()-GlobalDataStore.netport_base);
		this.node_name = new String("Node-");
		this.node_name += this.this_node_num.toString();
		
		//show curr node name
		setName(this.node_name);
		//server socket loop for nodes
		this.recv_socket = s_temp; 
		//do we have the token?
		this.flag = flag; 
		
		f_input = GlobalDataStore.infile_name + this.this_node_num.toString();	
		f_output = GlobalDataStore.outfile_name + this.this_node_num.toString();

		//build transmit socket for node
		try 
		{
			System.out.println(this.node_name+": client node: creating client socket.");
			this.send_socket = new Socket("localhost", p_temp);
		}
		catch(UnknownHostException host)
		//unable to find host?
		{
			System.err.println(node_name+": client node: Unknown Host used for client Socket.");
		}
		catch(IOException io)
		//io error?
		{
			System.err.println(node_name+": client node: IO error, client Socket.");
		}
		
		//generate frame
		generate_frames(f_input, p_temp);
		
		try
		{
			//input file for reading in
			this.infile_read = new BufferedReader(new FileReader("f_input.txt"));
		}
		//io error?
		catch (IOException io)
		{
			System.err.println(node_name + ": client node: " + io);
		}

		//if the node is the token holder, run the thread
		if (flag) run();
		else start();
	}
	
	public void run()
	{
		try
		{
			//initialise the token to transmit if we have the token
			if(this.flag) initialize_token(this.node_name);
			//else listen on the node
			else listen_state(this.node_name);
		}
		catch (IOException io)
		//io error?
		{
			System.err.println("thread run: IO Error, unknown");
		}
	}
	
	//clean up if exited
	public void exit()
	{	
		cleanup_node(this.node_name);
	}

	void cleanup_node(String node_name)
	{
		try
		{
			//close sockets and file io at end of its lifetime
			this.infile_read.close();
			this.recv_socket.close();
			this.send_socket.close();
		}
		catch (IOException io)
		//io error?
		{
			System.err.println(node_name+": cleanup_node: IO Error, Unknown");
		}
	}
	
	//2 states: receive frame OR pass frame
	void listen_state(String node_name)
	{
		//return to transmit state if in listen state and have the right to transmit
		if (this.flag)
		{
			System.out.println(node_name+": We still have the token!");
			transmit_state(node_name);
			return;
		}

		System.out.println(node_name+": Starting Listening State...");
		Socket conn = null;
		String data = null;
		Random rand = new Random();
		TokenFrame frame = new TokenFrame(node_name);
		
		try
		{
			//receive connection from neighbour before executing
			conn = this.recv_socket.accept();
			//fill buffer for connection
			BufferedReader si = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			data = si.readLine();
			
			if(data != null) 
			//process data if not null
			{
				System.out.println(node_name+": Intercepted packet...");
				//create token frame from incoming data
				frame.from_existing(data);
				
				//close connection after populating
				conn.close();

				if (frame.access_control().equals(0))
				{
					//access control packet allows to pass the token
					//move to transmit state with token from neighbour
					System.out.println(node_name+": Got Token!");
					this.flag = true;
					transmit_state(node_name);
				}
				else 
					//pass or keep
				{
					if (frame.dest().equals(this.this_node_num))
					//process if we're the recipient
					{
						//determine if frame is good or bad with status
						frame.set_frame_status();
						
						//drop if there's corruption
						if (frame.frame_status().equals(0))
							send_frame(node_name, frame);
						else
						{
							//keep if it's good
							save_frame_to_output(node_name, frame);
						}
					}
					else
					{
						//pass if we're not the recipient of the frame
						System.out.println(node_name+": We are not the recipent");
						if (frame.src().equals(this.this_node_num))
						{
							if (frame.frame_status().equals(1))
								System.out.println(node_name+": listen: draining frame");
							else 
							{
								/* if the frame status is 0, then we have an
								 * orphaned frame; right now we ignore this
								 * case
								 */
								System.out.println(node_name+": orphaned frame");
								
								if (this.flag)
								{
									send_frame(node_name, frame);
								}
								
							}
						}
						else {
							//pass if the source if not us
							System.out.println("pass sauce pls");
							send_frame(node_name, frame);
						}
					}
				}
			}
		}
		catch(IOException io)
		//buffered reader io error?
		{
			System.err.println(node_name+": listen state: IO Error, Buffered Reader");
		}

		//determine to continue or change state for the next node
		if (this.flag) {
			transmit_state(node_name);
		}
		else {
			listen_state(node_name);
		}
	}
	
	void transmit_state(String node_name)
	{
		//switch to listen state if we get out of transmission state
		if (!this.flag){
			System.out.println(node_name+": We Don't have the Token!");
			listen_state(node_name);
			return;
		}
		
		//create token for frame
		TokenFrame frame = new TokenFrame(node_name);
		Integer tht = new Integer(0);
		System.out.println(node_name+": transmit");
		try
		{
			if (this.infile_read.ready())
			{
				//read in data from file
				frame.from_input(this.infile_read.readLine());
				
				//calculate THT frame size
				tht = tht + frame.data_size();
				
				//check if the size is allowed
				if (tht > GlobalDataStore.tht_byte_count) //if yes
				{
					//release token to the next node and set listen state
					pass_token(node_name, frame);
					listen_state(node_name);
				}
				else //if no
				{
					//send frame
					send_frame(node_name, frame);
					
					//transmit
					transmit_state(node_name);
				}
			}
			else
			{
				//release token to neighbour and set listen state
				pass_token(node_name, frame);
				listen_state(node_name);
			}
		}
		catch (IOException io)
		{
			System.err.println(node_name+": transmit: infile_read, IO Error, Unknown");
		}
	}
	
    void send_frame(String node_name, TokenFrame frame)
	{
    	//check if we're passing the token to the next node
		if (frame.access_control().equals(0))
			//set flag to 0
			this.flag = false;
		
		System.out.println(node_name+": send: trying to send frame");
		try
		{
			//write frames received
			PrintWriter so = new PrintWriter(this.send_socket.getOutputStream(), true);
			//print frame
			so.println(frame.print());
		}
		catch(IOException io)
		{
			System.err.println(node_name+": send: IO Error, DataOutputStream");
		}
		System.out.println(node_name+": send: frame sent");
	}
	
	void pass_token(String node_name, TokenFrame frame)
	{
		//set access control to 0 as we pass to neighbour
		frame.set_access_control(0);

		//log
		frame.set_data("Pass Token");

		//size?
		frame.set_data_size();

		//send
		send_frame(node_name, frame);
	}
	
	void save_frame_to_output(String node_name, TokenFrame frame)
	{
		System.out.println(node_name+": saving frame to output");
		try
		{
			//open buffer for printing frame to output file
			PrintWriter outfile = new PrintWriter(new FileWriter("f_output.txt", true));
			
			//print to line
			outfile.println(frame.print());
			
			//close buffer
			outfile.close();
		}
		catch (IOException io)
		{
			System.err.println("save frame to file: outfile, IO error, Writes");
		}
		System.out.println(node_name+": saved frame to output");
	}
		
	void initialize_token(String node_name) throws IOException
	{
		//curr line from file
		String current_line;
		
		//create new token for object
		TokenFrame frame = new TokenFrame(node_name);

		try
		{
			//grab line and store
			current_line = this.infile_read.readLine();

			//if length is 0, populate the frame
			if (current_line.length() != 0) frame.from_input(current_line);

			//check the source of the frame
			if (frame.src().equals(this.this_node_num))
			{
				System.out.println(node_name+": send frame token");
				send_frame(node_name, frame);	
			}
		}
		catch(IOException io)
		{
			System.err.println("initialize_token: IO Error," + io + this.infile_read.toString());
			throw io;
		}
		
		//check to continue or switch states
		if (this.flag) transmit_state(node_name);
		else listen_state(node_name);
	}

	//generate frame
	void generate_frames(String str, int count) throws IOException
	{
		//if we're not the last frame to be created, we call ourself
		if (count >= 0) {
			generate_frames(str, count-1);
		}
		//create frame
		TokenFrame frame = new TokenFrame(node_name);
		
		Random rand = new Random();
		
		//send frame to random node
		Integer dest = rand.nextInt(5)+1;

		//set string
		frame.set_data(
			"From node "
			+this.this_node_num.toString()
			+" to node "
			+dest.toString()+" ");

		//generate token or data frame
		frame.set_access_control(1);

		//size
		frame.set_data_size();
		
		//dest
		frame.set_dest(dest);
		
		//src
		frame.set_src(this.this_node_num);

		//save each frame to file
		PrintWriter infile_write = new PrintWriter(new FileWriter("f_input.txt", true));
		infile_write.println(frame.print()); 
		infile_write.close(); 
	}
}
