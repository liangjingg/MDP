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
float FR_OFF = 0;      //A3 pin
float FC_OFF = 0;       //A2 pin
float FL_OFF = 0;      //A0 pin
float RB_OFF = 0;     //A1 pin
float RF_OFF = 0;   //A4 pin
float L_OFF = 0;  //A5 pin

/* =============================== Distance in CM ============================= */
float FR_a = -6.005 ;
float FR_b = 5908;
float FR_c = 0.08083;

float FC_a = -5.203;
float FC_b = 5148;
float FC_c =  -28.2;

float FL_a = -6.005;//NOT DONE
float FL_b = 5908;
float FL_c = 0.08086;

float RB_a = -7.287;
float RB_b = 5120;
float RB_c = -31.65;

float RF_a =  -7.604;
float RF_b = 5262;
float RF_c =  -25.75;

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
float RB_D = 0;
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
  else if (outdist > 35){
    return 35.00;
  }
  return outdist;
}

float getDistL(float analogreading,float a,float b,float c){
  float outdist = ((a*analogreading)+b)/(analogreading+c);

  outdist = abs(outdist);

  if (outdist<0){
    return 60.01;
  }
  else if (outdist > 60){
    return 60.01;
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
  L_D = getDistL(medianAnalog(SAMPLESIZE,L_PIN),L_a,L_b,L_c)-L_OFF;    
}

void updateSensorPrint(){
  takeReadings();
  setArraySensor();
  printArraySensor();
}

void updateSensorDone(){
  takeReadings();
  setArraySensor();
  printArraySensor();
}

void updateDone(){ //similar functionality as printArraySensor()
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
  Serial.print("|"); 
  Serial.println("1"); //algo team requested for this 1 to let them know it is complete
}

void updateSensor(){
  takeReadings();
  setArraySensor();
}
