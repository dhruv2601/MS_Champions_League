import os
from flask import Flask, request, abort, send_from_directory
from functools import wraps
import shutil
import argparse
import ast
import time
import demo
from gevent.pywsgi import WSGIServer
from flask_ngrok import run_with_ngrok

app = Flask(__name__)
# run_with_ngrok(app)


def limit_content_length(max_length):
    def decorator(f):
        @wraps(f)
        def wrapper(*args, **kwargs):
            cl = request.content_length
            if cl is not None and cl > max_length:
                abort(413)
            return f(*args, **kwargs)

        return wrapper

    return decorator


@app.route('/executor', methods=['GET', 'POST'])
@limit_content_length(2 * 100 * 1024 * 1024)
def executing():
    print('executing')
    static_file = request.files['image']
    removeExistingPhotos()
    time.sleep(0.10)
    static_file.save('TestImages/image.jpg')
    time.sleep(0.10)
    demo.main('TestImages/')
    time.sleep(1.0)
    return "200"


@app.route("/downloadObj", methods=['GET', 'POST'])  # check if methods is causing any problem
def downloadObj():
    return send_from_directory("TestImages/results/", "image.obj",
                               as_attachment=True)


@app.route("/downloadPng", methods=['GET', 'POST'])
def downloadPng():
    return send_from_directory("TestImages/results/", "image_texture.png",
                               as_attachment=True)


@app.route("/downloadMtl", methods=['GET', 'POST'])
def downloadMtl():
    return send_from_directory("TestImages/results/", "image.mtl",
                               as_attachment=True)


def removeExistingPhotos():
    import os, shutil
    folder = 'TestImages/'
    for filename in os.listdir(folder):
        file_path = os.path.join(folder, filename)
        try:
            if os.path.isfile(file_path) or os.path.islink(file_path):
                os.unlink(file_path)
            elif os.path.isdir(file_path):
                shutil.rmtree(file_path)
        except Exception as e:
            print('Failed to delete %s. Reason: %s' % (file_path, e))


def getParser():
    parser = argparse.ArgumentParser(
        description='Joint 3D Face Reconstruction and Dense Alignment with Position Map Regression Network')

    parser.add_argument('-i', '--inputDir', default='TestImages/alex.jpg', type=str,
                        help='path to the input directory, where input images are stored.')
    parser.add_argument('-o', '--outputDir', default='TestImages/results', type=str,
                        help='path to the output directory, where results(obj,txt files) will be stored.')
    parser.add_argument('--gpu', default='-1', type=str,  # changing GPU type to CPU, change if needed.
                        help='set gpu id, -1 for CPU')
    parser.add_argument('--isDlib', default=True, type=ast.literal_eval,
                        help='whether to use dlib for detecting face, default is True, if False, the input image should be cropped in advance')
    parser.add_argument('--is3d', default=True, type=ast.literal_eval,
                        help='whether to output 3D face(.obj). default save colors.')
    parser.add_argument('--isMat', default=False, type=ast.literal_eval,
                        help='whether to save vertices,color,triangles as mat for matlab showing')
    parser.add_argument('--isKpt', default=False, type=ast.literal_eval,
                        help='whether to output key points(.txt)')
    parser.add_argument('--isPose', default=False, type=ast.literal_eval,
                        help='whether to output estimated pose(.txt)')
    parser.add_argument('--isShow', default=False, type=ast.literal_eval,
                        help='whether to show the results with opencv(need opencv)')
    parser.add_argument('--isImage', default=False, type=ast.literal_eval,
                        help='whether to save input image')
    # update in 2017/4/10
    parser.add_argument('--isFront', default=False, type=ast.literal_eval,
                        help='whether to frontalize vertices(mesh)')
    # update in 2017/4/25
    parser.add_argument('--isDepth', default=True, type=ast.literal_eval,
                        help='whether to output depth image')
    # update in 2017/4/27
    parser.add_argument('--isTexture', default=True, type=ast.literal_eval,
                        help='whether to save texture in obj file')
    parser.add_argument('--isMask', default=False, type=ast.literal_eval,
                        help='whether to set invisible pixels(due to self-occlusion) in texture as 0')
    # update in 2017/7/19
    parser.add_argument('--texture_size', default=256, type=int,
                        help='size of texture map, default is 256. need isTexture is True')
    args, unknown = parser.parse_args()
    return (args)


if __name__ == '__main__':
    http_server = WSGIServer(('0.0.0.0', 5000), app)
    http_server.serve_forever()
    # app.run()
