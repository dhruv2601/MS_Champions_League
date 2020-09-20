import numpy as np
import os
from glob import glob
import scipy.io as sio
from skimage.io import imread, imsave
from skimage.transform import rescale, resize
from time import time
import argparse
import ast

from api import PRN

from utils.estimate_pose import estimate_pose
from utils.rotate_vertices import frontalize
from utils.render_app import get_visibility, get_uv_mask, get_depth_image
from utils.write import write_obj_with_colors, write_obj_with_texture, write_obj_with_colors_texture


def main(imgPath):
    print('imgPath: ' + imgPath)
    if (True):  # save texture to obj file
        import cv2
        from utils.cv_plot import plot_kpt, plot_vertices, plot_pose_box

    # ---- init PRN
    os.environ['CUDA_VISIBLE_DEVICES'] = '-1'  # GPU number, -1 for CPU
    prn = PRN(is_dlib=True)

    # ------------- load data
    image_folder = imgPath
    save_folder = 'TestImages/results'
    if not os.path.exists(save_folder):
        os.mkdir(save_folder)

    types = ('*.jpg', '*.png')
    image_path_list = []
    for files in types:
        image_path_list.extend(glob(os.path.join(image_folder, files)))
    total_num = len(image_path_list)

    for i, image_path in enumerate(image_path_list):
        print('i: '+str(i) + '  imgPath: '+image_path)
        name = image_path.strip().split('/')[-1][:-4]

        # read image
        image = imread(image_path)
        [h, w, c] = image.shape
        if c > 3:
            image = image[:, :, :3]


        # the core: regress position map
        if True:
            print('original shape: '+str(image.shape))
            max_size = max(image.shape[0], image.shape[1])
            # if max_size > 1000:
            #     image = rescale(image, 1000. / max_size)
            #     image = (image * 255).astype(np.uint8)
            #     print('original shape 1: '+str(image.shape))
            pos = prn.process(image)  # use dlib to detect face
        else:
            if image.shape[0] == image.shape[1]:
                image = resize(image, (256, 256))
                pos = prn.net_forward(image / 255.)  # input image has been cropped to 256x256
            else:
                box = np.array([0, image.shape[1] - 1, 0, image.shape[0] - 1])  # cropped with bounding box
                pos = prn.process(image, box)

        image = image / 255.
        if pos is None:
            continue

        if True:
            # 3D vertices
            vertices = prn.get_vertices(pos)
            if False:
                save_vertices = frontalize(vertices)
            else:
                save_vertices = vertices.copy()
            save_vertices[:, 1] = h - 1 - save_vertices[:, 1]

        if False:
            imsave(os.path.join(save_folder, name + '.jpg'), image)

        if True:
            # corresponding colors
            colors = prn.get_colors(image, vertices)

            if True:
                pos_interpolated = pos.copy()
                texture = cv2.remap(image, pos_interpolated[:, :, :2].astype(np.float32), None,
                                    interpolation=cv2.INTER_LINEAR, borderMode=cv2.BORDER_CONSTANT, borderValue=(0))
                if False:
                    vertices_vis = get_visibility(vertices, prn.triangles, h, w)
                    uv_mask = get_uv_mask(vertices_vis, prn.triangles, prn.uv_coords, h, w, prn.resolution_op)
                    uv_mask = resize(uv_mask, (256, 256), preserve_range=True)
                    texture = texture * uv_mask[:, :, np.newaxis]
                write_obj_with_colors_texture(os.path.join(save_folder, name + '.obj'), save_vertices, colors,
                                              prn.triangles, texture,
                                              prn.uv_coords / prn.resolution_op)  # save 3d face with texture(can open with meshlab)
            else:
                write_obj_with_colors(os.path.join(save_folder, name + '.obj'), save_vertices, prn.triangles,
                                      colors)  # save 3d face(can open with meshlab)

        if False:
            depth_image = get_depth_image(vertices, prn.triangles, h, w, True)
            depth = get_depth_image(vertices, prn.triangles, h, w)
            imsave(os.path.join(save_folder, name + '_depth.jpg'), depth_image)
            sio.savemat(os.path.join(save_folder, name + '_depth.mat'), {'depth': depth})


if __name__ == '__main__':
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
    main(parser.parse_args())
