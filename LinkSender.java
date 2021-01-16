
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import java.util.Random;

// LinkSender sends a message to LinkReceiver and receives a reply.
// LinkReceiver needs to be started before LinkSender.
public class LinkSender {

    static int senderPort = 3200;   // port number used by sender
    static int receiverPort = 3300; // port number used by receiver
    static Scanner console = new Scanner(System.in);

    public static void main(String args[]) throws Exception {

        //variables
        String trace;
        String error;
        String partFrame;
        String fileInput = "";
        String status = "";
        byte endSeq = 0;
        byte endLen = 0;
        byte[] sendingBuffer = new byte[16];
        byte[] frame = new byte[19];
        byte[] endFrame = new byte[19];
        byte[] badFrame = new byte[19];
        byte[] data = new byte[16];
        byte[] ackFrame = new byte[1];
        byte[] emptyData = new byte[16];
        int seqNum = 1;
        int count;
        int errorRate = 0;
        int damagedFrames = 0;
        int framesSent = 0;
        int randomNumGen;
        int nakCount = 0;
        int packetsRead = 0;
        int framesTransmitted = 0;
        int maxRetransmission = 0;
        double theoreticalFrames = 0;
        boolean errorFlag;
        boolean traceFlag;
        boolean loopFlag;
        Random random;

        //read file
        System.out.println("Enter input file name: ");
        fileInput = console.nextLine();
        FileInputStream infile = new FileInputStream(new File(fileInput));
        //FileInputStream infile = new FileInputStream(new File("mission2.txt"));

        //ask for trace option
        System.out.println("Would you like to enable trace? Y/N");
        trace = console.nextLine();
        if (trace.equalsIgnoreCase("Y")) {
            traceFlag = true;
        } else {
            traceFlag = false;
        }//end trace option if/else

        //ask for error rate option
        System.out.println("Would you like to enable error rate? Y/N");
        error = console.nextLine();
        if (error.equalsIgnoreCase("Y")) {

            //enable errorFlag
            errorFlag = true;

            //what is the error rate?
            System.out.println("What percentage is the error rate? 0-100");
            errorRate = console.nextInt();

            //ensure errorRate is inbounds
            while (errorRate > 100 || errorRate < 0) {
                System.out.println("Invalid entry. What percentage is the error rate? 0-100");
                errorRate = console.nextInt();
            }

        } else {
            errorFlag = false;
        }//end error rate option

        // Set up a link with source and destination ports
        Link myLink = new SimpleLink(senderPort, receiverPort);

        while ((count = infile.read(sendingBuffer)) != -1) {

            //increment packetsRead
            packetsRead++;
                
            //set flag for do while loop
            loopFlag = true;

            //creates a random number between 0-100 to test against errorRate
            random = new Random();
            randomNumGen = random.nextInt(101);
            System.out.println("original randomNumGen: " + randomNumGen);

            //do (while loopFlag = true)
            do {

                //for loop to clear frame
                for (int j = 0; j < frame.length; j++) {
                    frame[j] = 0;
                }

                //create frame
                frame = createFrame(seqNum, count, sendingBuffer);

                //set status to OK for trace option output
                status = "OK!";

                //damage frame if asked to
                if (errorFlag = true) {

                    //test errorRate vs randomNumGen and damage random byte in frame
                    if (errorRate >= randomNumGen) {

                        //call error function to damage the frame
                        frame = damageFrame(frame);

                        //set status to error for trace option output
                        status = "error";

                        //partFrame = message in frame
                        partFrame = new String(frame, 2, count);

                        // Print out the message sent
                        System.out.println("Message sent is: [" + partFrame + "]");

                        //increment count of damaged frames
                        damagedFrames++;

                        //try to change randomNumGen
                        random = new Random();
                        randomNumGen = random.nextInt(101);

                        //debugging
                        System.out.println("errorRate: " + errorRate + "\nrandomNumGen: " + randomNumGen);

                    } else {

                        //partFrame = message in frame
                        partFrame = new String(frame, 2, count);

                        // Print out the message sent
                        System.out.println("Message sent is: [" + partFrame + "]");

                    }//end if/else

                }//end if errorFlag

                // Send the message
                myLink.sendFrame(frame, frame.length);

                //trace option output
                if (traceFlag = true) {
                    System.out.println("Frame " + seqNum
                            + " transmitted, " + status);
                }

                //increment count for total number of frames sent
                framesTransmitted++;

                //debugging
                System.out.println("framesSent: " + framesSent);

                // Receive a message
                myLink.receiveFrame(ackFrame);

                //resend frame if damaged
                if (ackFrame[0] == 0) {

                    //reset nakCount on successful acknowledgement
                    nakCount = 0;

                    //switch seqNum before breaking do while loop
                    if (seqNum == 1) {
                        seqNum = 0;
                    } else {
                        seqNum = 1;
                    }

                    //break do while loop
                    loopFlag = false;

                    //print ACK if positive
                    System.out.println("ACK: " + ackFrame[0]);

                } else {

                    //print NAK if negative
                    System.out.println("NAK: " + ackFrame[0]);

                    //increment Negative ACK count 
                    nakCount++;

                    //keep track of maximum retransmissions
                    if (nakCount > maxRetransmission) {
                        maxRetransmission = nakCount;
                    }

                }//end if/else resend damaged frame

            } while (loopFlag);//end do-while loop

        }//end while
        
        //calculate theoretical total number of frames transmitted
        theoreticalFrames = ((double)packetsRead / (1.0 - ((double)errorRate / 100.0)));

        //create empty data frame to signal receiver to stop
        endFrame = createFrame(endSeq, endLen, emptyData);

        // Send the message
        myLink.sendFrame(endFrame, endFrame.length);

        //Output information
        System.out.println("Total number of packets read: " + packetsRead);
        System.out.println("Total number of frames transmitted: " + framesTransmitted);
        System.out.println("Theoretical total number of frames transmitted: " + theoreticalFrames);
        System.out.println("Total number of frames damaged: " + damagedFrames);
        System.out.println("Maximum number of retransmissions for any single frame: " + maxRetransmission);

        // Close the connection
        myLink.disconnect();
        System.exit(0);

    }//end main

