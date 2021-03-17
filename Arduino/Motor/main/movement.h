#include "DualVNH5019MotorShield.h"
#include <SharpIR.h>
#include <EnableInterrupt.h>
#include <PID_v2.h>
#include <ArduinoSort.h>
#include "sensor.h"


#define LEFT_ENCODER 11 //left motor encoder A to pin 5
#define RIGHT_ENCODER 5//right motor encoder A to pin 13
#define FASTSPEED 320
#define SLOWSPEED 200
#define FRONTBUMPER 4


//tickss parameters for PID
double leftEncoderValue = 0;
double rightEncoderValue = 0;
double difference;                // Use to find the difference
double Setpoint, Input, Output;


PID straightPID(&leftEncoderValue, &Output, &rightEncoderValue, 0.92, 0.2, 0, DIRECT); //7.4 // 3.4, 3.0, 0.3 //1.4 0.3
PID leftPID(&leftEncoderValue, &Output, &rightEncoderValue, 1.9, 0.6, 0.0, DIRECT);
PID rightPID(&leftEncoderValue, &Output, &rightEncoderValue, 2.4, 0.5, 0.0, DIRECT); //1.5 0.3
DualVNH5019MotorShield md;

void leftEncoderInc(void) {
  leftEncoderValue++;
}
void rightEncoderInc(void) {
  rightEncoderValue++;
}
void leftEncoderRes(void) {
  leftEncoderValue = 0;
}
void rightEncoderRes(void) {
  rightEncoderValue = 0;
}


void movementSetup() {          //setup files used in the main
  md.init();

  //Serial.begin(9600);
  //md.init();
  pinMode (LEFT_ENCODER, INPUT); //set digital pin 5 as input
  pinMode (RIGHT_ENCODER, INPUT); //set digital pin 13 as input
  enableInterrupt(LEFT_ENCODER, leftEncoderInc, RISING);  // Reading the Encoder
  enableInterrupt(RIGHT_ENCODER, rightEncoderInc, RISING);// Reading the Encoder
  leftPID.SetOutputLimits(-50, 50);
  leftPID.SetMode(AUTOMATIC);
  rightPID.SetOutputLimits(-50, 50);
  rightPID.SetMode(AUTOMATIC);
  straightPID.SetOutputLimits(-50, 50);
  straightPID.SetMode(AUTOMATIC);
  rightEncoderRes();
  leftEncoderRes();
}





/* =============================== Go Straight and Go Back ============================= */
void goStraight(double ticks) {

straightPID.Compute();
float offset = 0.1;
//getSensorDist();
//Serial.println(sensorDist[3]);
//Serial.println("Sensor Dist");
  //if (front_left_inDistanceCM > 15){
  while ((leftEncoderValue < ticks) && (rightEncoderValue < ticks )){
    straightPID.Compute();
    //getSensorDist();
    float LeftEncoderFixed = leftEncoderValue;
    float LeftEncoderOutput = leftEncoderValue + Output;
    float RightEncoderOutput = rightEncoderValue - Output;

//    Serial.print("Output Left: ");
//    Serial.print(LeftEncoderOutput);
//    Serial.print(", Output Right: ");
//    Serial.print(RightEncoderOutput);
//    Serial.print(", Diff: ");
//    Serial.println(RightEncoderOutput - LeftEncoderOutput);
//    Serial.println(Output);
    md.setSpeeds(-((FASTSPEED - Output)), -((FASTSPEED + Output)));

    //md.setSpeeds(50, 350); //left,right
  }
  md.setBrakes(400, 400);
  delay(100);
  rightEncoderRes();
  leftEncoderRes();
//  }else{
//    Serial.println(front_left_inDistanceCM);
//    Serial.println("Too Close!!");
//  }

}

