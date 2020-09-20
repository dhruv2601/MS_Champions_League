from scipy.spatial import distance as dist
from imutils.video import FileVideoStream
from imutils.video import VideoStream
from imutils.video import WebcamVideoStream
from imutils import face_utils
import numpy as np
import argparse
import imutils
import time
import dlib
import cv2

import headpose


def eye_distance(left_eye, right_eye):
    lid_to_cornea_distance = dist.euclidean(left_eye[0], left_eye[2])
    extr_dist = dist.euclidean(left_eye[0], right_eye[0])
    extr_dist = extr_dist - lid_to_cornea_distance
    return extr_dist


# construct the argument parse and parse the arguments

ap = argparse.ArgumentParser()
ap.add_argument("-p", "--shape-predictor", required=True,
                help="path to facial landmark predictor")
ap.add_argument("-v", "--video", type=str, default="",
                help="path to input video file")
args = vars(ap.parse_args())

# initialize dlib's face detector (HOG-based) and then create
# the facial landmark predictor
print("[INFO] loading facial landmarks predictor...")
detector = dlib.get_frontal_face_detector()
predictor = dlib.shape_predictor(args["shape_predictor"])

# grab the indexes of the facial landmarks for the left and
# right eye, respectively
(lStart, lEnd) = face_utils.FACIAL_LANDMARKS_IDXS["left_eye"]
(rStart, rEnd) = face_utils.FACIAL_LANDMARKS_IDXS["right_eye"]
(eblStart, eblEnd) = face_utils.FACIAL_LANDMARKS_IDXS["left_eyebrow"]
(ebrStart, ebrEnd) = face_utils.FACIAL_LANDMARKS_IDXS["right_eyebrow"]
(jawStart, jawEnd) = face_utils.FACIAL_LANDMARKS_IDXS["jaw"]
(mouthStart, mouthEnd) = face_utils.FACIAL_LANDMARKS_IDXS["mouth"]
(noseStart, noseEnd) = face_utils.FACIAL_LANDMARKS_IDXS["nose"]

# start the video stream thread
print("[INFO] starting video stream thread...")

