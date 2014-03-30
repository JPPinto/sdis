package Backup;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.util.*;

import static Backup.PeerThread.*;
import static Backup.PBMessage.*;

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
		}catch(InterruptedException e){
			System.out.println("Sleep Malfunction!");
			e.printStackTrace();
		}
	}

	public void handleProtocol(PBMessage msg) throws InterruptedException {

		if (msg.getType().equals(PUTCHUNK)) {
			System.out.println("FROM: " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " - " + msg.getType() + " " + msg.version + " " + msg.fileId + " " + msg.getIntAttribute(0) + " " + msg.getIntAttribute(1));
			msg.saveChunk(PotatoBackup.backupDirectory);

			//				rand.nextInt((MAX-MIN) + 1) + MIN;
			int randomNum = rand.nextInt((400 - 0) + 1);

			PBMessage message = new Msg_Stored(msg.fileId, msg.getIntAttribute(0));
			sleep(randomNum);
			sendRequest(message, addrs.get(SOCKET_MC), ports.get(SOCKET_MC));

		} else if (msg.getType().equals(DELETE)) {
			System.out.println("HANDLED DELETE!");
		} else if (msg.getType().equals(STORED)) {
			System.out.println("FROM: " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " - " + msg.getType() + " " + msg.version + " " + msg.fileId + " " + msg.getIntAttribute(0) + " " + msg.getIntAttribute(0));
		} else if (msg.getType().equals(REMOVED)) {
			System.out.println("HANDLED REMOVED!");
		} else if (msg.getType().equals(CHUNK)) {
			System.out.println("HANDLED CHUNK!");
		} else if (msg.getType().equals(GETCHUNK)) {
			System.out.println("HANDLED GETCHUNK!");
		}
	}
}