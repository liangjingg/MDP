#include <DualVNH5019MotorShield.h>
#include "constants.h"
// GLOBAL VARAIBLES

// One delta is the same as one tick
int motorRightDelta = 0;
int motorLeftDelta = 0;

// For setup
double motorForwardLeftSpeed = 0;
double motorForwardRightSpeed = 0;

// Required delta for required distance
int target_Delta = 0;

// For rotateSubtleRight() and rotateSubtleLeft()
int leftMotorPower, rightMotorPower;
// Used to determine which way to turn to adjust based on the difference between wheels
signed long motorLeftDeltaDifference = 0;
signed long motorRightDeltaDifference = 0;

// To remember previous encoder counts
signed long motorLeftDelta_prev = motorLeftDelta;
signed long motorRightDelta_prev = motorRightDelta;
signed long motorLeftEncoderTicks = 0;
signed long motorRightEncoderTicks = 0;
int rMotorOffset, sMotorOffset;   // For rotation

// String that stores the command text
char cmd[MAX_STRING_LENGTH];
// Acts as a counter to keep track how many commands have been inputted and executed
int cmd_in = 0, cmd_out = 0;

// For PID calculation
int prevError;
float integral=0;

// Rotation variables
int rightAngleTurnDelta = 922;
int rotatePower = 250; // Power for rotation

DualVNH5019MotorShield md;  

int loop_count = 0;