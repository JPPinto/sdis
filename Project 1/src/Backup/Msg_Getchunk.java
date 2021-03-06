package Backup;


/**
 * SDIS TP1
 * <p/>
 * Eduardo Fernandes
 * José Pinto
 * <p/>
 * Backup.Msg_Getchunk class
 * <p/>
 * Syntax:
 * GETCHUNK <Version> <FileId> <ChunkNo> <CRLF><CRLF>
 */
public class Msg_Getchunk extends PBMessage {
    private byte[] data;
    private int chunkNo;

    // Received message constructor
    public Msg_Getchunk(byte[] inputData, int packetLenght) {
        super(GETCHUNK);
        receivedMessage = true;

        header = getHeaderFromMessage(inputData);

        // Decode header
        String[] splitHeader = header.split(" ");

        if (splitHeader.length == 4) {
            if (!splitHeader[0].equals(GETCHUNK)) {
                throw new IllegalAccessError("Invalid Message!");
            }

            if (!validateVersion(splitHeader[1])) {
                throw new IllegalAccessError("Invalid Message Version!");
            }

            if (!Utilities.validateFileId(splitHeader[2])) {
                throw new IllegalAccessError("Invalid Message file ID!");
            }

            if (!Utilities.validateChunkNo(Integer.parseInt(splitHeader[3]))) {
                throw new IllegalAccessError("Invalid Message chunk number!");
            }

            version = splitHeader[1];
            fileId = splitHeader[2];
            chunkNo = Integer.parseInt(splitHeader[3]);
        } else {
            throw new IllegalAccessError("Invalid Message!");
        }
    }

    // Message to be sent constructor
    public Msg_Getchunk(Chunk chunk) {
        super(GETCHUNK);
        receivedMessage = false;
        chunkNo = chunk.getChunkNo();
        fileId = chunk.getFileId();

        String[] stringArray = new String[4];
        stringArray[0] = GETCHUNK;
        stringArray[1] = version;
        stringArray[2] = chunk.getFileId();
        stringArray[3] = Integer.toString(chunkNo);

        data = constructHeaderFromStringArray(stringArray);
    }

    @Override
    public void saveChunk(String dir) {
    }

    @Override
    public int getIntAttribute(int type) {
        return chunkNo;
    }

    @Override
    public byte[] getData(int type) {
        return data;
    }
}
