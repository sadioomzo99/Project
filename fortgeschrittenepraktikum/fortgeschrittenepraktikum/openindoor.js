let map = new openindoor({
    container: 'map',
    center: [8.773329,  50.810789],
    zoom: 17,
    //  layer: "0_EG.json",
     source: 'map.geojson',
    state: 'indoor_state',
    
    modal: false,
    popup: true,
    icon_tags: {
        icon_url: "icon-image",
        icon_name: "icon-name",
        filter: {
            layer_id: "indoor-stand_name-symbol",
            rules: ["!", [
                "has",
                "icon-name"
            ]]
        }
    }
});