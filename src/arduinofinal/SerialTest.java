package arduinofinal;

//import java.awt.Component;
import java.awt.Dimension;
//import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.util.Enumeration;
import java.util.HashMap;

//import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import java.util.Scanner;
import java.util.Set;
//import java.util.concurrent.TimeUnit;
import java.io.FileReader;
import java.io.FileWriter;
//import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;



public class SerialTest extends JPanel implements SerialPortEventListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 199050153260062318L;
	SerialPort serialPort;
	/** The port we're normally going to use. */
	HashMap<String, String[]> tabla = new HashMap<String, String[]>(30); 
	private String inputLine="";
	private String actualName="";
	
		
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
				this.inputLine=input.readLine();
				this.asistenciaHash();
				System.out.println(inputLine);
			} catch (Exception e) {
				//System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}
	
	public synchronized void turnOnOff(String data){
        try{
            output.write(data.getBytes()); //Envía los datos por medio del Serial
            //System.out.println(data.getBytes());
        } catch(Exception e){
           // System.err.println(e.toString());
        }
     }
	
	private void doctohash() throws IOException{
		FileReader file = new FileReader("D:/ferpa/workspace/DataStructuresFinalProject/src/arduinofinal/lista.txt");
		BufferedReader br = new BufferedReader(file);
		Scanner in = new Scanner(br);
		in.nextLine();
		in.nextLine();
		in.nextLine();
		in.nextLine();
		while (in.hasNextLine()){
			String key=in.nextLine();
			String name=in.nextLine();
			String flag=in.nextLine();
			String value[]={name,flag};
			tabla.put(key, value);
		}
		in.close();
	}
	
	private void hashtodoc() throws IOException{
		FileWriter file = new FileWriter("D:/ferpa/workspace/DataStructuresFinalProject/src/arduinofinal/lista.txt");
		BufferedWriter bw = new BufferedWriter(file);
		Set<String> keylist = tabla.keySet();
		Object[] keyliststr = keylist.toArray();
		bw.write("Lista de alumnos organizada por:");
		bw.newLine();
		bw.write("1. Llave RFID");
		bw.newLine();
		bw.write("2. Nombre Alumno");
		bw.newLine();
		bw.write("3. Asistencia");
		bw.newLine();
		for(int i=0; i<tabla.size(); i++){
			String usedkey = (String) keyliststr[i];
			bw.write(usedkey);
			bw.newLine();
			bw.write(tabla.get(usedkey)[0]);
			bw.newLine();
			bw.write(tabla.get(usedkey)[1]);
			bw.newLine();
			
		}
		bw.close();
	}
	
	private void asistenciaHash(){
		if (tabla.containsKey(inputLine)){
			String[] tempStore= tabla.remove(inputLine);
			this.actualName=tempStore[0];
			System.out.println(actualName);
			this.name.setText("Nombre: " + actualName);
			tempStore[1]="1";
			tabla.put(inputLine, tempStore);
			try {
				output.write("1".getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		else {
			try {
				output.write("0".getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
	}
	
	//Atributos para botones
    private JButton save, saveAndExit;
    private JLabel name;
    
    /*private synchronized Component guitest(){
    	this.save= new JButton(actualName);
    	return this.test;
    	
    }*/
	
	public SerialTest() {
		super();
		this.setLayout(new GridLayout (0,1));
		this.setPreferredSize(new Dimension(200,100));
		this.save = new JButton("Guardar");
		this.saveAndExit = new JButton("Guardar y Salir");
		this.name = new JLabel("Nombre:", SwingConstants.CENTER);
		//this.actname=new JLabel("");
		//this.name.setFont(new Font("Verdana",1,15));
		this.add(this.name);
		//this.add(this.actname);
		this.add(this.save);
		this.add(this.saveAndExit);
		this.save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					SerialTest.this.hashtodoc();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}});
		this.saveAndExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					SerialTest.this.hashtodoc();
				} catch (IOException e) {
					//e.printStackTrace();
				}
				System.exit(0);
			}
			
		});
	
		}
		
	public static void main(String[] args) throws Exception {
		 SerialTest main = new SerialTest();
         main.initialize();
         main.doctohash();
         JFrame jf = new JFrame();
         jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         jf.add(main);
         //jf.add(main.guitest());
         jf.pack();
         jf.setVisible(true);
         //System.out.println("Started");
	}
}