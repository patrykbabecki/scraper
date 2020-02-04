chrome.storage.local.get('config', function (items) {
    console.log(items.config);
    let rules = items.config.rules;
    let elements = items.config.elements;
    let showScrapedElements = items.config.showScrapedElements;

    while(document.getElementsByClassName('mark-sign').length !== 0) {
        clearMarkSigns();
    }

    scrapedElements = calculateModel(rules, elements);
    preffilScrapedElements(scrapedElements)

    console.log(scrapedElements);

    drawScrapedElements(scrapedElements);

    chrome.storage.local.remove('config');
});

function clearMarkSigns() {
    let markSigns = document.getElementsByClassName('mark-sign');
    for(i = 0; i < markSigns.length; i++) {
        markSigns[i].remove();
    }
}

function findRuleByTagNameAndAttribute(rules, element) {
     for(j = 0; j < rules.length; j++) {
        let rule = rules[j];
        if(rule.tagName.toUpperCase() === element.tagName.toUpperCase()) {
            if (rule.attributes.length === 0) {
                return rule;
            }
            let attributes = rule.attributes
            if(isElemContainsAttributes(element, attributes)) {
                return rule;
            }
        }
     }
     return null;
}

function isElemContainsAttributes(element, attributes) {
            for(a = 0; a < element.attributes.length; a++) {
                let attributeName = element.attributes[a].name;
                let attributeValue = element.attributes[a].value;
                if(isRuleContainsAttribute(attributes, attributeName, attributeValue)) {
                    return true;
                }
            }
            return false;
            debugger;
}

function isElemMatchRuleAttributes(elem, ruleAttributes) {
    let orAttributes = ruleAttributes.filter(x => !x.andAttribute);
    let andAttributes = ruleAttributes.filter(x => x.andAttribute);

    let isElemContainsOrAttributes = isElemContainsAttributes(elem, orAttributes);
    if(isElemContainsOrAttributes && andAttributes.length === 0) {
        return true;
    }
    if(orAttributes.length === 0) {
        isElemContainsOrAttributes = true;
    }
    let isElemContainAndAttributes = true;
    for(y = 0; y < andAttributes.length; y++) {
        let isElemContainAttribute = isElemContainsAttributes(elem, andAttributes[y])
        isElemContainAndAttributes = isElemContainAndAttributes && (isElemContainAttribute == andAttributes[y].containAttributes)
    }
    return isElemContainAndAttributes && isElemContainsOrAttributes;
}

function isRuleContainsAttribute(attributes, name, value) {
    for(h = 0; h < attributes.length; h++) {
        if(attributes[h].name === name) {
            for(y = 0; y < attributes[h].values.length; y++) {
                let ruleAttributeValue = attributes[h].values[y];
                if(value !== undefined && value !== null && value.includes(ruleAttributeValue)) {
                    return true;
                }
            }
        }
    }
    return false;
}

function findElement(elementPosition, xOffset, yOffset, elemType) {
    let x = parseInt(elementPosition.x, 10) + parseInt(xOffset, 10);
    let y = parseInt(elementPosition.y, 10) + parseInt(yOffset, 10);
    let elem = document.elementFromPoint(x, y);
    if(elem !== undefined && elem !== null) {
        elem.setAttribute('pos', 'x = ' + x + ' y = ' + y);
//        drawPointElement(x, y, elemType);
    }
    return elem;
}

function drawPointElement(x, y, elemType) {
    let point = document.createElement('span');
    point.innerHTML = '<b>*<b>';
    point.setAttribute('style', getStyle(elemType));
    point.style.position = "absolute";
    point.style.left = x + 'px';
    point.style.top = y + 'px';
    point.setAttribute('class', 'mark-sign');
    document.getElementsByTagName('body')[0].appendChild(point);
}

function findLabelElement(rule, elementPosition) {
    return findElement(elementPosition, rule.labelXOffset, rule.labelYOffset, 'label');
}

function findValidationElement(rule, elementPosition) {
    return findElement(elementPosition, rule.validationXOffset, rule.validationYOffset, 'validation');
}

function calculateModel(rules, elements) {
    let scrapedElements = [];
    for(i = 0; i < elements.length; i++) {
         let elem = elements[i];
         let elemRule = findRuleByTagNameAndAttribute(rules, elem);
         if(elemRule === null || elemRule === undefined) {
            continue;
         }
         let element = scrollToElementByXPath(elem.xPath);
         let scrolledElement = prepareParsedElement(element);
         let labelElement = prepareParsedElement(findLabelElement(elemRule, scrolledElement.position));
         let validationElem = findValidationElement(elemRule, scrolledElement.position);
         let validationElement = prepareParsedElement(validationElem);
         let isElemValid = null;
         if(validationElem !== undefined && validationElem !== null) {
            isElemValid = !isElemContainsAttributes(validationElem, elemRule.validationAttributes);
         }
         let scrapedElement = {
                element: elem,
                label: labelElement,
                validation: validationElement,
                isValid: isElemValid,
                rule: elemRule
             };
         scrapedElements.push(scrapedElement);
    }
    return scrapedElements;
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
    var count = 1;
    for (var sib = elt.previousSibling; sib ; sib = sib.previousSibling)
    {
        if(sib.nodeType == 1 && sib.tagName == elt.tagName)	count++
    }
    return count;
}

