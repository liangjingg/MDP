#pragma once

#include <DualVNH5019MotorShield.h>
#include <constants.h>
#include "variables.h"

// One delta is the same as one tick
extern int motorRightDelta;
extern int motorLeftDelta;

// Required delta for required distance
extern int target_Delta;

// For rotateSubtleRight() and rotateSubtleLeft()
extern int leftMotorPower, rightMotorPower;
// Used to determine which way to turn to adjust based on the difference between wheels
extern signed long motorLeftDeltaDifference;
extern signed long motorRightDeltaDifference;

// To remember previous encoder counts
extern signed long motorLeftDelta_prev;
extern signed long motorRightDelta_prev;
extern signed long motorLeftEncoderTicks;
extern signed long motorRightEncoderTicks;
extern int rMotorOffset, sMotorOffset;   // For rotation

// String that stores the command text
extern char cmd[MAX_STRING_LENGTH];
// Acts as a counter to keep track how many commands have been inputted and executed
extern int cmd_in, cmd_out;

// For PID calculation
extern int prevError;
extern float integral;

// Rotation variables
extern int rightAngleTurnDelta;
extern int rotatePower; // Power for rotation

extern DualVNH5019MotorShield md;

extern int loop_count;