// ------------------------------------------------------------
//
//                        SENSORS
//
// ------------------------------------------------------------

#include <math.h>
#include <SharpIR.h>

//FRONTt DEFINE
//#define FRONTLEFTA 4618.42
//#define FRONTLEFTB 3374.61
//#define FRONTLEFTC 0.0655686
//
//#define FRONTCENTERA 4618.42
//#define FRONTCENTERB 3374.61
//#define FRONTCENTERC 0.0655686
//
//#define FRONTRIGHTA 4618.42
//#define FRONTRIGHTB 3374.61
//#define FRONTRIGHTC 0.0655686
//
////LEFT DEFINE
//#define LEFTA 4618.42
//#define LEFTB 3374.61
//#define LEFTC 0.0655686
//
//
////RIGHT DEFINE
//#define RIGHTFRONTA 4618.42
//#define RIGHTFRONTB 3374.61
//#define RIGHTFRONTC 0.0655686
//
//#define RIGHTBACKA 4618.42
//#define RIGHTBACKB 3374.61
//#define RIGHTBACKC 0.0655686

/* =============================== sensor pin declaration ============================= */
int ps1 = 0;   // A0 left analog pin used to connect the sharp sensor A0
int ps2 = 1;   // A1 right bottom
int ps3 = 4;   // A2 front left
int ps4 = 5;   // A3 right front
int ps5 = 3;   // A4 front center
int ps6 = 2;   // A5 front right

/* =============================== a2d values from sensor ============================= */
int left_analog = 500;
int right_front_analog = 500;
int right_back_analog = 500;
int front_center_analog = 500;
int front_left_analog = 500;
int front_right_analog = 500;

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


void sensorSetup()
{
  //Serial.begin(9600); // start the serial port
    //set sensor pins
  pinMode (A0, INPUT);
  pinMode (A1, INPUT);
  pinMode (A2, INPUT);
  pinMode (A3, INPUT);
  pinMode (A4, INPUT);
  pinMode (A5, INPUT);
}


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
 
  for (int i = 0; i < sample_size; i++)
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
double getDistanceFrontLeft(int analogValue)
{
  double distanceInCM = (8687.689/(analogValue - 75.9339));
  if (distanceInCM > 70 || distanceInCM <= -0.00)
  {
    distanceInCM = 70;
  }
  return distanceInCM;
}


double getDistanceFrontRight(int analogValue)
{
  double distanceInCM = (3867.508/(analogValue - 130.319));
  
  if (distanceInCM > 40 || distanceInCM <= -0.00)
  {
    distanceInCM = 40;
  }
  return distanceInCM;
}
double getDistanceFrontCenter(int analogValue)
{
  double distanceInCM = (3867.508/(analogValue - 130.319));

  if (distanceInCM > 40 || distanceInCM <= -0.00)
  {
    distanceInCM = 40;
  }
  return distanceInCM;
}

/* =============================== Right Sensors ============================= */
double getDistanceRightFront(int analogValue)
{
  double distanceInCM = (3867.508/(analogValue - 130.319));
  
  if (distanceInCM > 40 || distanceInCM <= -0.0)
  {
    distanceInCM = 40;
  }
  return distanceInCM;
}


double getDistanceRightBack(int analogValue)
{
  double distanceInCM = (3867.508/(analogValue - 130.319));

  if (distanceInCM > 40 || distanceInCM <= -0.0)
  {
    distanceInCM = 40;
  }
  return distanceInCM;
}

/* =============================== Left Sensor ============================= */

double getDistanceLeft(int analogValue)
{
  double distanceInCM = (8687.689/(analogValue - 75.9339))+4;

  if (distanceInCM > 70 || distanceInCM <= -0.00)
  {
    distanceInCM = 70;
  }
  return distanceInCM;
}


/* =============================== define distance arr from obstacle for each sensor ============================= */
int getLeftAnalog(){
  left_analog = rawIRSensorMedian(30, 0);
  return left_analog;
}

