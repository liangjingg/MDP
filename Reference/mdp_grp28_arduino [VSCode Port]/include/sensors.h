#pragma once
#include "variables.h"

String getAllIRSensor();
String echoGetAllIRSensor();
String getAllIRSensorRaw();
String echoGetAllIRSensorRaw();

// For calibration 
float caliRawIRSensor(int sensorIndx, int arrLen);
float shortRangeSensorDistance(uint16_t value);
float longRangeSensorDistance(uint16_t value);
void insertionSort(float arr[], float n);

// For 2D array
int getIRSensor(int sensorIndex, float sensorReading);
float longRangeSensorDistance(int value);
float shortRangeSensorDistance(int value);
// Readings in voltage (from analogRead)
void getRawIRSensor(int sensorDatas[6][SENSOR_READINGS], int sensorReadings);
void sort2DRowWise(int m[][SENSOR_READINGS], int rows, int cols);
