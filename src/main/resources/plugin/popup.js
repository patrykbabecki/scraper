let allInputTypes = ['text', 'checkbox', ]
let showElementsCheckbox = document.getElementById('show_recognized_elements');
let calculateButton = document.getElementById('calc_elements_button');
let generateScript = document.getElementById('generate_script_button');
let addRuleButton = document.getElementById('add_rule_button');
let loadRuleInput = document.getElementById('load_rule_file');
let atsNameInput = document.getElementById('ats_name');

let sendToBackendButton = document.getElementById('send_form_values');
let generateRules = document.getElementById('generate_rules');
let formOption = document.getElementById('send_document_elements');

sendToBackendButton.onclick = function(elem) {
    chrome.tabs.query({active: true, currentWindow: true}, function(tabs) {
           chrome.tabs.executeScript(
              tabs[0].id,
              {file: 'prepareParsedElements.js'},
              function(result) {
                       let formOpt = formOption.options[formOption.selectedIndex].text;
                       let atsNameValue = atsNameInput.value;
                       let postObject = {
                            atsName: atsNameValue,
                            option: formOpt,
                            elements: result[0]
                       };

                       let xhr = new XMLHttpRequest();
                       let url = "http://127.0.0.1:8080/page/elements";
                       xhr.open("POST", url, true);
                       xhr.setRequestHeader("Content-Type", "application/json");
//                       xhr.onreadystatechange = function () {
//                           if (xhr.readyState === 4 && xhr.status === 200) {
//                               var json = JSON.parse(xhr.responseText);
//                               console.log(json.email + ", " + json.password);
//                           }
//                       };
                       let data = JSON.stringify(postObject);
                       xhr.send(data);

               })});

}

generateRules.onclick = function(elem) {
    console.log("generate rules");
}

showElementsCheckbox.onclick = function(element) {
    console.log('show elements');
}

calculateButton.onclick = function(element) {
      calculateElements(scrapeElementsNextToInputs)
}

generateScript.onclick = function(element) {
    let rules = getRules();
    let docContent = JSON.stringify(rules, null, 4);
    let doc = URL.createObjectURL( new Blob([docContent], {type: 'application/json'}) );
    let filename = atsNameInput.value + "_rules.json";
    chrome.downloads.download({ url: doc, filename: filename, conflictAction: 'overwrite', saveAs: true });
}

loadRuleInput.onchange = function(element) {
    let loadedFile = loadRuleInput.files[0];
    let reader = new FileReader();
    reader.onload = function(e) {
        let resultJsonContent = e.target.result;
        let rules = JSON.parse(resultJsonContent);
        let fileName = loadRuleInput.files[0].name;
        let atsName = fileName.indexOf('_') !== 0 ? fileName.split('_')[0] : "";
        atsNameInput.value = atsName;
        drawRules(rules);
    }
    reader.readAsText(loadedFile);
}

addRuleButton.onclick = cloneRuleElem;


function cloneRuleElem(element) {
    let ruleElement = document.getElementById('rule_element');
    let clonedElement = ruleElement.cloneNode(true);
    clonedElement.style.display = "block";

    let attributeElement =  document.getElementById('attribute_value_div');
    let attributeElementClone = attributeElement.cloneNode(true);
    attributeElementClone.style.display = "block";
    clonedElement.getElementsByClassName('rule_attributes')[0].appendChild(attributeElementClone);
    let attributeButton = clonedElement.getElementsByClassName('rule_attributes')[0].getElementsByClassName('attribute_name_add')[0];
    let attributeButtonClear = clonedElement.getElementsByClassName('rule_attributes')[0].getElementsByClassName('attribute_name_clear')[0];
    attributeButton.onclick = attributeButtonClickAction;
    attributeButtonClear.onclick = attributeClearButtonAction;

    let attributeValidationClone = document.getElementById('attribute_value_div').cloneNode(true);
    attributeValidationClone.style.display = "block";
    clonedElement.getElementsByClassName('validation_message_attributes')[0].appendChild(attributeValidationClone);
    let validationAttributeButton = clonedElement.getElementsByClassName('validation_message_attributes')[0].getElementsByClassName('attribute_name_add')[0];
    let validationAttributeButtonClear = clonedElement.getElementsByClassName('validation_message_attributes')[0].getElementsByClassName('attribute_name_clear')[0];
    validationAttributeButton.onclick = attributeButtonClickAction;
    validationAttributeButtonClear.onclick = attributeClearButtonAction;

    document.getElementById('rule_list').appendChild(clonedElement);
    let deleteRuleButton = clonedElement.getElementsByClassName('delete_rule')[0];
    deleteRuleButton.onclick = function(element) {
        let rules = document.getElementById('rule_list');
        rules.removeChild(element.srcElement.parentNode);
    }
    return clonedElement;
}

