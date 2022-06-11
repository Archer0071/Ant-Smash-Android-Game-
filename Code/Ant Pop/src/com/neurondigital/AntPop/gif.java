package com.neurondigital.AntPop;

import java.util.Random;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;

public class gif {

	int length, itemsx, itemsy, speed;
	Bitmap[] image;
	Bitmap[] rotated_image;
	Bitmap sheet;
	int[] imagenumber;
	long[] start_time;

	int height, imgheight;
	int width, imgwidth;
	float direction = 0;

	/**
	 * 
	 * @param res
	 *            The resources object containing the image data
	 * @param id
	 *            The resource id of the image data
	 * @param itemsx
	 *            number ofcoloums
	 * @param itemsy
	 *            number of rows
	 * @param length
	 *            number of images in animation
	 * @param speed
	 *            speed of animation 1-100
	 * @param distinct
	 *            number of different sub animations - ie. different start image
	 * @param scale
	 *            scale factor of image
	 */
	public gif(Resources res, int id, int itemsx, int itemsy, int length, int speed, int distinct, float scale) {
		sheet = BitmapFactory.decodeResource(res, id);
		this.length = length;
		this.itemsx = itemsx;
		this.itemsy = itemsy;
		this.speed = speed;
		image = new Bitmap[length];
		rotated_image = new Bitmap[length];
		setDistinctAnimations(distinct);
		convert();

		//scale images
		for (int count = 0; count < length; count++) {
			image[count] = Bitmap.createScaledBitmap(image[count], (int) ((float) (scale)), (int) (((float) (scale) / image[count].getWidth()) * image[count].getHeight()), true);
		}

		imgheight = (int) image[0].getHeight();
		imgwidth = (int) image[0].getWidth();

	}

	public void setDistinctAnimations(int distinct) {
		imagenumber = new int[distinct];
		start_time = new long[distinct];
		for (int count = 0; count < distinct; count++) {
			imagenumber[count] = -1;
			start_time[count] = SystemClock.uptimeMillis();
		}
	}

	public void draw(Canvas canvas, int x, int y, int id) {
		if (imagenumber[id] == -1) {
			imagenumber[id] = new Random().nextInt(length - 1);
		}

		//draw image
		canvas.drawBitmap(rotated_image[imagenumber[id]], x, y, null);
		//update to next image
		long now = SystemClock.uptimeMillis();
		if (now > start_time[id] + (100 - speed)) {
			start_time[id] = SystemClock.uptimeMillis();
			imagenumber[id]++;
			if (imagenumber[id] + 1 > length)
				imagenumber[id] = 0;
		}
	}

	public void drawRenderedSheet(Canvas canvas, int x, int y) {
		//render image in sheet form and draw (for testing)
		int tile_height = (int) (sheet.getHeight() / itemsy);
		int tile_width = (int) (sheet.getWidth() / itemsx);
		for (int y2 = 0; y2 < itemsy; y2++) {
			for (int x2 = 0; x2 < itemsx; x2++) {
				if ((x2 + (itemsx * y2)) < length)
					canvas.drawBitmap(image[x2 + (itemsx * y2)], (x2 * tile_width) + 50, (y2 * tile_height) + 50, null);
			}
		}
	}

	private void convert() {
		//convert from sheet to image array
		int tile_height = (int) (sheet.getHeight() / itemsy);
		int tile_width = (int) (sheet.getWidth() / itemsx);
		for (int y = 0; y < itemsy; y++) {
			for (int x = 0; x < itemsx; x++) {
				if ((x + (itemsx * y)) < length)
					image[x + (itemsx * y)] = Bitmap.createBitmap(sheet, (x * tile_width), (y * tile_height), tile_width, tile_height);
			}
		}
		for (int count = 0; count < length; count++) {
			rotated_image[count] = image[count];
		}
	}

	public void rotate(float direction) {

		float angle = direction;
		this.direction = direction;

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		Bitmap m = Bitmap.createBitmap((int) 5, 5, Bitmap.Config.ARGB_8888);

		height = ((int) (imgwidth * Math.abs(Math.sin(direction * (Math.PI / 180)))) == 0) ? imgheight : (int) (imgwidth * Math.abs(Math.sin(direction * (Math.PI / 180))));
		width = ((int) (imgwidth * Math.abs(Math.cos(direction * (Math.PI / 180)))) == 0) ? imgwidth : (int) (imgwidth * Math.abs(Math.cos(direction * (Math.PI / 180))));

		for (int i = 0; i < length; i++) {
			rotated_image[i] = Bitmap.createScaledBitmap(m, width, height, true);
			Canvas canvas = new Canvas(rotated_image[i]);
			//canvas.drawColor(Color.RED);
			canvas.rotate(angle, rotated_image[i].getWidth() / 2, rotated_image[i].getHeight() / 2);
			canvas.drawBitmap(image[i], (rotated_image[i].getWidth() / 2) - (imgwidth / 2), (rotated_image[i].getHeight() / 2) - (imgheight / 2), paint);
			canvas.rotate(-angle, rotated_image[i].getWidth() / 2, rotated_image[i].getHeight() / 2);
		}

	}

}
