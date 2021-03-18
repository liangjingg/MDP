// ------------------------------------------------------------
//
//                        SENSORS
//
// ------------------------------------------------------------

#include <math.h>

///* =============================== sensor pin declaration ============================= */
//int ps1 = 0;   // A0 left analog pin used to connect the sharp sensor A0
//int ps2 = 1;   // A1 right bottom
//int ps3 = 4;   // A2 front left
//int ps4 = 5;   // A3 right front
//int ps5 = 3;   // A4 front center
//int ps6 = 2;   // A5 front right

/* =============================== sensor pin declaration ============================= */
int ps1 = 4;   // A0 left analog pin used to connect the sharp sensor A0
int ps2 = 5;   // A1 right bottom
int ps3 = 2;   // A2 front left
int ps4 = 1;   // A3 right front
int ps5 = 3;   // A4 front center
int ps6 = 0;   // A5 front right

/* =============================== a2d values from sensor ============================= */
int FR_PIN = 5;
int FC_PIN = 1;
int FL_PIN = 3;
int RB_PIN = 4;
int RF_PIN = 2;
int L_PIN = 0;

int SAMPLESIZE = 20;

/* =============================== Offset from robot edge ============================= */
float FR_OFF = -0.6;      //A3 pin
float FC_OFF = 0;       //A2 pin
float FL_OFF = 0;      //A0 pin
float RB_OFF = 0;     //A1 pin
float RF_OFF = 0;   //A4 pin
float L_OFF = 0;  //A5 pin

/* =============================== Distance in CM ============================= */
float FR_a = -5.874;
float FR_b = 5713;
float FR_c = -0.6672;

float FC_a = -4.846;
float FC_b = 5293;
float FC_c = -11.1;

float FL_a = -4.905;
float FL_b = 5258;
float FL_c = -13.36;

float RB_a = -3.43;
float RB_b = 5263;
float RB_c = -22.36;

float RF_a = -3.477;
float RF_b = 5981;
float RF_c = -5.528;

//float RF_a = -2.561;
//float RF_b = 5341;
//float RF_c = -16.29;


float L_a = -22.02;
float L_b = 16650;
float L_c = 29.35;


/* =============================== Distance in CM ============================= */
float FR_D = 0;
float FC_D = 0;
float FL_D = 0;
float RB_D = -2.5;
float RF_D = 0;
float L_D = 0;

float sensorDist[6];


void sensorSetup()
{
  pinMode (A0, INPUT);
  pinMode (A1, INPUT);
  pinMode (A2, INPUT);
  pinMode (A3, INPUT);
  pinMode (A4, INPUT);
  pinMode (A5, INPUT);
}

void setArraySensor(){
  sensorDist[0]=FR_D;
  sensorDist[1]=FC_D;
  sensorDist[2]=FL_D;
  sensorDist[3]=RB_D;
  sensorDist[4]=RF_D;
  sensorDist[5]=L_D;
}

void printArraySensor(){
  Serial.print(sensorDist[0]); //FR
  Serial.print("|");
  Serial.print(sensorDist[1]); //FC
  Serial.print("|");
  Serial.print(sensorDist[2]); //FL
  Serial.print("|");
  Serial.print(sensorDist[3]); //RB
  Serial.print("|");
  Serial.print(sensorDist[4]); //RF
  Serial.print("|");
  Serial.print(sensorDist[5]); //L
  Serial.println("|");  
}

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


float getDist(float analogreading,float a,float b,float c){
  float outdist = ((a*analogreading)+b)/(analogreading+c);

  outdist = abs(outdist);

  if (outdist<0){
    return 60.00;
  }
  else if (outdist > 60){
    return 60.00;
  }
  return outdist;
}

/* =============================== Calculate median of sample size values ============================= */
int medianAnalog(int sample_size, int pin)
{
  int reading[sample_size];
  int analog_distance = 0;
  for (int i = 0; i < sample_size; i++)
  {
    analog_distance = analogRead(pin);       // reads the value of the sharp sensor
    reading[i] = analog_distance;
  }
  sort(reading,sample_size);
  sample_size = ((sample_size+1) / 2) - 1;

  return reading[sample_size];
}


