

;(function(){
    cordova.define("com.plugins.barcodeScanner", function(require, exports, module) {
        //-------------------------------------------------------------------
        var BarcodeScanner = function() {
        };

        //-------------------------------------------------------------------
        BarcodeScanner.Encode = {
                TEXT_TYPE:     "TEXT_TYPE",
                EMAIL_TYPE:    "EMAIL_TYPE",
                PHONE_TYPE:    "PHONE_TYPE",
                SMS_TYPE:      "SMS_TYPE",
                CONTACT_TYPE:  "CONTACT_TYPE",
                LOCATION_TYPE: "LOCATION_TYPE"
        };

        //-------------------------------------------------------------------
        BarcodeScanner.prototype.scan = function(success, fail, options) {
            function successWrapper(result) {
                result.cancelled = (result.cancelled == 1);
                success.call(null, result)
            }

            if (!fail) { fail = function() {}}

            if (typeof fail != "function")  {
                console.log("BarcodeScanner.scan failure: failure parameter not a function");
                return
            }

            if (typeof success != "function") {
                fail("success callback parameter must be a function");
                return
            }

            if ( null == options )
              options = [];

            return cordova.exec(successWrapper, fail, "BarcodeScanner", "scan", options)
        };

        //-------------------------------------------------------------------
        BarcodeScanner.prototype.encode = function(type, data, success, fail, options) {
            if (!fail) { fail = function() {}}

            if (typeof fail != "function")  {
                console.log("BarcodeScanner.scan failure: failure parameter not a function");
                return
            }

            if (typeof success != "function") {
                fail("success callback parameter must be a function");
                return
            }

            return cordova.exec(success, fail, "BarcodeScanner", "encode", [{type: type, data: data, options: options}])
        }

        //-------------------------------------------------------------------
        BarcodeScanner.prototype.create = function(x,y,width,height) {
                    cordova.exec(null, null, "BarcodeScanner","create", [{x: x, y: y, width: width, height: height} ]);
         };
        BarcodeScanner.prototype.show = function() {
            cordova.exec(null, null, "BarcodeScanner","show", []);


        };

        BarcodeScanner.prototype.hide = function() {
        cordova.exec(null, null, "BarcodeScanner","hide", []);

        };
        BarcodeScanner.prototype.clear = function() {
                cordova.exec(null, null, "BarcodeScanner","clear", []);

                };
        // remove Cordova.addConstructor since it was not supported on PhoneGap 2.0
        /*if (!window.plugins) window.plugins = {};

        if (!window.plugins.barcodeScanner) {
            window.plugins.barcodeScanner = new BarcodeScanner()
        }*/
        module.exports = new BarcodeScanner();
    });
})();