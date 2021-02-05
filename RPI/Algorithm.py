import socket

LOCALE = 'UTF-8'
AlgoBufferSize = 512 
WIFI_IP = '192.168.11.11'
WIFI_PORT = 5040

#To kill port: sudo lsof -i:5040 -> sudo kill -9 <pid>
class Algorithm:
	def __init__(self, host=WIFI_IP, port=WIFI_PORT):
		self.host=host
		self.port=port
		
		self.clientsocket=None
		self.socket=None
		self.clientaddress=None

		self.server=socket.socket(socket.AF_INET, socket.SOCK_STREAM)

		self.server.bind((self.host, self.port))
		self.server.listen(4)

	def connect(self):
		while True:
			reconnect = False
			try:
				print("Establishing connection with Algorithm...")

				if self.clientsocket is None:
					self.clientsocket, self.clientaddress = self.server.accept()
					print(self.clientaddress)
					print('Connected')
					reconnect = False

			except Exception as error:
				print('Error! {}'.format(str(error)))
				if self.clientsocket is not None:
					self.clientsocket.close()
					self.clientsocket = None
				reconnect = True


			if reconnect is False:
				break
			else:
				print('Retrying connection...')

	def read(self):
		try:
			message = self.clientsocket.recv(AlgoBufferSize).strip()
			print("From Algo: {}".format(message))
			if len(message) > 0:
				return message


		except Exception as error:
			print('Failed to read from PC: {}'.format(str(error)))
			raise error
		

	def write(self, msg):
		try:
			print("To Algo: {}".format(msg))
			self.clientsocket.send(msg)
		except Exception as error:
                        print ('Failed to write from PC: {}'.format(str(error))
			raise error
			       
## if you guys ever need a disconnect function for pc			       
##	def disconnect(self):
##                try:
##                        self.socket.close()
##                        print('disconnected')
##
##                except Exception as error:
##                        print('PC disconnection error: {}'.format(str(error))
##                        raise error 


