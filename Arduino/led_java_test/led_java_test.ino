int inByte = 0; //Variable designada para los valores que se recibirán a través del Serial Port
 
void setup(){
      Serial.begin(9600); //Abrir el Serial Port
      pinMode(13, OUTPUT);
      digitalWrite(13, LOW);
 }
 
void loop(){
 
      if(Serial.available() > 0){
 
          inByte = Serial.read();
          if(inByte == '1'){ //Encendido
              digitalWrite(13,HIGH);
          }
          else if(inByte == '0'){ //Apagado
            digitalWrite(13,LOW);
          }
    }
}