function attributeClearButtonAction(element) {
    let ruleList = element.srcElement.parentNode.parentNode.getElementsByClassName('attribute_rules_list')[0];
    while(ruleList.firstChild) {
         ruleList.firstChild.remove();
    }
}

function attributeButtonClickAction(element) {
        let attributeNameValue = document.getElementById('attribute_name_value');
        let attributeName = element.srcElement.parentNode.getElementsByClassName('attribute_rule_name')[0].value;
        let attributeNameValueClone = attributeNameValue.cloneNode(true);
        attributeNameValueClone.style.display = "block";
        attributeNameValueClone.getElementsByClassName('attribute_name_label')[0].innerHTML = attributeName;
        element.srcElement.parentNode.parentNode.getElementsByClassName('attribute_rules_list')[0].appendChild(attributeNameValueClone);
        attributeNameValueClone.getElementsByClassName('attribute_value_add')[0].onclick = function(element) {
            let ruleValueElement = element.srcElement.parentNode;
            let ruleValue = ruleValueElement.getElementsByClassName('attribute_rule_value')[0].value;
            let spanValueElement = document.createElement('span');
            spanValueElement.innerHTML = ruleValue + '<br>';
            ruleValueElement.getElementsByClassName('attribute_values')[0].appendChild(spanValueElement);
        }
        attributeNameValueClone.getElementsByClassName('attribute_value_clear')[0].onclick = function(elem) {
            let ruleValueElement = elem.srcElement.parentNode;
            let ruleValuesList = ruleValueElement.getElementsByClassName('attribute_values')[0];
            while(ruleValuesList.firstChild) {
                ruleValuesList.firstChild.remove();
            }
        }
    }

addRuleButton.click();

function drawRules(rules) {
    let ruleList = document.getElementById('rule_list');
    while(ruleList.firstChild) {
        ruleList.firstChild.remove();
    }
    for(i = 0 ; i < rules.length; i++) {
        let rule = rules[i];
        let clonedElement = cloneRuleElem(null);
        drawRule(clonedElement, rule);
    }
}

function drawRule(elem, rule) {
    elem.getElementsByClassName('rule_name')[0].value = rule.name;
    elem.getElementsByClassName('rule_tag_name')[0].value = rule.tagName;
    elem.getElementsByClassName('is_apply_button_rule')[0].checked = rule.isApplyButton;
    elem.getElementsByClassName('is_navigation_rule')[0].checked = rule.isNavigation;

    elem.getElementsByClassName('label_x_offset')[0].value = rule.labelXOffset;
    elem.getElementsByClassName('label_y_offset')[0].value = rule.labelYOffset;
    elem.getElementsByClassName('validation_x_offset')[0].value = rule.validationXOffset;
    elem.getElementsByClassName('validation_y_offset')[0].value = rule.validationYOffset;

    drawAttributes(rule.attributes, 'rule_attributes', elem);
    drawAttributes(rule.validationAttributes, 'validation_message_attributes', elem);
}

function drawAttributes(attributes, attributeClassName, elem) {
        for(j = 0; j < attributes.length; j++) {
            let ruleAttributes = attributes[j];
            let ruleAttributesElem = document.getElementById('attribute_name_value');
            let clonedElement = ruleAttributesElem.cloneNode(true);
            clonedElement.style.display = "block";
            elem.getElementsByClassName(attributeClassName)[0].getElementsByClassName('attribute_rules_list')[0].appendChild(clonedElement);
            clonedElement.getElementsByClassName('attribute_name_label')[0].textContent = ruleAttributes.name;
            clonedElement.getElementsByClassName('attribute_contain_checkbox')[0].checked = ruleAttributes.containAttributes;
            clonedElement.getElementsByClassName('attribute_name_and')[0].checked = ruleAttributes.andAttribute;
            for(z = 0; z < ruleAttributes.values.length; z++) {
                let spanValueElement = document.createElement('span');
                spanValueElement.innerHTML = ruleAttributes.values[z] + '<br>';
                clonedElement.getElementsByClassName('attribute_values')[0].appendChild(spanValueElement);
            }
            clonedElement.getElementsByClassName('attribute_value_add')[0].onclick = function(element) {
                let ruleValueElement = element.srcElement.parentNode;
                let ruleValue = ruleValueElement.getElementsByClassName('attribute_rule_value')[0].value;
                let spanValueElement = document.createElement('span');
                spanValueElement.innerHTML = ruleValue + '<br>';
                ruleValueElement.getElementsByClassName('attribute_values')[0].appendChild(spanValueElement);
            }
            clonedElement.getElementsByClassName('attribute_value_clear')[0].onclick = function(elem) {
                let ruleValueElement = elem.srcElement.parentNode;
                let ruleValuesList = ruleValueElement.getElementsByClassName('attribute_values')[0];
                while(ruleValuesList.firstChild) {
                     ruleValuesList.firstChild.remove();
                }
            }
        }
}

