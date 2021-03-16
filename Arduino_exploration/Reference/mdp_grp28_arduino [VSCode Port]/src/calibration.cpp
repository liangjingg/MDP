
// ------------------------------------------------------------
//
//                       CALLIBRATION
//
// ------------------------------------------------------------

#include "calibration.h"

/*
   Function:  initCalibration
   --------------------
   Calibrates the positioning of the robot before exploration starts
   Sensor readings indicate completion

*/
void initCalibration()
{
  // Robot faces down towards the wall
  rotateRight(90);
  // Ensure that minimum distance between robot and right wall is created by reversing (forward is bugged)
  alignFrontToWall(FL, FR);
  wallHug();
  // Robot faces left towards the wall
  rotateRight(90);
  // Ensure that minimum distance between robot and right wall is created by reversing (forward is bugged)
  alignFrontToWall(FL, FR);
  wallHug();
  // Robot faces up, no adjustment should be made since there should be no obstacles
  rotateRight(90);
  alignFrontToWall(FL, FR);
  wallHug();
  // Robot faces right, no adjustment should be made since there should be no obstacles
  rotateRight(90);
  alignFrontToWall(FL, FR);
  // Make sure robot is parallel to right wall for straight movement
  wallHug();
  delay(100);
}

/*
   Function:  manualCalibration
   --------------------
   Calibrates the positioning of the robot during exploration
   Assuming there will always a front wall for robot to calibrate 

*/
void manualCalibration()
{
  // Ensure that minimum distance between robot and right wall is created by reversing (forward is bugged)
  alignFrontToWall(FL, FR);
  wallHug();
}

/*
   Function:  alignFrontToWall
   --------------------
   Ensures FL/FR/FC sensor readings are equal
   Rotate left or right if needed to adjust
   Equal readings indicate FRONT of robot is parallel to wall

*/
void alignFrontToWall(int sensorA, int sensorB)
{
  float readingSensorA = caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE);
  float readingSensorB = caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE);
  float average = (readingSensorA + readingSensorB) / 2;
  float error = readingSensorA - readingSensorB;
  while (average < 3 && abs(error) > 0.2) // 7, 0.2 (less than 0.5)
  {
    // adjustment to ensure front of robot is parallel to wall
    if (error > 0.2)
    {
      rotateSubtleLeft(0.5);
      error = caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE) - caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE);
    }
    else if (error < -0.2)
    {
      rotateSubtleRight(0.5);
      error = caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE) - caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE);
    }
  }
  int count = 0;
  // adjustment to ensure front of robot is of an acceptable range between front and wall
  float avgReading = (caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE) + caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE)) / 2;
  if (average < 9)
  {
    while ((avgReading < 3 && count < 10) || (avgReading > 3.2 && count < 10)) // 3 to 3.2
    {
      if (avgReading < 3)
      {
        reverse(100, 100);
        delay(50);
        brake();
        avgReading = (caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE) + caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE)) / 2;
        count++;
      }
      else if (avgReading > 3.2)
      {
        drive(100, 100);
        delay(50);
        brake();
        avgReading = (caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE) + caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE)) / 2;
        count++;
      }
    }
  }
}

/*
   Function:  wallHug
   --------------------
   Ensures RF and RB sensor readings are equal
   Rotate left or right if needed to adjust
   Equal readings indicate RIGHT of robot is parallel to wall
   Works best when moving a grid forward

*/
void wallHug()
{
  int count = 0; // To set a limit to how much adjustment can be made, if needed
  float RFsensor = caliRawIRSensor(RF, SENSOR_READINGS_COARSE);
  float RBsensor = caliRawIRSensor(RB, SENSOR_READINGS_COARSE);
  float difference = RFsensor - RBsensor;
  // robot only adjusts itself when it senses a wall/obstacle one grid away
  if (RFsensor < 15 && RBsensor < 15)
  {
    // if difference is greater than 0.5cm, the robot is not parallel to the wall, adjust accordingly
    while (abs(difference) > 0.5 && count < 30)
    {
      if (difference > 0)
      {
        rotateSubtleLeft(0.5); // 0.5
        difference = caliRawIRSensor(RF, SENSOR_READINGS_COARSE) - caliRawIRSensor(RB, SENSOR_READINGS_COARSE);
        count++;
      }
      else
      {
        rotateSubtleRight(0.5); //0.5
        difference = caliRawIRSensor(RF, SENSOR_READINGS_COARSE) - caliRawIRSensor(RB, SENSOR_READINGS_COARSE);
        count++;
      }
    }
  }
  count = 0;
}

