function UrlParams() {
    var name, value;
    var str = location.href; //取得整个地址栏
    var num = str.indexOf("?");
    str = str.substr(num + 1); //取得所有参数
    var arr = str.split("&"); //各个参数放到数组里
    for (var i = 0; i < arr.length; i++) {
        num = arr[i].indexOf("=");
        if (num > 0) {
            name = arr[i].substring(0, num);
            value = arr[i].substr(num + 1);
            this[name] = value;
        }
    }
}

var urlparams = new UrlParams();//所有url参数
var cityid = decodeURI(urlparams.cityids);
var kbdataid = decodeURI(urlparams.kbdataids);
var brand = decodeURIComponent(urlparams.brand);
var service = decodeURIComponent(urlparams.service);
var topic = decodeURIComponent(urlparams.topic);
var _abstract = decodeURIComponent(urlparams._abstract);
var serviceid = decodeURIComponent(urlparams.serviceids);
// var kbdataid=10591491;
// var cityids=284;