void takeReadings(){
  FR_D = getDist(medianAnalog(SAMPLESIZE,FR_PIN),FR_a,FR_b,FR_c)-FR_OFF;
  FC_D = getDist(medianAnalog(SAMPLESIZE,FC_PIN),FC_a,FC_b,FC_c)-FC_OFF;
  FL_D = getDist(medianAnalog(SAMPLESIZE,FL_PIN),FL_a,FL_b,FL_c)-FL_OFF;
  RB_D = getDist(medianAnalog(SAMPLESIZE,RB_PIN),RB_a,RB_b,RB_c)-RB_OFF;   
  RF_D = getDist(medianAnalog(SAMPLESIZE,RF_PIN),RF_a,RF_b,RF_c)-RF_OFF;
  L_D = getDist(medianAnalog(SAMPLESIZE,L_PIN),L_a,L_b,L_c)-L_OFF;    
}

void updateSensorPrint(){
  takeReadings();
  setArraySensor();
  printArraySensor();
}

void updateSensor(){
  takeReadings();
  setArraySensor();
}


/* =============================== Front Sensors ============================= */
//
//
//double getDistanceFrontLeft(int analogValue)
//{
//  double distanceInCM = (pow(7.231,4)*pow(analogValue,-1.446));
//  if (distanceInCM > 70 || distanceInCM <= -0.00)
//  {
//    distanceInCM = 70;
//  }
//
//  
//}
//
//
//double getDistanceFrontRight(int analogValue)
//{
//  double distanceInCM = (3867.508/(analogValue - 130.319));
//  
//  if (distanceInCM > 40 || distanceInCM <= -0.00)
//  {
//    distanceInCM = 40;
//  }
//  return distanceInCM;
//}
//double getDistanceFrontCenter(int analogValue)
//{
//  double distanceInCM = (3867.508/(analogValue - 130.319));
//
//  if (distanceInCM > 40 || distanceInCM <= -0.00)
//  {
//    distanceInCM = 40;
//  }
//  return distanceInCM;
//}
//
///* =============================== Right Sensors ============================= */
//double getDistanceRightFront(int analogValue)
//{
//  int A=5.504;
//int AA=4;
//int B=-1.4;
//int C=0.0181;
//
//  double distanceInCM = (pow(A,AA)*pow(analogValue,B))+C;
//  if (distanceInCM > 40 || distanceInCM <= 0.00)
//  {
//    distanceInCM = 70;
//  }
//  return distanceInCM;
//
//}
//
//
//double getDistanceRightBack(int analogValue)
//{
//  double distanceInCM = (3867.508/(analogValue - 130.319));
//
//  if (distanceInCM > 40 || distanceInCM <= -0.0)
//  {
//    distanceInCM = 40;
//  }
//  return distanceInCM;
//}
//
///* =============================== Left Sensor ============================= */
//
//double getDistanceLeft(int analogValue)
//{
//  double distanceInCM = (8687.689/(analogValue - 75.9339))+4;
//
//  if (distanceInCM > 70 || distanceInCM <= -0.00)
//  {
//    distanceInCM = 70;
//  }
//  return distanceInCM;
//}
//
//
//int SAMPLESIZE = 100;
///* =============================== define distance arr from obstacle for each sensor ============================= */
//int getLeftAnalog(){
//  left_analog = rawIRSensorMedian(SAMPLESIZE, 0);
//  return left_analog;
//}
//
//int getRFAnalog(){
//  right_front_analog = rawIRSensorMedian(SAMPLESIZE, 2); 
//  return right_front_analog;
//}
//
//int getRBAnalog(){
//  right_back_analog = rawIRSensorMedian(SAMPLESIZE, 4); 
//  return right_back_analog;
//}
//
//int getFCAnalog(){
//  front_center_analog = rawIRSensorMedian(SAMPLESIZE, 1); 
//  return front_center_analog;
//}
//
////SOMETHING IS WRONG WITH THIS 
//int getFLAnalog(){
//  front_left_analog = rawIRSensorMedian(SAMPLESIZE, 3);
//  //Serial.println(front_left_analog);
//  //Serial.println("Completed! :-) "); 
//  return front_left_analog;
//}
//
//int getFRAnalog(){
//  front_right_analog = rawIRSensorMedian(SAMPLESIZE, 5);
//  return front_right_analog;
//}
//
//void getAnalog(){
//  getLeftAnalog();
//  getRFAnalog();
//  getRBAnalog();
//  getFCAnalog();
//  getFRAnalog();
//  getFLAnalog();
//  
//}
//
//void getSensorDist()
//{
//  getAnalog();
//  left_inDistanceCM = getDistanceLeft(left_analog);
//  right_front_inDistanceCM = getDistanceRightFront(right_front_analog);
//  right_back_inDistanceCM = getDistanceRightBack(right_back_analog);
//  front_center_inDistanceCM = getDistanceFrontCenter(front_center_analog);
//  front_left_inDistanceCM = getDistanceFrontLeft(front_left_analog) ;
//  front_right_inDistanceCM = getDistanceFrontRight(front_right_analog);
//  
////  Serial.print("Left Sensor: ");
////  Serial.println(left_inDistanceCM);
////  
////  Serial.print("Right Front Sensor: ");
////  Serial.println(right_front_inDistanceCM);
////  
////  Serial.print("Right Back Sensor: ");
////  Serial.println(right_back_inDistanceCM);
////  
////  Serial.print("Front Center Sensor: ");
////  Serial.println(front_center_inDistanceCM);
////  
////  Serial.print("Front Left Sensor: ");
////  Serial.println(front_left_inDistanceCM);
////  
////  Serial.print("Front Right Sensor: ");
////  Serial.println(front_right_inDistanceCM);
//
//  Serial.print(front_right_inDistanceCM);
//  Serial.print("|");
//  Serial.print(front_center_inDistanceCM);
//  Serial.print("|");
//  Serial.print(front_left_inDistanceCM);
//  Serial.print("|");
//  Serial.print(right_back_inDistanceCM);
//  Serial.print("|");
//  Serial.print(right_front_inDistanceCM);
//  Serial.print("|");
//  Serial.println(left_inDistanceCM);
//
//}

