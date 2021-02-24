// ------------------------------------------------------------
//
//                        SENSORS
//
// ------------------------------------------------------------

#include <math.h>

//FRONT DEFINE
#define FRONTLEFTA 4618.42
#define FRONTLEFTB 3374.61
#define FRONTLEFTC 0.0655686

#define FRONTCENTERA 4618.42
#define FRONTCENTERB 3374.61
#define FRONTCENTERC 0.0655686

#define FRONTRIGHTA 4618.42
#define FRONTRIGHTB 3374.61
#define FRONTRIGHTC 0.0655686

//LEFT DEFINE
#define LEFTA 4618.42
#define LEFTB 3374.61
#define LEFTC 0.0655686


//RIGHT DEFINE
#define RIGHTFRONTA 4618.42
#define RIGHTFRONTB 3374.61
#define RIGHTFRONTC 0.0655686

#define RIGHTBACKA 4618.42
#define RIGHTBACKB 3374.61
#define RIGHTBACKC 0.0655686
/* =============================== sensor pin declaration ============================= */
int ps1 = 0;   // A0 left analog pin used to connect the sharp sensor A0
int ps2 = 1;   // A1 right bottom
int ps3 = 4;   // A2 front left
int ps4 = 5;   // A3 right front
int ps5 = 3;   // A4 front center
int ps6 = 2;   // A5 front right

/* =============================== a2d values from sensor ============================= */
int left_analog = 0;
int right_front_analog = 0;
int right_back_analog = 0;
int front_center_analog = 0;
int front_left_analog = 0;
int front_right_analog = 0;

/* =============================== Offset from robot edge ============================= */
float short_rightFront = 0;      //A3 pin
float long_frontLeft = 0;       //A2 pin
float long_Left = 0;      //A0 pin
float short_rightBack = 0;     //A1 pin
float short_frontCenter = 3;   //A4 pin
float short_frontRight = 3;  //A5 pin

/* =============================== Distance in CM ============================= */
float left_inDistanceCM = 0;
float right_front_inDistanceCM = 0;
float right_back_inDistanceCM = 0;
float front_center_inDistanceCM = 0;
float front_left_inDistanceCM = 0;
float front_right_inDistanceCM = 0;

float sensorDist[6];
int counter = 0;

/*
void sensorSetup()
{
  Serial.begin(9600); // start the serial port
}
*/

/* =============================== Sorting Algorithm ============================= */
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

/* =============================== Calculate median of sample size values ============================= */
int rawIRSensorMedian(int sample_size, int pin)
{
  int reading[sample_size];
  int analog_distance = 0;

  for (int i = 1; i <= sample_size; i++)
  {
    analog_distance = analogRead(pin);       // reads the value of the sharp sensor
    reading[i] = analog_distance;
    //delay(10);
  }
  sort(reading,sample_size);
  sample_size = (sample_size+1) / 2 - 1;
  //Serial.println(reading[sample_size]);

  return reading[sample_size];
}


/* =============================== Front Sensors ============================= */
float getDistanceFrontLeft(int analogValue)
{
  float distanceInCM = exp(log((FRONTLEFTA-analogValue)/FRONTLEFTB)/FRONTLEFTC);

  if (distanceInCM > 70 || distanceInCM < 0)
  {
    distanceInCM = 70;
  }
  return distanceInCM;
}


float getDistanceFrontRight(int analogValue)
{
  float distanceInCM = exp(log((FRONTRIGHTA-analogValue)/FRONTRIGHTB)/FRONTRIGHTC);

  if (distanceInCM > 70 || distanceInCM < 0)
  {
    distanceInCM = 70;
  }
  return distanceInCM;
}
float getDistanceFrontCenter(int analogValue)
{
  float distanceInCM = exp(log((FRONTCENTERA-analogValue)/FRONTCENTERB)/FRONTCENTERC);

  if (distanceInCM > 70 || distanceInCM < 0)
  {
    distanceInCM = 70;
  }
  return distanceInCM;
}

/* =============================== Right Sensors ============================= */
float getDistanceRightFront(int analogValue)
{
  float distanceInCM = exp(log((RIGHTFRONTA-analogValue)/RIGHTFRONTB)/RIGHTFRONTC);

  if (distanceInCM > 70 || distanceInCM < 0)
  {
    distanceInCM = 70;
  }
  return distanceInCM;
}


float getDistanceRightBack(int analogValue)
{
  float distanceInCM = exp(log((RIGHTBACKA-analogValue)/RIGHTBACKB)/RIGHTBACKC);

  if (distanceInCM > 70 || distanceInCM < 0)
  {
    distanceInCM = 70;
  }
  return distanceInCM;
}

/* =============================== Left Sensor ============================= */

float getDistanceLeft(int analogValue)
{
  float distanceInCM = exp(log((LEFTA-analogValue)/LEFTB)/LEFTC);

  if (distanceInCM > 70 || distanceInCM < 0)
  {
    distanceInCM = 70;
  }
  return distanceInCM;
}


/* =============================== define distance arr from obstacle for each sensor ============================= */
void getSensorDist()
{
  
  left_analog = rawIRSensorMedian(50, 0);
  left_inDistanceCM = getDistanceLeft(left_analog);

  right_front_analog = rawIRSensorMedian(50, 3);
  right_front_inDistanceCM = getDistanceRightFront(right_front_analog);

  right_back_analog = rawIRSensorMedian(50, 1);
  right_back_inDistanceCM = getDistanceRightBack(right_back_analog);

  front_center_analog = rawIRSensorMedian(50, 4);
  front_center_inDistanceCM = getDistanceFrontCenter(front_center_analog);

  front_left_analog = rawIRSensorMedian(50, 2);
  front_left_inDistanceCM = getDistanceFrontLeft(front_left_analog) ;

  front_right_analog = rawIRSensorMedian(50, 5);
  front_right_inDistanceCM = getDistanceFrontRight(front_right_analog);

  //adds to the the array
  sensorDist[0] = left_inDistanceCM;
  sensorDist[1] = right_front_inDistanceCM;
  sensorDist[2] = right_back_inDistanceCM;
  sensorDist[3] = front_center_inDistanceCM;
  sensorDist[4] = front_left_inDistanceCM;
  sensorDist[5] = front_right_inDistanceCM;

//  Serial.print("Left Sensor");
//  Serial.println(sensorDist[0]);
//  Serial.print("Right Front Sensor");
//  Serial.println(sensorDist[1]);
//  Serial.print("Right Back Sensor");
//  Serial.println(sensorDist[2]);
//  Serial.print("Front Center Sensor");
//  Serial.println(sensorDist[3]);
//  Serial.print("Front Left Sensor");
//  Serial.println(sensorDist[4]);
//  Serial.print("Front Right Sensor");
//  Serial.println(sensorDist[5]);
}

/* =============================== Return Distance Message ============================= */
//String getDistanceMsg()
//{
//  int read_counter = 0;
//  if (counter == 0){
//    read_counter = 1;
//  }
//  String message = String(sensorDist[0])+ "," + String(sensorDist[1])+ "," + String(sensorDist[2]) + "," + String(sensorDist[3]) + "," + String(sensorDist[4]) + "," + String(sensorDist[5]);
//  Serial.println(message);
//  return message;
//}
