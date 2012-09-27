function highlightLink(linkToActivate){

  element = parent.frames[0].document.getElementById(linkToActivate);

  var allLinks =new Array()
  allLinks[0]="counselingHomeLink";
  allLinks[1]="incompleteOrdersLink";
  allLinks[2]="shipmentSearchLink";
  allLinks[3]="dtodLink";
  allLinks[4]="gettingReadyLink";
  allLinks[5]="limitationsLink";
  allLinks[6]="glossaryLink";
  allLinks[7]="onlineBrochuresLink";
  allLinks[8]="faqsLink";

  for (x in allLinks)
  {
    elem = parent.frames[0].document.getElementById(allLinks[x]);
    if(elem != null)
    {
      elem.className = "";
    }
  }

  if(element != null) {
  element.className = "labelBold";
  }
}