//Make Robot go Back
void goBack(double ticks) {
  straightPID.Compute();

  while ((leftEncoderValue < ticks) && (rightEncoderValue < ticks)) {

    straightPID.Compute();
    //    Serial.print("Left: ");
    //    Serial.print(leftEncoderValue);
    //    Serial.print(", Left (output): ");
    //    Serial.print(leftEncoderValue+Output);
    //    Serial.print(", Right: ");
    //    Serial.print(rightEncoderValue);
    //    Serial.print(", Diff: ");
    //    Serial.println(rightEncoderValue-(leftEncoderValue+Output));
    md.setSpeeds((FASTSPEED -Output), (FASTSPEED + Output));
  }
  md.setBrakes(400, 400);
  delay(100);
  rightEncoderRes();
  leftEncoderRes();

}


/* =============================== Turn Left & Turn Right ============================= */
void turnLeft(double ticks) {
  delay(200);
  leftPID.Compute();

  while ((leftEncoderValue < ticks) && (rightEncoderValue < ticks))
  {
    leftPID.Compute();

    md.setSpeeds(-(FASTSPEED+ Output), (FASTSPEED - Output));
  }
  md.setBrakes(FASTSPEED, FASTSPEED);
  delay(200);
  rightEncoderRes();
  leftEncoderRes();
}

void turnRight(double ticks) {
  delay(200);
  rightPID.Compute();
  while ((leftEncoderValue < ticks) && (rightEncoderValue < ticks))
  {
    rightPID.Compute();
    //     Serial.print("Left:");
    //    Serial.print(leftEncoderValue);
    //                    Serial.print("Left (output):");
    //    Serial.print(leftEncoderValue+Output);
    //    Serial.print(", Right:");
    //    Serial.print(rightEncoderValue);
    //    Serial.print(", Diff:");
    //    Serial.println(rightEncoderValue-(leftEncoderValue+Output));
    md.setSpeeds((FASTSPEED + Output), -(FASTSPEED - Output));
  }
  md.setBrakes(FASTSPEED, FASTSPEED);
  delay(200);
  leftEncoderRes();
  rightEncoderRes();
}

/* ============================RPM to Distance ============================= */
double distToTicks(double dist) {
  double circumference = 18.85;
  double pulse = 562.25;
  double oneCM = circumference / pulse;
  double ticks = dist / oneCM;
  return ticks;
}

/* ====================== Rotation Ticks Left ============================= */
double rotationTicksLeft(double angle) {
  double perDegree = 4.54;
  return angle * perDegree;
}

double rotationTicksRight(double angle) {
  double perDegree = 4.5;
  return angle * perDegree;
}


/* =============================== Adjusting the Steps ============================= */
//ADJUST THE VALUE HERE TO CONTROL DISTANCE MOVED
//ONE STEP = 1 Block --> define the distance travelled for one block
#define STRAIGHTSTEP 290
#define BACKSTEP 290
#define RIGHTSTEP 400
#define LEFTSTEP 400


void moveStraightStep(double steps) {
  goStraight(steps * STRAIGHTSTEP);
  Serial.print("Moving forward: ");
  Serial.print(steps);
  Serial.println(" blocks");

}

void moveBackStep(double steps) {
  goBack(steps * BACKSTEP);
  Serial.print("Moving backward: ");
  Serial.print(steps);
  Serial.println(" blocks");
}

void moveRightStep(double steps) {
  turnRight(steps * RIGHTSTEP);
  Serial.print("Turning right: ");
  Serial.print(steps);
  Serial.println(" blocks");
}

void moveLeftStep(double steps) {
  turnLeft(steps * LEFTSTEP);
  Serial.print("Turning right: ");
  Serial.print(steps);
  Serial.println(" blocks");
}

