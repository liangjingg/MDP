// ------------------------------------------------------------
// MDP GROUP 28
//
//
// Version 1.5.7 (14.10.2019)   
// - Added single sensor distance calibration
// - Added step calibration (yes, for the fifth time already)
// - Adjusted PID values for fastest path 
// - Added documentation
// 
// ------------------------------------------------------------

#include "main.h"

// ------------------------------------------------------------
//
//                       SETUP
//
// ------------------------------------------------------------
void setup()
{
  // put your setup code here, to run once:
  Serial.flush();
  Serial.begin(115200);
  md.init();

  pinMode(sensorRIGHTF, INPUT);
  pinMode(sensorRIGHTB, INPUT);
  pinMode(sensorLEFTF, INPUT);
  pinMode(sensorFRONTC, INPUT);
  pinMode(sensorFRONTL, INPUT);
  pinMode(sensorFRONTR, INPUT);


  /*
     INTERRUPTS:
     Arduino UNO provides two default interrupt pins int.0 and int.1 which correspond to pin 2 and pin 3 respectively.
     They can be implemented with the following syntax: attachInterrupt(pin,ISR,mode).

     However, by default Arduino UNO comes with only two default interrupt pins, which is not enough.
     Fortunately, this can be overcome by using the “PinChangeInt.h” library that allows mapping other Arduino pins
     as interrupt pins

  */
  PCintPort::attachInterrupt(motor_RIGHT_DATA, updateRightMotorDelta, HIGH);
  PCintPort::attachInterrupt(motor_LEFT_DATA, updateLeftMotorDelta, HIGH);
  Serial.println("P:Setup complete.");
  Serial.flush();
  Serial.println("P:" + getAllIRSensor());
}

// ------------------------------------------------------------
//
//                       LOOP
//
// ------------------------------------------------------------
void loop()
{  
  // put your main code here, to run repeatedly:
  // Reads commands and executes in the loop() function. DO NOT DELETE!
  if (readCommand() == true)
  { // if there are commands that are available
    while (cmd_out < cmd_in)
    { // execute each command until the last char has been executed
      executeCommand();
      cmd_out++;
    }
    // reset cmd_out counter
    cmd_out = 0;
  }
  delay(50);
}
