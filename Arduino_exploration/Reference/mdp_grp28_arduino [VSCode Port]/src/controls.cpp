// ====================================================================================
//                           CONTROLS
//
// W_   : [Fastest Path] Move forward for distance _ without alignment
// A    : [Fastest Path] Turn right for 90 degrees without alignment
// D    : [Fastest Path] Turn left for 90 degrees without alignment
//
// F    : [Exploration] Move forward for one step with alignment
// R    : [Exploration] Turn right for 90 degrees with alignment
// L    : [Exploration] Turn left for 90 degrees with alignment
//
// C    : [Exploration] Initial calibration
// Q    : [Exploration] Force recalibration during exploration when robot faces a wall
// S    : [Exploration] Send cached sensor values in weighted values
// X    : [DEBUG] Send sensor values in weighted values and cm
// ====================================================================================

#include "controls.h"
#include "calibration.h"
#include "motorDrive.h"
#include "motorPIDcontrol.h"
#include "sensors.h"
#include "variables.h"

bool readCommand()
{
  cmd_in = 0;
  // This is data that is already arrived and stored in the serial receive buffer (which holds 64 bytes)
  // while receiving data, do the block below.
  while (Serial.available() > 0)
  {
    cmd[cmd_in] = Serial.read();
    cmd_in++;

    // exceed buffer, stop reading additional commands
    if (cmd_in >= 100)
      break;
  }
  if (cmd_in > 0)
    return true;
  else
    return false;
}

