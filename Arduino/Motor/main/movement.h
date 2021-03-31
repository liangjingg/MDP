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


PID straightPID(&leftEncoderValue, &Output, &rightEncoderValue, 6.9,0.3 , 0.0, DIRECT); //2.3,1.0 5.9
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


void turnFixedLeft() {
  rightEncoderRes();
  leftEncoderRes();
  
  leftPID.Compute();
  while ((leftEncoderValue < 380) && (rightEncoderValue < 380)) {
    leftPID.Compute();
    md.setSpeeds(-(FASTSPEED - Output), (FASTSPEED + Output));
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
    md.setSpeeds((FASTSPEED - Output), -(FASTSPEED + Output));
    rightPID.Compute();
    //Serial.println(rightEncoderValue);
  }
  md.setBrakes(400, 400);
  
  leftEncoderRes();
  rightEncoderRes();
}


void checkFrontDist(){
  delay(300);
  pullData();
  //Serial.println(FL);
  if (FL < 5){
    while (FL<4.8){
      md.setSpeeds(110,100);
      pullData();
    }
    md.setBrakes(400,400);
  }
  else if (FC < 5){
    while (FC<4.8){
      md.setSpeeds(110,100);
      pullData();
    }
    md.setBrakes(400,400);
  }
  else if (FR < 5){
    while (FR<4.8){
      md.setSpeeds(110,100);
      pullData();
    }
    md.setBrakes(400,400);
  }

//  else if (FL < 13 && FL >10){
//    while (FL<15){
//      //Serial.print("FL WORKS");
//      md.setSpeeds(110,100);
//      pullData();
//    }
//    md.setBrakes(400,400);
//  }
//   else if (FC < 13 && FC >10){
//    while (FC<15){
//      md.setSpeeds(110,100);
//      pullData();
//    }
//    md.setBrakes(400,400);
//  }
//  else if (FR < 13 && FR >10){
//    while (FR<15){
//      md.setSpeeds(110,100);
//      pullData();
//    }
//    md.setBrakes(400,400);
//  }

  //MOVE FORWARD

  else if (FL >5 && FL<10){
    while (FL>5.2){
      md.setSpeeds(-110,-100);
      pullData();
    }
    md.setBrakes(400,400);
  }
  else if (FC >5 && FC<10){
    while (FC>5.2){
      md.setSpeeds(-110,-100);
      pullData();
    }
    md.setBrakes(400,400);
  }
  else if (FR >5 && FR<10){
    while (FR>5.2){
      md.setSpeeds(-110,-100);
      pullData();
    }
    md.setBrakes(400,400);
  }

//  else if (FL >13 && FL<25){
//    while (FL>15){
//      md.setSpeeds(-110,-100);
//      pullData();
//    }
//    md.setBrakes(400,400);
//  }
//  else if (FC >13 && FC<20){
//    while (FC>15){
//      md.setSpeeds(-110,-100);
//      pullData();
//    }
//    md.setBrakes(400,400);
//  }
//  else if (FR >13 && FR<20){
//    while (FR>15){
//      md.setSpeeds(-110,-100);
//      pullData();
//    }
//    md.setBrakes(400,400);
//  }

  
}



