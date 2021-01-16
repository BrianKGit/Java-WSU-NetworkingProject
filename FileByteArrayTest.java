// This program copies infile.txt to outfile.txt by repeatedly 
// reading 16 bytes from infile.txt into a byte array and write 
// the bytes to outfile.txt.
//
// 10/18/2018 by Chi-Cheng Lin

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileByteArrayTest
{
	public static void main(String[] args) {
		byte[] data = new byte[16];
		int count;
		
		try {
			// create input and output streams
			FileInputStream infile = new FileInputStream(new File("infile.txt"));
			FileOutputStream outfile = new FileOutputStream(new File("outfile.txt"));

			// main loop of I/O
			// count is the number of bytes read
			// terminates when read() returns -1
			while ((count = infile.read(data)) != -1) {
				// print out number of bytes read (for debugging)
				System.out.println(count);
				// write bytes data[0..count-1]
				// count is needed as there might not be 16 bytes in the last read 
				outfile.write(data, 0, count);
			}
			
			// It's a good practice to close files/streams.
			infile.close();
			outfile.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
		} catch (IOException e) {
			System.out.println("IO exception!");
		}
	}
}