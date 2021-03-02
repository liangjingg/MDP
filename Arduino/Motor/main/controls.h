// dont need to include movement.h here because main is calling controls.h and already has an include for movement
int cmd_in,cmd_out;
char cmd[100];
//#include "sensor.h"


#define W01  300
#define W02  610
#define W03  880
#define W04  1230
#define W05  1520
#define W06  1850
#define W07  2180
#define W08  8*300
#define W09  9*300
#define W10  10*300
#define W11  11*300
#define W12  12*300
#define W13  13*300
#define W14  14*300
#define W15  15*300
#define W16  16*300
#define W17  17*300
#define W18  18*300
#define W19  19*300


#define S01  300
#define S02  600
#define S03  900
#define S04  1200
#define S05  1500
#define S06  1800
#define S07  2100
#define S08  2400
#define S09  2700
#define S10  3000
#define S11  3300
#define S12  3600
#define S13  100
#define S14  100
#define S15  100
#define S16  100
#define S17  100
#define S18  100
#define S19  100

#define A01  376
#define A02  200
#define A03  300
#define A04  400
#define A05  500
#define A06  600
#define A07  700
#define A08  800
#define A09  900
#define A10  1000

#define D01  376
#define D02  200
#define D03  300
#define D04  400
#define D05  500
#define D06  600
#define D07  700
#define D08  800
#define D09  900
#define D10  1000




bool readCommand()
{
  cmd_in = 0;
  // This is data that is already arrived and stored in the serial receive buffer (which holds 64 bytes)
  // while receiving data, do the block below.
  while (Serial.available() > 0)
  {
    cmd[cmd_in] = Serial.read();
    cmd_in++;

    // exceed buffer, stop reading additional commands
    if (cmd_in >= 100)
      break;
  }
  if (cmd_in > 0)
    return true;
  else
    return false;
}


void executeFastestPathCommand(){
  char letter = cmd[cmd_out];
//  Serial.println("index");
//  Serial.println(cmd_out);

  // MOVE FOWARD
   if (letter == 'W')
 {
//  Serial.println("W' Removing :");
//       Serial.println(cmd[cmd_out]);
    cmd_out++;

    char firstDigit = cmd[cmd_out];
    if (firstDigit == '0'){
//      Serial.println("0' Removing :");
//       Serial.println(cmd[cmd_out]);
       cmd_out++;
       char secondDigit = cmd[cmd_out];
//       Serial.println("Second' Removing :");
//       Serial.println(cmd[cmd_out]);
       cmd_out++;
//       Serial.println(cmd[cmd_out]);
       switch(secondDigit){
         case '1': 
           goStraight(W01);
           break;
         case '2': 
           goStraight(W02);
//           Serial.println("RUNNING W02");
           break;
         case '3': 
           goStraight(W03);
           break;
         case '4': 
           goStraight(W04);
           break;
         case '5': 
           goStraight(W05);
           break;
         case '6': 
           goStraight(W06);
           break;
         case '7': 
           goStraight(W07);
           break;
         case '8': 
           goStraight(W08);
           break;
         case '9': 
           goStraight(W09);
           break;
         default:
//           Serial.println("Nothing Matches!");
//           Serial.println(secondDigit);

           break;    
       }
    }
    else if (firstDigit == '1'){
       cmd_out++;
       char secondDigit = cmd[cmd_out];
       cmd_out++;
       switch(secondDigit){
         case '1': 
           goStraight(W11);
           break;
         case '2': 
           goStraight(W12);
           break;
         case '3': 
           goStraight(W13);
           break;
         case '4': 
           goStraight(W14);
           break;
         case '5': 
           goStraight(W15);
           break;
         case '6': 
           goStraight(W16);
           break;
         case '7': 
           goStraight(W17);
           break;
         case '8': 
           goStraight(W18);
           break;
         case '9': 
           goStraight(W19);
           break;
         default:
           Serial.println("Input Error!");
           break;    
        }
    }
    cmd_out++;
 }
   else if (letter == 'S')
 {
//  Serial.println("S' Removing :");
//       Serial.println(cmd[cmd_out]);
    cmd_out++;
    char firstDigit = cmd[cmd_out];
    if (firstDigit == '0'){
//       Serial.println("Removing :");
//       Serial.println(cmd[cmd_out]);
       cmd_out++;
//       Serial.println("Removing :");
//       Serial.println(cmd[cmd_out]);
       char secondDigit = cmd[cmd_out];
       cmd_out++;
       switch(secondDigit){
         case '1': 
           goBack(S01);
           break;
         case '2': 
           goBack(S02);
           break;
         case '3': 
           goBack(S03);
           break;
         case '4': 
           goBack(S04);
           break;
         case '5': 
           goBack(S05);
           break;
         case '6': 
           goBack(S06);
           break;
         case '7': 
           goBack(S07);
           break;
         case '8': 
           goBack(S08);
           break;
         case '9': 
           goBack(S09);
           break;
         default:
           Serial.println("Nothing Matches!");
//           Serial.println(secondDigit);

           break;    
       }
    }
    else if (firstDigit == '1'){
       cmd_out++;
       char secondDigit = cmd[cmd_out];
       cmd_out++;
       switch(secondDigit){
         case '1': 
           goBack(S11);
           break;
         case '2': 
           goBack(S12);
           break;
         case '3': 
           goBack(S13);
           break;
         case '4': 
           goBack(S14);
           break;
         case '5': 
           goBack(S15);
           break;
         case '6': 
           goBack(S16);
           break;
         case '7': 
           goBack(S17);
           break;
         case '8': 
           goBack(S18);
           break;
         case '9': 
           goBack(S19);
           break;
         default:
           Serial.println("Input Error!");
           break;    
        }
    }
    cmd_out++;
 }

    else if (letter == 'A')
 {
    cmd_out++;
    turnLeft(A01);
    cmd_out++;
 }

     else if (letter == 'D')
 {
//  Serial.println("Removing :");
     //  Serial.println(cmd[cmd_out]);
    cmd_out++;
    turnRight(D01);
    cmd_out++;

 }
 else{
  cmd_out++;
  cmd_out++;
 }


}

void vroom(){
  if (readCommand() == true)
  { // if there are commands that are available
    while (cmd_out < cmd_in)
    { // execute each command until the last char has been executed
      executeFastestPathCommand();
       Serial.print("Servicing Command: ");
       Serial.println(cmd);
    //de   Serial.println(cmd[cmd_out]);
      //cmd_out++;
    }
    // reset cmd_out counter
    cmd_out = 0;
    memset(&cmd[0],0,sizeof(cmd));
  }

}
