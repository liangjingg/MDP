
/*
   Version: 1.10 09 March 2021

   Description:
   This code is written for integration testing of Arduino Robot Hardware to Rpi via serial communication.


   Features:
   Moving Straight Line
   Rotate Left/Right
   Calibration Front (WIP)
   Calibration right wall (WIP)

   This is the code that was most recently updated to gihub

*/


/*
   ===================================
   Packages
   DualVNH5019MotorShield - Library to interface with Pololu motorshield to control DC motors
   SharpIR - Library to interface with Sharp IR sensors
   EnableInterrupt - Library to allows interrupts control on any digital pin. Note: Arduino Uno can only configure D0 and D1 as interrupt
   pin by AttachInterrupt() without this library.
   PID_v1 - Library to easy and quick integration of PID Controller
   ArduinoSort - Library for insertion sort algorithm for implementing median filtering of sensor value
   ===================================
*/
#include "DualVNH5019MotorShield.h"
#include <SharpIR.h>
#include <EnableInterrupt.h>
#include <PID_v2.h>
#include <ArduinoSort.h>
#include "movement.h"         //includes the header for the files of going straight,back,left,right
#include "controls.h"         //allows input from the serial monitor
//#include "sensor.h"

/*
   ==============================
   Variables declaration
   ==============================
*/


//Operating states
bool FASTEST_PATH = false;
bool DEBUG = false;
//byte delayExplore = 2.5;
//byte delayFastestPath = 500;

//CONSTANTS SAMPLE SIZE OF 50(FOR SENSOR)
#define SAMPLE 50

volatile double sensor_reading[SAMPLE];  //Store Reading for Median_filtering

//Define constructor for sensor
//Note : front_right(front-sensor(RHS)), right_front(Right-position sensor)

//// VARIABLES FOR COMMAND CHAINING FROM ALGO //
//char command;
//char execute[30];
//int index = 0;
//int dash = 0;

/*
   ==============================
   Main Program
   ==============================
*/
void setup() {
  Serial.begin(115200);
  Serial.println("Connected");

  movementSetup();        //pulls the setup files from movement.h
  sensorSetup();

}

void loop() {
  // put your main code here, to run repeatedly:
  vroom();
  delay(100);
  //getSensorDist();
  //getDistanceMsg();
  //delay(3000);


}

//
////method to get command string into the buffer
//void get_command(){
//    int i = 0;
//    while(Serial.available()>0){
//       command[i] = Serial.read();
//       i++;
//       delay(2); //essential delay cause of serial being too slow
//    }
//    command[i] = '\0';
//
//    //Debug print command
//    if(DEBUG && command[0]!='\0'){
//        Serial.print("COMMAND :");
//        Serial.println(command);
//    }
//}
//
////method to print all characters of string received (for debug)
//void print_all_commands(){
//  int i = 0;
//  Serial.println("Msg Received: ");
//  while (command[i] != '\0'){
//    Serial.print(command[i]);
//    i++;
//  }
//  Serial.print("EndOfLine");
//  Serial.println();
//}
