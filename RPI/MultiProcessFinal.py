from multiprocessing import Process, Value, Queue, Manager
from Algorithm import Algorithm
from Arduino import Arduino
from Android import Android

from socket import error as SocketError

from picamera import PiCamera
import socket
import cv2
import imagezmq
from imutils.video import VideoStream

ANDROID_HEADER = 'AND'.encode()
ARDUINO_HEADER = 'ARD'.encode()
ALGORITHM_HEADER = 'ALG'.encode()
NEWLINE = '\n'.encode()
MOVE_FORWARD_AND = '{m:W'.encode()
TURN_LEFT_AND = '{m:A'.encode()
TURN_RIGHT_AND = '{m:D'.encode()
MDF_STRING = 'M'.encode()[0]
TAKE_PICTURE = 'P'.encode()[0]
EXPLORATION_COMPLETE = 'N'.encode()
FASTEST_PATH = 'K'.encode()[0]
RESULT = 'R' 
image_processing_server_url = 'tcp://192.168.11.11:5555'

class MultiProcessCommunication:
	def __init__(self):
		#Connect to Arduino, Algo and Android and ImageRecPC
		self.arduino = Arduino()
		self.algorithm = Algorithm()
		self.android = Android()

		self.manager = Manager()
		self.mdf_string = self.manager.list([0])
		self.IMAGE_LIST = self.manager.list()
		
		#Messages queue
		self.message_queue = self.manager.Queue() #arduino and algorithm
		self.image_queue = self.manager.Queue() #image rec
		self.android_queue = self.manager.Queue() #android 

		#read processes
		self.read_arduino_process = Process(target=self.read_arduino)
		self.read_algorithm_process = Process(target = self.read_algorithm)
		self.read_android_process = Process(target=self.read_android)

		#write processes
		self.write_arduino_and_algorithm_process = Process(target=self.write_arduino_and_algorithm)
		self.write_android_process = Process(target=self.write_android)
		self.image_process = Process(target=self.image_processing)
		print('MultiProcessCommunication initialized')

		self.dropped_connection = Value('i',0)


	def start(self):
		try:
			#Connect to arduino, algo, imagerecpc and android
			self.arduino.connect()
			self.algorithm.connect()
			self.android.connect()
			self.image_process.start()
			
			#Start the process to listen and read from algo, android and arduino
			self.read_arduino_process.start()
			self.read_algorithm_process.start()
			self.read_android_process.start()

			#Start the process to write to algo and arduino
			self.write_arduino_and_algorithm_process.start()

			#Start the process to write to android
			self.write_android_process.start()

			print("Image server connected!")
			print('Comms started. Reading from algo and android and arduino and imagerec.')		

		except Exception as err:
			raise err

		self.reconnection()

	def reconnection(self):
		while True:
			try:
				if not self.read_android_process.is_alive():
					self.android_reconnection()
				if not self.write_android_process.is_alive():
					self.android_reconnection()
			except Exception as error:
				print('Error in reconnection')
				raise error

	def android_reconnection(self):
		self.android.disconnect()

		self.read_android_process.terminate()
		self.write_arduino_and_algorithm_process.terminate()
		self.write_android_process.terminate()

		self.android.connect()

		self.read_android_process = Process(target=self.read_android)
		self.read_android_process.start()

		self.write_arduino_and_algorithm_process = Process(target=self.write_arduino_and_algorithm)
		self.write_arduino_and_algorithm_process.start()

		self.write_android_process = Process(target=self.write_android)
		self.write_android_process.start()

		print('Reconnected to android!')


	def format_for(self, target, message):
		#Function to format message
		return {
			'target': target,
			'payload': message,
		}

	def read_arduino(self):

		while True:
			try:
				rawmessage = self.arduino.read()

				if rawmessage == None:
					continue

				message_list = rawmessage.splitlines()
				
				for message in message_list:
					if len(message) <= 0:
						continue
					else:
						self.message_queue.put_nowait(self.format_for(ALGORITHM_HEADER, message + NEWLINE))

			except Exception as error:
				print("_read_arduino failed - {}".format(str(error)))
				break


	def read_algorithm(self):
		picam = VideoStream(usePicamera=True).start()
		while True:
			try:
				raw_message = self.algorithm.read()
				if raw_message is None:
					continue

				message_list = raw_message.decode().split("|")
				if(len(message_list) > 2):
					print(message_list)
		
				for message in message_list:
					if len(message) <= 0:
						continue

					elif (message[0] == 'C'):
						image = picam.read() #capture image
						self.image_queue.put_nowait([image, message[1:].encode()])
						pass

					elif (message == 'EF'):
						image = picam.read()
						self.image_queue.put_nowait([ image, "END"])
						pass

					elif (message[0] == 'M'):
						mdf_string_pc = message[1:]
						self.mdf_string[0] = mdf_string_pc
						self.android_queue.put_nowait("{M:"+mdf_string_pc+ "}|")
						
					elif(message[0] == 'K'):
						self.message_queue.put_nowait(self.format_for(ARDUINO_HEADER, message[1:].encode()))
					else:
						print('from _read_algo = {}'.format(message))
						self.algorithm_to_android(message.encode())
						messageArd = message + '|'
						self.message_queue.put_nowait(self.format_for(ARDUINO_HEADER, messageArd.encode()))

			except Exception as error:
				raise error


	def algorithm_to_android(self, message):

		msg_separator = ""
		msg_send = "{m:"+message.decode()[0]
		msg_send = msg_send.encode()

		if len(msg_send) <= 0:
			return

		elif (msg_send == TURN_LEFT_AND):
			self.android_queue.put_nowait("{m:A|")

		elif (msg_send == TURN_RIGHT_AND):
			self.android_queue.put_nowait("{m:D|")

		elif (msg_send == MOVE_FORWARD_AND):
			self.android_queue.put_nowait("{m:W1|")


	def read_android(self):
		while True:
			try:
				rawmessage = self.android.read()
				if rawmessage == None:
					continue
				message_list = rawmessage.splitlines()
				for message in message_list:
					if len(message) <= 0:
						continue
					elif(message == "MDF".encode()):
						self.android_queue.put_nowait("{M:"+self.mdf_string[0]+ "}|")
					elif(message == "IMAGE".encode()):
						for image in self.IMAGE_LIST:
							self.android_queue.put_nowait('{s:'+image+'|')
					else:
						print("from _read_Android = {}".format(message+NEWLINE))
						self.message_queue.put_nowait(self.format_for(ALGORITHM_HEADER, message + NEWLINE))

			except Exception as error:
				print('read android error - {}'.format(str(error)))
				break

	def write_arduino_and_algorithm(self):
		while True:
			target = None
			try:
				if not self.message_queue.empty():
					message = self.message_queue.get_nowait()
					target, payload = message['target'], message['payload']
					if target == ALGORITHM_HEADER:
						self.algorithm.write(payload)
					elif target == ARDUINO_HEADER:
						self.arduino.write(payload)
			except Exception as error:
				print('failed {}'.format(error))
				break

	def write_android(self):
		while True:
			try:
				if not self.android_queue.empty():
					message = self.android_queue.get_nowait()
					self.android.write(message)
			except Exception as error:
				print('Process write_android failed: ' + str(error))
				break

	def image_processing(self):
		send_images = imagezmq.ImageSender(connect_to=image_processing_server_url)
		listOfImageId = []

		while True:
			try:
				if not self.image_queue.empty():
					image = self.image_queue.get_nowait()
					image_coordinates = image[1]
					result = send_images.send_image(image_coordinates, image[0])
					result = result.decode('utf-8')

					if result != 'End':
						if(len(result) != 0):
							if result != 'None':
								self.IMAGE_LIST.append(result)
						self.android_queue.put_nowait('{s:'+result+'|')
						
					else:
						break

			except Exception as error:
				print("_process_pic failed: {}".format(str(error)))


