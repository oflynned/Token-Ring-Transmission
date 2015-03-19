import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.sql.Date;
import java.text.DateFormat;
import java.util.Locale;

import javax.swing.*;

public class TokenRing extends JFrame
{
	public JList list;
	public DefaultListModel model;	
	
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
            
            Locale locale = Locale.getDefault();
            Date date = new Date(arg0.getWhen());
            String tm = DateFormat.getTimeInstance(DateFormat.SHORT,
                    locale).format(date);
            String source = arg0.getSource().getClass().getName();
            int mod = arg0.getModifiers();

            if (!model.isEmpty()) {
                model.clear();
            }

            model.addElement("Time initiated: " + tm);

            
            //model.addElement("Source: " + source);

            //add text + var
            //StringBuffer buffer = new StringBuffer("Modifiers: ");
            //model.addElement(buffer);
            
            //add text output no vars
            //model.addElement("test");
            
            model.addElement("-----------------");
          
        }
    }        
	
	public static void main(String[] args) throws Exception
	{
		/*EventQueue.invokeLater(new Runnable(){
			@Override
			public void run(){
				TokenRing ex = new TokenRing();
				ex.setVisible(true);
			}
		});*/
		
		init();
	}

	public static void init() throws Exception{
		//create server sockets for each node 
		ServerSocket s1 = new ServerSocket(GlobalDataStore.netport_base+1);
		ServerSocket s2 = new ServerSocket(GlobalDataStore.netport_base+2);
		ServerSocket s3 = new ServerSocket(GlobalDataStore.netport_base+3);
		ServerSocket s4 = new ServerSocket(GlobalDataStore.netport_base+4);
		ServerSocket s5 = new ServerSocket(GlobalDataStore.netport_base+5);

		//create client nodes for the token ring
		//these wont have the initial token for sending
		//s1 contains the initial token
		ClientNode node2 = new ClientNode(s2, GlobalDataStore.netport_base+3, false);
		ClientNode node3 = new ClientNode(s3, GlobalDataStore.netport_base+4, false);
		ClientNode node4 = new ClientNode(s4, GlobalDataStore.netport_base+5, false);
		ClientNode node5 = new ClientNode(s5, GlobalDataStore.netport_base+1, false);
		
		//client node will be the initial holder of the token 
		ClientNode node1 = new ClientNode(s1, GlobalDataStore.netport_base+2, true);

		//check if node 1 is still alive and continue 
		while(node1.isAlive()){
			//
		}
		//on exit
		node2.exit();
		node3.exit();
		node4.exit();
		node5.exit();
	}
}
	


