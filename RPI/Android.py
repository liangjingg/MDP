from bluetooth import *
import os

LOCALE = 'UTF-8'
BUFFERSIZE = 1024
RFCOMM = 7
UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee"


class Android:
    def __init__(self):
        self.serversocket = None
        self.clientsocket = None
        os.system("sudo hciconfig hci0 piscan")

        self.serversocket = BluetoothSocket(RFCOMM)
        self.serversocket.bind(("", RFCOMM))
        self.serversocket.listen(RFCOMM)
        port = self.serversocket.getsockname()[1]

        print("Waiting for connection on RFCOMM channel %d" % port)


    def connect(self):
        try:
            print("Connecting to Android...")

            if self.clientsocket == None:
                self.clientsocket, clientID = self.serversocket.accept()

                print("Accepted connection from ", clientID)
                reconnect = False

        except Exception as errorMsg:
            print("Connection to Android failed. Error = {}".format(str(errorMsg)))

            if self.clientsocket is not None:
                self.clientsocket.close()
                self.clientsocket = None
    
    def read(self):

        try:
            readMsg = self.clientsocket.recv(BUFFERSIZE).strip()
            print("From Android = {}".format(readMsg))

            if len(readMsg) > 0:
                return readMsg
            
            return None

        except Exception as errorMsg:
            print('Android read failed: ' + str(errorMsg))
            raise errorMsg
        

    def write(self, msgToWrite):
        print("To Android: {}".format(msgToWrite))
        try:
             self.clientsocket.send(msgToWrite)
        except Exception as errorMsg:
            print("Fail to send to Android... {}".format(str(errorMsg)))
            raise errorMsg

    def disconnect(self):
        try:
            if self.clientsocket is not None:
                self.clientsocket.close()
                self.clientsocket = None

        except Exception as errorMsg:
            print("android.disconnect() failed:" +str(errorMsg))
