/*
MAPSTRACTION   vtimemap   http://www.mapstraction.com

Copyright (c) 2011 Tom Carden, Steve Coast, Mikel Maron, Andrew Turner, Henri Bergius, Rob Moran, Derek Fowler, Gary Gale
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the Mapstraction nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
mxn.register("microsoft",{Mapstraction:{init:function(a,b){var c=this;if(!VEMap){throw b+" map script not imported"}this.maps[b]=new VEMap(a.id);this.maps[b].AttachEvent("onclick",function(g){c.clickHandler();var i=c.maps[c.api];var e=i.GetShapeByID(g.elementID);if(e){if(e.mapstraction_marker){e.mapstraction_marker.click.fire()}if(e.mapstraction_polyline){e.mapstraction_polyline.click.fire()}}else{var d=g.mapX;var j=g.mapY;var f=new VEPixel(d,j);var h=i.PixelToLatLong(f);c.click.fire({location:new mxn.LatLonPoint(h.Latitude,h.Longitude)})}});this.maps[b].AttachEvent("onendzoom",function(d){c.moveendHandler(c);c.changeZoom.fire()});this.maps[b].AttachEvent("onendpan",function(d){c.moveendHandler(c);c.endPan.fire()});this.maps[b].AttachEvent("onchangeview",function(d){c.endPan.fire()});this.maps[b].LoadMap();document.getElementById("MSVE_obliqueNotification").style.visibility="hidden";this.loaded[b]=true;c.load.fire()},applyOptions:function(){var a=this.maps[this.api];if(this.options.enableScrollWheelZoom){a.enableContinuousZoom();a.enableScrollWheelZoom()}},resizeTo:function(b,a){this.maps[this.api].Resize(b,a)},addControls:function(a){var b=this.maps[this.api];if(a.pan){b.SetDashboardSize(VEDashboardSize.Normal)}else{b.SetDashboardSize(VEDashboardSize.Tiny)}if(a.zoom=="large"){b.SetDashboardSize(VEDashboardSize.Small)}else{if(a.zoom=="small"){b.SetDashboardSize(VEDashboardSize.Tiny)}else{b.HideDashboard();b.HideScalebar()}}},addSmallControls:function(){var a=this.maps[this.api];a.SetDashboardSize(VEDashboardSize.Tiny)},addLargeControls:function(){var a=this.maps[this.api];a.SetDashboardSize(VEDashboardSize.Normal);this.addControlsArgs.pan=true;this.addControlsArgs.zoom="large"},addMapTypeControls:function(){var a=this.maps[this.api];a.addTypeControl()},dragging:function(a){var b=this.maps[this.api];if(a){b.enableDragMap()}else{b.disableDragMap()}},setCenterAndZoom:function(a,b){var d=this.maps[this.api];var c=a.toProprietary(this.api);var e=b;d.SetCenterAndZoom(new VELatLong(a.lat,a.lon),e)},addMarker:function(b,a){var d=this.maps[this.api];b.pinID="mspin-"+new Date().getTime()+"-"+(Math.floor(Math.random()*Math.pow(2,16)));var c=b.toProprietary(this.api);d.AddShape(c);return c},removeMarker:function(b){var c=this.maps[this.api];var d=b.proprietary_marker.GetID();var a=c.GetShapeByID(d);c.DeleteShape(a)},declutterMarkers:function(a){var b=this.maps[this.api]},addPolyline:function(b,a){var d=this.maps[this.api];var c=b.toProprietary(this.api);c.HideIcon();d.AddShape(c);return c},removePolyline:function(b){var c=this.maps[this.api];var d=b.proprietary_polyline.GetID();var a=c.GetShapeByID(d);c.DeleteShape(a)},getCenter:function(){var b=this.maps[this.api];var c=b.GetCenter();var a=new mxn.LatLonPoint(c.Latitude,c.Longitude);return a},setCenter:function(a,b){var d=this.maps[this.api];var c=a.toProprietary(this.api);d.SetCenter(new VELatLong(a.lat,a.lon))},setZoom:function(a){var b=this.maps[this.api];b.SetZoomLevel(a)},getZoom:function(){var b=this.maps[this.api];var a=b.GetZoomLevel();return a},getZoomLevelForBoundingBox:function(e){var d=this.maps[this.api];var c=e.getNorthEast();var a=e.getSouthWest();var b;return b},setMapType:function(a){var b=this.maps[this.api];switch(a){case mxn.Mapstraction.ROAD:b.SetMapStyle(VEMapStyle.Road);break;case mxn.Mapstraction.SATELLITE:b.SetMapStyle(VEMapStyle.Aerial);break;case mxn.Mapstraction.HYBRID:b.SetMapStyle(VEMapStyle.Hybrid);break;default:b.SetMapStyle(VEMapStyle.Road)}},getMapType:function(){var a=this.maps[this.api];var b=a.GetMapStyle();switch(b){case VEMapStyle.Aerial:return mxn.Mapstraction.SATELLITE;case VEMapStyle.Road:return mxn.Mapstraction.ROAD;case VEMapStyle.Hybrid:return mxn.Mapstraction.HYBRID;default:return null}},getBounds:function(){var b=this.maps[this.api];view=b.GetMapView();var a=view.TopLeftLatLong;var c=view.BottomRightLatLong;return new mxn.BoundingBox(c.Latitude,a.Longitude,a.Latitude,c.Longitude)},setBounds:function(b){var d=this.maps[this.api];var a=b.getSouthWest();var c=b.getNorthEast();var e=new VELatLongRectangle(new VELatLong(c.lat,c.lon),new VELatLong(a.lat,a.lon));d.SetMapView(e)},addImageOverlay:function(c,a,e,i,f,g,d,h){var b=this.maps[this.api]},setImagePosition:function(e,b){var d=this.maps[this.api];var c;var a},addOverlay:function(a,c){var e=this.maps[this.api];var b=new VEShapeLayer();var d=new VEShapeSourceSpecification(VEDataType.GeoRSS,a,b);e.ImportShapeLayerData(d)},addTileLayer:function(e,a,b,c,d){throw"Not implemented"},toggleTileLayer:function(a){throw"Not implemented"},getPixelRatio:function(){throw"Not implemented"},mousePosition:function(a){var c=document.getElementById(a);if(c!==null){var b=this.maps[this.api];b.AttachEvent("onmousemove",function(e){var d=b.PixelToLatLong(new VEPixel(e.mapX,e.mapY));var f=d.Latitude.toFixed(4)+" / "+d.Longitude.toFixed(4);c.innerHTML=f});c.innerHTML="0.0000 / 0.0000"}},openBubble:function(a,c){var d=this.maps[this.api],b=new VEShape(VEShapeType.Pushpin,a.toProprietary(this.api));b.SetDescription(c);d.AddShape(b);b.Hide();d.HideInfoBox();d.ShowInfoBox(b);this.bubble_shape=b},closeBubble:function(){var a=this.maps[this.api];a.HideInfoBox();a.DeleteShape(this.bubble_shape)}},LatLonPoint:{toProprietary:function(){return new VELatLong(this.lat,this.lon)},fromProprietary:function(a){this.lat=a.Latitude;this.lon=a.Longitude}},Marker:{toProprietary:function(){var a=new VEShape(VEShapeType.Pushpin,this.location.toProprietary("microsoft"));a.SetTitle(this.labelText);if(this.iconUrl){var b=new VECustomIconSpecification();b.Image=this.iconUrl;if(this.iconAnchor){b.ImageOffset=new VEPixel(-this.iconAnchor[0],-this.iconAnchor[1])}else{if(this.iconSize){b.ImageOffset=new VEPixel(-this.iconSize[0]/2,-this.iconSize[1]/2)}}a.SetCustomIcon(b)}if(this.draggable){a.Draggable=true}return a},openBubble:function(){if(!this.map){throw"Marker must be added to map in order to display infobox"}this.proprietary_marker.SetDescription(this.infoBubble);this.map.HideInfoBox();this.map.ShowInfoBox(this.proprietary_marker)},closeBubble:function(){if(!this.map){throw"Marker must be added to map in order to display infobox"}this.map.HideInfoBox()},hide:function(){this.proprietary_marker.Hide();this.hidden=true},show:function(){this.proprietary_marker.Show();this.hidden=false},isHidden:function(){return this.hidden||false},update:function(){throw"Not implemented"}},Polyline:{toProprietary:function(){var e=[];for(var d=0,h=this.points.length;d<h;d++){e.push(this.points[d].toProprietary("microsoft"))}var g=this.closed?VEShapeType.Polygon:VEShapeType.Polyline,c=new VEShape(g,e);var a,b,f;if(this.color){a=new mxn.util.Color(this.color);b=(this.opacity==="undefined"||this.opacity===null)?1:this.opacity;f=new VEColor(a.red,a.green,a.blue,b);c.SetLineColor(f)}if(this.fillColor){a=new mxn.util.Color(this.fillColor);b=(this.fillOpacity==="undefined"||this.fillOpacity===null)?0.3:this.fillOpacity;f=new VEColor(a.red,a.green,a.blue,b);c.SetFillColor(f)}if(this.width){c.SetLineWidth(this.width)}return c},show:function(){this.proprietary_polyline.Show()},hide:function(){this.proprietary_polyline.Hide()},isHidden:function(){return this.proprietary_polyline.Visibility}}});