/* =============================== Go Straight Until Obstacle Encountered ============================= */
//void goStraightObstacle(double ticks) {
//  straightPID.Compute();
//  getSensorDist();
//  
//  if (front_center_inDistanceCM < 15 || front_left_inDistanceCM < 15 || front_right_inDistanceCM < 15 ){
//    Serial.println("");
//  }
//  else{
//    while ((leftEncoderValue < ticks) && (rightEncoderValue < ticks)) {
//      straightPID.Compute();
//      float LeftEncoderFixed = leftEncoderValue;
//      float LeftEncoderOutput = leftEncoderValue + Output;
//      float RightEncoderOutput = rightEncoderValue;
//      md.setSpeeds((FASTSPEED), (FASTSPEED + Output));
//    }
//    md.setBrakes(FASTSPEED, FASTSPEED+Output);
//    delay(100);
//    rightEncoderRes();
//    leftEncoderRes();
//  }
//}
//
void alignRight() {
  updateSensor();
  float sensor_RF = sensorDist[4];
  float sensor_RB = sensorDist[3];
  float sensorDiff;

  // if robot is too far from wall & there is insufficient wall to align against, don't align
  if ((sensor_RF > 25) || (sensor_RB > 25))
  {
    return;
  }

  sensorDiff = abs(sensor_RF - sensor_RB);
  Serial.println(sensorDiff);

  while ((abs(sensorDist[4]-sensorDist[3]) > 0.06) && (abs(sensorDist[4]-sensorDist[3]) < 9)) {
    if (sensorDist[4] > sensorDist[3]) { //tilting to the left
      md.setSpeeds(100, -100);
      Serial.println("turn left");
    }
    else if (sensorDist[3] > sensorDist[4]) { //tilting to the right
      md.setSpeeds(-100, 100);
      Serial.println("turn right");
    }
    //delay(30);
    updateSensor();
    sensorDiff = abs(sensorDist[4] - sensorDist[3]);
    Serial.println(sensorDiff);
  }
  md.setBrakes(400, 400);

}


// ensure that the front of the robot is straight
//void alignFront() {
//  double rad2deg = 180 / 3.14159;
//
//  double sensor_R_dis;
//  double sensor_L_dis;
//
//  int count = 0;
//
//  double sensorDiff;
//
//  sensor_R_dis = front_left_inDistanceCM;
//  sensor_L_dis = front_center_inDistanceCM;
//
//  sensorDiff = abs(sensor_R_dis - sensor_L_dis);
//
//  while (sensorDiff > 0.2 && sensorDiff < 6) {
//    if (sensor_L_dis > sensor_R_dis) {
//      md.setSpeeds(100, -100);
//    }
//    else if (sensor_R_dis > sensor_L_dis) {
//      md.setSpeeds(-100, 100);
//    }
//    //delay(20);
//    sensor_R_dis = front_left_inDistanceCM;
//    sensor_L_dis = front_right_inDistanceCM;
//    sensorDiff = abs(sensor_R_dis - sensor_L_dis);
//  }
//  md.setBrakes(400, 400);
//}
//
//// robot moves forward if the robot is too far away from the wall
//void moveCloserToWall(double sensor_R_dis, double sensor_L_dis) {
//  while ((sensor_R_dis > 9.5) || (sensor_L_dis > 9.5)) {
//    md.setSpeeds(100, 100);
//    getSensorDist();
//    sensor_R_dis = front_left_inDistanceCM;
//    sensor_L_dis = front_right_inDistanceCM;
//  }
//}
//
//// robot moves backwards if the front is too close to the wall
//void adjustDistance() {
//  double sensor_R_dis = front_left_inDistanceCM;
//  double sensor_L_dis = front_center_inDistanceCM;
//
//  moveCloserToWall(sensor_R_dis, sensor_L_dis);
//  while ((sensor_R_dis < 8.9) || (sensor_L_dis < 8.9)) {
//    md.setSpeeds(-100, -100);
//    sensor_R_dis = front_left_inDistanceCM;
//    sensor_L_dis = front_center_inDistanceCM;
//  }
//  md.setBrakes(400, 400);
//}


