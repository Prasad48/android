var UIQ = (function () {
  'use strict';

  console.log('UIQ: Loading');

  function captureLayout() {
    console.log('****** captureLayout **********');
    var hierarchy = {};
    console.log(document.readyState);
    buildLayout(document.body, hierarchy, 0);
    var layoutStr = JSON.stringify(hierarchy);
    return layoutStr;
  }

  function buildLayout(node, output, index) {
    var name = node.nodeName.toLowerCase();

    output.index = index;


    if (name) { output.cls = name; }

    var attrNames = node.getAttributeNames();

    for (var i = 0; i < attrNames.length; i++) {
        var key = attrNames[i];
        var value = node.getAttribute(key);

        if (key === 'style') { continue }
        if (key === 'id') { key = 'resourceId'; }
        if (key === 'class') { key = 'className'; }

        output[key] = value;
    }

    output.opacity = +getComputedStyle(node).getPropertyValue('opacity');
    output.bgColor = getComputedStyle(node).getPropertyValue('background-color');

    var text = getText(node);
    if (text) { output.text = node.text; }
    if (node.onClick !== null) { output.clickable = true; }
    if (!output.children) { output.children = []; }

    var childs = node.children;
    var childLen = childs.length;

    for (var i = 0, idx = 0; i < childLen; i++) {
        var bounds = calcBounds(childs[i]);
        if (bounds === null) { continue }

        var obj = (output.children[idx] = { bounds: bounds });
        buildLayout(childs[i], obj, idx);
        idx++;
    }

    console.log('Build layout finished');
  }

  function getText(node) {
      var text = '';
      for (var i = 0; i < node.childNodes.length; i++) {
        var child = node.childNodes[i];
        if (child.nodeType !== Node.TEXT_NODE) { continue }
        text += child.textContent.trim();
      }

      return text;
  }

   function calcBounds(node) {
    if (!node.offsetWidth && !node.offsetHeight) { return null }

    var rect = node.getBoundingClientRect();

    var isVisible = isElemVisibleInViewport(node);

    if (!isVisible) { return null; }

    return [
      Math.round(rect.left),
      Math.round(rect.top),
      Math.round(rect.right),
      Math.round(rect.bottom) ]
   }

   function isElemVisibleInViewport(node) {
       var rect = node.getBoundingClientRect();

       var isVisible =
         rect.top >= 0 &&
         rect.left >= 0 &&
         rect.left < (window.innerWidth || document.documentElement.clientWidth) &&
         rect.top < (window.innerHeight || document.documentElement.clientHeight);

       if (!isVisible) { return false; }

       return true;
   }

  var main = {
    captureLayout: captureLayout,
  };

  return main;

}());