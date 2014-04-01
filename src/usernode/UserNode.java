package usernode;


public class UserNode {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
        //We first read the config file
        SystemInfo info = new SystemInfo("config.txt");
        info.print();
        UserCom user = new UserCom(info);
        user.start();
        StateUpdate state = new StateUpdate(info,user);
        state.start();
        user.setState(state);
           
    }
}
