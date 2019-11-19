#include <SoftwareSerial.h>

SoftwareSerial BTSerial(7, 8);
const char trigPin = 13;
const char echoPin = 12;
int buzzerPin = 9;
int tilt = 4;
int led = 2;

int pulseWidth;
int distance;

int alert = 200;

void setup() {
  Serial.begin(9600);
  BTSerial.begin(9600);
  pinMode(buzzerPin, OUTPUT);
  pinMode(trigPin,OUTPUT);
  pinMode(echoPin,INPUT);
  pinMode(tilt, INPUT);
  pinMode(led, OUTPUT);
  digitalWrite(trigPin, LOW);
}


void loop() {

  int state = digitalRead(tilt);
  
  digitalWrite(trigPin,HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  
  pulseWidth = pulseIn(echoPin,HIGH);
  distance = (pulseWidth / 58); // change into 'cm' 


  if(distance >= 10){
    tone(buzzerPin,alert,1000);
    BTSerial.println("t");
  }

  if(state == HIGH) {
    digitalWrite(led, HIGH);
    BTSerial.println("f");
  }

  else { 
    digitalWrite(led, LOW);
  }

  if(BTSerial.available()) {
    Serial.write(BTSerial.read());
  }

  if(Serial.available()) {
    BTSerial.write(Serial.read());
  }
}
