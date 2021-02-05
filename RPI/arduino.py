import serial

SERIAL_PORT = "/dev/ttyACM0"
BAUD_RATE = '115200'
LOCALE = 'UTF-8'

class Arduino:
    def __init__(self, serial_port=SERIAL_PORT, baud_rate=BAUD_RATE):
        self.serial_port = serial_port
        self.baud_rate = baud_rate
        self.connection = None

    def connect(self):
        count = 1000

        while True:
            reconnect = False

            try:
                if count >= 1000:
                    print('connecting with arduino')

                self.connection = serial.Serial(self.serial_port, self.baud_rate)

                if self.connection is not None:
                    print('Successfully connected with Arduino: ' + str(self.connection.name))
                    reconnect = False

            except:
                if count >= 1000:
                    print('Connection with Arduino failed')

                reconnect = True

            if reconnect is False:
                break

            elif count >= 1000:
                print('Reconnecting...')
                count = 0

            count++

    def read(self):
        try:
            msg = self.connection.readline().strip()
            print("From Arduino: {}".format(msg))

            if len(msg)!= 0:
                return msg

            except Exception as error:
                print('Arduino failed to read' + str(error))

    def write(self, msg):
        try:
            print('To Arduino:')
            print(msg)
            self.connection.write.(msg)

            except Exception as error:
                print('Arduino failed to write' + str(error))
                
                

            
        
