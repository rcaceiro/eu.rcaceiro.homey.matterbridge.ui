function onHomeyReady(Homey) {
    load(() => {
        Homey.ready();
    })
}

function load(onload) {
    const script = window.document.createElement('script');
    script.onload = function () {
        if (!onload) {
            return;
        }
        onload();
    };
    script.src = "application.js";
    script.type = "application/javascript";
    window.document.head.appendChild(script);
    return script;
}

function startup() {
    if (!window.document.URL.includes("http://localhost")) {
        return;
    }
    load()
}

startup();