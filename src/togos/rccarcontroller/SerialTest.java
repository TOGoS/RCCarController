package togos.rccarcontroller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class SerialTest
{
    /** */
    public static class SerialReader implements Runnable 
    {
        InputStream in;
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }
        
        public void run ()
        {
            byte[] buffer = new byte[1024];
            int len = -1;
            try
            {
                while ( ( len = this.in.read(buffer)) > -1 )
                {
                    System.out.print(new String(buffer,0,len));
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }            
        }
    }

    public static class Piper implements Runnable 
    {
    	InputStream in;
        OutputStream out;
        
        public Piper( InputStream in, OutputStream out ) {
        	this.in = in;
            this.out = out;
        }
        
        public void run () {
            try {
            	byte[] buffer = new byte[32];
                int r = 0;
                while( (r = in.read(buffer)) > -1 ) {
                    out.write(buffer, 0, r);
                    out.flush();
                }                
            } catch ( IOException e ) {
                e.printStackTrace();
            }            
        }
    }
	
	public static void main( String[] args ) throws Exception {
		String portName = "COM10";
		
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if( portIdentifier.isCurrentlyOwned() ) {
            System.err.println("Error: Port is currently in use");
            return;
        }
        
        CommPort commPort = portIdentifier.open("RC Car Controller", 2000);
        if( !(commPort instanceof SerialPort) ) {
        	System.err.println("Error: Comm port's not a serial port, but a "+commPort.getClass());
        	return;
        }
        
        SerialPort serialPort = (SerialPort) commPort;
        new Thread(new Piper(System.in, serialPort.getOutputStream())).start();
        new Thread(new Piper(serialPort.getInputStream(), System.out)).start();
	}
}
