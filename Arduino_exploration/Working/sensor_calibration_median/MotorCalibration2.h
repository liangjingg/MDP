#ifndef MOTOR_CALIBRATION_H
#define MOTOR_CALIBRATION_H
/*
 * Version: 1.0
 * 
 * Description:
 * This code is written for integration testing of Arduino Robot Hardware to Rpi via serial communication. 
 * 
 *  
 * Features:
 * Moving Straight Line
 * Rotate Left/Right
 * Calibration Front (WIP)
 * Calibration right wall (WIP)
 * 
 */


/*
 * ===================================
 * Packages 
 * DualVNH5019MotorShield - Library to interface with Pololu motorshield to control DC motors
 * SharpIR - Library to interface with Sharp IR sensors
 * EnableInterrupt - Library to allows interrupts control on any digital pin. Note: Arduino Uno can only configure D0 and D1 as interrupt 
 * pin by AttachInterrupt() without this library.
 * PID_v1 - Library to easy and quick integration of PID Controller
 * ArduinoSort - Library for insertion sort algorithm for implementing median filtering of sensor value
 * ===================================
 */
#include "DualVNH5019MotorShield.h"
#include <SharpIR.h>
#include <EnableInterrupt.h>
#include <PID_v2.h>
#include <ArduinoSort.h>

/*
 * ==============================
 * Variables declaration
 * ==============================
 */


//#define LEFT_ENCODER 5 //left motor encoder A to pin 5
//#define RIGHT_ENCODER 13 //right motor encoder A to pin 13


double distance_cm; //distance in cm that the robot need to move.
//ticks parameters for PID
double leftEncoderValue = 0;
double rightEncoderValue = 0;
double difference;                // Use to find the difference
double Setpoint, Input, Output;


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
DualVNH5019MotorShield md;


PID straightPID(&leftEncoderValue, &Output, &rightEncoderValue, 1.7, 0.4, 0.0, DIRECT); //7.4 // 3.4, 3.0, 0.3 //1.4 0.3
PID leftPID(&leftEncoderValue, &Output, &rightEncoderValue, 1.9, 0.6, 0.0, DIRECT);
PID rightPID(&leftEncoderValue, &Output, &rightEncoderValue, 2.4, 0.5, 0.0, DIRECT); //1.5 0.3


// VARIABLES FOR COMMAND CHAINING FROM ALGO //
char command;
char execute[30];
int index = 0;
int dash = 0;

/*
 * ==============================
 * Main Program
 * ==============================
 
void setup() {

  leftPID.SetOutputLimits(-50,50);
  leftPID.SetMode(AUTOMATIC);
  rightPID.SetOutputLimits(-50,50);
  rightPID.SetMode(AUTOMATIC); 
  straightPID.SetOutputLimits(-50,50);
  straightPID.SetMode(AUTOMATIC);
          rightEncoderRes();
        leftEncoderRes();
  
}*/

//void loop() {
  // put your main code here, to run repeatedly:
    //char var = 'R'; 
    //char readChar;
    //goStraight(5000);
    //turnLeft(394); //90 degree turn
    //turnLeft(394*12 + 100); //1080 degree turn
    //turnLeft(394*8 + 75); //720 degree turn
    //turnRight(380);  // 90 degree turn 
    //turnRight(385*8 + 80);  // 720 degree turn 
    //turnRight(385*12 + 195);  // 1080 degree turn 

    //goStraight(330);
    
//}


/*
 * =======================================================
 * Motion Controls
 * Methods to move the robot straight, left, right, etc...
 * =======================================================
 */
