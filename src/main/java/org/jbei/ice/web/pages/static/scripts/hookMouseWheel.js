function isOverSwf(mEvent)
{
	var elem;
	if (mEvent.srcElement) {
		elem = mEvent.srcElement;
	} else if (mEvent.target) {
		elem = mEvent.target;
	}
	if (elem.nodeName.toLowerCase() == "object" || elem.nodeName.toLowerCase() == "embed") {
			if (elem.getAttribute("type") == "application/x-shockwave-flash") {
				return true;
			}
	}
	return false;
}

function onMouseWheel(event)
{
	if (!event)
		event = window.event;

	if (isOverSwf(event)) {
		return cancelEvent(event);
	}

	return true;
}

function cancelEvent(e)
{
	e = e ? e : window.event;
	if (e.stopPropagation)
		e.stopPropagation();
	if (e.preventDefault)
		e.preventDefault();
	e.cancelBubble = true;
	e.cancel = true;
	e.returnValue = false;
	return false;
}

if (window.addEventListener) window.addEventListener('DOMMouseScroll', onMouseWheel, false);
window.onmousewheel = document.onmousewheel = onMouseWheel;