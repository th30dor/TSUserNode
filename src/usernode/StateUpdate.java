package usernode;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class takes care of updating the information regarding the nodes
 * that are considered alive.
 * 
 */
public class StateUpdate extends Thread {

    private SystemInfo info;
    private UserCom usercom;
    
    /**
     *Constructor of the StateUpdate class
     * @param info SystemInfo object containing the information of the system
     * @param usercom UserCom object that takes care of the communication with the
     * admin
     */
    public StateUpdate(SystemInfo info, UserCom usercom) {
        this.info = info;
        this.usercom = usercom;
    }

    /**
     * This function takes care of the requesting a state update to the admin node
     * and subsequently update the internal state representation.
     * 
     */
    private void update(){


        if (this.info.connectionOk){
                
             try {
                this.usercom.out.writeUTF("state");
                String data;
                data = this.usercom.in.readUTF();
                this.updateSystemInfo(data);
                
                
             } catch (IOException ex) {
                 System.out.println("IO Exception " + ex.getMessage());
             }
                
         }
    
    }
    
        /**
     * This function takes care of parsing the state message sent by the admin and
     * update the internal representation accordingly.
     * 
     */
    private void updateSystemInfo(String data) {
    
        for(int i=0; i<this.info.num_nodes; i++) {
        
            char val = data.charAt(i);
            if (val != '0'){
                 this.info.setNodeAlive(i);
            }
        }
    }
    
    @Override
    public void run() {
    
        //the amoun of millisecond we should wait before asking for the state
        int waitTime = 5000; 
        System.out.println("System Update started");
        
        while(!this.isInterrupted()){
            try {
                
                Thread.sleep(waitTime);
            } catch (InterruptedException ex) {
                break;
            }
            
            if (this.info.allAlive() == false) 
            {
                this.update();
                //this.info.printState();
            }
        }
    }
}
