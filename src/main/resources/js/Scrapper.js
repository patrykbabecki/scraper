
var bodyElements = document.getElementsByTagName("body")[0].getElementsByTagName("*");
var elementsCount = bodyElements.length;
var parsedElements = [];
for(i = 0; i < elementsCount; i++) {
    var elem = bodyElements[i];
    var elemRectangle = elem.getBoundingClientRect();
    if(elemRectangle.width != 0 && elemRectangle.height != 0) {
        var parsedElement = prepareParsedElement(elem);
        parsedElements.push(parsedElement);
    }
    console.log(i + " from " + elementsCount);
}
console.log(parsedElements);
return parsedElements;



function getElementXPath(elt) {
     var path = "";
     for (; elt && elt.nodeType == 1; elt = elt.parentNode)
    {
   	idx = getElementIdx(elt);
	xname = elt.tagName;
	if (idx > 1) xname += "[" + idx + "]";
	path = "/" + xname + path;
     }
     return path;
}

function getElementIdx(elt) {
    var count = 1;
    for (var sib = elt.previousSibling; sib ; sib = sib.previousSibling)
    {
        if(sib.nodeType == 1 && sib.tagName == elt.tagName)	count++
    }
    return count;
}

function prepareParsedElement(elem) {
    var elemRectangle = elem.getBoundingClientRect();
    var xPathValue = getElementXPath(elem);
    var parentXpathValue = getElementXPath(elem.parentNode);
    var elemPosition = {
        x: elemRectangle.x,
        y: elemRectangle.y,
        width: elemRectangle.width,
        height: elemRectangle.height
    };
    var elemAttributes = {
        class: elem.attributes["class"] !== undefined ? elem.attributes["class"].value : undefined,
        type: elem.attributes["type"] !== undefined ? elem.attributes["type"].value : undefined,
        id: elem.attributes["id"] !== undefined ? elem.attributes["id"].value : undefined,
        name: elem.attributes["name"] !== undefined ? elem.attributes["name"].value : undefined,
        value: elem.attributes["value"] !== undefined ? elem.attributes["value"].value : undefined
    }
    var parsedElement = {
        xPath: xPathValue,
        parentXPath: parentXpathValue,
        tagName: elem.tagName,
        attributes: elemAttributes,
        text: elem.text,
        innerText: elem.innerText,
        position: elemPosition
    };
    return parsedElement;
}
