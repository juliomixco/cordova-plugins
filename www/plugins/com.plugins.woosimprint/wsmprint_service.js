cordova.define("com.plugins.woosimprint.WSMPrintService", function(require, exports, module) {
		var WSMPrintService = function() {
		};

		WSMPrintService.prototype.printText = function(successCallback,
				errorCallback,text) {
			cordova.exec(successCallback, errorCallback, "WSMPrintService",
					"executePrinter", [ "printText", text]);
		};
		WSMPrintService.prototype.printBluetoothText = function(successCallback,
        				errorCallback,address,text) {
        			cordova.exec(successCallback, errorCallback, "WSMPrintService",
        					"executePrinter", [ "printBluetoothText",address, text]);
        		};
		WSMPrintService.prototype.selectPrinter = function(successCallback,
        				errorCallback) {
        			cordova.exec(successCallback, errorCallback, "WSMPrintService",
        					"printerInfo", [ "selectPrinter"]);
        		};

        /*
		BXLService.prototype.printBitmap = function(successCallback,
				errorCallback, station, fileName, width, alignment) {
			cordova.exec(successCallback, errorCallback, "BXLService",
					"executePrinter", [ "printBitmap", station, fileName,
							width, alignment ]);
		};*/



		module.exports = new WSMPrintService();
});