int getRFAnalog(){
  right_front_analog = rawIRSensorMedian(50, 3);
  return right_front_analog;
}

int getRBAnalog(){
  right_back_analog = rawIRSensorMedian(50, 1);
  return right_back_analog;
}

int getFCAnalog(){
  front_center_analog = rawIRSensorMedian(50, 4);
  return front_center_analog;
}

//SOMETHING IS WRONG WITH THIS 
int getFLAnalog(){
  front_left_analog = rawIRSensorMedian(50, 2);
  //Serial.println(front_left_analog);
  //Serial.println("Completed! :-) "); 
  return front_left_analog;
}

int getFRAnalog(){
  front_right_analog = rawIRSensorMedian(50, 5);
  return front_right_analog;
}

void getAnalog(){
  getLeftAnalog();
  getRFAnalog();
  getRBAnalog();
  getFCAnalog();
  getFRAnalog();
  getFLAnalog();
  
}

void getSensorDist()
{
  getAnalog();
  left_inDistanceCM = getDistanceLeft(left_analog);
  right_front_inDistanceCM = getDistanceRightFront(right_front_analog);
  right_back_inDistanceCM = getDistanceRightBack(right_back_analog);
  front_center_inDistanceCM = getDistanceFrontCenter(front_center_analog);
  front_left_inDistanceCM = getDistanceFrontLeft(front_left_analog) ;
  front_right_inDistanceCM = getDistanceFrontRight(front_right_analog);
  
//  Serial.print("Left Sensor: ");
//  Serial.println(left_inDistanceCM);
//  
//  Serial.print("Right Front Sensor: ");
//  Serial.println(right_front_inDistanceCM);
//  
//  Serial.print("Right Back Sensor: ");
//  Serial.println(right_back_inDistanceCM);
//  
//  Serial.print("Front Center Sensor: ");
//  Serial.println(front_center_inDistanceCM);
//  
//  Serial.print("Front Left Sensor: ");
//  Serial.println(front_left_inDistanceCM);
//  
//  Serial.print("Front Right Sensor: ");
//  Serial.println(front_right_inDistanceCM);

  Serial.print(front_right_inDistanceCM);
  Serial.print("|");
  Serial.print(front_center_inDistanceCM);
  Serial.print("|");
  Serial.print(front_left_inDistanceCM);
  Serial.print("|");
  Serial.print(right_back_inDistanceCM);
  Serial.print("|");
  Serial.print(right_front_inDistanceCM);
  Serial.print("|");
  Serial.println(left_inDistanceCM);

}

/* =============================== Return Distance Message ============================= */
void getDistanceMsg()
{
  String message = String(sensorDist[0])+ "," + String(sensorDist[1])+ "," + String(sensorDist[2]) + "," + String(sensorDist[3]) + "," + String(sensorDist[4]) + "," + String(sensorDist[5]);
  //Serial.println(message);
  String info = ""+String(sensorDist[0])+","+String(sensorDist[1])+ "," + String(sensorDist[2]) + "," + String(sensorDist[3]) + "," + String(sensorDist[4]) + "," + String(sensorDist[5]);
  Serial.println(info);
  
  //return message;
}

void sense(){
  getSensorDist();
  String message = String(front_right_inDistanceCM)+ "|" + String(front_center_inDistanceCM)+ "|" + String(front_left_inDistanceCM);
  //Serial.println(message);
}

// ensure that the right of the robot is straight

void getSensorDist_noMsg()
{
  getAnalog();
  left_inDistanceCM = getDistanceLeft(left_analog);
  right_front_inDistanceCM = getDistanceRightFront(right_front_analog);
  right_back_inDistanceCM = getDistanceRightBack(right_back_analog);
  front_center_inDistanceCM = getDistanceFrontCenter(front_center_analog);
  front_left_inDistanceCM = getDistanceFrontLeft(front_left_analog) ;
  front_right_inDistanceCM = getDistanceFrontRight(front_right_analog);

}
