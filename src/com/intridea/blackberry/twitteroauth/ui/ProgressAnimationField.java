/**
 * Copyright (c) E.Y. Baskoro, Research In Motion Limited.
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without 
 * restriction, including without limitation the rights to use, 
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 * This License shall be included in all copies or substantial 
 * portions of the Software.
 * 
 * The name(s) of the above copyright holders shall not be used 
 * in advertising or otherwise to promote the sale, use or other 
 * dealings in this Software without prior written authorization.
 * 
 */
package com.intridea.blackberry.twitteroauth.ui;

import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;

public class ProgressAnimationField extends Field implements Runnable {
	private Bitmap _bitmap;
	private int _numFrames;
	private int _frameWidth;
	private int _frameHeight;

	private int _currentFrame;
	private int _timerID = -1;

	private Application _application;
	private boolean _visible;

	public ProgressAnimationField(Bitmap bitmap, int numFrames, long style) {
		super(style | Field.NON_FOCUSABLE);
		_bitmap = bitmap;
		_numFrames = numFrames;
		_frameWidth = _bitmap.getWidth() / _numFrames;
		_frameHeight = _bitmap.getHeight();

		_application = Application.getApplication();
	}

	public void run() {
		if (_visible) {
			invalidate();
		}
	}

	protected void layout(int width, int height) {
		setExtent(_frameWidth, _frameHeight);
	}

	protected void paint(Graphics g) {
		g.drawBitmap(0, 0, _frameWidth, _frameHeight, _bitmap, _frameWidth * _currentFrame, 0);
		_currentFrame++;
		if (_currentFrame >= _numFrames) {
			_currentFrame = 0;
		}
	}

	protected void onDisplay() {
		super.onDisplay();
		_visible = true;
		if (_timerID == -1) {
			_timerID = _application.invokeLater(this, 200, true);
		}
	}

	protected void onUndisplay() {
		super.onUndisplay();
		_visible = false;
		if (_timerID != -1) {
			_application.cancelInvokeLater(_timerID);
			_timerID = -1;
		}
	}
}