/* =============================== Go Straight ============================= */
void goStraight(double ticks){
   straightPID.Compute();  
  

   while((leftEncoderValue < ticks) && (rightEncoderValue < ticks)) { 

    straightPID.Compute(); 
    Serial.print("Left:"); 
    Serial.print(leftEncoderValue);
    Serial.print("Left (output):"); 
    Serial.print(leftEncoderValue+Output);
    Serial.print(", Right:");
    Serial.print(rightEncoderValue);
    Serial.print(", Diff:");
    Serial.println(rightEncoderValue-(leftEncoderValue+Output));      
        md.setSpeeds((300),(300+Output));
      }
        md.setBrakes(400,400);
        delay(5000);
        rightEncoderRes();
        leftEncoderRes();
        
}

void goBack(double ticks){
//   md.setSpeeds(-(200+Output),-200);
//   straightPID.Compute();  
//   while((leftEncoderValue < ticks) && (rightEncoderValue < ticks)) {     
//        straightPID.Compute();  
//        md.setSpeeds(-(200+Output),-200);
//      }
//        md.setBrakes(400,400);
//        delay(5000);
//        rightEncoderRes();
//        leftEncoderRes();
}


/* =============================== Turn Left & Turn Right ============================= */
void turnLeft(double ticks){
  leftPID.Compute();

   while ((leftEncoderValue < ticks) && (rightEncoderValue < ticks))
  {
    leftPID.Compute();
   
    md.setSpeeds(-(200),(200+Output));
  }
  md.setBrakes(400, 400);
  delay(1500);
        rightEncoderRes();
        leftEncoderRes();
}

void turnRight(double ticks){
  rightPID.Compute();
  Serial.print("Left:"); 
    Serial.print(leftEncoderValue);
    Serial.print(", Right:");
    Serial.print(rightEncoderValue);
    Serial.print(", Diff:");
    Serial.println(Output);
   while ((leftEncoderValue < ticks) && (rightEncoderValue < ticks))
  {
    rightPID.Compute();
     Serial.print("Left:"); 
    Serial.print(leftEncoderValue);
                    Serial.print("Left (output):"); 
    Serial.print(leftEncoderValue+Output);
    Serial.print(", Right:");
    Serial.print(rightEncoderValue);
    Serial.print(", Diff:");
    Serial.println(rightEncoderValue-(leftEncoderValue+Output));  
    md.setSpeeds((200),-(200+Output));
	Serial.println(ticks);
  }
  md.setBrakes(400, 400);
  delay(1500);
  //break;
  leftEncoderRes();
  rightEncoderRes();
}



/* ============================RPM to Distance ============================= */
double distToTicks(double dist){
  double circumference = 18.85;
  double pulse = 562.25;
  double oneCM = circumference / pulse;
  double ticks = dist / oneCM;
  return ticks;
}

/* ====================== Rotation Ticks Left ============================= */
double rotationTicksLeft(double angle){
  double perDegree = 4.54; 
  return angle*perDegree;
}

double rotationTicksRight(double angle){
  double perDegree = 4.5; 
  return angle*perDegree;
}

/*
 * ==================================
 * Interrupt Service Routine
 * For counting ticks
 * ================================== 
 */
void leftEncoderInc(void){
  leftEncoderValue++;
  }
void rightEncoderInc(void){
  rightEncoderValue++;
  }
void leftEncoderRes(void){
  leftEncoderValue = 0;
}
void rightEncoderRes(void){
  rightEncoderValue = 0;
}

/*
 * ======================================================
 * Conversion
 * Methods to convert stuffs
 * ======================================================
 */

double rpm_to_speed_1(double RPM){
  if (RPM>0)
    return 2.8356*RPM + 19.531; 
  else if (RPM == 0)
    return 0;
  else
    return -2.91*(-1)*RPM - 16.165; 
}

double rpm_to_speed_2(double RPM){
  if (RPM>0)
    return 2.5776*RPM + 23.946;
  else if (RPM == 0)
    return 0;
  else
    return -2.6397*(-1)*RPM - 16.022;  
}


/*
 * ======================================================
 * Communication
 * Methods to help in communication with RPI
 * ======================================================
 */
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

#endif
