#include "DualVNH5019MotorShield.h"
#include <SharpIR.h>
#include <EnableInterrupt.h>
#include <PID_v2.h>
#include <ArduinoSort.h>
#include "sensor.h"
//hakim change the ticks for turning to static value

#define LEFT_ENCODER 11 //left motor encoder A to pin 5
#define RIGHT_ENCODER 5//right motor encoder A to pin 13
#define FASTSPEED 340
#define SLOWSPEED 200

#define WALLDIST 5


//tickss parameters for PID
double leftEncoderValue = 0;
double rightEncoderValue = 0;
double difference;                // Use to find the difference
double Setpoint, Input, Output;


PID straightPID(&leftEncoderValue, &Output, &rightEncoderValue, 4.2, 2.0, 0.1, DIRECT); //7.4 // 3.4, 3.0, 0.3 //1.4 0.3
PID leftPID(&leftEncoderValue, &Output, &rightEncoderValue, 1.2, 0.2, 0.0, DIRECT);
PID rightPID(&leftEncoderValue, &Output, &rightEncoderValue, 1.5, 0.3, 0.0, DIRECT); //1.5 0.3
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

float correction[2] = {
  0.00
};
float right = correction[0];
float left = correction[1];
float RB;
float RF;
float FC;
float FL;
float FR;

void pullData() {
  updateSensor();
  RB = sensorDist[3];
  RF = sensorDist[4];
  FC = sensorDist[1];
  FL = sensorDist[2];
  FR = sensorDist[0];
}

int rtolerance = 5;
int ftolerance = 5;

void turnFixedLeft() {
  rightEncoderRes();
  leftEncoderRes();
  leftPID.Compute();
  
  while ((leftEncoderValue < 380) && (rightEncoderValue < 380)) {
    leftPID.Compute();
    md.setSpeeds(-(340 - Output), (340 + Output));
   //   Serial.print("Left Encoder Value: ");
 
    //Serial.println(rightEncoderValue);
  }
  md.setBrakes(400, 400);
  rightEncoderRes();
  leftEncoderRes();
}
void turnFixedRight() {
  rightEncoderRes();
  leftEncoderRes();
  rightPID.Compute();
  while ((leftEncoderValue < 370) && (rightEncoderValue < 370)) {
    rightPID.Compute();
    md.setSpeeds((340 - Output), -(340 + Output));

    //Serial.println(rightEncoderValue);
  }
  md.setBrakes(400, 400);
  leftEncoderRes();
  rightEncoderRes();
}



void checkRightDist(){
    pullData();
    if(RB < rtolerance-2 && RF <rtolerance-2){ //if need to move away from wall
        turnFixedRight();
        delay(300);
        pullData();
        while(FC<WALLDIST){
            md.setSpeeds(100,100); //reverse robot
            pullData();
            //Serial.print("Reversing . ");
            //Serial.println(FC);
        }
        md.setBrakes(400,400);
        turnFixedLeft();
        //delay(500);
    }

    else if (RB < 20 && RF <20){
        float rfmoduluo = fmod(RF-rtolerance,10);
        float rbmoduluo = fmod(RB-rtolerance,10);
        if (rfmoduluo >=3.00 && rfmoduluo <5.00){ //round down
            float target = RF - rfmoduluo;
            turnFixedRight();
            delay(300);
            pullData();
            while (FC>target){
                md.setSpeeds(-100,-100); //accelerate robot
                pullData();
            }
            md.setBrakes(400,400);
            turnFixedLeft();

        }
        else if (rfmoduluo >=5.00 && rfmoduluo <=7.00){
            float target = RF - rfmoduluo + 10.00;
            turnFixedRight();
            delay(300);
            pullData();
            while (FC<target){
                md.setSpeeds(100,100); //reverse robot
                pullData();
            }
            md.setBrakes(400,400);
            turnFixedLeft();

        }
    }
    delay(100);
    rightEncoderRes();
  leftEncoderRes();
}


