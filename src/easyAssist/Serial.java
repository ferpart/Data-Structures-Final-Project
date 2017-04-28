package easyAssist;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class Serial extends JPanel implements SerialPortEventListener{
	
	
	private static final long serialVersionUID = 199050153260062318L;
	SerialPort serialPort;
	
	/*
	 *  El puerto que sera utilizado normalmente.
	 */
	HashMap<String, String[]> tabla = new HashMap<String, String[]>(30); 
	
	/*
	 * Cambiar COM por el puerto que utiliize el arduino.
	 */
	private static final String PORT_NAMES[] = {
		"COM6", // Windows
	};
	
	/*
	* El BufferedReader sera alimentado por un InputStreamReader
	* convertiendo los bytes en caracteres
	* asi mostrando los resultados independientemente.
	*/
	private BufferedReader input;
	
	/* El stream de salida para el puerto */
	private OutputStream output;
	
	/* 
	* Milisegundos para bloquear mientras 
	* se espera a que el puerto se abra
	*/
	private static final int TIME_OUT = 2000;
	
	/* 
	 * Bits por default por segundo en el puerto COM 
	 */
	private static final int DATA_RATE = 9600;
	
	public void initialize() {
		
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
		
		/*
		 * Primero, encontrar una instancia del puerto serial como fue puesto en PORT_NAMES
		 */
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
			
			/*
			 * Abrir el puerto serial, y usar la clase name para appName.
			 */
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
			TIME_OUT);
			
			/*
			 *  Poner los parametros de los puertos
			 */
			serialPort.setSerialPortParams(DATA_RATE,
			SerialPort.DATABITS_8,
			SerialPort.STOPBITS_1,
			SerialPort.PARITY_NONE);
			
			/*
			 *  Abrir los "streams"
			 */
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();
			
			/*
			 *  Agregar listeners para los eventos
			 */
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}
	
	/*
	* Esta funcion se llama para cerrar el puerto cuando se
	* termina de usar.
	*/
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}
	
	/*
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
				System.err.println(e.toString());
			}
		}
	}
	
	/*
	 * Metodo utilizado para almacenar el documento txt en una hash en la cual
	 * las entradas se organiizan de la siguiente manera {llave, {nombre, bandera}} 
	 */
	private void doctohash() throws IOException{
		FileReader file = new FileReader("res/lista.txt");
		BufferedReader br = new BufferedReader(file);
		Scanner in = new Scanner(br);
		while (in.hasNextLine()){
			String key=in.nextLine();
			String name=in.nextLine();
			String flag=in.nextLine();
			if (Serial.this.resetAlumnos){
				flag="0";
			}
			String value[]={name,flag};
			tabla.put(key, value);
		}
		in.close();
	}
	
	/*
	 * Metodo utilizado para almacenar toda la hash en un documento txt.
	 */
	private void hashtodoc() throws IOException{
		FileWriter file = new FileWriter("res/lista.txt");
		BufferedWriter bw = new BufferedWriter(file);
		Set<String> keylist = tabla.keySet();
		Object[] keyliststr = keylist.toArray();
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
	
	/*
	 * Metodo utilizado para tomar la asistencia de los alumnos si es que la
	 * llave RFID utilizada existe.
	 */
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
				System.err.println(e.toString());
			}
		}
		else {
			try {
				/*
				 * Si es que la llave utilizada no existe se envia un 0 a el arduino
				 * para que este muestre un mensaje de "no existente"
				 */
				output.write("0".getBytes());
				/*
				 * Si es que la opcion para registrar nuevos alumnos esta habilitada,
				 * este if se encarga de habilitar el uso de el boton para añadir un
				 * nuevo usuario.
				 */
				if (Serial.this.registroAlumno){
					Serial.this.name.setHorizontalAlignment(JTextField.CENTER);
					Serial.this.anadirAlumno.setEnabled(true);
					Serial.this.name.setText("Escribir Nombre de nuevo Alumno");
					Serial.this.name.selectAll();
				}
			} catch (IOException e) {
				System.err.println(e.toString());
			}
		}
	}
	
	/*
	 * Metodo utilizado para agregar un nuevo nombre a el documento con
	 * el uso de la llave RFID.
	 */
	private void anadirHash(String name){
		String[] tempStore = {name, "0"};
		if (Serial.this.registrarEnCreacion){
			tempStore[1] = "1";
		}
		tabla.put(inputLine, tempStore);
	}
	
	/*
	 * Esta variable almacena la key de la ultima llave RFID utilizada
	 */
	private String inputLine="";
	
	/*
	 * Esta variable almacena el nombre que le corresponde a la llave 
	 * RFID si es que esta esta dentro de la hash
	 */
	private String actualName="";
	
	/*
	 * Atributos para botones
	 */
    private JButton save, saveAndExit, anadirAlumno;
    
    /*
     * private JLabel name;
     */
    private JTextField name;
    
	/*
	 * La opcion para agregar alumnos desde la gui esta por 
	 * default desactivada, para utilizar cambiar
	 * registroAlumno a "true".
	 */
	private Boolean registroAlumno = false;
	
	/*
	 * La opcion para automaticamente quitar todas las asistencias
	 * esta por default desactivada, para activar esta opcion, poner
	 * "true" en la variable resetAlumno
	 */
	private Boolean resetAlumnos = false;
    
	/*
	 * La opcion para automaticamente regisrar la asistencia de un alumno
	 * recientemente creado esta por default desactivada, para encender,
	 * cambiar la variable de registrarEnCreacion a "True"
	 */
	private Boolean registrarEnCreacion = false;
    
	/*
	 * Constructor de la clase utilizado para hacer la creacion de la gui
	 * con el uso de JPanel.
	 */
	public Serial() {
		super();
		this.setLayout(new GridLayout (0,1));
		this.setPreferredSize(new Dimension(800,200));
		this.save = new JButton("Guardar");
		this.saveAndExit = new JButton("Guardar y Salir");
		this.name = new JTextField("Nombre: ");
		this.add(this.name);
		
		/*
		 * Si es que se habilita lo opcion de añadir nuevos alumnos, 
		 * con esta opcion se añade el boton para hacer la incercion de 
		 * datos.
		 */
		if (registroAlumno){
			this.anadirAlumno = new JButton("Añadir Alumno");
			this.add(this.anadirAlumno);
			this.anadirAlumno.setEnabled(false);
			
			/*
			 * Action listener de el botom anadirAlumno que se encarga de
			 * guardar lo que fue insertado en el textfield como el nombre
			 * de el nuevo alumno.
			 */
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
						System.err.println(e1.toString());
					}
				}
			});
		}
		
		/*
		 * Se añaden los botones save y saveAndExit a la gui.
		 */
		this.add(this.save);
		this.add(this.saveAndExit);
		
		/*
		 * ActionListener de el boton save, que se encarga de registrar los
		 * cambios hechos al documento de texto.
		 */
		this.save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Serial.this.hashtodoc();
				} catch (IOException e2) {
					System.err.println(e2.toString());
				}
		}});
		
		/*
		 * Action listener de el boton saveAndExit, que se encarga de hacer
		 * lo miismo que el boton de save, pero cerrando el programa despues
		 * de usarlo.
		 */
		this.saveAndExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Serial.this.hashtodoc();
				} catch (IOException e3) {
					System.err.println(e3.toString());
				}
				System.exit(0);
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
        mainFrame.setTitle("Easy Assist");
        mainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("res/easyAssist.png"));
        mainFrame.add(main);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }
}