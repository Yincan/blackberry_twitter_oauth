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

import net.rim.device.api.ui.*;

public class EvenlySpacedHorizontalFieldManager extends Manager {
	private static final int SYSTEM_STYLE_SHIFT = 32;

	public EvenlySpacedHorizontalFieldManager(long style) {
		super(style);
	}

	protected void sublayout(int width, int height) {
		int availableWidth = width;

		int prevRightMargin = 0;
		int numFields = getFieldCount();
		for (int i = 0; i < numFields; i++) {
			Field currentField = getField(i);
			availableWidth -= Math.max(prevRightMargin, currentField.getMarginLeft());
			prevRightMargin = currentField.getMarginRight();
		}
		availableWidth -= prevRightMargin;

		int maxHeight = 0;
		for (int i = 0; i < numFields; i++) {
			Field currentField = getField(i);
			int currentVerticalMargins = currentField.getMarginTop() + currentField.getMarginBottom();
			layoutChild(currentField, availableWidth, height - currentVerticalMargins);
			availableWidth -= currentField.getWidth();
			maxHeight = Math.max(maxHeight, currentField.getHeight() + currentVerticalMargins);
		}

		int spaceBetweenFields = isStyle(USE_ALL_WIDTH) ? (availableWidth / (numFields + 1)) : 0;

		prevRightMargin = 0;
		int usedWidth = 0;
		int y;
		for (int i = 0; i < numFields; i++) {

			Field currentField = getField(i);

			switch ((int) ((currentField.getStyle() & FIELD_VALIGN_MASK) >> SYSTEM_STYLE_SHIFT)) {
			case (int) (FIELD_BOTTOM >> SYSTEM_STYLE_SHIFT):
				y = maxHeight - currentField.getHeight() - currentField.getMarginBottom();
				break;
			case (int) (FIELD_VCENTER >> SYSTEM_STYLE_SHIFT):
				y = currentField.getMarginTop() + (maxHeight - currentField.getMarginTop() - currentField.getHeight() - currentField.getMarginBottom()) >> 1;
				break;
			default:
				y = currentField.getMarginTop();
			}
			usedWidth += Math.max(prevRightMargin, currentField.getMarginLeft()) + spaceBetweenFields;
			setPositionChild(currentField, usedWidth, y);
			usedWidth += currentField.getWidth();
			prevRightMargin = currentField.getMarginRight();
		}
		usedWidth += prevRightMargin;
		if (isStyle(USE_ALL_WIDTH)) {
			usedWidth = width;
		}
		setExtent(usedWidth, maxHeight);
	}

}
