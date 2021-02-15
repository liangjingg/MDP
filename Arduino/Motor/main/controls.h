// dont need to include movement.h here because main is calling controls.h and already has an include for movement
int cmd_in,cmd_out;
char cmd[100];




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


void executeCommand(){
  char letter = cmd[cmd_out];

  // MOVE FOWARD
  if (letter == 'W')
  {
    // cmd_out is set to 1
    cmd_out++;
    // reads the second char to determine the distance specified
    char firstDigit = cmd[cmd_out];
    if (firstDigit == '0')
    {
      cmd_out++;
      int secondDigit = cmd[cmd_out];
      if (49<=secondDigit<=57){
        int secondint = secondDigit-48;
        moveStraightStep(secondint);
      }
      else{
        Serial.println("Input Error!");
      }
    }
    else if (firstDigit == '1'){
       cmd_out++;
      int secondDigit = cmd[cmd_out];
      if (49<=secondDigit<=57){
        int secondint = secondDigit-38;
        moveStraightStep(secondint);
      }
      else{
        Serial.println("Input Error!");
      }
    }
    else {
      Serial.println("Input Error!");
    }
  }

  // MOVE BACK
  else if (letter == 'A')
  {
    // cmd_out is set to 1
    cmd_out++;
    // reads the second char to determine the distance specified
    char firstDigit = cmd[cmd_out];
    if (firstDigit == '0')
    {
      cmd_out++;
      int secondDigit = cmd[cmd_out];
      if (49<=secondDigit<=57){
        int secondint = secondDigit-48;
        moveBackStep(secondint);
      }
      else{
        Serial.println("Input Error!");
      }
    }
    else if (firstDigit == '1'){
       cmd_out++;
      int secondDigit = cmd[cmd_out];
      if (49<=secondDigit<=57){
        int secondint = secondDigit-38;
        moveBackStep(secondint);
      }
      else{
        Serial.println("Input Error!");
      }
    }
    else {
      Serial.println("Input Error!");
    }
  }
  
   // TURN RIGHT
  else if (letter == 'D')
  {
    // cmd_out is set to 1
    cmd_out++;
    // reads the second char to determine the distance specified
    char firstDigit = cmd[cmd_out];
    if (firstDigit == '0')
    {
      cmd_out++;
      int secondDigit = cmd[cmd_out];
      if (49<=secondDigit<=57){
        int secondint = secondDigit-48;
        moveRightStep(secondint);
      }
      else{
        Serial.println("Input Error!");
      }
    }
    else if (firstDigit == '1'){
       cmd_out++;
      int secondDigit = cmd[cmd_out];
      if (49<=secondDigit<=57){
        int secondint = secondDigit-38;
        moveRightStep(secondint);
      }
      else{
        Serial.println("Input Error!");
      }
    }
    else {
      Serial.println("Input Error!");
    }
  }

   // TURN LEFT
  else if (letter == 'D')
  {
    // cmd_out is set to 1
    cmd_out++;
    // reads the second char to determine the distance specified
    char firstDigit = cmd[cmd_out];
    if (firstDigit == '0')
    {
      cmd_out++;
      int secondDigit = cmd[cmd_out];
      if (49<=secondDigit<=57){
        int secondint = secondDigit-48;
        moveLeftStep(secondint);
      }
      else{
        Serial.println("Input Error!");
      }
    }
    else if (firstDigit == '1'){
       cmd_out++;
      int secondDigit = cmd[cmd_out];
      if (49<=secondDigit<=57){
        int secondint = secondDigit-38;
        moveLeftStep(secondint);
      }
      else{
        Serial.println("Input Error!");
      }
    }
    else {
      Serial.println("Input Error!");
    }
  }
}

void vroom(){
  if (readCommand() == true)
  { // if there are commands that are available
    while (cmd_out < cmd_in)
    { // execute each command until the last char has been executed
      executeCommand();
      cmd_out++;
    }
    // reset cmd_out counter
    cmd_out = 0;
  }
  delay(50);
}
