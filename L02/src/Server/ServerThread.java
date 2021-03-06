package Backup.Server;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * SDIS Lab 01
 * Eduardo Fernandes
 * José Pinto
 *
 * Server Thread
 *
 * register
 * to register the association of a plate number to the owner. Returns -1 if the plate
 * number has already been registered; otherwise, returns the number of vehicles in the database.
 *
 * lookup
 * to obtain the owner of a given plate number. Returns the owner's name or the string
 * NOT_FOUND if the plate number was never registered.
 */
public class ServerThread extends Thread {

    protected DatagramSocket socket = null;
    protected boolean serverIsRunning = false;

    private HashMap<String, String> dataBase = new HashMap<String, String>();

    public ServerThread() {

        try {
            //initializeConnection();
            socket = new DatagramSocket(60001);
        } catch (IOException e) {
            System.out.println("Cannot create server");
        }

        serverIsRunning = true;
    }

    public void run(){
        while(serverIsRunning) {
            try {
                byte[] buf = new byte[256];

                /* receive request */
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String input, response;
                input = new String(buf, "UTF-8");

                /* Split input */
                String[] commandArray = input.split(" ");
                int numberOfArgs = Array.getLength(commandArray);

                /* DEBUG */
                System.out.println("Input: " + input);
                System.out.println("Number of args: " + Integer.toString(numberOfArgs));
                /* DEBUG END */

                /* figure out response */
                switch (numberOfArgs){
                    case 2:
                        if (commandArray[0].equals("lookup")) {
                            /*Isolating plate number from unknown caracter from empty buf positions*/
                            commandArray[1] = commandArray[1].substring(0,8);
                            response = lookupPlate(commandArray[1]);
                            break;
                        }

                    case 3:
                        if (commandArray[0].equals("register")) {
                            commandArray[1] = commandArray[1].replace('_', ' ');
                            /*Isolating plate number from unknown characters from empty buf positions*/
                            commandArray[2] = commandArray[2].substring(0,8);
                            response = registerPlate(commandArray[2], commandArray[1]); //Order of arguments was wrong - changed
                            break;
                        }

                    default:
                        response = "INVALID_INPUT";
                        break;
                }

                /* DEBUG */
                System.out.println("Response: " + response);
                /* DEBUG END */

                byte[] buf_responce = response.getBytes();

                /* send the response to the client at "address" and "port" */
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf_responce, buf_responce.length, address, port);
                socket.send(packet);

            } catch (IOException e) {
                e.printStackTrace();
                serverIsRunning = false;
            }
        }

        closeConnection();
    }

    /**
     * Returns -1 if plate already exists, else returns the number of vehicles in the database.
     */
    private String registerPlate(String plate, String owner){
        if(dataBase.containsKey(plate))
            return "-1";
        dataBase.put(plate, owner);
        return Integer.toString(dataBase.size());
    }

    /**
     * Returns NOT_FOUND if plate doesn't exist, else returns the owners name.
     */
    private String lookupPlate(String plate){
        if(!dataBase.containsKey(plate))
            return "NOT_FOUND";
        String owner = dataBase.get(plate);
        return owner;
    }

    private void initializeConnection() throws IOException {
        socket = new DatagramSocket(60000);
    }

    private void closeConnection(){
        socket.close();
    }
}
