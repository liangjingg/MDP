from MultiProcessCommunication import MultiProcessCommunication
import time
def init():
	try:
		rpi = MultiProcessCommunication()
		rpi.start()
	except Exception as error:
		print('Main.py Error!: {}'.format(str(error)))

if __name__ == '__main__':
	init()
