var styles = [
  {
    "elementType": "labels",
    "stylers": [
      { "visibility": "on" },
      { "saturation": -4 },
      { "weight": 0.1 }
    ]
  },{
    "elementType": "geometry",
    "stylers": [
      { "saturation": -100 }
    ]
  },{
		"featureType":"poi",
		"stylers": [ 
			{"visibility":"simplified"}
		]
  },{
    "featureType": "transit.station.rail",
    "stylers": [
      { "visibility": "off" }
    ]
  },{
    "featureType": "transit.line",
    "stylers": [
      { "visibility": "off" }
    ]
  },{
		"featureType": "poi.business",
		"stylers": [
			{ "visibility": "off"}
		]
	}
]

// Data for heatmap
var bumpHeatData = [];
var stopHeatData = [];
function bumpData(){
	var zurl = "data/bumpdata.json";
	var jsonData = null;
	$.ajax({
		url: zurl,
		type: "get",
		dataType: "json",
		async: false,
		success: function(data){
			jsonData = data;
		}
	});
	var bump = jsonData.bumpData;
	var size = bump.length;
	for(i=0; i<size; i++){
	  var b = bump[i];
		bumpHeatData[i] = {location: new google.maps.LatLng(b.lat, b.lon), weight: b.weight};
	}
	return jsonData;
}
function stopData(){
	var zurl = "data/stopdata.json";
	var jsonData = null;
	$.ajax({
		url: zurl,
		type: "get",
		dataType: "json",
		async: false,
		success: function(data){
			jsonData = data;
		}
	});
	var stop = jsonData.stopData;
	var size = stop.length;
	for(i=0; i<size; i++){
	  var s = stop[i];
		stopHeatData[i] = {location: new google.maps.LatLng(s.lat, s.lon), weight: s.weight};
	}
	return jsonData;
}

var London = new google.maps.LatLng(51.5171, -0.1062);
var map, heatmap, heatmap2;

// Initialisation of map and fetching of data
function init(){
	google.maps.visualRefresh = true;
	var styledMap = new google.maps.StyledMapType(styles, {name: "Stylised Map"});	
	var mapOptions = {
		center: London,
		zoom: 12,
		scaleControl: true,
		mapTypeControlOptions: {
			mapTypeIds: [google.maps.MapTypeId.ROADMAP, 'map_style', google.maps.MapTypeId.HYBRID]
		},
		zoomControlOptions: {
		style: google.maps.ZoomControlStyle.LARGE
		}
	};

	map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
	map.mapTypes.set('map_style', styledMap);
	map.setMapTypeId('map_style');

	bumpData();
	heatmap = new google.maps.visualization.HeatmapLayer({
		data: bumpHeatData
	});
	heatmap.setMap(map);
	stopData();
	heatmap2 = new google.maps.visualization.HeatmapLayer({
		data: stopHeatData
	});
	$('#map-canvas').css("height", $(window).height());
}


function toggleBumps(){
	heatmap.setMap(heatmap.getMap() ? null : map);
}
function toggleStops(){
	heatmap2.setMap(heatmap2.getMap() ? null : map);
}
function toggleDriving(){

}

function showAbout(){
	$('#about').toggle();
}

