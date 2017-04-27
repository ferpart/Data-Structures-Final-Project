#include <LiquidCrystal.h>
#include <SPI.h>
#include <MFRC522.h>

#define RST_PIN  9    //Pin 9 para el reset del RC522
#define SS_PIN  10   //Pin 10 para el SS (SDA) del RC522
MFRC522 mfrc522(SS_PIN, RST_PIN); //Creamos el objeto para el RC522

//Crear el objeto LCD con los números correspondientes (rs, en, d4, d5, d6, d7)
LiquidCrystal lcd(8, 3, 4, 5, 6, 7);
byte key[4]= {};
int inByte = 0;

void setup() {
     Serial.begin(9600); //Iniciamos la comunicación  serial
     SPI.begin();        //Iniciamos el Bus SPI
     pinMode(2, OUTPUT);
     digitalWrite(2, LOW);
     mfrc522.PCD_Init(); // Iniciamos  el MFRC522
     lcd.begin(16, 2);
     digitalWrite(2,HIGH);
     delay(100);
     digitalWrite(2,LOW);
     delay(100);
     digitalWrite(2,HIGH);
     delay(100);
     digitalWrite(2,LOW);
     delay(100);
     digitalWrite(2,HIGH);
     delay(100);
     digitalWrite(2,LOW);
}

void loop() {
    // Revisamos si hay nuevas tarjetas  presentes
    if(Serial.available() > 0){
      inByte = Serial.read();
      if(inByte == '0'){ //Aviso de no exisitente
        lcd.clear();
        lcd.setCursor(7,0);
        lcd.print("No");
        lcd.setCursor(3,1);
        lcd.print("Existente!");
        //digitalWrite(2,LOW);
        delay(2000);
        lcd.clear();
      }
      else if(inByte == '1'){ //Aviso de existente
        lcd.clear();
        lcd.setCursor(3,0);
        lcd.print("Asistencia");
        lcd.setCursor(3,1);
        lcd.print("Registrada");
        //digitalWrite(2,HIGH);
        delay(2000);
        lcd.clear();
      }
      else if (inByte == '2'){ //Aviso de nuevo usuario registrado
        lcd.clear();
        lcd.setCursor(4,0);
        lcd.print("Usuario");
        lcd.setCursor(3,1);
        lcd.print("Registrado");
        //digitalWrite(2,LOW);
        delay(2000);
        lcd.clear();
      }
      }
      else{
      lcd.setCursor(3,0);
      lcd.print("Registrate");
      lcd.setCursor(3,1);
      lcd.print("Por Favor!");
      }
      if ( mfrc522.PICC_IsNewCardPresent()){  
        digitalWrite(2, HIGH);
        //Seleccionamos una tarjeta
        if ( mfrc522.PICC_ReadCardSerial()){
          for (byte i = 0; i < mfrc522.uid.size; i++) {                    
            Serial.print(mfrc522.uid.uidByte[i] < 0x10 ? "0" : "");
            Serial.print(mfrc522.uid.uidByte[i], HEX);
            key[i]=mfrc522.uid.uidByte[i];
            } 
            Serial.println();
            mfrc522.PICC_HaltA();         
        }      
      }  
      digitalWrite(2, LOW); 
}

