import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Bitmap {

	//Variables for header data
	private int	offset;					// start of data from front of file, should be 54
	private int	width;					// width of image in pixels
	private int	height;					// height of image in pixels
	
	//Variables for other data in img file
	private byte[][] colorData;
	private byte[][][] colorData3D;
	private byte[][][] newColorData3D;
	private FileInputStream imgFileInputStream;
	private FileOutputStream imgFileOutputStream;
	
	/*
	 * Constructor for the header of the bmp file
	 */
	Bitmap( FileInputStream img ) throws IOException
	{
		
		imgFileInputStream = img;
		
		//Read in the header information and store them in the public variables
		readHeader();
		
		//Read in the color data into colorData array
		readColorData();
		
		//Convert the 2D array to a 3D array for use in the manipulations
		convert3D();
	}
	
	/*
	 * Method to read the header of the img file
	 */
	public void readHeader() throws IOException
	{	

		//Skip header variables until offset, width, and height
		imgFileInputStream.skip( 10 );
		
		offset = readInt();
		
		imgFileInputStream.skip( 4 );
		
		width = readInt();
		height = readInt();
		
		//Skip for the variables of type short
		imgFileInputStream.skip( 28 );
		
		
		//Check to make sure the header has been completely skipped,
		if( offset >= 54 )
			imgFileInputStream.skip( offset - 54 );

	}
	
	/*
	 * Method to read all of the color data from the image
	 */
	public void readColorData( ) throws IOException
	{	
		//This remainder signifies the amount of padding needed at the end of each row
		int remainder = width % 4;


		//Create a 2D array of bytes to hold the 24 bit, (8bit per color) color data
		colorData = new byte[height][width * 3];			// *3 for the three colors
		
		//Populate the colorData array with the data from the file
		for( int i = 0; i < height; ++i )
		{
			imgFileInputStream.read( colorData[i] );
			
			//Check to see if any extra padding bytes need to be ignored
			imgFileInputStream.skip( remainder );
		}	
		
		//Skip the last two bytes because bmp files always need to be evenly divisible by 4
		imgFileInputStream.skip( 2 );
			
	}
	
	/*
	 * Method to write the header to the new image
	 */
	public void writeHeader( FileOutputStream newImg ) throws IOException
	{
		imgFileOutputStream = newImg;							
		
		imgFileOutputStream.write( 'B' );				// Bitmap notation
		imgFileOutputStream.write( 'M' );
		writeInt( 54 + colorData.length + 2 );			// Total size of file
		writeInt( 0 );									// Reserved
		writeInt( offset );								// start of data from front of file, should be 54
		writeInt( 40 );									// size of header, always 40
		writeInt( width );								// width of image in pixels
		writeInt( height );								// height of image in pixels
		writeShort( 1 );								// planes in image, always 1
		writeShort( 24 );								// color bit depths, always 24
		writeInt( 0 );									// always 0		
		writeInt( colorData.length );					// size of color data in bytes
		writeInt( 72 );									// unreliable, use 72 when writing
		writeInt( 72 );									// unreliable, use 72 when writing
		writeInt( 0 );									// colors in palette, use 0 when writing
		writeInt( 0 );									// important colors, use 0 when writing
		
	}
	
	/*
	 * Method to write all of the color data to a new image file
	 */
	public void writeColorData( FileOutputStream newImg ) throws IOException
	{
		//Convert the 3D array back to a 2D array
		convert2D();
		
		//This remainder signifies the amount of padding needed at the end of each row
		int remainder = width % 4;
				
		//Go through the entire array and output data by creating integers from the bytes
		for( int i = 0; i < height; ++i )
		{
			
			newImg.write( colorData[i] );
			
			//Pad out the number of 0es to make the rows evenly divisible by 4
			while( remainder != 0 )
			{
				newImg.write( 0 );
				remainder --;
			}
			
			//Find remainder again
			remainder = width % 4;
		}
		
		//Pad out 2 0es after the color data, because of bmp file formatting
		newImg.write( 0 );
		newImg.write( 0 );
	}
	
	/*
	 * Method to read in 4 bytes from the fileinputstream and convert them to a single int
	 */
	public int readInt() throws IOException
	{
		int temp, temp1, temp2, temp3;	//Use to read the bytes and shift them to create a full int
		int returnedInt;
		temp = imgFileInputStream.read();
		temp1 = imgFileInputStream.read();
		temp2 = imgFileInputStream.read();
		temp3 = imgFileInputStream.read();
		
		//Combine the 4 bytes into an int (Assuming Little Endianness)
		temp1 <<= 8;
		temp2 <<= 16;
		temp3 <<= 24;
		returnedInt = temp3 | temp2 | temp1 | temp;
		
		return returnedInt;
	}
	
	/*
	 * Method to decompose short values and write 2 bytes to the fileoutputstream
	 */
	public void writeShort( int data ) throws IOException
	{
		int temp, temp1;
		
		//Combine the 2 bytes into an int (Assuming Little Endianness)
		temp1 = (data >> 8) & 0xFF;
		temp = data & 0xFF;
		
		//Write the bytes to the output stream
		imgFileOutputStream.write( temp );
		imgFileOutputStream.write( temp1 );
	}
	
	/*
	 * Method to decompose int values and write 4 bytes to the fileoutputstream
	 */
	public void writeInt( int data ) throws IOException
	{
		int temp, temp1, temp2, temp3;	
		
		//Combine the 4 bytes into an int (Assuming Little Endianness)
		temp3 = (data >> 24) & 0xFF;
		temp2 = (data >> 16) & 0xFF;
		temp1 = (data >> 8) & 0xFF;
		temp = data & 0xFF;
		
		//Write the bytes to the output stream
		imgFileOutputStream.write( temp );
		imgFileOutputStream.write( temp1 );
		imgFileOutputStream.write( temp2 );
		imgFileOutputStream.write( temp3 );
	}
	
	/*
	 * Method to convert the current 2D array to a 3D array
	 */
	public void convert3D()
	{
		//Allocate memory for the 3D array
		colorData3D = new byte[height][width][3];
		
		//Use this variable to decide whether to put the byte in which dimension of the new 3D array
		int BGR;
		
		//Go through each pixel in the array and split up the BGR values
		for( int i = 0; i < height; ++i )
		{
			for( int j = 0; j < width * 3; ++j )
			{
				BGR = j % 3;
				switch( BGR )
				{
					case 0:
						colorData3D[i][j/3][0] = colorData[i][j];
						break;
					case 1:
						colorData3D[i][j/3][1] = colorData[i][j];
						break;
					case 2:
						colorData3D[i][j/3][2] = colorData[i][j];
						break;
					default:
						System.out.println("Unknown Error!");
				}
			}
		}
	}
	
	/*
	 * Method to read all of the color data from the image
	 */
	public void convert2D()
	{
		//Allocate memory for the 2D array
		colorData = new byte[height][width*3];
		
		//Use this variable to decide whether to put the byte in which dimension of the new 3D array
		int BGR;
		
		//Go through each pixel in the array and split up the BGR values
		for( int i = 0; i < height; ++i )
		{
			for( int j = 0; j < width * 3; ++j )
			{
				BGR = j % 3;
				
				switch( BGR )
				{
					case 0:
						colorData[i][j] = colorData3D[i][j/3][0];
						break;
					case 1:
						colorData[i][j] = colorData3D[i][j/3][1];
						break;
					case 2:
						colorData[i][j] = colorData3D[i][j/3][2];
						break;
					default:
						System.out.println("Unknown Error!");
				}
			}
		}
	}
	
	/*
	 * Invert the colors of the bitmap. If the original color of a pixel was (R, G, B), the new color value should be (255 - R, 255 - G, 255 - B).
	 */
	public void invertColors( int threads )
	{
		Worker[] workers = new Worker[threads];
		for(int i = 0; i < workers.length; i++)
		{
			workers[i] = new Worker( 'i', colorData3D, i, threads );
			workers[i].start();
		}
		
		for(int i = 0; i < workers.length; i++)
		{
			try 
			{
				workers[i].join();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
				System.err.println("Thread Interrupted!");
			}
		}
	}
	
	/*
	 * Make the image grayscale. Set the R, G, and B values for a single pixel to one value. That value should be: .3R + .59G + .11B, rounded to the nearest integer.
	 */
	public void grayscale( int threads )
	{
		Worker[] workers = new Worker[threads];
		for(int i = 0; i < workers.length; i++)
		{
			workers[i] = new Worker( 'g', colorData3D, i, threads );
			workers[i].start();
		}
		
		for(int i = 0; i < workers.length; i++)
		{
			try 
			{
				workers[i].join();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
				System.err.println("Thread Interrupted!");
			}
		}
	}
	
	
	/*
	 * Blur the image. For each pixel, make each of its color components the weighted average of all the pixels in a two pixel radius.
	 */
	public void blur( int threads )
	{
		Worker[] workers = new Worker[threads];
		for(int i = 0; i < workers.length; i++)
		{
			workers[i] = new Worker( 'b', colorData3D, newColorData3D, i, threads );
			workers[i].start();
		}
		
		for(int i = 0; i < workers.length; i++)
		{
			try 
			{
				workers[i].join();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
				System.err.println("Thread Interrupted!");
			}
		}
	}
	
	/*
	 * Horizontally mirror the bitmap. Flip the pixels of the bitmap about the y-axis. 
	 */
	public void horizontalMirror(  int threads )
	{
		Worker[] workers = new Worker[threads];
		for(int i = 0; i < workers.length; i++)
		{
			workers[i] = new Worker( 'h', colorData3D, i, threads );
			workers[i].start();
		}
		
		for(int i = 0; i < workers.length; i++)
		{
			try 
			{
				workers[i].join();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
				System.err.println("Thread Interrupted!");
			}
		}
	}
	
	/*
	 * Shrink the bitmap to half its size in both height and width directions.
	 */
	public void shrink( int threads )
	{	
		Worker[] workers = new Worker[threads];
		for(int i = 0; i < workers.length; i++)
		{
			workers[i] = new Worker( 's', colorData3D, newColorData3D, i, threads );
			workers[i].start();
		}
		
		for(int i = 0; i < workers.length; i++)
		{
			try 
			{
				workers[i].join();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
				System.err.println("Thread Interrupted!");
			}
		}
	}
	
	/*
	 * Double the size of the bitmap in both height and width directions.
	 */
	public void doubleSize( int threads )
	{
		Worker[] workers = new Worker[threads];
		for(int i = 0; i < workers.length; i++)
		{
			workers[i] = new Worker( 'd', colorData3D, newColorData3D, i, threads );
			workers[i].start();
		}
		
		for(int i = 0; i < workers.length; i++)
		{
			try 
			{
				workers[i].join();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
				System.err.println("Thread Interrupted!");
			}
		}
	}
	
	public void rotate( int threads )
	{
		Worker[] workers = new Worker[threads];
		for(int i = 0; i < workers.length; i++)
		{
			workers[i] = new Worker( 'r', colorData3D, newColorData3D, i, threads );
			workers[i].start();
		}
		
		for(int i = 0; i < workers.length; i++)
		{
			try 
			{
				workers[i].join();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
				System.err.println("Thread Interrupted!");
			}
		}
	}
}