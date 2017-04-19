#include <LiquidCrystal.h>
#include <SPI.h>
#include <MFRC522.h>

#define RST_PIN  9    //Pin 9 para el reset del RC522
#define SS_PIN  10   //Pin 10 para el SS (SDA) del RC522
MFRC522 mfrc522(SS_PIN, RST_PIN); //Creamos el objeto para el RC522

//Crear el objeto LCD con los números correspondientes (rs, en, d4, d5, d6, d7)
LiquidCrystal lcd(8, 3, 4, 5, 6, 7);
byte key[4]= {};
//String save="";
void setup() {
  Serial.begin(9600); //Iniciamos la comunicación  serial
  SPI.begin();        //Iniciamos el Bus SPI
  pinMode(2, OUTPUT);
  mfrc522.PCD_Init(); // Iniciamos  el MFRC522
  Serial.println("Lectura del UID");
  // Inicializar el LCD con el número de  columnas y filas del LCD
  lcd.begin(16, 2);
  // Escribimos el Mensaje en el LCD.
  //lcd.print("Veamos anime");
  
}

void loop() {
  // Revisamos si hay nuevas tarjetas  presentes
  
  if ( mfrc522.PICC_IsNewCardPresent()) 
        {  
      digitalWrite(2, HIGH);
      //Seleccionamos una tarjeta
            if ( mfrc522.PICC_ReadCardSerial()) 
            {
                  
                  // Enviamos serialemente su UID
                  Serial.print("Card UID:");
                  for (byte i = 0; i < mfrc522.uid.size; i++) {
                          Serial.print(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " ");
                          Serial.print(mfrc522.uid.uidByte[i], HEX);
                          key[i]=mfrc522.uid.uidByte[i];
                  } 
                  //Serial.print(save);
                  Serial.println();
                  // Terminamos la lectura de la tarjeta  actual
                  mfrc522.PICC_HaltA();         
            }      
  }
  digitalWrite(2, LOW); 
   // Ubicamos el cursor en la primera posición(columna:0) de la segunda línea(fila:1)
  //lcd.setCursor(0, 1);
   // Escribimos el número de segundos trascurridos
  //lcd.print(millis()/1000);
  lcd.setCursor(3, 0);
  lcd.print(displayKey(key));
  //delay(100);
}

String displayKey(byte key[]){
  String hexa="";
  String save="";
  for (int i = 0; i<4; i++){
    if (i>0){
      save+=" ";
      hexa= String(key[i], HEX);
      save+=hexa;
    }
    else{
      hexa= String(key[i], HEX);
      save+=hexa;  
    }
  }
  return save;
}

String addZero(String element){
  
  int intelement= (element.toInt());
  String zero="0";
  if (intelement<10 && intelement>0){
    zero+=element;
    return zero;
  }
  else{
    return element;
  }
}