void checkFrontDist(){
    float walldist = 4.50;
    pullData();

    if (FC <walldist){
        while (FC < walldist){ /////////
            md.setSpeeds(100,100);
            pullData();
        }
        //Serial.println("FC KICK OUT");
        md.setBrakes(400,400);
    }
    else if (FC<15){
             // Serial.println("FC <15 OUT");
        float fcmoduluo = fmod(FC - ftolerance,10);
        if (fcmoduluo >= 2.00 && fcmoduluo <5.00){
//             Serial.println("Moving Forward");
            float target = FC - fcmoduluo;
            //Serial.print("Target: ");
            //Serial.print(target);
            pullData();
            while(FC>target){
                md.setSpeeds(-100,-100);
                pullData();
//                Serial.println("Moving Forward");
            }
            md.setBrakes(400,400);
        }
        else if (fcmoduluo >=5.00 && fcmoduluo <= 8.00){
           //Serial.println("FC >15 OUT");
            float target = FC - fcmoduluo + 10.00;
            pullData();
            while (FC<target){
                md.setSpeeds(100,100);
                pullData();
//                                Serial.println("Moving Backward");

            }
            md.setBrakes(400,400);
        }

    }


    else if (FR <walldist){
        while (FR < walldist){
            md.setSpeeds(100,100);
            pullData();
        }
        //Serial.println("Kick out");
        md.setBrakes(400,400);
    }
    else if (FR<15){
        float frmoduluo = fmod(FR - ftolerance,10);
        if (frmoduluo >= 2.00 && frmoduluo <5.00){
//             Serial.println("Moving Forward");
            float target = FR - frmoduluo;
            //Serial.print("Target: ");
            //Serial.print(target);
            pullData();
            while(FR>target){
                md.setSpeeds(-100,-100);
                pullData();
//                Serial.println("Moving Forward");
            }
            md.setBrakes(400,400);
        }
        else if (frmoduluo >=5.00 && frmoduluo <= 8.00){
          
            float target = FR - frmoduluo + 10.00;
            pullData();
            while (FR<target){
                md.setSpeeds(100,100);
                pullData();
//                             Serial.println("Moving Backward");

            }
            md.setBrakes(400,400);
        }

    }


    

}

void checkFrontAlign(){
  pullData();
  float difference = abs(FR-FL);
  //Serial.println(difference);

  if (difference > 6 || FR > 25 || FL >25){
    return;
  }

  else{
    if (FR>FL && difference > 0.15){
      while(FR>FL){
        md.setSpeeds(-70,70);
        pullData();
      }
      md.setBrakes(400,400);
    }
    else if (FL>FR && difference > 0.15 ){
      while(FL>FR){
        md.setSpeeds(70,-70);
        pullData();
      }
      md.setBrakes(400,400);
    }
  }

  rightEncoderRes();
  leftEncoderRes();
}

void checkRightAlign(){
    pullData();
    float difference = abs(RB-RF);

//    Serial.println(difference);
//    Serial.println("");

    if (RB >25 || RF >25){
        return;
    }
    else if (difference < 6){ //same x-wall adjustment
        if (RB>RF && difference > 0.06){
            while(RB>RF){
            md.setSpeeds(-60,60);
            pullData();
            }
       
            md.setBrakes(400,400);
            //Serial.println("Turning Left");

        }
        else if (RF>RB && difference >0.06){
            while (RB<RF){
                md.setSpeeds(60,-60);
                pullData();
            }
            md.setBrakes(400,400);
            //Serial.println("Turning Right");
        }
    }

    else {
      float modrb = fmod(RB,10);
      float modrf = fmod(RF,10);
      difference = abs(modrb-modrf);

      if (modrb>modrf && difference > 0.1){
            while(modrb>modrf){
            md.setSpeeds(-60,60);
            pullData();
            modrb = fmod(RB,10);
            modrf = fmod(RF,10);
            }
       
            md.setBrakes(400,400);
            //Serial.println("Turning Left");

        }
        else if (modrf>modrb && difference >0.1){
            while (modrb<modrf){
                md.setSpeeds(60,-60);
                pullData();
                modrb = fmod(RB,10);
                modrf = fmod(RF,10);
            }
            md.setBrakes(400,400);
            //Serial.println("Turning Right");
        }
      
    }
    rightEncoderRes();
  leftEncoderRes();
}

