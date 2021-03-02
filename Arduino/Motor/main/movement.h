#include "DualVNH5019MotorShield.h"
#include <SharpIR.h>
#include <EnableInterrupt.h>
#include <PID_v2.h>
#include <ArduinoSort.h>

#define LEFT_ENCODER 13 //left motor encoder A to pin 5
#define RIGHT_ENCODER 5//right motor encoder A to pin 13
#define FASTSPEED 350
#define SLOWSPEED 200



//ticks parameters for PID
double leftEncoderValue = 0;
double rightEncoderValue = 0;
double difference;                // Use to find the difference
double Setpoint, Input, Output;


PID straightPID(&leftEncoderValue, &Output, &rightEncoderValue, 0.8, 0.0, 0.0, DIRECT); //7.4 // 3.4, 3.0, 0.3 //1.4 0.3
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

  while ((leftEncoderValue < ticks) && (rightEncoderValue < ticks)){
    straightPID.Compute();
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
    md.setSpeeds(((FASTSPEED + Output)), ((FASTSPEED - Output)));

    //md.setSpeeds(50, 350); //left,right
  }
  md.setBrakes(FASTSPEED, FASTSPEED);
  delay(100);
  rightEncoderRes();
  leftEncoderRes();

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
    md.setSpeeds(-(FASTSPEED +Output), -(FASTSPEED - Output));
  }
  md.setBrakes(FASTSPEED, FASTSPEED);
  delay(100);
  rightEncoderRes();
  leftEncoderRes();

}


/* =============================== Turn Left & Turn Right ============================= */
void turnLeft(double ticks) {
  leftPID.Compute();

  while ((leftEncoderValue < ticks) && (rightEncoderValue < ticks))
  {
    leftPID.Compute();

    md.setSpeeds(-(FASTSPEED+ Output), (FASTSPEED - Output));
  }
  md.setBrakes(FASTSPEED, FASTSPEED);
  delay(100);
  rightEncoderRes();
  leftEncoderRes();
}

void turnRight(double ticks) {
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
  delay(100);
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