/*
   Function:  alignableWallCheck
   --------------------
   Each sensor checks for obstacles in front first, then compare if the two obstacles are on the same row.
   If they are on the same row, it's either a 3-length block or a wall, which can be used to align the robot to ensure it is 
   parallel to wall/obstacle
   If not, it is probably a "step" obstacle, not possible to try and make the robot align

*/
int alignableWallCheck()
{
  float leftblock = caliRawIRSensor(FL, SENSOR_READINGS_COARSE);
  float middleblock = caliRawIRSensor(FC, SENSOR_READINGS_COARSE);
  float rightblock = caliRawIRSensor(FR, SENSOR_READINGS_COARSE);
  int output = 0;

  // Check if blocks detected by sensors are on the same row (i.e. not a "step" obstacle)
  // Different output for different sensors to be used for adjustment
  if (leftblock < 11.0 && rightblock < 11.0)
  {
    output = 1;
  }
  // else if (middleblock < 11.0 && rightblock < 11.0)
  // {
  //   output = 2;
  // }
  // else if (leftblock < 11.0 && middleblock < 11.0)
  // {
  //   output = 3;
  // }
  // EXPERIMENTAL STEP CALIBRATION - "upstairs"
  else if ((abs((leftblock - 10) - rightblock) < 4.0 && rightblock < 11.0)) 
  {
    output = 4;
  }
  else if (leftblock < 11.0)
  {
    output = 5;
  }
  else if (middleblock < 11.0)
  {
    output = 6;
  }
  else if (rightblock < 11.0)
  {
    output = 7;
  }
   else if ((abs((leftblock) - (rightblock - 10)) < 4.0 && leftblock < 11.0)) 
  {
    output = 8;
  }
  return output;
}

/*
   Function:  alignFrontToWall
   --------------------
   Ensures FL/FR/FC sensor readings are equal
   Rotate left or right if needed to adjust
   Equal readings indicate FRONT of robot is parallel to wall

*/
void alignFrontToWallSingleSensor(int sensorA)
{
  float readingSensorA = caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE);
  int count = 0;
  if (readingSensorA < 9) {
  // adjustment to ensure front of robot is of an acceptable range between front and wall
  while ((readingSensorA < 3 && count < 10) || (readingSensorA > 3.2 && count < 10)) // 3 to 3.2
  {
    if (readingSensorA < 3)
    {
      reverse(100, 100);
      delay(50);
      brake();
      readingSensorA = caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE);
      count++;
    }
    else if (readingSensorA > 3.2)
    {
      drive(100, 100);
      delay(50);
      brake();
      readingSensorA = caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE);
      count++;
    }
  }
  }
}

/*
   Function:  stepAlignFrontToWall
   --------------------
   Ensures FL/FR/FC sensor readings are equal
   Rotate left or right if needed to adjust
   Equal readings indicate FRONT of robot is parallel to wall
   EXPERIMENTAL 
*/
void stepAlignFrontToWall(int sensorA, int sensorB, int type)
{
  if (type == 1 ) {
    float readingSensorA = (caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE) - 10);
    float readingSensorB = caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE);
    float average = (readingSensorA + readingSensorB) / 2;
    float error = readingSensorA - readingSensorB;
    int counter = 0;
    while (average < 9 && abs(error) < -0.2)
    {
      // adjustment to ensure front of robot is parallel to wall
      if (error > 0.2 && counter < 5)
      {
        rotateSubtleLeft(0.5);
        error = (caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE) - 10) - caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE);
        counter++;
      }
      if (error < -0.2 && counter < 5)
      {
        rotateSubtleRight(0.5);
        error = (caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE) - 10) - caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE);
        counter++;
      }
      if (counter >= 5) {
        counter = 0;
        break;
      }
    }
    int count = 0;
    // adjustment to ensure front of robot is of an acceptable range between front and wall
    float avgReading = ((caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE) - 10) + caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE)) / 2;
    if (average < 9)
    {
      while ((avgReading < 6.8 && count < 10) || (avgReading > 7.0 && count < 10)) 
      {
        if (avgReading < 6.8)
        {
          reverse(100, 100);
          delay(50);
          brake();
          avgReading = ((caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE) - 10)+ caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE)) / 2;
          count++;
        }
        else if (avgReading > 7.0)
        {
          drive(100, 100);
          delay(50);
          brake();
          avgReading = ((caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE) - 10)+ caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE)) / 2;
          count++;
        }
      }
    }
  delay(50);
  }
  else if (type == 2) {
    float readingSensorA = caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE);
    float readingSensorB = (caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE) - 10);
    float average = (readingSensorA + readingSensorB) / 2;
    float error = readingSensorA - readingSensorB;
    int counter = 0;
    while (average < 9 && abs(error) < -0.2)
    {
      // adjustment to ensure front of robot is parallel to wall
      if (error > 0.2 && counter < 5)
      {
        rotateSubtleLeft(0.5);
        error = (caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE)) - (caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE) - 10);
        counter++;
      }
      if (error < -0.2 && counter < 5)
      {
        rotateSubtleRight(0.5);
        error = (caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE)) - (caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE) - 10);
        counter++;
      }
      if (counter >= 5) {
        counter = 0;
        break;
      }
    }
    int count = 0;
    // adjustment to ensure front of robot is of an acceptable range between front and wall
    float avgReading = ((caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE)) + (caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE) - 10)) / 2;
    if (average < 9)
    {
      while ((avgReading < 6.8 && count < 10) || (avgReading > 7.0 && count < 10)) 
      {
        if (avgReading < 6.8)
        {
          reverse(100, 100);
          delay(50);
          brake();
          avgReading = (caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE) + (caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE) - 10)) / 2;
          count++;
        }
        else if (avgReading > 7.0)
        {
          drive(100, 100);
          delay(50);
          brake();
          avgReading = (caliRawIRSensor(sensorA, SENSOR_READINGS_COARSE) + (caliRawIRSensor(sensorB, SENSOR_READINGS_COARSE) - 10)) / 2;
          count++;
        }
      }
    }
  delay(50);
  }
}  

