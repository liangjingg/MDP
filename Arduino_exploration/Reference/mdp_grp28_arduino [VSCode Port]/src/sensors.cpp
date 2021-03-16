
// ------------------------------------------------------------
//
//                        SENSORS
//
// ------------------------------------------------------------

#include "sensors.h"

// Global variables for cm reading
float sensorFRONTLreading;
float sensorFRONTCreading;
float sensorFRONTRreading;
float sensorRIGHTFreading;
float sensorRIGHTBreading;
float sensorLEFTFreading;
// Create a 2D array to store all readings of the sensors
int sensorDatas[6][SENSOR_READINGS];
// Offsets for sensor readings
// AFTERNOON RUN [7.5,6.8,7.5,17.0]
// 081019 [7.4,6.7,7.4,17.0]
float sensorFRONTLoffset = 6.4;
float sensorFRONTCoffset = 6.7; // 5.7
float sensorFRONTRoffset = 5.4; // 6.4
float sensorRIGHTFoffset = 0.0; // 0.0
float sensorRIGHTBoffset = 0.0;
float sensorLEFTFoffset = 17.0; // 14.0

/*
   Function:  getAllIRSensor
   --------------------
   Returns sensor readings of all sensors in weighted values.

*/
String getAllIRSensor()
{
  getRawIRSensor(sensorDatas, SENSOR_READINGS);
  String message = (String(getIRSensor(FL, sensorFRONTLreading)) + "," + String(getIRSensor(FC, sensorFRONTCreading)) + "," + String(getIRSensor(FR, sensorFRONTRreading)) + "," + String(getIRSensor(RF, sensorRIGHTFreading)) + "," + String(getIRSensor(RB, sensorRIGHTBreading)) + "," + String(getIRSensor(LFLR, sensorLEFTFreading)));
  return message;
}

/*
   Function:  echoGetAllIRSensor
   --------------------
   Echoes previous sensor readings of all sensors in centimeters in weighted values.
   Readings stored at global variables. 

*/
String echoGetAllIRSensor()
{
  String message = (String(getIRSensor(FL, sensorFRONTLreading)) + "," + String(getIRSensor(FC, sensorFRONTCreading)) + "," + String(getIRSensor(FR, sensorFRONTRreading)) + "," + String(getIRSensor(RF, sensorRIGHTFreading)) + "," + String(getIRSensor(RB, sensorRIGHTBreading)) + "," + String(getIRSensor(LFLR, sensorLEFTFreading)));
  return message;
}

/*
   Function: getAllIRSensorRaw
   --------------------
   Returns sensor readings of all sensors in centimeters in float values.

*/
String getAllIRSensorRaw()
{
  getRawIRSensor(sensorDatas, SENSOR_READINGS);
  String message = (String(sensorFRONTLreading) + "," + String(sensorFRONTCreading) + "," +
                    String(sensorFRONTRreading) + "," + String(sensorRIGHTFreading) + "," + String(sensorRIGHTBreading) +
                    "," + String(sensorLEFTFreading));
  return message;
}

/*
   Function: echoGetAllIRSensorRaw
   --------------------
   Echoes previous sensor readings of all sensors in centimeters in float values.
   Readings stored at global variables. 

*/
String echoGetAllIRSensorRaw()
{
  String message = (String(sensorFRONTLreading) + "," + String(sensorFRONTCreading) + "," +
                    String(sensorFRONTRreading) + "," + String(sensorRIGHTFreading) + "," + String(sensorRIGHTBreading) +
                    "," + String(sensorLEFTFreading));
  return message;
}

/*
   Function:  caliRawIRSensor
   --------------------
   Returns sensor reading of a specified sensor in cm.
   Parameters include sensor index number and number of readings to take
   Used for calibration only

*/
float caliRawIRSensor(int sensorIndx, int arrLen)
{
  // Create an array to store all readings of the sensor
  float sensorDatas[arrLen];

  for (int i = 0; i < arrLen; i++)
  {
    float rawSensorValue;
    if (sensorIndx == 0)
    {
      rawSensorValue = shortRangeSensorDistance(analogRead(sensorFRONTL)) - 7.5;
    }
    else if (sensorIndx == 1)
    {
      rawSensorValue = shortRangeSensorDistance(analogRead(sensorFRONTC)) - 7.5; // 7.0
    }
    else if (sensorIndx == 2)
    {
      rawSensorValue = shortRangeSensorDistance(analogRead(sensorFRONTR)) - 7.5;
    }
    else if (sensorIndx == 3)
    {
      rawSensorValue = shortRangeSensorDistance(analogRead(sensorRIGHTF));
    }
    else if (sensorIndx == 4)
    {
      rawSensorValue = shortRangeSensorDistance(analogRead(sensorRIGHTB));
    }
    else if (sensorIndx == 5)
    {
      rawSensorValue = longRangeSensorDistance(analogRead(sensorLEFTF)) - 17.0;
    }
    // store each reading in the array
    sensorDatas[i] = rawSensorValue;
    // insertionSort(sensorDatas, i); // Might not be needed?
    delay(2); // 2ms instead of 20
  }
  // Sort the readings in ascending order
  insertionSort(sensorDatas, arrLen);
  // Select the median value as the reading of the sensor
  return sensorDatas[arrLen / 2];
}

