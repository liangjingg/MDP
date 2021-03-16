#include <DualVNH5019MotorShield.h>
#include "PinChangeInt.h"

DualVNH5019MotorShield md;

int pin = 1;
unsigned long duration;

void setup()
{
  Serial.begin(115200);
  Serial.println("Dual VNH5019 Motor Shield");
  md.init();
  pinMode(pin, INPUT);
}

void loop()
{
  md.setM1Speed(-175); // Right 
  md.setM2Speed(200); // Left  
  duration = pulseIn(pin, HIGH);
  Serial.println(duration);
  delay(1000);
  md.setM1Speed(175); // Right 
  md.setM2Speed(-200); // Left
  duration = pulseIn(pin, HIGH);
  delay(1000);
}
