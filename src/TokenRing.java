import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.*;
import java.sql.Date;
import java.text.DateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class TokenRing
{		

	public static ServerSocket s1;
	public static ServerSocket s2;
	public static ServerSocket s3;
	public static ServerSocket s4;
	public static ServerSocket s5;
	public static ClientNode node1;
	public static ClientNode node2;
	public static ClientNode node3;
	public static ClientNode node4;
	public static ClientNode node5; 

	public static void init(){
		try{
			//create server sockets for each node 
			s1 = new ServerSocket(GlobalDataStore.netport_base+1);
			s2 = new ServerSocket(GlobalDataStore.netport_base+2);
			s3 = new ServerSocket(GlobalDataStore.netport_base+3);
			s4 = new ServerSocket(GlobalDataStore.netport_base+4);
			s5 = new ServerSocket(GlobalDataStore.netport_base+5);
	
			//create client nodes for the token ring
			//these wont have the initial token for sending
			//s1 contains the initial token
			node2 = new ClientNode(s2, GlobalDataStore.netport_base+3, false);
			node3 = new ClientNode(s3, GlobalDataStore.netport_base+4, false);
			node4 = new ClientNode(s4, GlobalDataStore.netport_base+5, false);
			node5 = new ClientNode(s5, GlobalDataStore.netport_base+1, false);
			
			//client node will be the initial holder of the token 
			node1 = new ClientNode(s1, GlobalDataStore.netport_base+2, true);
	
			while(node1.isAlive()){
			
			}
			node2.exit();
			node3.exit();
			node4.exit();
			node5.exit();
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}	
}
	


