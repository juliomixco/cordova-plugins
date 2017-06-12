cordova.define('cordova/plugin_list', function (require, exports, module) {
  module.exports = [
    {
      "file"     : "plugins/com.plugins.bixolonprint/bxl_service.js",
      "id"       : "com.plugins.bixolonprint.BXLService",
      "clobbers" : [
        "bxl_service"
      ]
    },
    {
      "file"     : "plugins/com.plugins.woosimprint/wsmprint_service.js",
      "id"       : "com.plugins.woosimprint.WSMPrintService",
      "clobbers" : [
        "wsmprint_service"
      ]
    },
    {
      "id"       : "cordova-plugin-printer.Printer",
      "file"     : "plugins/cordova-plugin-printer.www/printer.js",
      "pluginId" : "cordova-plugin-printer",
      "clobbers" : [
        "plugin.printer",
        "cordova.plugins.printer"
      ]
    },
    {
      "file"     : "plugins/com.plugins.barcodeScanner/BarCodeScanner.js",
      "id"       : "com.plugins.barcodeScanner",
      "clobbers" : [
        "window.plugins.barCodeScan", "window.plugins.BarCodeScan"
      ]
    }
  ];
  module.exports.metadata = 
    // TOP OF METADATA
    {
      "cordova-plugin-printer" : "0.7.2"
    }
  // BOTTOM OF METADATA
});