// ------------------------------------------------------------
//
//                  ROTATION FOR CALIBRATION
//
// ------------------------------------------------------------

/*
   Function:  rotateSubtleRight
   --------------------
   Allows robot to rotate in a specified angle to the right
   To be used during callibration

*/
void rotateSubtleRight(float angle)
{
  int rotateDelta;
  if (angle == 0.5)
    rotateDelta = 4;
  else
    rotateDelta = (rightAngleTurnDelta + 10) * (angle / 90);
  leftMotorPower = -rotatePower;
  rightMotorPower = -rotatePower + 10;
  sMotorOffset = -5;
  rMotorOffset = -2;
  rotateDeltaCalculation(rotateDelta);
}

/*
   Function:  rotateSubtleLeft
   --------------------
   Allows robot to rotate in a specified angle to the left
   To be used during callibration

*/
void rotateSubtleLeft(float angle)
{
  int rotateDelta;
  if (angle == 0.5)
    rotateDelta = 3;
  else
    rotateDelta = (rightAngleTurnDelta + 10) * (angle / 90);
  leftMotorPower = rotatePower;
  rightMotorPower = rotatePower - 10;
  sMotorOffset = 5;
  rMotorOffset = 2;
  rotateDeltaCalculation(rotateDelta);
}

/*
   Function:  rotateDeltaCalculation
   --------------------
   To calculate the difference between both wheels and adjust without PID
   To be used during callibration

*/
void rotateDeltaCalculation(int rotateDelta)
{
  int deccel = 250; // decceleration count

  // Reset encoder counts
  motorLeftDelta = 0;
  motorRightDelta = 0;

  // Store previous encoder counts
  motorLeftDelta_prev = motorLeftDelta;
  motorRightDelta_prev = motorRightDelta;

  while ((motorLeftDelta < rotateDelta) && (motorRightDelta < rotateDelta))
  {

    // Sample number of encoder ticks
    motorLeftEncoderTicks = motorLeftDelta;
    motorRightEncoderTicks = motorRightDelta;

    if ((rotateDelta - motorLeftEncoderTicks) < deccel || (rotateDelta - motorRightEncoderTicks) < deccel)
    {
      md.setSpeeds(leftMotorPower / 2, rightMotorPower / 2); //rotate at a slower speed
    }
    else
    {
      md.setSpeeds(leftMotorPower, rightMotorPower);
    }

    // Number of ticks counted since last interrupt
    motorLeftDeltaDifference = motorLeftEncoderTicks - motorLeftDelta_prev;
    motorRightDeltaDifference = motorRightEncoderTicks - motorRightDelta_prev;

    // Store current tick counter
    motorLeftDelta_prev = motorLeftEncoderTicks;
    motorRightDelta_prev = motorRightEncoderTicks;

    // If left is faster, slow it down and speed up right
    if (motorLeftDeltaDifference > motorRightDeltaDifference)
    {
      leftMotorPower -= sMotorOffset;
      rightMotorPower += sMotorOffset;
    }

    // If right is faster, slow it down and speed up left
    else if (motorLeftDeltaDifference < motorRightDeltaDifference)
    {
      leftMotorPower += sMotorOffset;
      rightMotorPower -= sMotorOffset;
    }

    // Adjust speed to make the number of ticks equal
    if ((motorLeftDelta - motorRightDelta) > 5)
    { // M1's rotation is ahead of M2
      rightMotorPower += rMotorOffset;
      if (abs(leftMotorPower) > 110)
        leftMotorPower -= rMotorOffset;
    }
    else if ((motorRightDelta - motorLeftDelta) > 5)
    { // M2's rotation is ahead of M1
      leftMotorPower += rMotorOffset;
      if (abs(rightMotorPower) > 110)
        rightMotorPower -= rMotorOffset;
    }

    delay(20);
  }
  brake();
}