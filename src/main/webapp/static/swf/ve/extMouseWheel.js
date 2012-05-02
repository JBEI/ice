/* 
*  Mouse wheel support for OS X - implemented in javascript because Adobe
*  hasn't implemented it in the mac version of Flash Player  >:(
*  
*  Copyright (c) 2007-2008 Ali Rantakari ( http://hasseg.org/blog )
*  
*  Requires related ActionScript 3 class:
*  org.hasseg.externalMouseWheel.ExternalMouseWheelSupport
*  
*  
*  Changelog:
*  ---------------------------------------------------------------------------------
*  VERSION 1.0 (Ali Rantakari)
*  July 2007
*  
*  - Initial implementation
*  
*  
*  ---------------------------------------------------------------------------------
*  VERSION 1.5 (Changes by Pavel Fljot, http://inreflected.com)
*  Jan 2008
*  
*  - Added statement to support Safari under Windows
*  
*  
*  ---------------------------------------------------------------------------------
*  VERSION 2.0 (Changes by Ali Rantakari)
*  Apr 2008
*  
*  - Changed structure of JavaScript so that everything is encapsulated
*    inside one top-level function
*  - Added support for modifier keys (ctrl (i.e. command on a Mac,) alt, shift)
*  - Fixed code for determining coordinates on different (recent) browsers
*  - Added support for registering multiple Flash objects and their container DIVs
*  
*  
*  ---------------------------------------------------
*  This code is licensed under the MIT License:
*  
*  Permission is hereby granted, free of charge, to any person obtaining a copy
*  of this software and associated documentation files (the "Software"), to deal
*  in the Software without restriction, including without limitation the rights
*  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
*  copies of the Software, and to permit persons to whom the Software is
*  furnished to do so, subject to the following conditions:
*  
*  The above copyright notice and this permission notice shall be included in
*  all copies or substantial portions of the Software.
*  
*  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
*  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
*  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
*  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
*  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
*  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
*  THE SOFTWARE.
*/

var extMouseWheel = function()
{
	// let's not allow other deltas than +/- 3 because that's what flash player does:
	var keepDeltaAtPlusMinusThree = true;
	
	
	// an associative array, where the keys are the ids of flash
	// objects that have registered for the mouse wheel support, and
	// the values their respective container div elements.
	var f = [];
	
	
	
	
	// helper function courtesy of the Adobe peepz
	var thisMovie = function(movieName)
	{
		if (navigator.appName.indexOf("Microsoft") != -1) {
			return window[movieName];
		} else {
			return document[movieName];
		}
	}
	
	// helper functions for getting the position of
	// an element
	// from:  http://blog.firetree.net/2005/07/04/javascript-find-position/
	var findPosX = function(obj)
	{
		var curleft = 0;
		if(obj.offsetParent)
			while(1) 
			{
			  curleft += obj.offsetLeft;
			  if(!obj.offsetParent)
				break;
			  obj = obj.offsetParent;
			}
		else if(obj.x)
			curleft += obj.x;
		return curleft;
	}
	var findPosY = function(obj)
	{
		var curtop = 0;
		if(obj.offsetParent)
			while(1)
			{
			  curtop += obj.offsetTop;
			  if(!obj.offsetParent)
				break;
			  obj = obj.offsetParent;
			}
		else if(obj.y)
			curtop += obj.y;
		return curtop;
	}
	
	// helper function for searching the user-agent string
	var uaContains = function(str)
	{
		return (navigator.userAgent.indexOf(str) != -1);
	}
	
	
	
	
	
	
	// Handler for mouse wheel event:
	var onWheelHandler = function(event)
	{
		var delta = 0;
		if (!event) event = window.event;
		if (event.wheelDelta)
		{
			// Safari
			delta = event.wheelDelta/120;
			if (window.opera) delta = -delta;
		}
		else if (event.detail) // Firefox
			delta = -event.detail*3;
		
		if (keepDeltaAtPlusMinusThree)
		{
			if (delta > 0) delta = 3;
			else if (delta == 0) delta = 0;
			else delta = -3;
		}
		
		if (delta)
		{
			
			var thisFlashMovieId = null;
			for (var j in f)
				if (f[j] != null && f[j] == event.currentTarget) thisFlashMovieId = j;
			
			var thisMouse;
			
			if (uaContains("Camino"))
				thisMouse = {x:event.layerX, y:event.layerY};
			else if (uaContains('Firefox'))
				thisMouse = {x:(event.layerX - findPosX(event.currentTarget)), y:(event.layerY - findPosY(event.currentTarget))};
			else
				thisMouse = {x:event.offsetX, y:event.offsetY};
			
			thisMouse.ctrlKey = (uaContains('Mac')) ? (event.metaKey || event.ctrlKey) : event.ctrlKey;
			thisMouse.altKey = event.altKey;
			thisMouse.shiftKey = event.shiftKey;
			thisMouse.buttonDown = false;
			
			if (thisMovie(thisFlashMovieId).dispatchExternalMouseWheelEvent)
				thisMovie(thisFlashMovieId).dispatchExternalMouseWheelEvent(delta, thisMouse.x, thisMouse.y,
																			thisMouse.ctrlKey, thisMouse.altKey,
																			thisMouse.shiftKey, thisMouse.buttonDown);
		};
		
		// Prevent default actions caused by mouse wheel
		if (event.preventDefault) event.preventDefault();
		event.returnValue = false;
		
	}
	
	
	
	
	
	
	
	
	
	return {
		// initialize mouse wheel capturing by setting the listener
		// (this is called from within the Flash app):
		initCaptureFor: function(aFlashObjectId)
		{
			if (uaContains('Mac') || uaContains('Safari'))
			{
				// find flash object's div container
				var parentdiv = document.getElementById(aFlashObjectId).parentNode;
				while(parentdiv != null && parentdiv.nodeName != "DIV")
					parentdiv = parentdiv.parentNode;
				
				if (parentdiv != undefined && parentdiv != null)
				{
					f[aFlashObjectId] = parentdiv;
					
					if (parentdiv.addEventListener) parentdiv.addEventListener('DOMMouseScroll', onWheelHandler, false); // Firefox
					parentdiv.onmousewheel = onWheelHandler; // Safari
					
					return true;
				}
				else
					return false;
			}
			else
				return false;
		}
	};
	
}();