/* =============================== Return Distance Message ============================= */
//void getDistanceMsg()
//{
//  String message = String(sensorDist[0])+ "," + String(sensorDist[1])+ "," + String(sensorDist[2]) + "," + String(sensorDist[3]) + "," + String(sensorDist[4]) + "," + String(sensorDist[5]);
//  //Serial.println(message);
//  String info = ""+String(sensorDist[0])+","+String(sensorDist[1])+ "," + String(sensorDist[2]) + "," + String(sensorDist[3]) + "," + String(sensorDist[4]) + "," + String(sensorDist[5]);
//  Serial.println(info);
//  
//  //return message;
//}
//
////void sense(){
////  getSensorDist();
////  String message = String(front_right_inDistanceCM)+ "|" + String(front_center_inDistanceCM)+ "|" + String(front_left_inDistanceCM);
////  //Serial.println(message);
////}
//
//// ensure that the right of the robot is straight
//
//void getSensorDist_noMsg()
//{
//  getAnalog();
//  left_inDistanceCM = getDistanceLeft(left_analog);
//  right_front_inDistanceCM = getDistanceRightFront(right_front_analog);
//  right_back_inDistanceCM = getDistanceRightBack(right_back_analog);
//  front_center_inDistanceCM = getDistanceFrontCenter(front_center_analog);
//  front_left_inDistanceCM = getDistanceFrontLeft(front_left_analog) ;
//  front_right_inDistanceCM = getDistanceFrontRight(front_right_analog);
//
//}
