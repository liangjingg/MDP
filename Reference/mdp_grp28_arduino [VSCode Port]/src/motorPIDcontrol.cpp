// ------------------------------------------------------------
//
//                   MOTOR PID CONTROL / TURNS
//
// ------------------------------------------------------------
#include "motorPIDcontrol.h"

/*
   From the software side, in order to achieve straight-line motion, P.I.D. should ensure that:
   No. of left wheel rotations = No. of rightwheel rotations.
   If the number of rotations of the left wheel is greater than right, there is a need to decrease the
   power supplied to the left wheel or increase the power supplied to the right wheel.

   Proportional Term Kp -> The appropriate value by which the error needs to be scaled to reach the desired set point
   Bigger no., the harder the controller pushes.
   Accounts for PRESENT values of error.

   Integral Term Ki -> The integral component of a control loop has the effect of continuing to increase or decrease the output as long as any offset ordroop
   continues to exist.  * This action drives the controller in the direction necessary to eliminate the error caused by the offset.
   Smaller no., the more quickly the controller reacts to load changes, but greater risk of oscillations.
   Accounts for PAST values of error.

   Derivative Term Kd ->This means when an error changes by more than a given percentage during a specified time period, a portion
   of the error is added to the calculated output to boost the output response.  Typically, using derivative action is effective only if the loop can respond
   to a “surge” in the output very quickly.
   Bigger no., the more the controller dampens oscillations (until performance may be hindered).
   Accounts for possible future values of the error based on its current rate of change.
*/

// Constants for forward movement
const float kp_fwd = 1.85;  
const float ki_fwd = 0.015; 
const float kd_fwd = 0.555;
// Constants for forward movement
const float kp_fwdFP = 1.85;  
const float ki_fwdFP = 0.0003122;  //0.0002747
const float kd_fwdFP = 47.755; 
// Constants for left turn
const float kp_left = 8.875;
const float ki_left = 0.02747;
const float kd_left = 47.755;
// Constants for right turn
const float kp_right = 8.875;  
const float ki_right = 0.02747; 
const float kd_right = 47.755;  

/*
   Function:  moveForward
   --------------------
   Allows the robot to move foward desired distance
   With PID control included

*/
void moveForward(int target_distance)
{
  int distanceMultiplier;
  int pidValue;

  switch (target_distance)
  {
  case 1:
    distanceMultiplier = 537; // 532 lounge // 537 SWLAB3
    break;
  case 2:
    distanceMultiplier = 560;
    break;
  case 3:
    distanceMultiplier = 580;
    break;
  case 4:
    distanceMultiplier = 580;
    break;
  case 5:
    distanceMultiplier = 590;
    break;
  case 6:
    distanceMultiplier = 590;
    break;
  case 7:
    distanceMultiplier = 590; // delta = 4130
    break;
  case 8:
    distanceMultiplier = 590;
    break;
  case 9:
    distanceMultiplier = 590; // delta = 5310
    break;
  case 10:
    distanceMultiplier = 600;
    break;
  case 11:
    distanceMultiplier = 600;
    break;
  case 12:
    distanceMultiplier = 600;
    break;
  case 13:
    distanceMultiplier = 600;
    break;
  case 14:
    distanceMultiplier = 600;
    break;
  case 15:
    distanceMultiplier = 600;
    break;
  case 16:
    distanceMultiplier = 600;
    break;
  case 17:
    distanceMultiplier = 600;
    break;
  default:
    distanceMultiplier = 537;
    break;
  }

  target_Delta = distanceMultiplier * target_distance;
  motorLeftDelta = motorRightDelta = 0;
  md.setSpeeds(0, 0);

  while (1)
  {
    // desired distance travelled, time to stop
    if (motorRightDelta > target_Delta)
    {
      brake();
      break;
    }
    pidValue = computePIDForward(motorLeftDelta, motorRightDelta);
    // Speeds up left motor, since it is slower than right
    int output2 = 300 + pidValue;
    drive(250, output2 + 20); // M1 - Right, M2 - Left (KEEP RIGHT AT 250 MAX) // 275
    // Without line below, robot is bugged in an infinite loop. How come?
    String message = ("Left Delta:" + String(motorLeftDelta) + " Right Delta:" + String(motorRightDelta));
    motorRightDelta_prev = motorRightDelta;
  }
  motorLeftDelta = motorRightDelta = 0;
}


