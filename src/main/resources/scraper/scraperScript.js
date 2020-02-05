function scrapeElem(elem) {
      let rules = getRules();
      let elemRule = findRuleByTagNameAndAttribute(rules, elem);
      if(elemRule === null || elemRule === undefined) {
            return null;
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
                rule: elemRule,
                isNavigation: rule.isNavigation,
                isApplyButton: rule.isApplyButton,
                prefill: getPreffilScrapedElement(labelElement)
       };
    return scrapedElement;
}

function findRuleByTagNameAndAttribute(rules, element) {
     for(let j = 0; j < rules.length; j++) {
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
            for(let a = 0; a < element.attributes.length; a++) {
                let attributeName = element.attributes[a].name;
                let attributeValue = element.attributes[a].value;
                if(isRuleContainsAttribute(attributes, attributeName, attributeValue)) {
                    return true;
                }
            }
            return false;
            debugger;
}

function isRuleContainsAttribute(attributes, name, value) {
    for(let h = 0; h < attributes.length; h++) {
        if(attributes[h].name === name) {
            for(let y = 0; y < attributes[h].values.length; y++) {
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
    }
    return elem;
}

function findLabelElement(rule, elementPosition) {
    return findElement(elementPosition, rule.labelXOffset, rule.labelYOffset, 'label');
}

function findValidationElement(rule, elementPosition) {
    return findElement(elementPosition, rule.validationXOffset, rule.validationYOffset, 'validation');
}

function getElementXPath(elt) {
     let path = "";
     for (; elt && elt.nodeType == 1; elt = elt.parentNode)
     {
   	    let idx = getElementIdx(elt);
	    let xname = elt.tagName;
	    if (idx > 1) xname += "[" + idx + "]";
	    path = "/" + xname + path;
     }
     return path;
}

function getElementIdx(elt) {
    let count = 1;
    for (let sib = elt.previousSibling; sib ; sib = sib.previousSibling)
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

function getPreffilScrapedElement(labelElem) {
    let preffilMap = getPreffilMap();
    let labelText = labelElem !== null ? labelElem.text : null;
    if(labelText !== null) {
         for(let [k, v] of Object.entries(preffilMap)) {
            if(v.includes(cleanLabelText(labelText))) {
                return k;
            }
         }
    }

}

function cleanLabelText(labelText) {
    let label = labelText.toLowerCase();
    label = label.split("*").join("");
    label = label.split(":").join("");
    label = label.trim();
    return label;
}

function getPreffilMap() {
    let map = [];
    map['firstName'] = ['imię','first name', 'name', 'der name', 'vorname'];
    map['lastName'] = ['nazwisko', 'last name', 'der familienname', 'nachname'];
    map['city'] = ['miasto', 'city', 'stadt', 'city name'];
    map['cv'] = ['cv'];
    map['phone'] = ['telefon', 'phone', 'mobile', 'telephone', 'telephon', 'phone number', 'telefonnummer'];
    map['email'] = ['email', 'e-mail-adresse/login', 'e-mail'];
    map['title'] = ['titel', 'title', 'tytół'];
    map['birth'] = ['geburtsdatum', 'birthday', 'date of birth'];
    return map;
}

function getRules() {
    return [
             {
               "name": "inputs",
               "tagName": "INPUT",
               "labelXOffset": "-30",
               "labelYOffset": "5",
               "validationXOffset": "0",
               "validationYOffset": "0",
               "attributes": [],
               "validationAttributes": [],
               "isNavigation": false,
               "isApplyButton": false
             },
             {
               "name": "submit",
               "tagName": "BUTTON",
               "labelXOffset": "5",
               "labelYOffset": "5",
               "validationXOffset": "-50",
               "validationYOffset": "10",
               "attributes": [],
               "validationAttributes": [],
               "isNavigation": false,
               "isApplyButton": true
             }
           ];
}