/*
   Function:  shortRangeSensorDistance
   --------------------
   Formula for short range IR sensors

*/
float shortRangeSensorDistance(uint16_t value)
{
  // Short range IR (10-30cm)
  int voltFromRaw = map(value, 0, 1023, 0, 5000);
  float volts = voltFromRaw / 1000.0;
  float distanceInCm = 27.728 * pow(volts + 0.01, -1.2045) + 0.15;
  return distanceInCm;
}

/*
   Function:  longRangeSensorDistance
   --------------------
   Formula for long range IR sensors

*/
float longRangeSensorDistance(uint16_t value)
{
  // Long range IR (20cm - 70cm)
  int voltFromRaw = map(value, 0, 1023, 0, 5000);
  float volts = voltFromRaw / 1000.0;
  float distanceInCm = 61.573 * pow(volts - 0.253, -0.8) - 9.5;
  return distanceInCm;
}

/*
   Function:  insertionSort
   --------------------
   Sorts sensor readings in ascending order
   Nothing particularly special

*/
void insertionSort(float arr[], float n)
{
  int i, j;
  float key;
  for (i = 1; i < n; i++)
  {
    key = arr[i];
    j = i - 1;
    while (j >= 0 && arr[j] > key)
    {
      arr[j + 1] = arr[j];
      j = j - 1;
    }
    arr[j + 1] = key;
  }
}

// ------------------------------------------------------------
//
//                        FOR 2D ARRAY
//
// ------------------------------------------------------------

/*
   Function: getIRSensor
   --------------------
   Converts sensor readings in cm to weighted values based on specifc sensor 

*/
int getIRSensor(int sensorIndex, float sensorReading)
{
  float rSensorValue = -1;
  // Weighted values for front sensors
  if (sensorIndex == FL || sensorIndex == FC || sensorIndex == FR)
  {
    if (sensorReading <= 9.0)
    {
      rSensorValue = 1;
    }
    else if (sensorReading > 9.0 && sensorReading <= 19.0)
    {
      rSensorValue = 2;
    }
    else if (sensorReading > 19.0)
    {
      rSensorValue = 3;
    }
  }
  else if (sensorIndex == RF || sensorIndex == RB)
  {
    if (sensorReading <= 11.5)
    {
      rSensorValue = 1;
    }
    else if (sensorReading > 11.5 && sensorReading <= 21.5)
    {
      rSensorValue = 2;
    }
    else if (sensorReading > 21.5)
    {
      rSensorValue = 3;
    }
    // Weighted values for long range sensor at left front
  }
  else if (sensorIndex == LFLR)
  {
    // Without this line, reading is bugged
    String message = ("DEBUG: " + String(sensorReading));
    // if there is no obstacle detected at all, returns NAN value
    if (isnan(sensorReading))
    {
      rSensorValue = 5;
    }
    else if (sensorReading <= 8.0)
    {
      rSensorValue = 1;
    }
    else if (sensorReading > 8.0 && sensorReading <= 20.0)
    {
      rSensorValue = 2;
    }
    else if (sensorReading > 20.0 && sensorReading <= 30.0)
    {
      rSensorValue = 3;
    }
    else if (sensorReading > 30.0 && sensorReading <= 40.0)
    {
      rSensorValue = 4;
    }
    else if (sensorReading > 40.0)
    {
      rSensorValue = 5;
    }
  }
  return rSensorValue;
}

/*
   Function:  shortRangeSensorDistance
   --------------------
   Formula for short range IR sensors

*/
float shortRangeSensorDistance(int value)
{
  // Short range IR (10-30cm)
  int voltFromRaw = map(value, 0, 1023, 0, 5000);
  float volts = voltFromRaw / 1000.0;
  float distanceInCm = 27.728 * pow(volts + 0.01, -1.2045) + 0.15;
  return distanceInCm;
}

