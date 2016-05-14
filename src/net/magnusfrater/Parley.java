package net.magnusfrater;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Parley extends JFrame implements FocusListener {
    private static final long serialVersionUID = 1L;

    //FORM COMPONENTS
    private final Dimension minSize = new Dimension(700,400);
    private final Insets jtfMargin = new Insets(0,3,1,5);
    //top
    private ButtonGroup bg;
    private JRadioButton jrbServer;
    private JRadioButton jrbClient;

    private JLabel jlHostIP;
    private JTextField jtfHostIP;

    private JLabel jlHostPort;
    private JTextField jtfHostPort;

    private JLabel jlName;
    private JTextField jtfUsername;

    private JCheckBox jcbSpeak;
    private JButton jbInfo;

    private JButton jbClientServerAction;
    //mid
    private JTextArea jtaDisplay;
    private JScrollPane jspDisplay;
    //bot
    private JTextField jtfInput;

    //NETWORKING
    BufferedReader in;
    PrintWriter out;

    private String hostIP;
    private int hostPort;
    private String username;
    private boolean listening;

    public Parley(){
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(minSize);
        setResizable(false);
        setLocationRelativeTo(null);
        setTitle("Parley: By Todd Griffin");
        setBackground(Color.WHITE);

        //init chat variables
        hostIP = "127.0.0.1";
        hostPort = 43336;
        username = "Anonymous";
        listening = false;

        initComponents(); //initializes all components for use
        initClientComponents(); //enables only client-needed components only

        setVisible(true); //everything is initialized, ready to be seen

        appendParleyMessage("Welcome to Parley, the simple instant messenger!"); //welcome message
    }

    private void initComponents(){
        //initialize panels
        JPanel top = new JPanel(new GridBagLayout());
        JPanel mid = new JPanel(new BorderLayout());
        JPanel bot = new JPanel(new BorderLayout());

        //TOP
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,0,0);

        bg = new ButtonGroup();

        gbc.gridx = 0; //server radio button
        gbc.gridy = 0;
        jrbServer = new JRadioButton("Server");
        jrbServer.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                initServerComponents();
            }
        });
        bg.add(jrbServer);
        top.add(jrbServer,gbc);

        gbc.gridx = 0;  //client radio button
        gbc.gridy = 1;
        jrbClient = new JRadioButton("Client");
        jrbClient.setSelected(true);
        jrbClient.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                initClientComponents();
            }
        });
        bg.add(jrbClient);
        top.add(jrbClient,gbc);

        gbc.gridx = 1; //host IP label
        gbc.gridy = 0;
        jlHostIP = new JLabel("Host IP: ");
        top.add(jlHostIP,gbc);

        gbc.gridx = 1; //host IP text field
        gbc.gridy = 1;
        jtfHostIP = new JTextField(hostIP);
        jtfHostIP.setMargin(jtfMargin);
        jtfHostIP.addFocusListener(this);
        jtfHostIP.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if (jtfHostIP.getText().equals("localhost")){
                    hostIP = "127.0.0.1";
                }else{
                    hostIP = jtfHostIP.getText();
                }

                jtfHostIP.setText(hostIP);
            }
        });
        top.add(jtfHostIP,gbc);

        gbc.gridx = 2; //host port label
        gbc.gridy = 0;
        jlHostPort = new JLabel("Host Port: ");
        top.add(jlHostPort,gbc);

        gbc.gridx  = 2; //host port text field
        gbc.gridy = 1;
        jtfHostPort = new JTextField(String.valueOf(hostPort));
        jtfHostPort.setMargin(jtfMargin);
        jtfHostPort.addFocusListener(this);
        jtfHostPort.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                hostPort = Integer.valueOf(jtfHostPort.getText());

                jtfHostPort.setText(String.valueOf(hostPort));
            }
        });
        top.add(jtfHostPort,gbc);

        gbc.gridx = 3; //username label
        gbc.gridy = 0;
        jlName = new JLabel("Username: ");
        top.add(jlName,gbc);

        gbc.gridx = 3; //username text field
        gbc.gridy = 1;
        jtfUsername = new JTextField(username);
        jtfUsername.setMargin(jtfMargin);
        jtfUsername.addFocusListener(this);
        jtfUsername.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                username = jtfUsername.getText();

                jtfUsername.setText(username);
            }
        });
        top.add(jtfUsername,gbc);

        gbc.gridx = 4; //speak check box
        gbc.gridy = 0;
        jcbSpeak = new JCheckBox("Speak");
        top.add(jcbSpeak,gbc);

        gbc.gridx = 4; //database info
        gbc.gridy = 1;
        jbInfo = new JButton("Info");
        top.add(jbInfo,gbc);

        gbc.gridx = 5; //client/server action button
        gbc.gridy = 1;
        jbClientServerAction = new JButton("JButton");
        jbClientServerAction.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if (jbClientServerAction.getText().equals("Start")){ //if server,
                    runServer();
                }
                if (jbClientServerAction.getText().equals("Stop")){
                    listening = false;
                }

                if (jbClientServerAction.getText().equals("Join")){ //if client, primed to attempt server connection
                    runClient();
                }
                if (jbClientServerAction.getText().equals("Leave")){ //if client, primed to attempt server disconnection
                    listening = false;
                }
            }
        });
        top.add(jbClientServerAction,gbc);

        //MID
        jtaDisplay = new JTextArea();
        jspDisplay = new JScrollPane(jtaDisplay);
        jtaDisplay.setEditable(false);
        jtaDisplay.setLineWrap(true);
        jtaDisplay.setMargin(new Insets(0,5,5,0));
        mid.add(jspDisplay);

        mid.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        //BOT
        jtfInput = new JTextField(); //user input text field
        jtfInput.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                if (listening)
                    out.println(jtfInput.getText());

                jtfInput.setText("");
            }
        });
        bot.add(jtfInput);

        bot.setBorder(BorderFactory.createEmptyBorder(0,5,5,5));

        //add to frame
        add(top, BorderLayout.NORTH);
        add(mid, BorderLayout.CENTER);
        add(bot, BorderLayout.SOUTH);

        pack();
    }

    protected void appendServerMessage(String message){
        String output = "";

        DateFormat df = new SimpleDateFormat("h:mm a"); //current time in hours:minutes AM/PM
        output += ("["+ df.format(new Date()) +"] "); //adds time message was received

        output += message; //adds message

        jtaDisplay.append(output +"\n");
    }
    protected void appendParleyMessage(String message){
        String output = "";

        DateFormat df = new SimpleDateFormat("h:mm a"); //current time in hours:minutes AM/PM
        output += ("["+ df.format(new Date()) +"] "); //adds time message was sent

        output += "<PARLEY>: "; //labels this as a PARLEY message

        output += message; //adds message

        jtaDisplay.append(output +"\n");
    }

    @Override
    public void focusGained(FocusEvent e) {
        //not used
    }
    @Override
    public void focusLost(FocusEvent e) {
        Component c = e.getComponent();

        if (c == jtfHostIP) //host IP text field
            jtfHostIP.setText(hostIP);

        if (c == jtfHostPort) //host port text field
            jtfHostPort.setText(String.valueOf(hostPort));

        if (c == jtfUsername) //username text field
            jtfUsername.setText(username);
    }

    private void initServerComponents(){
        listening = false; //server isn't in use, don't listen

        jrbServer.setEnabled(true); //allowed to specify if server
        jrbClient.setEnabled(true); //allowed to specify if client

        jtfHostIP.setEnabled(false); //server uses its own address
        jtfHostPort.setEnabled(true); //server can specify which port to listen
        jtfUsername.setEnabled(false); //servers don't need usernames

        jbClientServerAction.setEnabled(true); //allowed to attempt server startup
        jbClientServerAction.setText("Start"); //set text relevant to expected action

        jtfInput.setEnabled(false); //server cannot send own messages
    }
    private void initClientComponents(){
        listening = false; //client isn't in use, don't listen

        jrbServer.setEnabled(true); //allowed to specify if server
        jrbClient.setEnabled(true); //allowed to specify if client

        jtfHostIP.setEnabled(true); //need to specify server's address
        jtfHostPort.setEnabled(true); //need to specify server's port
        jtfUsername.setEnabled(true); //every user needs a username

        jbClientServerAction.setEnabled(true); //allowed to attempt server connection
        jbClientServerAction.setText("Join"); //set text relevant to expected action

        jtfInput.setEnabled(false); //client can't send data until connected to server
    }
    private void initListeningComponents(){
        listening = true; //client/server is ready to listen

        jrbServer.setEnabled(false); //cannot change to server while listening
        jrbClient.setEnabled(false); //cannot change to client while listening

        jtfHostIP.setEnabled(false); //cannot change host IP while listening
        jtfHostPort.setEnabled(false); //cannot change host port while listening
        jtfUsername.setEnabled(false); //cannot change username while listening

        jbClientServerAction.setEnabled(true); //allowed to attempt (leaving server) or (stopping server)
        if (jrbServer.isSelected())
            jbClientServerAction.setText("Stop"); //if server, allowed to stop server
        if (jrbClient.isSelected()){
            jbClientServerAction.setText("Leave"); //if client, allowed to leave server
            jtfInput.setEnabled(true); //if client, allowed to send data to server
        }
    }

    private void runClient(){
        initListeningComponents();

        Socket socket = null;

        try {
            socket = new Socket(hostIP, hostPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (listening){
                String line = in.readLine();

                if (line.startsWith("SUBMITNAME")) {
                    out.println(username);
                    System.out.println("submitting username");
                    //} else if (line.startsWith("NAMEACCEPTED")) { //test chat client code
                    //textField.setEditable(true); //test chat client code
                } else if (line.startsWith("MESSAGE")) {
                    appendParleyMessage(line.substring(8) + "\n");
                }

                System.out.println("listening...");
            }

            System.out.println("done.");

            socket.close();
            in.close();
            out.close();

        } catch (SocketException e){
            appendParleyMessage("SocketException: "+ e);
        } catch (IOException e){
            appendParleyMessage("IOException: "+ e);
        } catch (Exception e){
            appendParleyMessage("Exception: "+ e);
        } finally {
            initClientComponents();
        }
    }

    private void runServer(){
        initListeningComponents();

        ArrayList<ClientHandler> clients;

        ServerSocket listener = null;
        try {
            listener = new ServerSocket(hostPort);
        } catch (IOException ioe) {
            appendParleyMessage("IOException: "+ ioe);
        }
    }

    class ClientHandler implements Runnable { //handles client

        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket){
            this.socket = socket;
        }

        public void run(){

        }
    }

    public static void main(String[] args){
        Parley p = new Parley();
    }
}

//research runnable, threads, synchronized, synchronized variables