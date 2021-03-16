#include "DualVNH5019MotorShield.h"
#include <SharpIR.h>
#include <EnableInterrupt.h>
#include <PID_v2.h>
#include <ArduinoSort.h>
#include "sensor.h"

#define LEFT_ENCODER 11 //left motor encoder A to pin 5
#define RIGHT_ENCODER 5//right motor encoder A to pin 13
#define FASTSPEED 250
#define SLOWSPEED 200


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

    Serial.print("Output Left: ");
    Serial.print(LeftEncoderOutput);
    Serial.print(", Output Right: ");
    Serial.print(RightEncoderOutput);
    Serial.print(", Diff: ");
    Serial.println(RightEncoderOutput - LeftEncoderOutput);
    Serial.println(Output);
    md.setSpeeds(-((FASTSPEED - Output)), -((FASTSPEED + Output)));

    //md.setSpeeds(50, 350); //left,right
  }
  md.setBrakes(FASTSPEED, FASTSPEED);
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
    md.setSpeeds((FASTSPEED +Output), (FASTSPEED - Output));
  }
  md.setBrakes(FASTSPEED, FASTSPEED);
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
//void alignRight() {
//  getSensorDist_noMsg();
//  
//  double sensor_RF;
//  double sensor_RB;
//
//  double sensorDiff;
//
//  sensor_RF = right_front_inDistanceCM;
//  sensor_RB = right_back_inDistanceCM+0.5; //increase '0.2' if robot tilted right after alignment, dont forget below
//
//  // if robot is too far from wall & there is insufficient wall to align against, don't align
//  if ((sensor_RF > 15) || (sensor_RB > 15))
//  {
//    return;
//  }
//
//  sensorDiff = abs(sensor_RF - sensor_RB);
//  Serial.println(sensorDiff);
//
//  while ((sensorDiff > 0.2) && (sensorDiff < 6)) {
//    if (sensor_RF > sensor_RB) { //tilting to the left
//      md.setSpeeds(100, -100);
//    }
//    else if (sensor_RB > sensor_RF) { //tilting to the right
//      md.setSpeeds(-100, 100);
//    }
//    //delay(30);
//    getSensorDist_noMsg();
//    sensor_RF = right_front_inDistanceCM;
//    sensor_RB = right_back_inDistanceCM;
//    sensorDiff = abs(sensor_RF - sensor_RB);
//  }
//  md.setBrakes(400, 400);
//
//}
//
//
//// ensure that the front of the robot is straight
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