    public static byte[] damageFrame(byte[] frame) {

        //variables for damageFrame method
        Random random = new Random();
        int randomNum;
        int randomNum2;
        int randomInt;
        int randomInt2;
        int randomOneOrTwo;

        //one or two errors
        randomOneOrTwo = random.nextInt(101);

        //25% chance of two errors 75% of one error
        if (randomOneOrTwo >= 75) {

            //256 for possible ASCII codes
            randomInt = random.nextInt(256);
            randomInt2 = random.nextInt(256);

            //18 for frame.length -1
            randomNum = random.nextInt(18);
            randomNum2 = random.nextInt(18);

            //damage random byte in frame
            frame[randomNum] = (byte) randomInt;
            frame[randomNum2] = (byte) randomInt2;
        } else {
            //256 for possible ASCII codes
            randomInt = random.nextInt(256);

            //18 for frame.length -1
            randomNum = random.nextInt(18);

            //damage random byte in frame
            frame[randomNum] = (byte) randomInt;
        }

        return frame;

    }//end damageFrame

    public static byte[] createFrame(int seqNum, int length, byte[] sendingBuffer) {

        //variables for createFrame method
        byte[] frame = new byte[19];

        //vvv create frame vvv
        //set first byte to seqNum
        frame[0] = (byte) seqNum;

        //set second byte to count
        frame[1] = (byte) length;

        //payload
        System.arraycopy(sendingBuffer, 0, frame, 2, length);

        //add checksum to frame
        frame[18] = CRC8.checksum(frame);
        System.out.println("checksum: " + frame[18]);
        //^^^ frame created ^^^

        return frame;

    }//end createFrame

}//end LinkSender
