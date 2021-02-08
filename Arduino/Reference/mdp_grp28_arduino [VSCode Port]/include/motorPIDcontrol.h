#pragma once
#include "variables.h"
#include "motorDrive.h"

void moveForward(int target_distance);
void moveForwardFP(int target_distance);
float computePIDForward(int in_motorLeftDelta, int in_motorRightDelta);
float computePIDForwardFP(int in_motorLeftDelta, int in_motorRightDelta);
float computePIDLeft(int in_motorLeftDelta, int in_motorRightDelta);
float computePIDRight(int in_motorLeftDelta, int in_motorRightDelta);
void rotateLeft(int angle);
void rotateRight(int angle);