(function() {
    try {
        const id = "bilidl_phone_validate";
        if (document.head && !document.getElementById(id)) {
            const injectScript = document.createElement("script");
            injectScript.id = id;
            injectScript.textContent = "%%SRC_CODE%%";
            document.head.appendChild(injectScript);
        }
    } catch (e) { }
})();