/*
   Function:  longRangeSensorDistance
   --------------------
   Formula for long range IR sensors

*/
float longRangeSensorDistance(int value)
{
  // Long range IR (20cm - 70cm)
  int voltFromRaw = map(value, 0, 1023, 0, 5000);
  float volts = voltFromRaw / 1000.0;
  float distanceInCm = 61.573 * pow(volts - 0.253, -0.8) - 9.5;
  return distanceInCm;
}

/*
   Function:  getRawIRSensor
   --------------------
   Inserts sensor readings in voltage units into a 2D array
   Obtains median value of voltage from each sensor, and then converts to cm
   Stores readings in a global variable 

*/
void getRawIRSensor(int sensorData[6][SENSOR_READINGS], int sensorReadings)
{
  for (int col = 0; col < sensorReadings; col++)
  {
    // analogRead returns a int value of voltage from 0 to 1023
    sensorData[0][col] = analogRead(sensorFRONTL);
    sensorData[1][col] = analogRead(sensorFRONTC);
    sensorData[2][col] = analogRead(sensorFRONTR);
    sensorData[3][col] = analogRead(sensorRIGHTF);
    sensorData[4][col] = analogRead(sensorRIGHTB);
    sensorData[5][col] = analogRead(sensorLEFTF);
  }
  // Sort the readings in ascending order
  sort2DRowWise(sensorDatas, 6, sensorReadings);
  // Select the median value as the reading of the sensor
  int sensorFRONTLvolt = (sensorData[0][sensorReadings / 2] + sensorData[0][(sensorReadings / 2) - 2] + sensorData[0][(sensorReadings / 2) + 2]) / 3;
  int sensorFRONTCvolt = (sensorData[1][sensorReadings / 2] + sensorData[1][(sensorReadings / 2) - 2] + sensorData[1][(sensorReadings / 2) + 2]) / 3;
  int sensorFRONTRvolt = (sensorData[2][sensorReadings / 2] + sensorData[2][(sensorReadings / 2) - 2] + sensorData[2][(sensorReadings / 2) + 2]) / 3;
  int sensorRIGHTFvolt = (sensorData[3][sensorReadings / 2] + sensorData[3][(sensorReadings / 2) - 2] + sensorData[3][(sensorReadings / 2) + 2]) / 3;
  int sensorRIGHTBvolt = (sensorData[4][sensorReadings / 2] + sensorData[4][(sensorReadings / 2) - 2] + sensorData[4][(sensorReadings / 2) + 2]) / 3;
  int sensorLEFTFvolt = (sensorData[5][sensorReadings / 2] + sensorData[5][(sensorReadings / 2) - 2] + sensorData[5][(sensorReadings / 2) + 2]) / 3;
  // Convert voltage to distance with sensor offset
  sensorFRONTLreading = shortRangeSensorDistance(sensorFRONTLvolt) - sensorFRONTLoffset;
  sensorFRONTCreading = shortRangeSensorDistance(sensorFRONTCvolt) - sensorFRONTCoffset;
  sensorFRONTRreading = shortRangeSensorDistance(sensorFRONTRvolt) - sensorFRONTRoffset;
  sensorRIGHTFreading = shortRangeSensorDistance(sensorRIGHTFvolt) - sensorRIGHTFoffset;
  sensorRIGHTBreading = shortRangeSensorDistance(sensorRIGHTBvolt) - sensorRIGHTBoffset;
  sensorLEFTFreading = longRangeSensorDistance(sensorLEFTFvolt) - sensorLEFTFoffset;
}

/*
   Function:  sort2DRowWise
   --------------------
   Sorts sensor readings in 2D array in ascending order
   Nothing particularly special
    
*/
void sort2DRowWise(int arr[6][SENSOR_READINGS], int rows, int cols)
{
  // loop for rows of matrix
  for (int i = 0; i < rows; i++)
  {
    // loop for column of matrix
    for (int j = 0; j < cols; j++)
    {
      // loop for comparison and swapping
      for (int k = 0; k < cols - j - 1; k++)
      {
        if (arr[i][k] > arr[i][k + 1])
        {
          // swapping of elements
          int t = arr[i][k];
          arr[i][k] = arr[i][k + 1];
          arr[i][k + 1] = t;
        }
      }
    }
  }
}