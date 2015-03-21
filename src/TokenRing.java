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

public class TokenRing extends JFrame
{
	public JList list;
	public DefaultListModel model;		

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
	
	public TokenRing(){
		initUI();
	}
	
	private void initUI() {

        Container pane = getContentPane();
        GroupLayout gl = new GroupLayout(pane);
        pane.setLayout(gl);   

        model = new DefaultListModel<CharSequence>();
        list = new JList<CharSequence>(model);
        list.setMinimumSize(new Dimension(500, 300));
        list.setBorder(BorderFactory.createEtchedBorder());

        JButton startButton = new JButton("Start");
        startButton.addActionListener(new startAction());
        
        JButton quitButton = new JButton ("Quit");
        quitButton.addActionListener(new quitAction());
        
        JButton resetButton = new JButton ("Reset");
        resetButton.addActionListener(new resetAction());

        gl.setAutoCreateContainerGaps(true);
        
        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addComponent(startButton)
                .addComponent(resetButton)
                .addComponent(quitButton)
                .addGap(20)
                .addComponent(list)
        );

        gl.setVerticalGroup(gl.createParallelGroup()
                .addComponent(startButton)
                .addComponent(resetButton)
                .addComponent(quitButton)
                .addComponent(list)
        );
        
        pack();

        setTitle("CS3D3 Project 2 - Token Ring");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
	public class quitAction extends AbstractAction{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			System.exit(0);
		}
	}
	
	public class resetAction extends AbstractAction{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			model.clear();
		}
	}
	
    public class startAction extends AbstractAction {
        
        @Override
        public void actionPerformed(ActionEvent arg0){
            backgroundInit();
        }
    }   
    
    //multithreading for concurrent GUI behaviour with init()
    public Void backgroundInit(){
    	SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){

			@Override
			protected Void doInBackground() throws Exception {

	            if (!model.isEmpty()) {
	                model.clear();
	            }
	            
	            model.addElement("-----------------");
	            
				
				init();
				return null;
			}
			
			//safe end update for GUI
			@Override
			protected void done() {

				try {
					if(doInBackground()){
					model.addElement("thread done!");
					model.addElement("-----------------");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			//safe update during GUI thread
			@Override
			protected void process(){
				
			}
			
    	};
    	worker.execute();
		return null;
    }
    
    public void threadSleep(int milliseconds)
    {
        try
        {
            Thread.sleep(milliseconds);
        }
        catch(InterruptedException e)
        {
            System.out.println("Unexpected interrupt");
            System.exit(0);
        }
    }
	
	public static void main(String[] args) throws Exception
	{
		EventQueue.invokeLater(new Runnable(){
			@Override
			public void run(){
				TokenRing ex = new TokenRing();
				ex.setVisible(true);
			}
		});
		
		init();
	}

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
	