/*
   Function:  moveForwardFP
   --------------------
   Allows the robot to move foward desired distance for fastest path
   With PID control included, distinct PID values

*/
void moveForwardFP(int target_distance)
{
  int distanceMultiplier;
  int pidValue;

  switch (target_distance)
  {
  case 1:
    distanceMultiplier = 537; // 532 lounge
    break;
  case 2:
    distanceMultiplier = 560;
    break;
  case 3:
    distanceMultiplier = 580;
    break;
  case 4:
    distanceMultiplier = 580;
    break;
  case 5:
    distanceMultiplier = 590;
    break;
  case 6:
    distanceMultiplier = 590;
    break;
  case 7:
    distanceMultiplier = 590; // delta = 4130
    break;
  case 8:
    distanceMultiplier = 590;
    break;
  case 9:
    distanceMultiplier = 590; // delta = 5310
    break;
  case 10:
    distanceMultiplier = 600;
    break;
  case 11:
    distanceMultiplier = 600;
    break;
  case 12:
    distanceMultiplier = 600;
    break;
  case 13:
    distanceMultiplier = 600;
    break;
  case 14:
    distanceMultiplier = 600;
    break;
  case 15:
    distanceMultiplier = 600;
    break;
  case 16:
    distanceMultiplier = 600;
    break;
  case 17:
    distanceMultiplier = 600;
    break;
  default:
    distanceMultiplier = 537;
    break;
  }

  target_Delta = distanceMultiplier * target_distance;
  motorLeftDelta = motorRightDelta = 0;
  md.setSpeeds(0, 0);
  integral = 0;
  while (1)
  {
    // desired distance travelled, time to stop
    if (motorRightDelta > target_Delta)
    {
      brake();
      // correctWheelDelta();
      break;
    }
    pidValue = computePIDForwardFP(motorLeftDelta, motorRightDelta);
    // Speeds up left motor, since it is slower than right
    drive(250 - pidValue, 380); // M1 - Right, M2 - Left (KEEP RIGHT AT 250 MAX) 
    // Without line below, robot is bugged in an infinite loop. How come?
    String message = ("Left Delta:" + String(motorLeftDelta) + " Right Delta:" + String(motorRightDelta));
    motorRightDelta_prev = motorRightDelta;
  }
  motorLeftDelta = motorRightDelta = 0;
}

/*
   Function:  rotateLeft
   --------------------
   Allows the robot to turn left
   With PID control included

*/
void rotateLeft(int angle)
{
  float offset;
  if (angle == 90)
    offset = -120; // 105 // SWLAB3 = -95 
  else
    offset = 0;
  float target_ticks = (float)2 * (20.0 / ((360.0 / angle) * 6.0)) * 536.0 + offset; // MINUS MORE TURN LESS
  int pidValue;
  motorLeftDelta = motorRightDelta = 0;
  while (1)
  {
    if (motorRightDelta >= target_ticks)
    {
      brake();
      break;
    }
    pidValue = computePIDRight(motorLeftDelta, motorRightDelta);
    md.setSpeeds(-(250 - pidValue), -250); // M1 - Right, M2 - Left
    // Without line below, robot is bugged in an infinite loop. How come?
    String message = ("Left Delta:" + String(motorLeftDelta) + " Right Delta:" + String(motorRightDelta));
  }
  motorLeftDelta = motorRightDelta = 0;
}