void checkRightDist(){
  delay(100);
  int rtolerance = 5;
    pullData();
    
    if(RB < rtolerance-2){ //if need to move away from wall
        turnFixedRight();
        delay(300);
        pullData();
        while(FR<4.5){

            md.setSpeeds(110,100); //reverse robot
            pullData();
        }
        md.setBrakes(400,400);
        delay(100);
        turnFixedLeft();
        //delay(500);
    }
      else if(RF <rtolerance-2){ //if need to move away from wall
      turnFixedRight();
      delay(300);
      pullData();
      while(FL<4.5){

          md.setSpeeds(110,100); //reverse robot
          pullData();
      }
      md.setBrakes(400,400);
        delay(100);
      
      turnFixedLeft();
      //delay(500);
    }

    else if (RB > 10 && RB <15){
      turnFixedRight();
      delay(300);
      pullData();
      while(FR<14.5){

          md.setSpeeds(110,100); //reverse robot
          pullData();
      }
      md.setBrakes(400,400);
        delay(100);
      turnFixedLeft();
    }

    else if (RF > 10 && RF <15){
      turnFixedRight();
      delay(300);
      pullData();
      while(FL<14.5){

          md.setSpeeds(110,100); //reverse robot
          pullData();
      }
      md.setBrakes(400,400);
        delay(100);
      turnFixedLeft();
    }


    else if (RF > 7 && RF <10){
      turnFixedRight();
      delay(300);
      pullData();
      while (FL>4.5){
        md.setSpeeds(-110,-100);
        pullData();
      }
      md.setBrakes(400,400);
        delay(100);
      turnFixedLeft();


    }
    else if (RB > 7 && RB <10){
      turnFixedRight();
      delay(300);
      pullData();
      while (FR>4.5){
        //Serial.println(FR);
        md.setSpeeds(-110,-100);
        pullData();
      }
      md.setBrakes(400,400);
        delay(100);
      turnFixedLeft();


    }

      else if (RF > 17 && RF <20){
      turnFixedRight();
      delay(300);
      pullData();
      while (FL>14.5){
        md.setSpeeds(-110,-100);
        delay(100);
        pullData();
      }
      md.setBrakes(400,400);
      turnFixedLeft();


    }
    else if (RB > 17 && RB <20){
      turnFixedRight();
      delay(300);
      pullData();
      while (FR>14.5){
        md.setSpeeds(-110,-100);
         delay(100);
       pullData();
      }
      md.setBrakes(400,400);
      turnFixedLeft();


    }

    delay(100);
    rightEncoderRes();
    leftEncoderRes();
}


void checkFrontAlign(){
    pullData();
    float d = 8.00; //this is the minimal distance to do one block same plane allignment
    float d2 = 18.00; //this is the minimal distance to do two block allignement (stairs)
    float tolerance = 0.2;

    /** ALLIGN ONE BLOCK SAME LEVEL PLANE **/
    //FL VS FR
    if(FL<d && FR<d){ 
        if(FL<FR && abs(FL-FR)>tolerance){
            while(FL<FR){
                md.setSpeeds(-90,80); //right wheel turn more
                pullData();
            }
            md.setBrakes(400,400);
        }
        else if(FL>FR && abs(FL-FR)>tolerance){
            while(FL>FR){
                md.setSpeeds(90,-80); //left wheel turn more
                pullData();
            }
            md.setBrakes(400,400);
        }
    }
    //FL vs FC
    else if(FL<d && FC<d){
        if(FL<FC && abs(FL-FC)>tolerance){
            while(FL<FC){
                md.setSpeeds(-90,80); //right wheel turn more
                pullData();
            }
            md.setBrakes(400,400);
        }
        else if(FL>FC && abs(FL-FC)>tolerance){
            while(FL>FC){
                md.setSpeeds(90,-80); //left wheel turn more
                pullData();
            }
            md.setBrakes(400,400);
        }
    }
    //FR VS FC
    else if(FR<d && FC<d){
        if(FR<FC && abs(FR-FC)>tolerance){
            while(FR<FC){
                md.setSpeeds(90,-80); //left wheel turn more
                pullData();
            }
            md.setBrakes(400,400);
        }
        else if(FR>FC && abs(FL-FC)>tolerance){
            while(FR>FC){
                md.setSpeeds(-90,80); //right wheel turn more
                pullData();
            }
            md.setBrakes(400,400);
        }
    }

    /** ALLIGN ONE BLOCK SAME LEVEL PLANE **/
//    else if ( ((FL<d2) && (FR<d2)) || ((FL<d2) && (FC<d2)) || ((FR<d2) && (FC<d2)) ){ //make sure there is valid pairs that are not more than d2
//        float modfl = fmod(FL,10);
//        float modfc = fmod(FC,10);
//        float modfr = fmod(FR,10);
//
//        // modfl vs modfr
//        if (modfl<modfr && abs(modfl-modfr)>tolerance){
//            while (modfl<modfr){
//                md.setSpeeds(60,-60); //move closer to fr sensor
//                pullData();
//                modfl = fmod(FL,10);
//                modfc = fmod(FC,10);
//                modfr = fmod(FR,10);
//            }
//            md.setBrakes(400,400);
//        }
//        else if (modfl>modfr && abs(modfl-modfr)>tolerance){
//            while (modfl>modfr){
//                md.setSpeeds(-60,60); //move closer to fl sensor
//                pullData();
//                modfl = fmod(FL,10);
//                modfc = fmod(FC,10);
//                modfr = fmod(FR,10);
//            }
//            md.setBrakes(400,400);
//        }
//
//        // modfl vs modfc
//        else if (modfl<modfc && abs(modfl-modfc)>tolerance){
//            while (modfl<modfc){
//                md.setSpeeds(60,-60); //move closer to fr sensor
//                pullData();
//                modfl = fmod(FL,10);
//                modfc = fmod(FC,10);
//                modfr = fmod(FR,10);
//            }
//            md.setBrakes(400,400);
//        }
//        else if (modfl>modfc && abs(modfl-modfc)>tolerance){
//            while (modfl>modfc){
//                md.setSpeeds(-60,60); //move closer to fl sensor
//                pullData();
//                modfl = fmod(FL,10);
//                modfc = fmod(FC,10);
//                modfr = fmod(FR,10);
//            }
//            md.setBrakes(400,400);
//        }
//
//        // modfc vs modfr
//        else if (modfc<modfr && abs(modfc-modfr)>tolerance){
//            while (modfc<modfr){
//                md.setSpeeds(60,-60); //move closer to fr sensor
//                pullData();
//                modfl = fmod(FL,10);
//                modfc = fmod(FC,10);
//                modfr = fmod(FR,10);
//            }
//            md.setBrakes(400,400);
//        }
//        else if (modfc>modfr && abs(modfc-modfr)>tolerance){
//            while (modfc>modfr){
//                md.setSpeeds(-60,60); //move closer to fl sensor
//                pullData();
//                modfl = fmod(FL,10);
//                modfc = fmod(FC,10);
//                modfr = fmod(FR,10);
//            }
//            md.setBrakes(400,400);
//        }
//    }
}

