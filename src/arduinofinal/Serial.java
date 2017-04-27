package arduinofinal;

import java.awt.Dimension;
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

import javax.swing.JButton;
import javax.swing.JFrame;
//import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import java.util.Scanner;
import java.util.Set;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;



public class Serial extends JPanel implements SerialPortEventListener{
	
	
	private static final long serialVersionUID = 199050153260062318L;
	SerialPort serialPort;
	// El puerto que sera utilizado normalmente.
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
	* El BufferedReader sera alimentado por un InputStreamReader
	* convertiendo los bytes en caracteres
	* asi mostrando los resultados independientemente.
	*/
	private BufferedReader input;
	/** El stream de salida para el puerto */
	private OutputStream output;
	/** Milisegundos para bloquear mientras 
	* se espera a que el puerto se abra
	*/
	private static final int TIME_OUT = 2000;
	/** Bits por default por segundo en el puerto COM */
	private static final int DATA_RATE = 9600;
	
	public void initialize() {
		
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
		
		//Primero, encontrar una instancia del puerto serial como fue puesto en PORT_NAMES
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
			System.out.println("Puerto COM no encontrado");
			return;
		}
	
		try {
			// abrir el puerto setial, y usar la clase name para appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
			TIME_OUT);
			
			// poner los parametros de los puertos
			serialPort.setSerialPortParams(DATA_RATE,
			SerialPort.DATABITS_8,
			SerialPort.STOPBITS_1,
			SerialPort.PARITY_NONE);
			
			// abrir los "streams"
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();
			
			// agregar listeners para los eventos
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			//System.err.println(e.toString());
		}
	}
	
	/**
	* Esta funcion se llama para cerrar el puerto cuando se
	* termina de usar.
	*/
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}
	
	/**
	* Obtiene un evento en el puerto serial. Lee la data y lo guarda
	* en la variable global "inputLine".
	*/
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				this.inputLine=input.readLine();
				this.asistenciaHash();
				//System.out.println(inputLine);
			} catch (Exception e) {
				//System.err.println(e.toString());
			}
		}
	}
	
	public synchronized void turnOnOff(String data){
        try{
            output.write(data.getBytes()); //Envía los datos por medio del Serial
        } catch(Exception e){
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
			//System.out.println(actualName);
			this.name.setText("Nombre: " + actualName);
			tempStore[1]="1";
			tabla.put(inputLine, tempStore);
			try {
				output.write("1".getBytes());
				Serial.this.anadirAlumno.setEnabled(false);
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
		else {
			try {
				Serial.this.name.setHorizontalAlignment(JTextField.CENTER);
				output.write("0".getBytes());
				Serial.this.anadirAlumno.setEnabled(true);
				Serial.this.name.setText("Escribir Nombre de nuevo Alumno");
				Serial.this.name.selectAll();
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
	}
	
	private void anadirHash(String name){
		/**
		 * Para cambiar si se quiere dar asistencia o no a un alumno al registrarlo,
		 * cambiar las "//" de uno u otro "tempStore" con 0 es sin asistencia y con
		 * 1 es con asistencia.
		 */
		String[] tempStore = {name, "0"};
		//String[] tempStore = {name, "1"};
		tabla.put(inputLine, tempStore);
	}
	
	//Atributos para botones
    private JButton save, saveAndExit, anadirAlumno;
    //private JLabel name;
    private JTextField name;
	
	public Serial() {
		super();
		this.setLayout(new GridLayout (0,1));
		this.setPreferredSize(new Dimension(800,200));
		this.save = new JButton("Guardar");
		this.saveAndExit = new JButton("Guardar y Salir");
		this.anadirAlumno = new JButton("Añadir Alumno");
		this.name = new JTextField("Nombre: ");
		this.add(this.name);
		/**
		 * La opcion para agregar alumnos desde la consola esta por 
		 * default desactivada, para utilizar remover las "//" de la 
		 * siguiente linea.
		 */
		this.add(this.anadirAlumno);
		this.anadirAlumno.setEnabled(false);
		this.add(this.save);
		this.add(this.saveAndExit);
		this.save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Serial.this.hashtodoc();
				} catch (IOException e1) {
					//e1.printStackTrace();
				}
			}});
		this.saveAndExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					Serial.this.hashtodoc();
				} catch (IOException e) {
					//e.printStackTrace();
				}
				System.exit(0);
			}
			
		});
		this.anadirAlumno.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				anadirHash(Serial.this.name.getText());
				Serial.this.anadirAlumno.setEnabled(false);
				Serial.this.name.setHorizontalAlignment(JTextField.LEFT);
				Serial.this.name.setText("Nombre: ");
				try {
					output.write("2".getBytes());
				} catch (IOException e1) {
					//e1.printStackTrace();
				}
			}
		});
	
		}
		
	public static void main(String[] args) throws Exception {
		 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		 Serial main = new Serial();
         main.initialize();
         main.doctohash();
         JFrame mainFrame = new JFrame();
         mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         mainFrame.setTitle("Asistencia de Alumnos");
         mainFrame.add(main);
         mainFrame.pack();
         mainFrame.setLocationRelativeTo(null);
         mainFrame.setVisible(true);
         
	}
}