#pragma once

#include "variables.h"

void correctWheelDelta();
void drive(int power_a, int power_b);
void brake();
void reverse(int power_a, int power_b);
void updateLeftMotorDelta();
void updateRightMotorDelta();