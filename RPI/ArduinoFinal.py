import serial

SERIALPORT = "/dev/ttyACM0"
BAUDRATE = '115200'
LOCALE = 'UTF-8'

class Arduino:
    def __init__(self, serialport=SERIALPORT, baudrate=BAUDRATE):
        self.serial_port = serialport
        self.baud_rate = baudrate
        self.connection = None

    def connect(self):
        timer = 1000000
        while True:
            reconnect = False

            try:
                if timer >= 1000000:
                    print('Establishing connection with Arduino')

                self.connection = serial.Serial(self.serial_port, self.baud_rate)

                if self.connection is not None:
                    print('Successfully connected with Arduino: ' + str(self.connection.name))
                    reconnect = False

            except Exception as errorMsg:
                if timer >= 1000000:
                    print('Connection with Arduino failed: ' + str(errorMsg))

                reconnect = True

            if not reconnect:
                break

            if timer >= 1000000:
                print('Retrying Arduino connection...')
                timer=0

            timer += 1

    def read(self):
        try:
            readMsg = self.connection.readline().strip()
            print("From Arduino: {}".format(readMsg))

            if len(readMsg) > 0:
                return readMsg

            return None
       
        except Exception as errorMsg:
            print('Arduino read failed: ' + str(errorMsg))
            raise errorMsg
    
    def write(self, msgToWrite):
        try:
            print('To Arduino:')
            print(msgToWrite)
            self.connection.write(msgToWrite)

        except Exception as errorMsg:
            print('Arduino write failed: ' + str(errorMsg))
            raise errorMsg
