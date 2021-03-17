/* Multiplier & Weightage*/
#include "sensor.h"
#include "movement.h"  
DualVNH5019MotorShield md;


float correction[2]={0.00};
float right = correction[0];
float left = correction[1];
float RB;
float RF;


void pullData(){
  updateSensor();
  RB=sensorDist[3];
  RF=sensorDist[4];
}


void rightSlantCorrection(){
  pullData();
  float diffrfrb = abs(RF-RB);
  
  if (RF>RB){
    //ADJUST THE ROBOT TO TILT RIGHT

    
    right = right+diffrfrb*1.00;
    left = left-diffrfrb*1.00;

    Serial.println(right);
    Serial.println(left);
 
  }

  else if (RF<RB){
    //ADJUST THE ROBOT TO TILT LEFT
  }
  
  else{
  }

  md.setSpeeds(-200+left, -200+right);
  
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
