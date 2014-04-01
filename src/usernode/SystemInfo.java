package usernode;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;
/**
 *This class acts as a container of all the information of the system.
 * 
 */
public class SystemInfo {
   
    private ArrayList<String> ips; //ip addresses of the nodes
    private ArrayList<Integer> udp_ports; //udp ports for contacting the new admin nodes
    private ArrayList<Integer> tcp_ports; //tcp ports for contacting the admin node
    private ArrayList<Boolean> alive; //true for a certain node if alive
    private int admin_id; //id of the admin
    /**
     *
     */
    public int num_nodes;  //number of nodes 
    private int startNode;
    /**
     *
     */
    public Boolean connectionOk; //true if the connection with the admin is up and running
    /**
     * Constructor of the SystemInfo class
     * @param filename string containing the path of the config file
     */
    public SystemInfo(String filename){
        
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
        
            line = br.readLine();
            this.num_nodes = Integer.parseInt(line);
         
            this.ips = new ArrayList<>(this.num_nodes);
            this.udp_ports = new ArrayList<>(this.num_nodes);
            this.tcp_ports = new ArrayList<>(this.num_nodes);
            this.alive = new ArrayList<>(this.num_nodes);
            
            br.readLine(); //line containing comment
            line = br.readLine();

            for (int i = 0; i< this.num_nodes; i++) {
                        
                String[] dic = line.split("\\|");
                
                this.ips.add(dic[1]);
                this.udp_ports.add(Integer.parseInt(dic[3]));
                this.tcp_ports.add(Integer.parseInt(dic[4]));
                this.alive.add(true);
                
                if ( dic[2].equals("admin") ){
                    this.admin_id = i;           
                }
                line = br.readLine();
            }         
            
            br.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SystemInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SystemInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    
        this.startNode = 0; //we need to modify this!!!!
        this.connectionOk = false;
        
    }
    
    /**
    * This methods returns a string containing the IP address of the node with 
    * identifier ID.
    * @param ID the identifier of the node
    * @return the IP address string of the node with Identifier ID
    * 
    */
    public String getIp(int ID){
        return this.ips.get(ID);
    }
    
    /**
    * This methods returns an integer containing the TCP port of the (eventual) server 
    * of the node with identifier ID.
    * @param ID the identifier of the node
    * @return the TCP port of the node with identifier ID 
    */
    public int getTCPPort(int ID){
        return this.tcp_ports.get(ID);
    }
    
    /**
    * This methods returns an integer containing the UDP port of the regular node 
    * with identifier ID. This is the port where the regular nodes wait for the 
    * message from the user to become new admin.
    * @param ID identifier of the node
    * @return the UDP port of the node with identifier ID
    */
    public int getUDPPort(int ID){
        return this.udp_ports.get(ID);
    }    
    
 /**
 * This methods returns a string containing the IP address of the admin node. 
 * @return the IP address string of the actual admin node
 */
    public String getAdminIp(){
        return this.ips.get(this.admin_id);
    }
    
    /**
    * This methods returns a integer containing the TCP port of the admin node. 
    * @return the TCP port of the actual admin node
    */
    public int getAdminTCPPort(){
        return this.tcp_ports.get(this.admin_id);
    }
 
    
    /**
     * This methods set the node with identifier ID dead in the list of available
     * nodes.
     * @param ID identifier of the node
     */
    public void setNodeDead(int ID){
        this.alive.set(ID, false);
    }
    
    /**
     * This methods set the node with identifier ID alive in the list of available
     * nodes.
     * @param ID identifier of the node
     */
    public void setNodeAlive(int ID){
        this.alive.set(ID, true);
    }    

    /**
     * This methods set the dead in the list of available nodes.
     */
    public void setAdminDead(){
        this.alive.set(this.admin_id, false);
    }
    
    
    /**
     *
     * @return
     */
    public Boolean allAlive(){
        for (int i=0; i<this.num_nodes; i++){
            if (this.alive.get(i) == false){
                return false;
            }
        }
                
    
        return true;
    }
    
    /**
     *
     * @return
     */
    public Boolean allDead(){

        for (int i=0; i<this.num_nodes; i++){
            if (this.alive.get(i) == true){
                return false;
            }
        }
                
    
        return true;
    }
    
    /**
     * This function randomly returns the identifier of one of the nodes that
     * is still alive
     * @return the id of one of the nodes that is still alive, if present, otherwise
     * -1
     */
    public int selectRandomNode(){
        
        if (this.allDead() == true){
            System.out.println("All dead!");
            return -1;        
        }
        System.out.println("");
        
        int numNodes = this.num_nodes;

        Random generator = new Random();
        int newId = generator.nextInt(numNodes);
        while (this.alive.get(newId) == false){
            newId = generator.nextInt(numNodes);
        }
        
        
        return newId;
    }
    
    /**
     * This function changes the identifier of the admin.
     * @param ID identifier of the new admin node
     */
    public void setNewAdmin(int ID){
        this.admin_id = ID;    
    }
    
    
    /**
    * This methods prints the content state of the nodes to screen.
    * 
    */    
    public void printState(){
        
        String state;
        
        for (int i=0; i<this.num_nodes; i++){
            if (this.alive.get(i)){
                state = "alive";
            }
            else{
                state = "dead";
            }
            System.out.print(i +":" + state + " " );
        
        }
    
        System.out.println("");
    }
    
    /**
    * This methods prints the content of SystemInfo to screen.
    * 
    */
    public void print(){
        System.out.println("System Info");
        for (int i=0; i<this.num_nodes; i++) {
            System.out.print(i + " ");
            System.out.print(this.ips.get(i) + " ");
            System.out.print(this.udp_ports.get(i) + " ");
            System.out.print(this.tcp_ports.get(i) + " ");
            System.out.print(this.alive.get(i) + " ");
            System.out.println();
        }
        System.out.println("Admin node ID: " + this.admin_id);
    
    }
    
    
}
