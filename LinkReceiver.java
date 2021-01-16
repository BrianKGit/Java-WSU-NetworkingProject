
import java.util.Scanner;
import java.io.File;
import java.io.FileOutputStream;

// LinkReceiver receives a message from LinkSender and replies.
// LinkReceiver needs to be started before LinkSender.
public class LinkReceiver {

    static int senderPort = 3200;   // port number used by sender
    static int receiverPort = 3300; // port number used by receiver
    static Scanner console = new Scanner(System.in);

    public static void main(String args[]) throws Exception {

        int lengthMessageToSend = 1;
        int lengthMessageReceived;
        String messageReceived;
        String trace;
        String outfileName;
        String status;
        byte ACK = 0;
        byte NAK = 1;
        byte checkSum;
        byte[] receivingBuffer = new byte[19];
        byte[] ackFrame = new byte[1];
        byte[] textMessageData = new byte[16];
        boolean flag = true;
        boolean traceFlag = false;

        // Set up a link with source and destination ports
        // Any 4-digit number greater than 3000 should be fine.
        Link myLink = new SimpleLink(receiverPort, senderPort);

        //write file
        System.out.println("Enter output file name:");
        outfileName = console.nextLine();
        FileOutputStream outfile = new FileOutputStream(new File(outfileName));

        //ask for trace option
        System.out.println("Would you like to enable trace? Y/N");
        trace = console.nextLine();
        if (trace.equalsIgnoreCase("Y")) {
            traceFlag = true;
        } else {
            traceFlag = false;
        }

        do {

            // Receive a message
            myLink.receiveFrame(receivingBuffer);
            lengthMessageReceived = receivingBuffer[1];

            //debugging
            System.out.println("Data length: " + lengthMessageReceived);

            if (lengthMessageReceived > 16) {
                lengthMessageReceived = 16;
            }
            if (lengthMessageReceived < 0) {
                lengthMessageReceived = 0;
            }

            //calculate checksum
            checkSum = CRC8.checksum(receivingBuffer);

            //print for debugging
            System.out.println("checkSum = " + checkSum);
            System.out.println("lengthMessageReceived: " + lengthMessageReceived);

            //remove message from payload into textMessageData
            System.arraycopy(receivingBuffer, 2, textMessageData, 0, lengthMessageReceived);

            //message in payload
            messageReceived = new String(textMessageData, 0, lengthMessageReceived);

            if (traceFlag = true) {

                System.out.println("Message received: [" + messageReceived + "]");
            }
            if (checkSum == 0) {

                //set status for trace option output
                status = "ok";

                //trace option output
                if (traceFlag = true) {
                    System.out.println("Checksum: " + checkSum + "\nFrame "
                            + receivingBuffer[0] + " received, " + status);
                }

                //check buffer index 1 for empty data
                if (receivingBuffer[1] == 0) {
                    
                    //set break flag to exit if all data in buffer is empty
                    boolean breakFlag = true;
                    
                    //for loop to check all indices in buffer
                    for(int i= 0; i < receivingBuffer.length; i++){
                        
                        //if any byte in buffer has data do not break do while loop
                        if(receivingBuffer[i] != 0){
                            breakFlag = false;
                            break;
                        }
                    }//end for loop
                    
                    //break do while loop if all bytes in buffer are 0
                    if(breakFlag == true){
                        flag = false;
                    }
                }

                //write file
                outfile.write(textMessageData, 0, lengthMessageReceived);

                // Prepare a message
                ackFrame[0] = ACK;

                // Send the message
                myLink.sendFrame(ackFrame, lengthMessageToSend);

                // Print out the message sent
                System.out.println("Message sent: [ACK]");

            } else {

                //set status for trace option output
                status = "error";

                //trace option output
                if (traceFlag = true) {
                    System.out.println("Checksum: " + checkSum + "\nFrame "
                            + receivingBuffer[0] + " received, " + status);
                }

                // Prepare a message
                ackFrame[0] = NAK;

                // Send the message
                myLink.sendFrame(ackFrame, lengthMessageToSend);

                // Print out the message sent
                System.out.println("Message sent is: [NAK]");

            }//end if/else checksum == 0

        } while (flag == true);//end do while loop

        //debugging
        System.out.println("Receiver Exit");

        // Close the connection
        outfile.close();
        myLink.disconnect();
        System.exit(0);

    }//end main

}//end LinkReceiver
