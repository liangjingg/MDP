#pragma once
#include <Arduino.h>
#include <variables.h>
#include "calibration.h"
#include "variables.h"

bool readCommand();
void executeCommand();
int convertCharToInt(char c);