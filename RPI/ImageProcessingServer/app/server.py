import os
import shutil
import sys
import darknet
import imutils
import random
import time
from datetime import datetime
from image_receiver import imagezmq_custom as imagezmq
import cv2
import numpy as np
import tensorflow as tf
import pickle
import PIL
import glob
from PIL import Image
from math import ceil, floor
import timeit

IMG_ENCODING = '.jpg'

WEIGHT_FILE_PATH = 'yolov4tiny.weights'
CONFIG_FILE_PATH = './cfg/custom-yolov4-tiny-detector.cfg'
DATA_FILE_PATH = './cfg/coco.data'
RPI_IP = '192.168.11.11'
MJPEG_STREAM_URL = 'http://' + RPI_IP + '/html/cam_pic_new.php'
YOLO_BATCH_SIZE = 4
THRESH = 0.80 #may want to lower and do filtering for specific images later

IMG_WIDTH = 500

PORT = 5051
FORMAT = 'utf-8'
SERVER = '192.168.11.11'
ADDR = (SERVER, PORT)

PROCESSED_IMAGE_IDS = []

sys.path.append("..")

class ImageProcessingServer:
    def __init__(self):
        self.image_hub = imagezmq.CustomImageHub()
        self.results = {}
        self.images = {}

        # load the our fine-tuned model and label binarizer from disk
        print("Loading model...")
        self.network, self.class_names, self.class_colors = darknet.load_network(
            CONFIG_FILE_PATH,
            DATA_FILE_PATH,
            WEIGHT_FILE_PATH,
            YOLO_BATCH_SIZE
        )

    def image_detection(self, image):
        # Darknet doesn't accept numpy images.
        # Create one with image we reuse for each detect
        #Modified from darknet_images.py
        #Takes in direct image instead of path
        width = darknet.network_width(self.network)
        height = darknet.network_height(self.network)
        darknet_image = darknet.make_image(width, height, 3)

        image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        image_resized = cv2.resize(image_rgb, (width, height),
                                   interpolation=cv2.INTER_LINEAR)

        darknet.copy_image_from_bytes(darknet_image, image_resized.tobytes())
        detections = darknet.detect_image(self.network, self.class_names, darknet_image, thresh=THRESH)
        darknet.free_image(darknet_image)
        image = darknet.draw_boxes(detections, image_resized, self.class_colors)
        return cv2.cvtColor(image, cv2.COLOR_BGR2RGB), detections

    def show_all_images(self):
        frame_list = list(self.images.values())
        for index, frame in enumerate(frame_list):
            frame = imutils.resize(frame, width=400)
            cv2.imshow('Image' + str(index), frame)

        if cv2.waitKey() & 0xFF == ord('q'):
            cv2.destroyAllWindows()


    def start(self):
        print('\nStarted image processing server.\n')
        while True:
            print('Waiting for image from RPi...')

            #cdt,frame = self.image_hub.recv_image()

            # for testing purposes only
            cdt = "10:4:10:5:10:6"
            frame = cv2.imread(r"C:\Users\Mehul Kumar\Desktop\MDP\20S1-MDP-Image-Recognition\images\images-lab-labeled\multi_19.JPEG")
            
            if(cdt == "END"):
                # stitch images to show all identified obstacles
                self.stitch_images()
                print("Stitching Images...")
                result_frame_list = list(self.images.values())
                self.show_all_images(result_frame_list)
                print("Image Processing Server Ended")
                break
                
            print('Connected and received frame at time: ' + str(datetime.now()))

            frame = imutils.resize(frame, width=IMG_WIDTH)

            # form image file path for saving
            raw_image_name = cdt.replace(":","") + IMG_ENCODING
            raw_image_path = 'captured_images/' + raw_image_name
            # save raw image
            save_success = cv2.imwrite(raw_image_path, frame)

            # split using bounding boxes
            cdt_list = list(cdt.split(":"))
            cut_width = 3
            cut_height = 3
            start_time = timeit.default_timer()
            image, detections = self.image_detection(frame)
            reply = []

            for i in detections:
                id = i[0] #string
                confidence = i[1] #string
                bbox = i[2] #tuple

                x, y = self.calc_cdts(cdt_list, cut_width, bbox, image.shape[0])
                if(x == -1):
                    continue
                
                print('ID detected: ' + id, ', Confidence: ' + confidence)
                if id in self.results:
                    print('ID has been detected before')
                    if float(confidence) > float(self.results[id][1]):
                        print('Confidence higher. Replacing existing image.')
                        del self.results[id] #remove existing result from dict
                        del self.images[id] #remove existing img from dict
                        self.results[id] = i #add new result to dict. DUPLICATE ID IN VALUE PAIR!
                        self.images[id] = image #add new result to dict
                        processed_image_path = 'processed_images/' + raw_image_name[:raw_image_name.rfind(".")] + "_processed" + IMG_ENCODING
                        save_success = cv2.imwrite(processed_image_path, image)
                        reply.append(id + 'at' + x+ ',' + y)
                        
                    else:
                        print('Confidence lower. Keeping existing image.')
                        pass
                else:
                    print('New ID. Saving to results and image dict.')
                    self.results[id] = i
                    self.images[id] = image
                    processed_image_path = 'processed_images/' + raw_image_name[:raw_image_name.rfind(".")] + "_processed" + IMG_ENCODING
                    save_success = cv2.imwrite(processed_image_path, image)
                    reply.append(id + 'at' + x+ ',' + y)
                    
            #reply variable TODO
            print("Time to process: ", timeit.default_timer() - start_time)
            if len(reply) == 0:
                self.image_hub.send_reply("None")
            else:
                #self.image_hub.send_reply(str(reply))
                print(str(reply))

                # for TESTING ONLY
                self.stitch_images()
                print("Stitching Images...")
                self.show_all_images()
                print("Image Processing Server Ended")
                break

    def calc_cdts(self, cdt_list, cut_width, bbox, img_width):
        xmin, ymin, xmax, ymax = darknet.bbox2points(bbox)
        x = (xmin + xmax)/2
        box_width = xmax - xmin
        
                
        for w in range(cut_width):
            w = w+1
            section_width = float(img_width)/cut_width*w
            if xmin<section_width:
                if (box_width/2)<(section_width - xmin):
                    if cdt_list[2*w-1]=="-1":
                        text = ""
                        break
                    else:
                        return cdt_list[2*w-2], cdt_list[2*w-1]

        return -1, -1

        
    def stitch_images(self):
        frameWidth = 1920
        imagesPerRow = 5
        padding = 0

        # read processed_images from disk
        os.chdir('processed_images')
        images = glob.glob("*.jpg")

        imgWidth, imgHeight = Image.open(images[0]).size
        # set scaling factor
        scaleFactor = (frameWidth-(imagesPerRow-1)*padding)/(imagesPerRow*imgWidth)
        scaledImgWidth = ceil(imgWidth*scaleFactor)
        scaledImgHeight = ceil(imgHeight*scaleFactor)

        rowNo = ceil(len(images)/imagesPerRow)
        frameHeight = ceil(scaleFactor*imgHeight*rowNo)

        newImg = Image.new('RGB', (frameWidth, frameHeight))

        i,j=0,0
        for idx, img in enumerate(images):
            if idx%imagesPerRow==0:
                i=0
            img = Image.open(img)
            # resize image
            img.thumbnail((scaledImgWidth,scaledImgHeight)) 
            y_cord = (j//imagesPerRow)*scaledImgHeight
            newImg.paste(img, (i,y_cord))
            i=(i+scaledImgWidth)+padding
            j+=1
        newImg.save("stitched_output.png", "PNG", quality=80, optimize=True, progressive=True)
        os.chdir("..")