void executeCommand()
{
  char letter = cmd[cmd_out];

  // fastest path straight
  if (letter == 'W')
  {
    // cmd_out is set to 1
    cmd_out++;
    // reads the second char to determine the distance specified
    char firstDigit = cmd[cmd_out];
    if (firstDigit == '0')
    {
      cmd_out++;
      char secondDigit = cmd[cmd_out];
      switch (secondDigit)
      {
      case '1':
        moveForwardFP(1);
        break;
      case '2':
        moveForwardFP(2);
        break;
      case '3':
        moveForwardFP(3);
        break;
      case '4':
        moveForwardFP(4);
        break;
      case '5':
        moveForwardFP(5);
        break;
      case '6':
        moveForwardFP(6);
        break;
      case '7':
        moveForwardFP(7);
        break;
      case '8':
        moveForwardFP(8);
        break;
      case '9':
        moveForwardFP(9);
        break;
      default:
        break;
      }
    }
    else if (firstDigit == '1')
    {
      cmd_out++;
      char secondDigit = cmd[cmd_out];
      switch (secondDigit)
      {
      case '0':
        moveForwardFP(10);
        break;
      case '1':
        moveForwardFP(11);
        break;
      case '2':
        moveForwardFP(12);
        break;
      case '3':
        moveForwardFP(13);
        break;
      case '4':
        moveForwardFP(14);
        break;
      case '5':
        moveForwardFP(15);
        break;
      case '6':
        moveForwardFP(16);
        break;
      case '7':
        moveForwardFP(17);
        break;
      default:
        break;
      }
    }
    delay(50);
    // Align right side of robot to right wall first
    wallHug();
    // Then, based on obstacle in front, align robot to front
    int result = alignableWallCheck();
    switch (result)
    {
    case 1:
      alignFrontToWall(FL, FR);
      break;
    // case 2:
    //   alignFrontToWall(FC, FR);
    //   break;
    // case 3:
    //   alignFrontToWall(FL, FC);
    //   break;
    // case 4:
    //   stepAlignFrontToWall(FC, FR, 1);
    //   break;
    case 5:
      alignFrontToWallSingleSensor(FL);
      break;
    case 6:
      alignFrontToWallSingleSensor(FC);
      break;
    case 7:
      alignFrontToWallSingleSensor(FR);
      break;
    // case 8:
    //   stepAlignFrontToWall(FC, FR, 2);
    //   break;
    default:
      break;
    }
    Serial.println("P:ACK");
  }

  /// fastest path left
  else if (letter == 'A')
  {
    // cmd_out is set to 1
    cmd_out++;
    // reads the first digit 
    char firstDigit = cmd[cmd_out];
    // Keep track of number of rotations 
    int rotationCount = 0;
    // if "F0_"
    if (firstDigit == '0')
    {
      // cmd_out is set to 2 
      cmd_out++;
      // reads the second digit
      char secondDigit = cmd[cmd_out];
      switch (secondDigit)
      {
      case '1': // A01
        rotationCount = 1;
        break;
      case '2': // A02
        rotationCount = 2;
        break;
      case '3': // A03
        rotationCount = 3;
        break;
      case '4': // A04
        rotationCount = 4;
        break;
      default:
        break;
      }
    }
    for (int i = 0; i < rotationCount; i++)
    {
      rotateLeft(90);
      delay(50);
    }
    delay(50);
    // Align right side of robot to right wall first
    wallHug();
    // Then, based on obstacle in front, align robot to front
    int result = alignableWallCheck();
    switch (result)
    {
    case 1:
      alignFrontToWall(FL, FR);
      break;
    // case 2:
    //   alignFrontToWall(FC, FR);
    //   break;
    // case 3:
    //   alignFrontToWall(FL, FC);
    //   break;
    // case 4:
    //   stepAlignFrontToWall(FC, FR, 1);
    //   break;
    case 5:
      alignFrontToWallSingleSensor(FL);
      break;
    case 6:
      alignFrontToWallSingleSensor(FC);
      break;
    case 7:
      alignFrontToWallSingleSensor(FR);
      break;
    // case 8:
    //   stepAlignFrontToWall(FC, FR, 2);
    //   break;
    default:
      break;
    }
    Serial.println("P:ACK");
  }

  // fastest path right
  else if (letter == 'D')
  {
    // cmd_out is set to 1
    cmd_out++;
    // reads the first digit 
    char firstDigit = cmd[cmd_out];
    // Keep track of number of rotations 
    int rotationCount = 0;
    // if "D0_"
    if (firstDigit == '0')
    {
      // cmd_out is set to 2 
      cmd_out++;
      // reads the second digit
      char secondDigit = cmd[cmd_out];
      switch (secondDigit)
      {
      case '1': // D01
        rotationCount = 1;
        break;
      case '2': // D02
        rotationCount = 2;
        break;
      case '3': // D03
        rotationCount = 3;
        break;
      case '4': // D04
        rotationCount = 4;
        break;
      default:
        break;
      }
    }
    for (int i = 0; i < rotationCount; i++)
    {
      rotateRight(90);
      delay(50);
    }
    delay(50);
    // Align right side of robot to right wall first
    wallHug();
    // Then, based on obstacle in front, align robot to front
    int result = alignableWallCheck();
    switch (result)
    {
    case 1:
      alignFrontToWall(FL, FR);
      break;
    // case 2:
    //   alignFrontToWall(FC, FR);
    //   break;
    // case 3:
    //   alignFrontToWall(FL, FC);
    //   break;
    // case 4:
    //   stepAlignFrontToWall(FC, FR, 1);
    //   break;
    case 5:
      alignFrontToWallSingleSensor(FL);
      break;
    case 6:
      alignFrontToWallSingleSensor(FC);
      break;
    case 7:
      alignFrontToWallSingleSensor(FR);
      break;
    // case 8:
    //   stepAlignFrontToWall(FC, FR, 2);
    //   break;
    default:
      break;
    }
    Serial.println("P:ACK");
  }

  // exploration forward one step only
  else if (letter == 'F')
  {
    moveForward(1);
    delay(50);
    // Align right side of robot to right wall first
    wallHug();
    // Then, based on obstacle in front, align robot to front
    int result = alignableWallCheck();
    switch (result)
    {
    case 1:
      alignFrontToWall(FL, FR);
      break;
    // case 2:
    //   alignFrontToWall(FC, FR);
    //   break;
    // case 3:
    //   alignFrontToWall(FL, FC);
    //   break;
    // case 4:
    //   stepAlignFrontToWall(FC, FR, 1);
    //   break;
    case 5:
      alignFrontToWallSingleSensor(FL);
      break;
    case 6:
      alignFrontToWallSingleSensor(FC);
      break;
    case 7:
      alignFrontToWallSingleSensor(FR);
      break;
    // case 8:
    //   stepAlignFrontToWall(FC, FR, 2);
    //   break;
    default:
      break;
    }
    Serial.println("P:" + getAllIRSensor());
  }

  // exploration left
  else if (letter == 'L')
  {
    rotateLeft(90);
    delay(50);
    wallHug();
    int result = alignableWallCheck();
    switch (result)
    {
    case 1:
      alignFrontToWall(FL, FR);
      break;
    // case 2:
    //   alignFrontToWall(FC, FR);
    //   break;
    // case 3:
    //   alignFrontToWall(FL, FC);
    //   break;
    // case 4:
    //   stepAlignFrontToWall(FC, FR, 1);
    //   break;
    case 5:
      alignFrontToWallSingleSensor(FL);
      break;
    case 6:
      alignFrontToWallSingleSensor(FC);
      break;
    case 7:
      alignFrontToWallSingleSensor(FR);
      break;
    // case 8:
    //   stepAlignFrontToWall(FC, FR, 2);
    //   break;
    default:
      break;
    }
    Serial.println("P:" + getAllIRSensor());
  }

  // exploration right
  else if (letter == 'R')
  {
    rotateRight(90);
    delay(50);
    wallHug();
    int result = alignableWallCheck();
    switch (result)
    {
    case 1:
      alignFrontToWall(FL, FR);
      break;
    // case 2:
    //   alignFrontToWall(FC, FR);
    //   break;
    // case 3:
    //   alignFrontToWall(FL, FC);
    //   break;
    // case 4:
    //   stepAlignFrontToWall(FC, FR, 1);
    //   break;
    case 5:
      alignFrontToWallSingleSensor(FL);
      break;
    case 6:
      alignFrontToWallSingleSensor(FC);
      break;
    case 7:
      alignFrontToWallSingleSensor(FR);
      break;
    // case 8:
    //   stepAlignFrontToWall(FC, FR, 2);
    //   break;
    default:
      break;
    }
    Serial.println("P:" + getAllIRSensor());
  }

  // calibration before exploration begins
  else if (letter == 'C')
  {
    initCalibration();
    Serial.println("P:" + getAllIRSensor());
  }

  // send sensor readings in weighted values
  else if (letter == 'S')
  {
    // Reading sent should be the same as the readings sent after each movement,
    // since no new readings should be taken
    Serial.println("P:" + echoGetAllIRSensor());
  }

  // send sensor readings in weighted values and cm, for debug
  else if (letter == 'X')
  {
    Serial.println("DEBUG|P:" + getAllIRSensor());
    Serial.println("DEBUG|P:" + getAllIRSensorRaw());
  }

  // Manual calibration
  else if (letter == 'Q')
  {
    manualCalibration();
	  delay(100);
    Serial.println("P:" + getAllIRSensor());
  }
}