function prepareParsedElement(elem) {
    if(elem === null || elem === undefined) {
        return  elem;
    }
    let elemRectangle = elem.getBoundingClientRect();
    let xPathValue = getElementXPath(elem);
    let parentXpathValue = getElementXPath(elem.parentNode);
    let elemPosition = {
        x: elemRectangle.x,
        y: elemRectangle.y,
        width: elemRectangle.width,
        height: elemRectangle.height
    };
    let elemAttributes = {
        class: elem.attributes["class"] !== undefined ? elem.attributes["class"].value : undefined,
        type: elem.attributes["type"] !== undefined ? elem.attributes["type"].value : undefined,
        id: elem.attributes["id"] !== undefined ? elem.attributes["id"].value : undefined,
        name: elem.attributes["name"] !== undefined ? elem.attributes["name"].value : undefined,
        value: elem.attributes["value"] !== undefined ? elem.attributes["value"].value : undefined,
        style: elem.attributes["style"] !== undefined ? elem.attributes["style"].value : undefined
    };
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

function scrollToElementByXPath(xPath) {
    let element = document.evaluate(xPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
    element.scrollIntoView(false);
    window.scrollBy(0, 100);
    return element;
}

function getStyle(elemType) {
    if(elemType === 'elem') {
        return 'border-style: double; border-color: green';
    }
    if(elemType === 'label') {
        return 'border-style: double; border-color: blue';
    }
    if(elemType === 'validation') {
        return 'border-style: double; border-color: black';
    }
    return 'border-style: double; border-color: red';
}

function drawScrapedElements(scrapedElements) {
    for(i = 0; i < scrapedElements.length; i++) {
        let scrapedElem = scrapedElements[i];
        if(scrapedElem.element !== undefined && scrapedElem.element !== null) {
            drawScrapedElement(scrapedElem.element.xPath, 'elem');
            addTooltipWithInfo(scrapedElem.element.xPath, scrapedElem);
        }
        if(scrapedElem.label !== undefined && scrapedElem.label !== null) {
            drawScrapedElement(scrapedElem.label.xPath, 'label');
            addTooltipWithInfo(scrapedElem.label.xPath, scrapedElem);
        }
        if(scrapedElem.validation !== undefined && scrapedElem.validation !== null) {
            drawScrapedElement(scrapedElem.validation.xPath, 'validation');
            addTooltipWithInfo(scrapedElem.validation.xPath, scrapedElem);
        }
    }
}

function addTooltipWithInfo(xPath, scrapedElement) {
    let elem = document.evaluate(xPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
    let tooltipMessage = prepareTooltipMessage(scrapedElement);
    elem.setAttribute('title', tooltipMessage);
}

function prepareTooltipMessage(scrapedElement) {
    let message = "Rule Name : " + scrapedElement.rule.name + " \n " ;
    message += "Label : " + (scrapedElement.label !== null ? scrapedElement.label.text.trim() : "") + " \n ";
    message += "Pre-fill : " + ((scrapedElement.prefill !== null && scrapedElement.prefill !== undefined) ? scrapedElement.prefill : "") + "\n";
    message += "Is Valid : " + scrapedElement.isValid + " \n ";
    message += "Validation : " + ((scrapedElement.validation !== undefined && scrapedElement.validation !== null) ? scrapedElement.validation.text.trim() : "");
    return message;
}

function drawScrapedElement(xPath, elemType) {
    let element = document.evaluate(xPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
    if(element !== undefined && element !== null) {
        let originalStyle = element.attributes.style;
        let elemStyle = getStyle(elemType);
        let newStyle = elemStyle;
        if(originalStyle !== undefined && originalStyle !== null) {
            newStyle = element.attributes.style.value + "; " + elemStyle;
        }
        element.setAttribute('style', newStyle);
    }
}

function preffilScrapedElements(scrapedElements) {
    let preffilMap = getPreffilMap();
    for(i = 0 ; i < scrapedElements.length ; i++) {
        let scrapedElem = scrapedElements[i];
        let labelText = scrapedElem.label !== null ? scrapedElem.label.text : null;
        if(labelText !== null) {
            for(let [k, v] of Object.entries(preffilMap)) {
                if(v.includes(cleanLabelText(labelText))) {
                    scrapedElem.prefill = k;
                }
            }
        }
    }
}

function getPreffilMap() {
    let map = [];
    map['firstName'] = ['imię','first name', 'name', 'der name', 'vorname'];
    map['lastName'] = ['nazwisko', 'last name', 'der familienname', 'nachname'];
    map['city'] = ['miasto', 'city', 'stadt', 'city name'];
    map['cv'] = ['cv'];
    map['phone'] = ['telefon', 'phone', 'mobile', 'telephone', 'telephon', 'phone number', 'telefonnummer'];
    map['email'] = ['email', 'e-mail-adresse/login'];
    map['title'] = ['titel', 'title', 'tytół'];
    map['birth'] = ['geburtsdatum', 'birthday', 'date of birth'];
    return map;
}

function cleanLabelText(labelText) {
    let label = labelText.toLowerCase();
    label = label.split("*").join("");
    label = label.split(":").join("");
    return label;
}

function wait(ms){
   var start = new Date().getTime();
   var end = start;
   while(end < start + ms) {
     end = new Date().getTime();
  }
}