float correction[2]={0.00};
float right = correction[0];
float left = correction[1];
float RB;
float RF;


void pullData(){
  updateSensor();
  RB=sensorDist[3];
  RF=sensorDist[4];
  //FC=sensorDist[1];
}


void rightSlantCorrection(){
  pullData();
  float diffrfrb = abs(RF-RB);
  
  if (RF>RB){
    //ADJUST THE ROBOT TO TILT RIGHT
    
    right = right-  diffrfrb*1.00;
    left = left+    diffrfrb*1.00;

    Serial.println(right);
    Serial.println(left);
 
  }

  else if (RF<RB){
    //ADJUST THE ROBOT TO TILT LEFT

    right = right+  diffrfrb*1.00;
    left = left-    diffrfrb*1.00;

    Serial.println(right);
    Serial.println(left);
 
  }
  
  else{
  }

  md.setSpeeds(-100-left, -100-right);
  //md.setBrakes(400,400);
  right =0;
  left=0;
  
}

//float rightDistanceCorrection(){
//  
//  return;
//}
//
//
//float frontSlantCorrection(){
//
//  return;
//}
//
//float frontDistanceCorrection(){
//  
//  return;
//}
//
//float leftCorrection(){
//  
//  return;
//}
//
//float rightCorrection(){
//  
//  return;
//}


void goStraight1(double ticks) {

straightPID.Compute();
  while ((leftEncoderValue < ticks) && (rightEncoderValue < ticks )){
    straightPID.Compute();
    pullData();
    Serial.print(RB);
    Serial.print ("   ");
    Serial.println(RF);
    float diffrfrb = abs(RF-RB);
    
    float LeftEncoderFixed = leftEncoderValue;
    float LeftEncoderOutput = leftEncoderValue + Output ;
    float RightEncoderOutput = rightEncoderValue - Output;

  /** ADJUSTING TILT FROM RIGHT SENSOR**/
    int CONST = 4;

  if (RF>RB && diffrfrb<10){
    //ADJUST THE ROBOT TO TILT RIGHT
    right = right- diffrfrb*CONST;
    left = left+ diffrfrb*CONST;
  }
  else if (RF<RB && diffrfrb<5){
    //ADJUST THE ROBOT TO TILT LEFT
    right = right+diffrfrb*CONST;
    left = left-diffrfrb*CONST;
  }

  /** ADJUSTING DISTANCE FROM FRONT SENSOR**/
  
//  if (10<FC-FRONTBUMPER<40){
//    //ADJUST THE ROBOT TO TILT RIGHT
//
//    float error = (FC-FRONTBUMPER)%10;
//
//    if (error < 5){
//        right = right - error;
//        left = left - error;
//    }
//    else{
//      right = right + error;
//      left = left + error;
//    }
//  }
//
//  else if (10<FL-FRONTBUMPER<40){
//    //ADJUST THE ROBOT TO TILT RIGHT
//
//    float error = (FL-FRONTBUMPER)%10;
//
//    if (error < 5){
//        right = right - error;
//        left = left - error;
//    }
//    else{
//      right = right + error;
//      left = left + error;
//    }
//  }
//    else if (10<FR-FRONTBUMPER<40){
//    //ADJUST THE ROBOT TO TILT RIGHT
//
//    float error = (FR-FRONTBUMPER)%10;
//
//    if (error < 5){
//        right = right - error;
//        left = left - error;
//    }
//    else{
//      right = right + error;
//      left = left + error;
//    }
//  }
//  



    md.setSpeeds(-((FASTSPEED - Output+right)), -((FASTSPEED + Output+left)));

    //md.setSpeeds(50, 350); //left,right
  }
  md.setBrakes(400, 400);
  delay(100);
  rightEncoderRes();
  leftEncoderRes();
  right = 0;
  left = 0;
  
//  }else{
//    Serial.println(front_left_inDistanceCM);
//    Serial.println("Too Close!!");
//  }

}