function calculateElements(scrapeElementsNextToInputs) {
             chrome.tabs.query({active: true, currentWindow: true}, function(tabs) {
               chrome.tabs.executeScript(
                   tabs[0].id,
                   {file: 'prepareParsedElements.js'},
                   function(result) {
                        scrapeElementsNextToInputs(result);
                   }
               )
             });
}

function scrapeElementsNextToInputs(inputElementsInnerModel) {
    let rules = getRules();
    let model = calculateModel(rules, inputElementsInnerModel);
    console.log(model);
}

function getRules() {
   let rules = document.getElementById('rule_list').getElementsByClassName('rule');
   let rulesModel = [];
   for(i = 0; i < rules.length; i++) {
      let rule = rules[i];
      let ruleName = rule.getElementsByClassName('rule_name')[0].value;
      let ruleSelect = rule.getElementsByClassName('rule_tag_name')[0];
      let ruleTagName = ruleSelect.options[ruleSelect.selectedIndex].text;

      let labelX = rule.getElementsByClassName('label_x_offset')[0].value;
      let labelY = rule.getElementsByClassName('label_y_offset')[0].value;
      let validationX = rule.getElementsByClassName('validation_x_offset')[0].value;
      let validationY = rule.getElementsByClassName('validation_y_offset')[0].value;

      let isNavigationRule = rule.getElementsByClassName('is_navigation_rule')[0].checked;
      let isApplyButtonRule = rule.getElementsByClassName('is_apply_button_rule')[0].checked;

      let attributesValues = getRuleAttributes(rule.getElementsByClassName('rule_attributes')[0]);
      let validationAttributesValues = getRuleAttributes(rule.getElementsByClassName('validation_message_attributes')[0]);

      let ruleModel = {
         name : ruleName,
         tagName : ruleTagName,
         labelXOffset : labelX,
         labelYOffset : labelY,
         validationXOffset : validationX,
         validationYOffset : validationY,
         attributes : attributesValues,
         validationAttributes : validationAttributesValues,
         isNavigation : isNavigationRule,
         isApplyButton : isApplyButtonRule
      }
      rulesModel.push(ruleModel);
   }
   return rulesModel;
}

function getRuleAttributes(rule) {
    let attributeRuleElements = rule.getElementsByClassName('attribute_name_value');
    let attributeRule = [];
    for(k = 0; k < attributeRuleElements.length; k++) {
        let attributeName = attributeRuleElements[k].getElementsByClassName('attribute_name_label')[0].textContent;
        let attributeValues = attributeRuleElements[k].getElementsByClassName('attribute_values')[0].getElementsByTagName('span');
        let containAttribute = attributeRuleElements[k].getElementsByClassName('attribute_contain_checkbox')[0].checked;
        let isAndAttribute = attributeRuleElements[k].getElementsByClassName('attribute_name_and')[0].checked;
        let attributeNameValues = [];
        for(z = 0; z < attributeValues.length; z++) {
            attributeNameValues.push(attributeValues[z].textContent);
        }
        let attributeRuleValue = {
            name: attributeName,
            values: attributeNameValues,
            containAttributes : containAttribute,
            andAttribute : isAndAttribute
        }
        attributeRule.push(attributeRuleValue);
    }
    return attributeRule;
}

function calculateModel(rules, elements) {
    let scrapedElements = [];
    let elems = elements[0];
    let isShowElements = showElementsCheckbox.checked;
    findElementsByRules(rules, elems, isShowElements);
}

function findElementsByRules(rulesValues, elementsValues, isShowElements) {
        chrome.tabs.query({active: true, currentWindow: true}, function(tabs) {
            chrome.storage.local.set({
                config: {
                    rules: rulesValues,
                    elements: elementsValues,
                    showScrapedElements: isShowElements
                    }
            }, function() {
           chrome.tabs.executeScript(
                 tabs[0].id,
                 {file: 'scraperScript.js'},
                 function(result) {
                            console.log(result);
                 });
            })
        });
}
