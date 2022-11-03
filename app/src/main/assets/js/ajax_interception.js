let verify = false;
XMLHttpRequest.prototype.reallyOpen = XMLHttpRequest.prototype.open;
XMLHttpRequest.prototype.open = function(method, url, async, user, password) {
    verify = (url.toString() === "https://passport.bilibili.com/x/safecenter/login/tel/verify");
    this.reallyOpen(method, url, async, user, password);
};
XMLHttpRequest.prototype.reallySend = XMLHttpRequest.prototype.send;
XMLHttpRequest.prototype.send = function(body) {
    if (body != null) {
        Injection.setVerifyBody(body.toString());
    }
    this.reallySend(body);
};