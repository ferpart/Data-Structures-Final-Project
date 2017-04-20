package testing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.util.Enumeration;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class SerialTest extends JPanel implements SerialPortEventListener{
	SerialPort serialPort;
	/** The port we're normally going to use. */
	private static final String PORT_NAMES[] = {
		"/dev/tty.usbserial-A9007UX1", // Mac OS X
		"/dev/ttyACM0", // Raspberry Pi
		"/dev/ttyUSB0", // Linux
		"COM6", // Windows
	};
	/**
	* A BufferedReader which will be fed by a InputStreamReader
	* converting the bytes into characters
	* making the displayed results codepage independent
	*/
	private BufferedReader input;
	/** The output stream to the port */
	private OutputStream output;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;
	
	public void initialize() {
		// the next line is for Raspberry Pi and
		// gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
		// System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");
		
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
		
		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}
	
		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
			TIME_OUT);
			
			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
			SerialPort.DATABITS_8,
			SerialPort.STOPBITS_1,
			SerialPort.PARITY_NONE);
			
			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();
			
			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			//System.err.println(e.toString());
		}
	}
	
	/**
	* This should be called when you stop using the port.
	* This will prevent port locking on platforms like Linux.
	*/
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}
	
	/**
	* Handle an event on the serial port. Read the data and print it.
	*/
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				String inputLine=input.readLine();
				System.out.println(inputLine);
			} catch (Exception e) {
				//System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}
	
	public synchronized void turnOnOff(String data){
        try{
            output.write(data.getBytes()); //Env�a los datos por medio del Serial
            //System.out.println(data.getBytes());
        } catch(Exception e){
           // System.err.println(e.toString());
        }
     }
	
	//Atributos para botones
    private JButton on, off;
	
	public SerialTest() {
		super();
		this.setPreferredSize(new Dimension(200,100));
		this.on = new JButton("Registrado");
		this.off = new JButton("No Registrado");
		this.add(this.on);
		this.add(this.off);
		//this.off.setEnabled(false);
		this.on.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//SerialTest.this.off.setEnabled(true);
				//SerialTest.this.on.setEnabled(false);
				//Env�a 1 para encender
				SerialTest.this.turnOnOff("1");
			}});
		this.off.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//SerialTest.this.off.setEnabled(false);
				//SerialTest.this.on.setEnabled(true);
				//Env�a 0 para apagar
				SerialTest.this.turnOnOff("0");
			}});
		}
	
	public static void main(String[] args) throws Exception {
		 SerialTest main = new SerialTest();
         main.initialize();
         JFrame jf = new JFrame();
         jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         jf.add(main);
         jf.pack();
         jf.setVisible(true);
         System.out.println("Started");
	}
}