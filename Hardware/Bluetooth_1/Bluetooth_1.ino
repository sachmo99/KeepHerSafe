#include <SoftwareSerial.h>

SoftwareSerial mySerial(2,3); // RX, TX

int i = 0;
void setup() {
  // put your setup code here, to run once:

  Serial.begin(9600);

  mySerial.begin(9600);
  mySerial.println("Data from Arduino");

}

void loop() {
  // put your main code here, to run repeatedly:

    if (mySerial.available()) {
      Serial.write(mySerial.read());
    }
    if (Serial.available()) {
      mySerial.write(Serial.read());
    }

    delay(1000);
    mySerial.println(String(random(65,70))+ ";" + String(i));
    i++;
}
