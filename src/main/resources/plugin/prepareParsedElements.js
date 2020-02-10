
function parse() {
    let bodyElements = document.getElementsByTagName("body")[0].getElementsByTagName("*");
    let elementsCount = bodyElements.length;
    let parsedElements = [];
    for(i = 0; i < elementsCount; i++) {
        let elem = bodyElements[i];
        let elemRectangle = elem.getBoundingClientRect();
        if(elemRectangle.width != 0 && elemRectangle.height != 0) {
            let parsedElement = prepareParsedElement(elem);
            parsedElements.push(parsedElement);
        }
    }
    console.log(parsedElements);
    return parsedElements;
}

function getElementXPath(elt) {
     let path = "";
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
    let count = 1;
    for (var sib = elt.previousSibling; sib ; sib = sib.previousSibling)
    {
        if(sib.nodeType == 1 && sib.tagName == elt.tagName)	count++
    }
    return count;
}

function prepareParsedElement(elem) {
    let elemRectangle = elem.getBoundingClientRect();
    let xPathValue = getElementXPath(elem);
    let parentXpathValue = getElementXPath(elem.parentNode);
    let elemPosition = {
        x: elemRectangle.x,
        y: elemRectangle.y,
        width: elemRectangle.width,
        height: elemRectangle.height
    };
    let elemAttributes = [];
    for(k = 0; k < elem.attributes.length; k++) {
        let valueString = String(elem.attributes[k].value);
        let elemAttribute = {
            name: elem.attributes[k].name,
            value: valueString
        }
        elemAttributes.push(elemAttribute);
    }

    let parsedElement = {
        xPath: xPathValue,
        parentXPath: parentXpathValue,
        tagName: elem.tagName,
        attributes: elemAttributes,
        text: elem.textContent,
        innerText: elem.innerText,
        position: elemPosition
    };
    return parsedElement;
}

parse();
