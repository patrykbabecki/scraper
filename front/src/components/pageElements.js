import React from 'react'
import { Stage, Layer, Rect, Text } from 'react-konva';
import Konva from 'konva';

const PageElements = ({pageElements}) => {
    return (
    <div>
        {pageElements.map((element) => (
            <PageElement elem={element}/>
        ))}
    </div>
)
};

const PageElement = ({elem}) => {
    var borderStyle = '1px solid black';
    if (elem.tagName === 'INPUT' || elem.tagName === 'TEXTAREA') {
        borderStyle = '5px solid red';
    } else if (elem.tagName === "BUTTON") {
        borderStyle = '5px solid blue';
    } else if(elem.tagName === 'LABEL') {
        borderStyle = "3px solid green"
    }
    var elemStyle = {
        color: 'black',
        position: 'absolute',
        top: elem.position.y,
        left: elem.position.x,
        border: borderStyle,
        width: elem.position.width,
        height: elem.position.height
    }
    var text = "";
    if(elem.tagName === "LABEL" || elem.tagName === "H3" || elem.tagName === "BUTTON") {
        text = elem.innerText;
    } else {
        text = elem.text;
    }
    return (
        <div style={elemStyle}>
                {text}
        </div>
    )
}

export default PageElements