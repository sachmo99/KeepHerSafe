#include <Wire.h>
#include "MAX30105.h"

#include "heartRate.h"

#include <SoftwareSerial.h>

SoftwareSerial appSerial(2, 3); // RX, TX

MAX30105 particleSensor;

const byte RATE_SIZE = 30;  // Increase this for more averaging. 4 is good. 
byte rates[RATE_SIZE];      // Array of heart rates 
byte rateSpot = 0;
long lastBeat = 0; //Time at which the last beat occurred

float beatsPerMinute;
int beatAvg;
long irValue;
void setup()
{
  Serial.begin(9600);

  appSerial.begin(9600);

  Serial.println("Initializing...");
  appSerial.println("Arduino connection successfull");

  // Initialize sensor
  if (!particleSensor.begin(Wire, I2C_SPEED_FAST)) //Use default I2C port, 400kHz speed
  {
    Serial.println("MAX30105 was not found. Please check wiring/power. ");
    while (1)
      ;
  }
  Serial.println("Place your index finger on the sensor with steady pressure.");

  particleSensor.setup();        //Configure sensor with default settings
  particleSensor.setPulseAmplitudeRed(0x0A); //Turn Red LED to low to indicate sensor is running
  particleSensor.setPulseAmplitudeGreen(0);  //Turn off Green LED
}

void getHeartRate()
{
  // put your main code here, to run repeatedly:
  irValue = particleSensor.getIR();

  if (checkForBeat(irValue) == true)
  {
    //We sensed a beat!
    long delta = millis() - lastBeat;
    lastBeat = millis();

    beatsPerMinute = 60 / (delta / 1000.0);

    if (beatsPerMinute < 255 && beatsPerMinute > 20)
    {
      rates[rateSpot++] = (byte)beatsPerMinute; //Store this reading in the array
      rateSpot %= RATE_SIZE;        //Wrap variable

      //Take average of readings
      beatAvg = 0;
      for (byte x = 0; x < RATE_SIZE; x++)
        beatAvg += rates[x];
      beatAvg /= RATE_SIZE;
      Serial.println("beatAvg = " + String(beatAvg) + " ind = " + String(rateSpot) + "@" + String(lastBeat));
      for(int i = 0;i < RATE_SIZE; i ++)
      {
        Serial.print(rates[i]);
        Serial.print(" ");
      }
      Serial.println("");

      appSerial.println("datapoint;" + String(beatAvg) + ";" +  "spo2"+ ";" + String(rateSpot) + ";" + String(lastBeat/1000));

    }
  }
}

void loop()
{

  getHeartRate();

//  Serial.print("IR=");
//  Serial.print(irValue);
//  Serial.print(", BPM=");
//  Serial.print(beatsPerMinute);
//  Serial.print(", Avg BPM=");
//  Serial.print(beatAvg);

  if (irValue < 50000)
  {
//    Serial.println("0;0;0");
//    appSerial.println("0;0;0");
  }
  else
  {
//    Serial.println(String("1;") + String(beatsPerMinute) + String(";") + String(beatAvg));
//    appSerial.println(String("1;") + String(beatsPerMinute) + String(";") + String(beatAvg));
    //isFingerPresent, BPM , AVG BPM,
  }

  

//  Serial.println();
}
