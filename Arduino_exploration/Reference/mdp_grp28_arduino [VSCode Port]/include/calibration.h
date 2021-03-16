#pragma once
#include "variables.h"
#include "motorPIDcontrol.h"
#include "sensors.h"
#include "motorDrive.h"

int alignableWallCheck();
void initCalibration();
void wallHug();
void rotateDeltaCalculation(int rotateDelta);
void rotateSubtleLeft(float angle);
void rotateSubtleRight(float angle);
void quickWallHug();
void alignFrontToWall(int sensorA, int sensorB);
void manualCalibration();
// Experimental
void alignFrontToWallSingleSensor(int sensorA);
void stepAlignFrontToWall(int sensorA, int sensorB, int type);
void wallHugStart();
