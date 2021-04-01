
/*
   Version: 1.10 1 April 2021
  
  This code is written by the Arduino team from MDP Group 11 2020/21 Semester 2

*/


#include "DualVNH5019MotorShield.h"
#include <SharpIR.h>
#include <EnableInterrupt.h>
#include <PID_v2.h>
#include <ArduinoSort.h>
#include "movement.h"         //includes the header for the files of going straight,back,left,right
#include "controls.h"         //allows input from the serial monitor
//#include "sensor.h"


//Operating states
bool FASTEST_PATH = false;
bool DEBUG = false;

//Include all the setup functions required
void setup() {

  Serial.begin(115200);
  Serial.println("Connected");

  movementSetup();        //setup movement.h
  sensorSetup();          //setup sensor.h

}

//Program Loop
void loop() {

  if (FASTEST_PATH){
    vroom();             //fastest path function
  }
  else{
      valoom();          //exploration/imagerec function
  }
  delay(100);

}
