/* Allow a user to read in a bitmap file and perform a sequence of modifications consisting of the following six operations, depending on the commands given by the user
 * Files: Manipulator.java, Bitmap.java, and Worker.java
 *
 * Author:	Josiah Buxton and Jamal Scott
 * Course:	CS221
 * Assignment:	Bitmap Men
 * Date:	Friday, September 19, 2014 at 11:59pm
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Manipulator {

	public static void main( String[] args )
	{
		//Declare Variables for user input
		Scanner sc = new Scanner(System.in);
		FileInputStream imgReader;		
		String imgName;
		String path;
		int threads;
		String input;
		char command;
		Bitmap img = null;
		double executionTime, start, stop;
		
		//Ask the user for img file
		System.out.print("What image file would you like to edit:  ");
	    	imgName = sc.nextLine();
	    	
		//Concatenate the current directory with the filename
		path = System.getProperty("user.dir") + '\\' + imgName;
	    	
	    //Check to make sure the file opens
	    try
	    {
	    	//Open the file
		    File imgSource = new File( path );
		    
		    //Use the scanner to read in the bytes
		    imgReader = new FileInputStream( imgSource );
		    
		    //Create a new instance of the bitmap class by calling the constructor
		    img = new Bitmap( imgReader );
		    
	    }
	    catch ( IOException e )
		{
	    	//Handle the error if the file is not found
			e.printStackTrace();
			System.err.println ("File " + imgName + " not found!");
			System.exit(1);
		}
	    
		//Ask the user for the number of threads
		System.out.print("How many threads would you like to use:  ");
			threads = sc.nextInt();
	  	   
		//Loop to edit the image with the various commands
		do
		{
			//Ask the user what command to perform
			System.out.print("What command would you like to perform (i, g, b, h, s, d, r, or q):  ");
				input = sc.next();
				command = input.charAt(0);
			
			//Check to see if the command is longer than 1 character, make the command an unrecognizable command if true
			if( input.length() > 1 || input.length() == 0 )
				command = 'z';
			
			
			//Return the lowercase version of command
			command = toLower( command );
	
			//Switch statement to perform selected command
			switch( command )
			{
				//Invert Colors
				case 'i':
					start = System.nanoTime();
					img.invertColors( threads );
					stop = System.nanoTime();
					executionTime = (stop - start)/1000000000;
					System.out.printf("Command took %.3f seconds to execute\n", executionTime);
					break;
					
				//Grayscale
				case 'g':
					start = System.nanoTime();
					img.grayscale( threads );
					stop = System.nanoTime();
					executionTime = (stop - start)/1000000000;
					System.out.printf("Command took %.3f seconds to execute\n", executionTime);
					break;
				
				//Blur 
				case 'b':
					start = System.nanoTime();
					img.blur( threads );
					stop = System.nanoTime();
					executionTime = (stop - start)/1000000000;
					System.out.printf("Command took %.3f seconds to execute\n", executionTime);
					break;
				
				//Horizontal Mirror
				case 'h':
					start = System.nanoTime();
					img.horizontalMirror( threads );
					stop = System.nanoTime();
					executionTime = (stop - start)/1000000000;
					System.out.printf("Command took %.3f seconds to execute\n", executionTime);
					break;
					
				//Shrink
				case 's':
					start = System.nanoTime();
					img.shrink( threads );
					stop = System.nanoTime();
					executionTime = (stop - start)/1000000000;
					System.out.printf("Command took %.3f seconds to execute\n", executionTime);
					break;
					
				//DoubleSize
				case 'd':
					start = System.nanoTime();
					img.doubleSize( threads );
					stop = System.nanoTime();
					executionTime = (stop - start)/1000000000;
					System.out.printf("Command took %.3f seconds to execute\n", executionTime);
					break;
					
				//Rotate
				case 'r':
					start = System.nanoTime();
					img.rotate( threads );
					stop = System.nanoTime();
					executionTime = (stop - start)/1000000000;
					System.out.printf("Command took %.3f seconds to execute\n", executionTime);
					break;
					
				case 'q':
					//Get the filename that will be outputted
					System.out.print("What do you want to name your new image file:  ");
					
					//Flush out the newline character before getting the name of the image file
					sc.nextLine();
					imgName = sc.nextLine();
					
					//Concatenate the current directory with the filename
					path = System.getProperty("user.dir") + '\\' + imgName;
					
					//Make sure the file does not exist already
					try {
						File editedBMP = new File( path );
						
						FileOutputStream imgWriter = new FileOutputStream( editedBMP );
						
						//Write the data to the img file
						img.writeHeader( imgWriter );
						img.writeColorData( imgWriter );
						
					} catch (IOException e) {
						e.printStackTrace();
						System.err.println ("Error saving the file " + imgName + "!");
						System.exit(1);
					}
					break;
				default:
					System.out.println("Invalid command!");
					break;
			}
			
		}while( command != 'q' );
		
		//Close the scanner object
		sc.close();
	  
	}
	
	/*
	 * Method to return the lowercase version of a character
	 */
	private static char toLower( char input )
	{
		if( input >= 'A' && input <= 'Z' )
			return (char) (input + 'a' - 'A');
		else
			return input;
	}
	
}
