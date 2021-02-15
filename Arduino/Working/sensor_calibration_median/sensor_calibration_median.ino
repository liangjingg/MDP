#include <DualVNH5019MotorShield.h>
#include <EnableInterrupt.h>
#include "MotorCalibration2.h"

#define LEFT_ENCODER 5
#define RIGHT_ENCODER 13

//DualVNH5019MotorShield md;

volatile unsigned int E1Pos = 0;
volatile unsigned int E2Pos = 0;

int ps1 = 0;   // A0 left analog pin used to connect the sharp sensor A0
int ps2 = 1;   // A1 right
int ps3 = 2;   // A2 back
int ps4 = 3;   // A3 front 
int ps5 = 4;   // A4 top left
int ps6 = 5;   // A5 top right

// variable to store the a2d values from sensor(initially zero)
int left_analog = 0;   
int right_analog = 0; 
int back_analog = 0; 
int front_analog = 0; 
int topLeft_analog = 0; 
int topRight_analog = 0; 

//offset
float longFront = 0;      //A3 pin 
float longBack = 0;       //A2 pin
float shortLeft = 0;      //A0 pin
float shortRight = 0;     //A1 pin
float shortTopLeft = 3;   //A4 pin
float shortTopRight = 3;  //A5 pin

//distance in Cm
float left_distanceInCm = 0;
float right_distanceInCm = 0;
float back_distanceInCm = 0;
float front_distanceInCm = 0;
float topLeft_distanceInCm = 0;
float topRight_distanceInCm = 0;

void setup()
{
  Serial.begin(115200); // start the serial port
  Serial.println("SETUP");

  //setup for motor shield
  pinMode(LEFT_ENCODER, INPUT);
  digitalWrite(LEFT_ENCODER, HIGH);       // turn on pull-up resistor
  pinMode(RIGHT_ENCODER, INPUT);
  digitalWrite(RIGHT_ENCODER, HIGH);       // turn on pull-up resistor
  //delay(1000);
  enableInterrupt(LEFT_ENCODER, leftEncoderInc, RISING);
  delay(1000);
  enableInterrupt(RIGHT_ENCODER, rightEncoderInc, RISING);
  Serial.println("Dual VNH5019 Motor Shield");
  md.init();
  Serial.println("start");                // a personal quirk

  leftPID.SetOutputLimits(-50,50);
  leftPID.SetMode(AUTOMATIC);
  rightPID.SetOutputLimits(-50,50);
  rightPID.SetMode(AUTOMATIC); 
  straightPID.SetOutputLimits(-50,50);
  straightPID.SetMode(AUTOMATIC);
  //rightEncoderRes();
  //leftEncoderRes();
}

void loop()
{
  left_analog = rawIRSensorMedian(50, 0);
  left_distanceInCm = shortRangeDistance(left_analog);
  
  right_analog = rawIRSensorMedian(50, 1);
  right_distanceInCm = shortRangeDistance(right_analog);
  
  back_analog = rawIRSensorMedian(50, 2);
  back_distanceInCm = longRangeDistance(back_analog) + 5;
  
  front_analog = rawIRSensorMedian(50, 3);
  front_distanceInCm = longRangeDistance(front_analog);
  
  topLeft_analog = rawIRSensorMedian(50, 4);
  topLeft_distanceInCm = shortRangeDistance(topLeft_analog) -3 ;
  
  topRight_analog = rawIRSensorMedian(50, 5);
  topRight_distanceInCm = shortRangeDistance(topRight_analog) - 3;
  
  Serial.println("TopLeft Front TopRight");
  Serial.print(topLeft_distanceInCm);
  Serial.print("\t");
  Serial.print(front_distanceInCm);
  Serial.print("\t");
  Serial.println(topRight_distanceInCm);
  Serial.println("    Left Back Right   ");
  Serial.print(left_distanceInCm);
  Serial.print("\t");
  Serial.print(back_distanceInCm);
  Serial.print("\t");
  Serial.println(right_distanceInCm);
  
  if (topLeft_distanceInCm >= 10&&front_distanceInCm >= 20&&topRight_distanceInCm >= 10)
  {
    Serial.println("Moving Forward one box!");
    Serial.println("");
    moveForward();
  }else{
    Serial.println("Too close to obstacle!");
    Serial.println("");
    turnRight(380);
    md.setSpeeds(0,0);
  }
}

//calculate median of sample size values
int rawIRSensorMedian(int sample_size, int pin)
{
  int reading[sample_size];
  int analog_distance = 0;
  
  for (int i = 1; i <= sample_size; i++)
  {
    analog_distance = analogRead(pin);       // reads the value of the sharp sensor
    reading[i] = analog_distance;
    delay(10);
  }
  sort(reading,sample_size);
  sample_size = (sample_size+1) / 2 - 1;
  //Serial.println(reading[sample_size]); 
  
  return reading[sample_size];
}

void wait(int duration)
{
  for (int i = 1; i <= duration; i++)
  {
    delay(1000);
    Serial.println(i);
  }
}

/*bubble sort*/
void swap(int *x,int *y) {
   int q;
   
   q=*x; 
   *x=*y; 
   *y=q;
}

void sort(int arr[],int n) { 
   int i,j,temp;

   for(i = 0;i < n-1;i++) {
      for(j = 0;j < n-i-1;j++) {
         if(arr[j] > arr[j+1])
            swap(&arr[j],&arr[j+1]);
      }
   }
}

/* Effective Distance is 10cm - 40cm */
float shortRangeDistance(int analogValue)
{
  float distanceInCm = (3867.508/(analogValue - 130.319));
  if (distanceInCm > 70 || distanceInCm < 0)
  {
    distanceInCm = 70;
  }
  return distanceInCm;
}

/*Effective Distance is 20cm to 70cm*/
float longRangeDistance(int analogValue)
{
  float distanceInCm = (8687.689/(analogValue - 75.9339));

  if (distanceInCm > 70 || distanceInCm < 0)
  {
    distanceInCm = 70;
  }
  return distanceInCm;
}

void E1() {
  E1Pos++;
}

void E2() {
  E2Pos++;
}

//calculate average of 20 values 
void avg(int sample, int pin)
{
  int total = 0;
  float avg_val = 0;
  int analog_distance = 0;
  
  for (int i = 1; i <= sample; i++)
  {
    analog_distance = analogRead(pin);       // reads the value of the sharp sensor
    total += analog_distance;
  }
  avg_val = (total/sample);
  Serial.println("Average");
  Serial.println(avg_val);
}

void moveForward()
{
    md.setM1Speed(200*0.85);
    md.setM2Speed(200 *(1));
    delay(590);
    md.setM1Speed(0);
    md.setM2Speed(0);
}
/*
void turnLeft()
{
  md.setSpeeds(-(200),(200));
  delay(750);
  md.setSpeeds(0,0);
}

void turnRight()
{
  md.setSpeeds((200),-(200));
  delay(750);
  md.setSpeeds(0,0);
}
*/