vs = WebcamVideoStream(src=0).start()
fileStream = False
time.sleep(1.0)
print("video started")
while True:

    if fileStream and not vs.more():
        break
    frame = vs.read()
    frame = imutils.resize(frame, width=800)
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

    rects = detector(gray, 0)
    # hpd = headpose.HeadposeDetection()

    for rect in rects:

        shape = predictor(gray, rect)
        shape = face_utils.shape_to_np(shape)

        leftEye = shape[lStart:lEnd]
        rightEye = shape[rStart:rEnd]

        leftEyeBrow = shape[eblStart:eblEnd]
        rightEyeBrow = shape[ebrStart:ebrEnd]

        extreme_eye_distance = int(eye_distance(leftEye, rightEye))

        leftEyeHull = cv2.convexHull(leftEye)
        rightEyeHull = cv2.convexHull(rightEye)

        leftEyeBrowHull = cv2.convexHull(leftEyeBrow)
        rightEyeBrowHull = cv2.convexHull(rightEyeBrow)

        ##############
        # Mess Starts#
        ##############

        jaw = shape[jawStart:jawEnd]
        nose = shape[noseStart:noseEnd]
        mouth = shape[mouthStart:mouthEnd]
        fore = shape[69:80]
        lineThickness = 1
        radius = 2
        yellow = (0, 255, 255)

        # Forehead
        cv2.line(frame, (fore[7][0], fore[7][1]), (jaw[0][0], jaw[0][1]), yellow, lineThickness)
        cv2.line(frame, (fore[7][0], fore[7][1]), (fore[0][0], fore[0][1]), yellow, lineThickness)
        cv2.line(frame, (fore[0][0], fore[0][1]), (fore[2][0], fore[2][1]), yellow, lineThickness)
        cv2.line(frame, (fore[3][0], fore[3][1]), (fore[2][0], fore[2][1]), yellow, lineThickness)
        cv2.line(frame, (fore[4][0], fore[4][1]), (fore[3][0], fore[3][1]), yellow, lineThickness)
        cv2.line(frame, (fore[4][0], fore[4][1]), (jaw[16][0], jaw[16][1]), yellow, lineThickness)

        cv2.circle(frame, (fore[0][0], fore[0][1]), radius, yellow, lineThickness)
        cv2.circle(frame, (fore[2][0], fore[2][1]), radius, yellow, lineThickness)
        cv2.circle(frame, (fore[3][0], fore[3][1]), radius, yellow, lineThickness)
        cv2.circle(frame, (fore[4][0], fore[4][1]), radius, yellow, lineThickness)
        cv2.circle(frame, (fore[7][0], fore[7][1]), radius, yellow, lineThickness)
        cv2.circle(frame, (jaw[0][0], jaw[0][1]), radius, yellow, lineThickness)
        cv2.circle(frame, (jaw[16][0], jaw[16][1]), radius, yellow, lineThickness)


        # Jaw
        cv2.line(frame, (jaw[0][0], jaw[0][1]), (jaw[3][0], jaw[3][1]), yellow, lineThickness)
        cv2.line(frame, (jaw[3][0], jaw[3][1]), (jaw[6][0], jaw[6][1]), yellow, lineThickness)
        cv2.line(frame, (jaw[6][0], jaw[6][1]), (jaw[8][0], jaw[8][1]), yellow, lineThickness)
        cv2.line(frame, (jaw[8][0], jaw[8][1]), (jaw[10][0], jaw[10][1]), yellow, lineThickness)
        cv2.line(frame, (jaw[10][0], jaw[10][1]), (jaw[12][0], jaw[12][1]), yellow, lineThickness)
        cv2.line(frame, (jaw[12][0], jaw[12][1]), (jaw[15][0], jaw[15][1]), yellow, lineThickness)
        cv2.line(frame, (jaw[15][0], jaw[15][1]), (jaw[16][0], jaw[16][1]), yellow, lineThickness)

        cv2.circle(frame, (jaw[0][0], jaw[0][1]), radius, yellow, lineThickness)
        cv2.circle(frame, (jaw[3][0], jaw[3][1]), radius, yellow, lineThickness)
        cv2.circle(frame, (jaw[6][0], jaw[6][1]), radius, yellow, lineThickness)
        cv2.circle(frame, (jaw[8][0], jaw[8][1]), radius, yellow, lineThickness)
        cv2.circle(frame, (jaw[10][0], jaw[10][1]), radius, yellow, lineThickness)
        cv2.circle(frame, (jaw[12][0], jaw[12][1]), radius, yellow, lineThickness)
        cv2.circle(frame, (jaw[15][0], jaw[15][1]), radius, yellow, lineThickness)
        cv2.circle(frame, (jaw[16][0], jaw[16][1]), radius, yellow, lineThickness)

        # Left_Mouth_to_Jaw
        cv2.line(frame, (mouth[0][0], mouth[0][1]), (jaw[0][0], jaw[0][1]), yellow, lineThickness)
        cv2.line(frame, (mouth[0][0], mouth[0][1]), (jaw[3][0], jaw[3][1]), yellow, lineThickness)
        cv2.line(frame, (mouth[0][0], mouth[0][1]), (jaw[6][0], jaw[6][1]), yellow, lineThickness)
        cv2.line(frame, (mouth[0][0], mouth[0][1]), (jaw[10][0], jaw[10][1]), yellow, lineThickness)
        cv2.circle(frame, (mouth[0][0], mouth[0][1]), radius, yellow, lineThickness)

        # Righ_Mouth_to_jaw
        cv2.line(frame, (mouth[6][0], mouth[6][1]), (jaw[6][0], jaw[6][1]), yellow, lineThickness)
        cv2.line(frame, (mouth[6][0], mouth[6][1]), (jaw[10][0], jaw[10][1]), yellow, lineThickness)
        cv2.line(frame, (mouth[6][0], mouth[6][1]), (jaw[12][0], jaw[12][1]), yellow, lineThickness)
        cv2.line(frame, (mouth[6][0], mouth[6][1]), (jaw[16][0], jaw[16][1]), yellow, lineThickness)
        cv2.circle(frame, (mouth[6][0], mouth[6][1]), radius, yellow, lineThickness)

        #nose
        cv2.line(frame, (nose[0][0], nose[1][1]), (nose[4][0], nose[4][1]), yellow, lineThickness)
        cv2.line(frame, (nose[0][0], nose[1][1]), (nose[8][0], nose[8][1]), yellow, lineThickness)
        cv2.line(frame, (nose[4][0], nose[4][1]), (nose[8][0], nose[8][1]), yellow, lineThickness)

        cv2.circle(frame, (nose[0][0], nose[1][1]), radius, yellow, lineThickness)
        cv2.circle(frame, (nose[4][0], nose[4][1]), radius, yellow, lineThickness)
        cv2.circle(frame, (nose[8][0], nose[8][1]), radius, yellow, lineThickness)


        left_cornea = ( int((leftEye[0][0] + leftEye[3][0])/2)  ,  int((leftEye[0][1] + leftEye[3][1])/2) )
        right_cornea = ( int( (rightEye[0][0] + rightEye[3][0])/2 ) ,  int( (rightEye[0][1] + rightEye[3][1])/2 ) )

        cv2.circle(frame, left_cornea, radius, yellow, lineThickness)
        cv2.circle(frame, right_cornea, radius, yellow, lineThickness)


        ############
        # Mess Ends#
        ############

        cv2.drawContours(frame, [leftEyeHull], -1, yellow, 1)
        cv2.drawContours(frame, [rightEyeHull], -1, yellow, 1)

        cv2.drawContours(frame, [leftEyeBrowHull], -1, yellow, 1)
        cv2.drawContours(frame, [rightEyeBrowHull], -1, yellow, 1)

        eye_cam_distance = 2000 / extreme_eye_distance
        power_of_eye = (eye_cam_distance - 200) / (2 * eye_cam_distance)

        # frame, angles = hpd.process_image(frame)

        cv2.putText(frame, "Cornea Distance: {:.2f}".format(extreme_eye_distance), (10, 30),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
        cv2.putText(frame, "Power: {:.2f}D".format(power_of_eye), (300, 30),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
        if (eye_cam_distance >= 100):
            cv2.putText(frame, "Too Far", (150, 250),
                        cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2)

        # show the frame
    cv2.imshow("Frame", frame)
    key = cv2.waitKey(1) & 0xFF

    # if the `q` key was pressed, break from the loop
    if key == ord("q"):
        break

# do a bit of cleanup
cv2.destroyAllWindows()
vs.stop()
