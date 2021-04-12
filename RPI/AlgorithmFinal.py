import socket

LOCALE = 'UTF-8'
BufferSize = 512 
IpAddr = '192.168.11.11'
PortNumber = 4000

class Algorithm:
	def __init__(self, host=IpAddr, port=PortNumber):
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

			except Exception as errorMsg:
				print('Error! {}'.format(str(errorMsg)))
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
			readMsg = self.clientsocket.recv(BufferSize).strip()
			print("From Algo: {}".format(readMsg))
			if len(readMsg) > 0:
				return readMsg


		except Exception as errorMsg:
			print('Failed to read from PC: {}'.format(str(errorMsg)))
			raise errorMsg
		

	def write(self, msgToWrite):
		try:
			print("To Algo: {}".format(msgToWrite))
			self.clientsocket.send(msgToWrite)
		except Exception as errorMsg:
			print ('Failed to write from PC: {}'.format(str(errorMsg)))
			raise errorMsg

	def disconnect(self):
		try:
			self.socket.close()
			print('disconnected')	
		except Exception as errorMsg:
			print('PC disconnection error: {}'.format(str(errorMsg)))
			raise errorMsg 
		