/*
   Function:  rotateRight
   --------------------
   Allows the robot to turn right
   With PID control included

*/
void rotateRight(int angle)
{
  float offset;

  if (angle == 90)
    offset = -105; // -95 old value before 04.10
  else
    offset = 0;

  float target_ticks = (float)2 * (20.0 / ((360.0 / angle) * 6.0)) * 536.0 + offset; // MINUS MORE TURN LESS
  int pidValue;
  motorLeftDelta = motorRightDelta = 0;
  while (1)
  {
    if (motorRightDelta >= target_ticks)
    {
      brake();
      break;
    }
    pidValue = computePIDRight(motorLeftDelta, motorRightDelta);
    md.setSpeeds(250 - pidValue, 250);
    // Without line below, robot is bugged in an infinite loop. How come?
    String message = ("Left Delta:" + String(motorLeftDelta) + " Right Delta:" + String(motorRightDelta));
  }
  motorLeftDelta = motorRightDelta = 0;
}

/*
   Function:  computePIDForward
   --------------------
   Based on constants, calculates an output value to change PWM values of motor
   For forward movement

*/
float computePIDForward(int in_motorLeftDelta, int in_motorRightDelta)
{
  int error;
  float integral = 0;
  float derivative, output;

  // error is the difference in speed between the two motors
  error = in_motorRightDelta - in_motorLeftDelta;
  // integral is the cumulative sum of error each time PID function is run. This variable is constrained at a value of 255
  integral += error;
  // derivative (the delta error) is the difference between the current error and the previous error.
  derivative = error - prevError;

  output = kp_fwd * error + ki_fwd * integral + kd_fwd * derivative;
  prevError = error;

  // This output value is fed back to change the left and right PWM values
  return output;
}


/*
   Function:  computePIDForwardFP
   --------------------
   Based on constants, calculates an output value to change PWM values of motor
   For forward movement

*/
float computePIDForwardFP(int in_motorLeftDelta, int in_motorRightDelta)
{
  int error;
  // float integral;
  float derivative, output;

  // error is the difference in speed between the two motors
  error = in_motorRightDelta - in_motorLeftDelta;
  // integral is the cumulative sum of error each time PID function is run. This variable is constrained at a value of 255
  integral += error;
  // derivative (the delta error) is the difference between the current error and the previous error.
  derivative = error - prevError;

  output = kp_fwdFP * error + ki_fwdFP * integral + kd_fwdFP * derivative;
  prevError = error;

  // This output value is fed back to change the left and right PWM values
  return output;
}

/*
   Function:  computePIDRight
   --------------------
   Based on constants, calculates an output value to change PWM values of motor
   For right rotation

*/
float computePIDRight(int in_motorLeftDelta, int in_motorRightDelta)
{
  int error;
  float integral = 0;
  float derivative, output;

  // error is the difference in speed between the two motors
  error = in_motorRightDelta - in_motorLeftDelta;
  // integral is the cumulative sum of error each time PID function is run. This variable is constrained at a value of 255
  integral += error;
  // derivative (the delta error) is the difference between the current error and the previous error.
  derivative = error - prevError;

  output = kp_right * error + ki_right * integral + kd_right * derivative;
  prevError = error;

  // This output value is fed back to change the left and right PWM values
  return output;
}

/*
   Function:  computePIDLeft
   --------------------
   Based on constants, calculates an output valye to change PWM values of motor
   For left rotation

*/
float computePIDLeft(int in_motorLeftDelta, int in_motorRightDelta)
{
  int error;
  float integral = 0;
  float derivative, output;

  // error is the difference in speed between the two motors
  error = in_motorRightDelta - in_motorLeftDelta;
  // integral is the cumulative sum of error each time PID function is run. This variable is constrained at a value of 255
  integral += error;
  // derivative (the delta error) is the difference between the current error and the previous error.
  derivative = error - prevError;

  output = kp_left * error + ki_left * integral + kd_left * derivative;
  prevError = error;

  // This output value is fed back to change the left and right PWM values
  return output;
}