void uTurn(){
  rightEncoderRes();
  leftEncoderRes();
  checkFrontDist();
  rightEncoderRes();
  leftEncoderRes();
  checkRightAlign();
  leftEncoderRes();
  rightEncoderRes();
  leftPID.Compute();
  while ((leftEncoderValue < 800) && (rightEncoderValue < 800)) {
    leftPID.Compute();
    md.setSpeeds(-(FASTSPEED - Output), (FASTSPEED + Output));
  }
  md.setBrakes(400, 400);
  delay(200);
  rightEncoderRes();
  leftEncoderRes();
  checkRightAlign();
  rightEncoderRes();
  leftEncoderRes();
}


void goStraight(double ticks) {
  rightEncoderRes();
  leftEncoderRes();
  checkRightAlign();
  rightEncoderRes();
  leftEncoderRes();
  checkRightDist();
  rightEncoderRes();
  leftEncoderRes();
  checkRightAlign();
  rightEncoderRes();
  leftEncoderRes();
  straightPID.Compute();
  while ((leftEncoderValue < ticks) && (rightEncoderValue < ticks)) {
  
    
    straightPID.Compute();
    float LeftEncoderFixed = leftEncoderValue;
    float LeftEncoderOutput = leftEncoderValue + Output;
    float RightEncoderOutput = rightEncoderValue - Output;
//    Serial.print("Right ");
//    Serial.println(RightEncoderOutput);
//    Serial.print("Left ");
//    Serial.println(LeftEncoderOutput);
//    Serial.println(Output);
    
    md.setSpeeds(-((FASTSPEED - Output)), -((FASTSPEED + Output+20)));
  }
  md.setBrakes(400, 400);
  rightEncoderRes();
  leftEncoderRes();
  checkFrontDist();
  rightEncoderRes();
  leftEncoderRes();

}

void goBack(double ticks) {
  straightPID.Compute();

  while ((leftEncoderValue < 350) && (rightEncoderValue < 350)) {

    straightPID.Compute();
    md.setSpeeds((FASTSPEED - Output), (FASTSPEED + Output));
  }
  md.setBrakes(400, 400);
  delay(100);
  rightEncoderRes();
  leftEncoderRes();
}

/* =============================== Turn Left & Turn Right ============================= */
void turnLeft(double ticks) {
  leftEncoderRes();
  rightEncoderRes();  
  checkFrontDist();
  checkRightAlign();
  leftEncoderRes();
  rightEncoderRes();
  leftPID.Compute();
  while ((leftEncoderValue < ticks) && (rightEncoderValue < ticks)) {
    leftPID.Compute();
    md.setSpeeds(-(FASTSPEED - Output), (FASTSPEED + Output));
  }
  md.setBrakes(400, 400);
  delay(200);
  rightEncoderRes();
  leftEncoderRes();
  checkRightAlign();
}


void turnRight(double ticks) {
  leftEncoderRes();
  rightEncoderRes(); 
  checkFrontDist();
  checkRightAlign();
  leftEncoderRes();
  rightEncoderRes();
  rightPID.Compute();

  while ((leftEncoderValue < ticks) && (rightEncoderValue < ticks)) {
    rightPID.Compute();
    md.setSpeeds((FASTSPEED - Output), -(FASTSPEED + Output));
  }
  md.setBrakes(400, 400);
  delay(200);
  leftEncoderRes();
  rightEncoderRes();
  checkRightAlign();

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
