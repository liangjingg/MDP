// ------------------------------------------------------------
//
//                        SENSORS
//
// ------------------------------------------------------------

/* =============================== sensor pin declaration ============================= */
int ps1 = 0;   // A0 left analog pin used to connect the sharp sensor A0
int ps2 = 1;   // A1 right
int ps3 = 2;   // A2 back
int ps4 = 3;   // A3 front 
int ps5 = 4;   // A4 top left
int ps6 = 5;   // A5 top right

/* =============================== a2d values from sensor ============================= */
int left_analog = 0;   
int right_analog = 0; 
int back_analog = 0; 
int front_analog = 0; 
int topLeft_analog = 0; 
int topRight_analog = 0; 

/* =============================== Offset from robot edge ============================= */
float longFront = 0;      //A3 pin 
float longBack = 0;       //A2 pin
float shortLeft = 0;      //A0 pin
float shortRight = 0;     //A1 pin
float shortTopLeft = 3;   //A4 pin
float shortTopRight = 3;  //A5 pin

/* =============================== Distance in CM ============================= */
float left_distanceInCm = 0;
float right_distanceInCm = 0;
float back_distanceInCm = 0;
float front_distanceInCm = 0;
float topLeft_distanceInCm = 0;
float topRight_distanceInCm = 0;

float sensorDist[2][6];
int counter = 0;

void sensorSetup()
{
  Serial.begin(115200); // start the serial port
}

/* =============================== define distance arr from obstacle for each sensor ============================= */
void getSensorDist()
{
  left_analog = rawIRSensorMedian(50, 0);
  left_distanceInCm = shortRangeDistance(left_analog);
  
  right_analog = rawIRSensorMedian(50, 1);
  right_distanceInCm = shortRangeDistance(right_analog);

  back_analog = rawIRSensorMedian(50, 2);
  back_distanceInCm = longRangeDistance(back_analog) + 5;
  
  front_analog = rawIRSensorMedian(50, 3);
  front_distanceInCm = longRangeDistance(front_analog);
  
  topLeft_analog = rawIRSensorMedian(50, 4);
  topLeft_distanceInCm = shortRangeDistance(topLeft_analog) -3 ;
  
  topRight_analog = rawIRSensorMedian(50, 5);
  topRight_distanceInCm = shortRangeDistance(topRight_analog) - 3;

  //adds to the the array
  sensorDist[counter][0] = left_distanceInCm;
  sensorDist[counter][1] = right_distanceInCm;
  sensorDist[counter][2] = back_distanceInCm;
  sensorDist[counter][3] = front_distanceInCm;
  sensorDist[counter][4] = topLeft_distanceInCm;
  sensorDist[counter][5] = topRight_distanceInCm;

  counter = (counter+1)%2;
  
  Serial.println("TopLeft Front TopRight");
  Serial.print(topLeft_distanceInCm);
  Serial.print("\t");
  Serial.print(front_distanceInCm);
  Serial.print("\t");
  Serial.println(topRight_distanceInCm);
  Serial.println("    Left Back Right   ");
  Serial.print(left_distanceInCm);
  Serial.print("\t");
  Serial.print(back_distanceInCm);
  Serial.print("\t");
  Serial.println(right_distanceInCm);
}

/* =============================== Return Distance Message ============================= */
String getDistanceMsg()
{
  int read_counter = 0;
  if (counter == 0){
    read_counter = 1;
  }
  String message = String(SensorDist[read_counter][0])+ "," + String(SensorDist[read_counter][1])+ "," + String(SensorDist[read_counter][2]) + "," + String(SensorDist[read_counter][3]) + "," + String(SensorDist[read_counter][4]) + "," + String(SensorDist[read_counter][5]);
  Serial.println(message);
  return message;
}

/* =============================== Calculate median of sample size values ============================= */
int rawIRSensorMedian(int sample_size, int pin)
{
  int reading[sample_size];
  int analog_distance = 0;
  
  for (int i = 1; i <= sample_size; i++)
  {
    analog_distance = analogRead(pin);       // reads the value of the sharp sensor
    reading[i] = analog_distance;
    delay(10);
  }
  sort(reading,sample_size);
  sample_size = (sample_size+1) / 2 - 1;
  //Serial.println(reading[sample_size]); 
  
  return reading[sample_size];
}


/* =============================== Sorting Algorithm ============================= */
void swap(int *x,int *y) {
   int q;
   
   q=*x; 
   *x=*y; 
   *y=q;
}

void sort(int arr[],int n) { 
   int i,j,temp;

   for(i = 0;i < n-1;i++) {
      for(j = 0;j < n-i-1;j++) {
         if(arr[j] > arr[j+1])
            swap(&arr[j],&arr[j+1]);
      }
   }
}

/* =============================== Effective Short Range Distance is 10cm - 40cm ============================= */
float shortRangeDistance(int analogValue)
{
  float distanceInCm = (3867.508/(analogValue - 130.319));
  if (distanceInCm > 70 || distanceInCm < 0)
  {
    distanceInCm = 70;
  }
  return distanceInCm;
}

/* =============================== Effective Long Range Distance is 20cm - 70cm ============================= */
float longRangeDistance(int analogValue)
{
  float distanceInCm = (8687.689/(analogValue - 75.9339));

  if (distanceInCm > 70 || distanceInCm < 0)
  {
    distanceInCm = 70;
  }
  return distanceInCm;
}
