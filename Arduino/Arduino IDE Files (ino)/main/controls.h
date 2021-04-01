#include "variable.h"

int cmd_in,cmd_out;
char cmd[300];

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
    if (cmd_in >= 300)
      break;
  }
  if (cmd_in > 0)
    return true;
  else
    return false;
}

void executeExplorationCommand(){
  char letter = cmd[cmd_out];
  switch(letter){
    case 'W': goStraight(E_W);
              updateDone();
              break;
    case 'A': turnLeft(E_A);
              updateDone();
              break;
    case 'D': turnRight(E_D);
              updateDone();
              break;
    case 'Q':checkRightDist();
              break;
    case 'E':checkRightAlign();
              break;
    case 'Z':updateSensorPrint();
              break;
    case 'B': pullData();
              break;
    case 'V':checkFrontAlign();
              break;
    case 'U':uTurn();
             updateDone();
              break;
    case 'X': checkFrontDist();
              break;
    case 'C':break;

    

    
  }
  cmd_out+=2;
}

void executeFastestPathCommand(){
 char letter = cmd[cmd_out];

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
          //goStraightObstacle(W01);
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
      //goBack(10);
   }
   else if (firstDigit == '1'){
      cmd_out++;
      char secondDigit = cmd[cmd_out];
      cmd_out++;
      switch(secondDigit){
        case '0': 
          goStraight(W10);
          break;
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
       //goBack(10);
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
      //Serial.print("Servicing Command: ");
      //Serial.println(cmd);
   //de   Serial.println(cmd[cmd_out]);
     //cmd_out++;
   }
   // reset cmd_out counter
   cmd_out = 0;
 }

}

void valoom(){
if (readCommand() == true)
  { // if there are commands that are available
    while (cmd_out < cmd_in)
    { // execute each command until the last char has been executed
      executeExplorationCommand();
       //Serial.print("Servicing Command: ");
       //Serial.println(cmd);
    //de   Serial.println(ucmd[cmd_out]);
      //cmd_out++;
    }
    // reset cmd_out counter
    cmd_out = 0;
  }

}
