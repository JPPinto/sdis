package Backup;

import java.io.File;
import java.net.DatagramPacket;
import java.util.Random;
import java.util.Vector;

import static Backup.Chunk.chunkFileExtension;
import static Backup.PBMessage.*;
import static Backup.PeerThread.*;


/**
 * Created by Jose on 29/03/2014.
 */
public class ProtocolHandler extends Thread {

    private Vector<String> addrs;
    private Vector<Integer> ports;
    private Random rand;
    private PBMessage message_to_be_handled;
    private DatagramPacket packet;

    public ProtocolHandler(Vector<String> a, Vector<Integer> p, PBMessage m, DatagramPacket pac) {
        addrs = a;
        ports = p;
        message_to_be_handled = m;
        rand = new Random();
        packet = pac;
    }

    public void run() {
        try {
            handleProtocol(message_to_be_handled);
        } catch (InterruptedException e) {
            System.out.println("Sleep Malfunction!");
            e.printStackTrace();
        }
    }

    public void handleProtocol(PBMessage msg) throws InterruptedException {

        if (msg.getType().equals(PUTCHUNK)) {
            System.out.println("FROM: " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " - " + msg.getType() + " " + msg.version + " " + msg.fileId + " " + msg.getIntAttribute(0) + " " + msg.getIntAttribute(1));
            msg.saveChunk(Utilities.backupDirectory);

            int randomNum = rand.nextInt((400 - 0) + 1);

            PBMessage message = new Msg_Stored(msg.fileId, msg.getIntAttribute(0));
            sleep(randomNum);
            sendRequest(message, addrs.get(SOCKET_MC), ports.get(SOCKET_MC));

        } else if (msg.getType().equals(DELETE)) {
            System.out.println("FROM: " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " - " + msg.getType() + " " + msg.fileId);

            File[] chunksInBackup = Utilities.listFiles(Utilities.backupDirectory);

            int deleted_chunks = 0;
            for (int i = 0; i < chunksInBackup.length; i++) {

                if (chunksInBackup[i].length() < 66)
                    continue;

                String temp_id = chunksInBackup[i].getName().substring(0, 64);
                if (temp_id.equals(msg.fileId)) {
                    //System.out.println("Chunk Nº: " + chunksInBackup[i].getName() + " DELETED");
                    chunksInBackup[i].delete();
                    deleted_chunks++;
                }
            }

            System.out.println("All chunks from file: " + msg.fileId + " were DELETED (" + deleted_chunks + " chunks deleted)!");
        } else if (msg.getType().equals(STORED)) {
            System.out.println("FROM: " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " - " + msg.getType() + " " + msg.version + " " + msg.fileId + " " + msg.getIntAttribute(0));
        } else if (msg.getType().equals(REMOVED)) {
			System.out.println("FROM: " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " - " + msg.getType() + " " + msg.version + " " + msg.fileId + " " + msg.getIntAttribute(0));

			LocalFile local_File = Backup.peer.getDataBase().getFiles().get(msg.fileId);

			if(local_File != null){
				int file_rep_degree = local_File.getReplicationDegree(), chunk_rep_degree = local_File.getChunks_rep().get(msg.getIntAttribute(0)) - 1;
				if(chunk_rep_degree < file_rep_degree){
					PBMessage temp_message = new Msg_Putchunk(new Chunk(msg.fileId, msg.getIntAttribute(0), Utilities.getThrash()), 2);
					sendRequest(temp_message, addrs.get(SOCKET_MDB),ports.get(SOCKET_MDB));
				}
			}

        } else if (msg.getType().equals(CHUNK)) {
            System.out.println("FROM: " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " - " + msg.getType() + " " + msg.version + " " + msg.fileId + " " + msg.getIntAttribute(0));

            msg.saveChunk(Utilities.backupDirectory);
        } else if (msg.getType().equals(GETCHUNK)) {
            System.out.println("FROM: " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " - " + msg.getType() + " " + msg.version + " " + msg.fileId + " " + msg.getIntAttribute(0));

            String filename = msg.fileId + "-" + msg.getIntAttribute(0) + chunkFileExtension;
            File[] chunksInBackup = Utilities.listFiles(Utilities.backupDirectory);
            Chunk chunk_to_send = null;

            for (int i = 0; i < chunksInBackup.length; i++)
                if (chunksInBackup[i].getName().equals(filename)) {
                    chunk_to_send = Chunk.loadChunk(chunksInBackup[i].getPath());
                    break;
                }


            if (chunk_to_send != null) {
                int randomNum = rand.nextInt((400 - 0) + 1);
                PBMessage temp_message = new Msg_Chunk(chunk_to_send);

                sleep(randomNum);
                sendRequest(temp_message, addrs.get(SOCKET_MDR), ports.get(SOCKET_MDR));
            } else
                System.out.println("Chunk No: " + msg.getIntAttribute(0) + " FileID: " + msg.fileId + " does NOT EXIST! Message Discarded.");
        }
    }
}
