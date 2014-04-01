package usernode;

import java.io.Console;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UserCom extends Thread {


    private Socket socket;
    public DataInputStream in;
    public DataOutputStream out;
    private SystemInfo info;
    private StateUpdate stateUpdate;
/**
 * Constructor of UserCom object.
 * @param info contains the configuration info read from file
 */
    public UserCom(SystemInfo info) {
        this.info = info;
        this.connectToAdmin();
    }


/**
 * This function estabilished a TCP connection with the designed admin node.
 */    
    private void connectToAdmin(){
        
        try {
            this.socket = new Socket(this.info.getAdminIp(),this.info.getAdminTCPPort());
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (UnknownHostException ex) {
            System.out.println("UnknownHost Exception " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("IO Exception inside connectToAdmin " + ex.getMessage());
        }

        this.info.connectionOk = true;
    }
    
    
    /**
     * This function return the mean temperature as given by the admin node.
     * @return the mean temperature received from the admin node
     */    
    public Double getTemp(){
        Double Temp=0.0;
        try {
             out.writeUTF("Request temp");
             String data = in.readUTF();
             Temp = new Double(data);
        } catch (IOException ex) {
            System.out.println("I0 Exception " + ex.getMessage());
        }
        return Temp;

    }

    /**
     * This function sends the failure message to the admin node.
     */
    public void simulateFailure(){
        this.info.connectionOk = false;
        try {
             out.writeUTF("die");
        } catch (IOException ex) {
            System.out.println("I0 Exception " + ex.getMessage());
        }

    }
    
    
    /**
     * This function is used to keep a reference to the StateUpdate thread/class
     * @param state a reference to the StateUpdate thead/class
     */
    public void setState(StateUpdate state){
    
        this.stateUpdate = state;
    
    }

    /**
     * This function takes care of the admin node shifting. 
     * @return false if all the regular nodes are considered dead, true otherwise
     */
    public Boolean adminShifting() {

        
        //selecting the new admin
        this.info.setAdminDead();
        int newId = info.selectRandomNode();
        if (newId == -1){
            return false;
        }
        else {
        
            System.out.println("The new admin node has ID: " + newId);
        }
        this.info.setNewAdmin(newId);

        //closing the TCP connection with the old admin
        try {
            this.socket.close();
        } catch (IOException ex) {
            System.out.println("I0 Exception " + ex.getMessage());
        }

        // in the next section we send the admin message
        String message = "admin"; //modify this
        
        try {
            DatagramSocket aSocket = new DatagramSocket(8000);
            InetAddress serverIP = InetAddress.getByName(info.getAdminIp());
            byte[] m = message.getBytes();
            int serverPort = info.getUDPPort(newId);
            DatagramPacket request = new DatagramPacket(m, message.length(), serverIP, serverPort);
            aSocket.send(request);
            
            aSocket.setSoTimeout(1000);
            
            boolean noAnswer = true;
            
            // Until we don't receive an answer we keep sending the message
            // We assume that the regular node cannot die before becoming the
            // new admin
            while(noAnswer){
                System.out.println("No answer");
                byte [] rep = new byte[100];
                DatagramPacket reply = new DatagramPacket(rep,rep.length);
                try {
                    aSocket.receive(reply); //maybe we should check the message too
                    noAnswer = false;                
                }catch (SocketTimeoutException ex) {
                    try {
                        aSocket.send(request);
                    } catch (IOException ex1) {
                        System.out.println("IOException " + ex1.getMessage());
                    }
                
                } catch (IOException ex) {
                    System.out.println("IOException " + ex.getMessage());
                }
                
                
            }
            aSocket.close();

        } catch (SocketException ex) {
            System.out.println("Socket Exception " + ex.getMessage());
            ex.printStackTrace(new PrintStream(System.out));
        } catch (UnknownHostException ex) {
            System.out.println("UnknownHostException " + ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(UserCom.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(UserCom.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //we create the tcp connection with the newly elected admin
        this.connectToAdmin();
        

        return true; 
    }

    @Override
    public void run() {

        System.out.println("UserCom started");
        System.out.println("Commands available:");
        System.out.println("temp - temperature request");
        System.out.println("crash - crash message:");
        

        Scanner a = new Scanner(System.in);
        Boolean oneIsAlive = true;
        
        while (oneIsAlive) { 
            System.out.println("Insert Command: ");
            String s = a.nextLine();
            if (s.equals("temp")){
                Double temp = this.getTemp();
                System.out.println(temp.toString());
            }
            if (s.equals("crash")){
                System.out.println("Sending failure packet...");
                this.simulateFailure();
                System.out.println("Failure packet sent!");
                System.out.println("Begin of Admin node shifting procedure...");
                oneIsAlive = this.adminShifting();

            }

        }
        
        //if we reach this point all the nodes are considered dead
        System.out.println("All the nodes are considered dead");
        System.out.println("Exiting the program");
        stateUpdate.interrupt();
        
        

    }

}
