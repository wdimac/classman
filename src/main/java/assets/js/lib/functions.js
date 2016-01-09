//function to group items by region
window.__APP__ = {
  sortByRegion: function(data) {
    var cRegion = "";
    var sorted = {};
    //Create data structure for Accordian
    data.forEach(function(item){
      if (item.region !== cRegion) {
        cRegion = item.region;
        sorted[cRegion] = [];
      }
      sorted[cRegion].push( item);
    })
    return sorted;
  },

  decamel: function(string) {
    return string
      .replace(/([A-Z])/g, function($1){return ' ' + $1;})
      .replace(/^(.)/, function($1){return $1.toUpperCase();});
  },
}
