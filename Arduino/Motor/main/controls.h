// dont need to include movement.h here because main is calling controls.h and already has an include for movement
int cmd_in,cmd_out;
char cmd[100];



#define W01  100
#define W02  200
#define W03  300
#define W04  400
#define W05  100
#define W06  100
#define W07  100
#define W08  100
#define W09  100
#define W10  100
#define W11  100
#define W12  100
#define W13  100
#define W14  100
#define W15  100
#define W16  100
#define W17  100
#define W18  100
#define W19  100


#define S01  100
#define S02  200
#define S03  300
#define S04  400
#define S05  100
#define S06  100
#define S07  100
#define S08  100
#define S09  100
#define S10  100
#define S11  100
#define S12  100
#define S13  100
#define S14  100
#define S15  100
#define S16  100
#define S17  100
#define S18  100
#define S19  100

#define A01  100
#define A02  200
#define A03  300
#define A04  400
#define A05  500
#define A06  600
#define A07  700
#define A08  800
#define A09  900
#define A10  1000

#define D01  100
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

  // MOVE FOWARD
   if (letter == 'W')
 {
    cmd_out++;
    char firstDigit = cmd[cmd_out];
    if (firstDigit == '0'){
       cmd_out++;
       char secondDigit = cmd[cmd_out];
       switch(secondDigit){
         case '1': 
           goStraight(W01);
           break;
         case '2': 
           goStraight(W02);
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
           Serial.println("Nothing Matches!");
           Serial.println(secondDigit);

           break;    
       }
    }
    else if (firstDigit == '1'){
       cmd_out++;
       char secondDigit = cmd[cmd_out];
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
    cmd_out++;
    char firstDigit = cmd[cmd_out];
    if (firstDigit == '0'){
       cmd_out++;
       char secondDigit = cmd[cmd_out];
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
           Serial.println(secondDigit);

           break;    
       }
    }
    else if (firstDigit == '1'){
       cmd_out++;
       char secondDigit = cmd[cmd_out];
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
    char firstDigit = cmd[cmd_out];
    if (firstDigit == '0'){
       cmd_out++;
       char secondDigit = cmd[cmd_out];
       switch(secondDigit){
         case '1': 
           turnLeft(A01);
           break;
         case '2': 
           turnLeft(A02);
           break;
         case '3': 
           turnLeft(A03);
           break;
         case '4': 
           turnLeft(A04);
           break;
         case '5': 
           turnLeft(A05);
           break;
         case '6': 
           turnLeft(A06);
           break;
         case '7': 
           turnLeft(A07);
           break;
         case '8': 
           turnLeft(A08);
           break;
         case '9': 
           turnLeft(A09);
           break;
         default:
           Serial.println("Nothing Matches!");
           Serial.println(secondDigit);

           break;    
       }
    }
    cmd_out++;
 }

     else if (letter == 'D')
 {
    cmd_out++;
    char firstDigit = cmd[cmd_out];
    if (firstDigit == '0'){
       cmd_out++;
       char secondDigit = cmd[cmd_out];
       switch(secondDigit){
         case '1': 
           turnRight(D01);
           break;
         case '2': 
           turnRight(D02);
           break;
         case '3': 
           turnRight(D03);
           break;
         case '4': 
           turnRight(D04);
           break;
         case '5': 
           turnRight(D05);
           break;
         case '6': 
           turnRight(D06);
           break;
         case '7': 
           turnRight(D07);
           break;
         case '8': 
           turnRight(D08);
           break;
         case '9': 
           turnRight(D09);
           break;
         default:
           Serial.println("Nothing Matches!");
           Serial.println(secondDigit);

           break;    
       }
    }
    cmd_out++;
 }

 

}

void vroom(){
  if (readCommand() == true)
  { // if there are commands that are available
    while (cmd_out < cmd_in)
    { // execute each command until the last char has been executed
      executeFastestPathCommand();
      cmd_out++;
    }
    // reset cmd_out counter
    cmd_out = 0;
  }
  delay(50);
}
