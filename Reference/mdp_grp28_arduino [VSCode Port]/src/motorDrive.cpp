// ------------------------------------------------------------
//
//                        MOTOR DRIVE
//
// ------------------------------------------------------------

#include "motorDrive.h"

/*
   Function:  drive
   --------------------
   Drive forward. What else to say. :|
   To be used during exploration

*/
void drive(int power_a, int power_b) {
  md.setM2Speed(power_b);  // Set speed for M2 - Left
  md.setM1Speed(-(power_a));     // Set speed for M1 - Right
}


/*
   Function:  reverse
   --------------------
   Drive backwards. You get it. :)
   To be used during exploration

*/
void reverse(int power_a, int power_b) {
  md.setM1Speed(power_a);     // Set speed for M1 - Right
  md.setM2Speed(-power_b);  // Set speed for M2 - Left
}


/*
   Function:  brake
   --------------------
   Stops robot. I need a break! :o
   To be used during exploration

*/
void brake() {
  md.setBrakes(380, 380);
  delay(25);
}

/*
   Function:  correctWheelDelta
   --------------------
   Adjusts right wheel forward or backward to make up for differences in delta
   To be used during fastest path only

*/
void correctWheelDelta() {
  // limit the number of adjustments allowed
  int limit = 6;
  while (abs((motorRightDelta) - (motorLeftDelta)) > 2 && limit > 0) {
    if ((motorLeftDelta) > (motorRightDelta)) {
      // md.setSpeeds(0, -120);
      md.setSpeeds(120, 0); // Right wheel backward
      delay(25);
      md.setSpeeds(0, 0);
      delay(20);
    } else {
      md.setSpeeds(-120, 0); // Right wheel forward
      delay(25);
      md.setSpeeds(0, 0);
      delay(20);
    }
    limit--;
    delay(20);
  }
}

/*
   Function:  updateRightMotorDelta
   --------------------
   Increments encoder ticks for right motor

*/
void updateRightMotorDelta() {
  motorRightDelta++;
}

/*
   Function:  updateLeftMotorDelta
   --------------------
   Increments encoder ticks for left motor

*/
void updateLeftMotorDelta() {
  motorLeftDelta++;
}