void checkRightAlign(){
    pullData();
    float difference = abs(RB-RF);

    if (RB >20 || RF >20){
        return;
    }
    else if (difference < 6){ //same x-wall adjustment
        if (RB>RF && difference > 0.06){
            while(RB>RF  ){
            md.setSpeeds(-60,60);
            pullData();
            }
       
            md.setBrakes(400,400);
            //Serial.println("Turning Left");

        }
        else if (RF>RB && difference >0.06){
            while (RB<RF ){
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
            while(modrb>modrf ){
            md.setSpeeds(-60,60);
            pullData();
            modrb = fmod(RB,10);
            modrf = fmod(RF,10);

            }
       
            md.setBrakes(400,400);
            //Serial.println("Turning Left");

        }
        else if (modrf>modrb && difference >0.1){
            while (modrb<modrf ){
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
    md.setSpeeds(-(FASTSPEED - Output), (FASTSPEED + Output));
    leftPID.Compute();

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
  straightPID.Compute();
  while ((leftEncoderValue < ticks) && (rightEncoderValue < ticks)) {
    
    float LeftEncoderFixed = leftEncoderValue;
    float LeftEncoderOutput = leftEncoderValue + Output;
    float RightEncoderOutput = rightEncoderValue - Output;
    md.setSpeeds(-((FASTSPEED - Output)), -((FASTSPEED + 0.5*Output)));
    straightPID.Compute();
  }
  md.setBrakes(400, 400);
  delay(100);
  checkRightAlign();//
  delay(100);
  checkRightDist();//
  delay(100);
  checkRightAlign();//
  delay(100);
  checkFrontDist();
  delay(100);
  rightEncoderRes();
  leftEncoderRes();

}

void goBack(double ticks) {
  straightPID.Compute();

  while ((leftEncoderValue < 350) && (rightEncoderValue < 350)) {

    md.setSpeeds((FASTSPEED - Output), (FASTSPEED + Output));
        straightPID.Compute();
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
  leftPID.Compute();
  while ((leftEncoderValue < ticks) && (rightEncoderValue < ticks)) {
    md.setSpeeds(-(FASTSPEED - Output), (FASTSPEED + Output));
    leftPID.Compute();

  }
  md.setBrakes(400, 400);
  delay(200);
  rightEncoderRes();
  leftEncoderRes();
  checkRightAlign();
  delay(100);
  checkFrontDist();
  delay(100);
}


void turnRight(double ticks) {
  leftEncoderRes();
  rightEncoderRes();
  rightPID.Compute();

  while ((leftEncoderValue < ticks) && (rightEncoderValue < ticks)) {
    md.setSpeeds((FASTSPEED - Output), -(FASTSPEED + Output));
    rightPID.Compute();

  }
  md.setBrakes(400, 400);
  delay(200);
   rightEncoderRes();
  leftEncoderRes();
  checkRightAlign();
  delay(100);
  checkFrontDist();
  delay(100);

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
