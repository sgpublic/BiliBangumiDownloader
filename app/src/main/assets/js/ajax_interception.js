let reallyOpen = XMLHttpRequest.prototype.open;
XMLHttpRequest.prototype.open = function(method, url, async, user, password) {
    Injection.logOnOpen(url.toString());
    reallyOpen(method, url, async, user, password);
};
let reallySend = XMLHttpRequest.prototype.send;
XMLHttpRequest.prototype.send = function(body) {
    if (body != null) {
        Injection.setVerifyBody(body.toString());
    }
    reallySend(body);
};
