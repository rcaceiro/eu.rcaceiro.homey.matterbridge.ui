// Global utility function to support callback api
function utilCallbackAfterPromise(target, args, callback) {
    // the target should already be bound, applying with null
    // should not affect it anymore
    target
        .apply(null, args)
        .then(function (result) {
            callback(null, result);
        })
        .catch(function (error) {
            callback(error);
        });
}

(function () {
    var EVENT_MESSAGE = 'CF2_MESSAGE';
    var EVENT_READY = 'CF2_READY';

    var webviewReady = false;
    var webviewReadyFns = [];
    var webview = getParameterByName('webview') === '1';
    if (webview) {
        // Needed for proper active state on buttons on mobile
        document.addEventListener('touchstart', function () {}, false);
        document.addEventListener('message', function (e) {
            if (webviewReady === false && e.data === EVENT_READY) {
                webviewReady = true;
                webviewReadyFns.forEach(function (webviewReadyFn) {
                    webviewReadyFn();
                });
            }
        });
    }

    function CrossFrame(el, opts) {
        this._opts = opts || {};

        if (typeof this._opts.delay !== 'number') {
            this._opts.delay = 50;
        }

        this.onMessage = this.onMessage.bind(this);
        this._debug = this._debug.bind(this);
        this.on = this.on.bind(this);
        this.emit = this.emit.bind(this);
        this.send = this.send.bind(this);
        this.registerRealtimeListener = this.registerRealtimeListener.bind(this);
        this.destroy = this.destroy.bind(this);
        this.ready = this.ready.bind(this);
        this._ready = this._ready.bind(this);
        this._post = this._post.bind(this);
        this._posting = false;
        this._postNext = this._postNext.bind(this);
        this._postQueue = [];

        this._readyPromise = new Promise(
            function (resolve) {
                this._readyResolve = resolve;
            }.bind(this),
        );

        this._callbackId = 0;
        this._eventlisteners = {};
        this._realtimeListeners = {};
        this._unresolved = {};

        this._el = el;

        if (webview) {
            if (document && document.addEventListener) {
                document.addEventListener('message', this.onMessage);
            }
        } else {
            if (window && window.addEventListener) {
                window.addEventListener('message', this.onMessage);
            }
        }

        if (webview) {
            onWebviewReady(this._ready);
        } else {
            this._ready();
        }

        return this;
    }

    CrossFrame.prototype._debug = function () {
        if (window.DEBUG) console.log.bind(null, '[CrossFrame]').apply(null, arguments);
    };

    CrossFrame.prototype.onMessage = function (e) {
        if (!e.data || typeof e.data !== 'string') return;
        if (e.data.indexOf(EVENT_MESSAGE) !== 0) return;

        this._debug('onMessage', e);

        var str = e.data;
        str = str.substr(EVENT_MESSAGE.length);
        str = hexDecode(str);
        var obj = jsonToObj(str);
        if (obj.type === 'tx') {
            var callback = function (err, result) {
                var message = {
                    type: 'cb',
                    args: [err, result],
                    callbackId: obj.callbackId,
                };
                this.postMessage(message);
            }.bind(this);

            var listener = this._eventlisteners[obj.event];
            Promise.resolve(obj.data)
                .then(listener)
                .then((data) => callback(null, data))
                .catch((error) => console.error(error) || callback(error));
        } else if (obj.type === 'cb') {
            if (this._realtimeListeners[obj.callbackId]) {
                var listener = this._realtimeListeners[obj.callbackId];
                listener.apply(null, obj.args);
                return;
            }

            var unresolved = this._unresolved[obj.callbackId];
            if (!unresolved) return;

            if (obj.args[0] instanceof Error) {
                unresolved.reject(obj.args[0]);
            } else {
                unresolved.resolve(obj.args[1]);
            }

            delete this._unresolved[obj.callbackId];
        }
    };

    CrossFrame.prototype.on = function (event, callback) {
        if (this._eventlisteners[event]) {
            // TODO: is this what we want to happen?
            throw new Error('event handler already registered for this event');
        }

        this._eventlisteners[event] = callback;
    };

    CrossFrame.prototype.emit = function (event, data, callback) {
        if (typeof callback === 'function') {
            return utilCallbackAfterPromise(this.emit, [event, data], callback);
        }

        var callbackId = ++this._callbackId;

        var message = {
            type: 'tx',
            event: event,
            data: data,
            callbackId: callbackId,
        };

        this.postMessage(message);

        return new Promise(
            function (resolve, reject) {
                // TODO: timeout after a delay?
                this._unresolved[callbackId] = {
                    resolve: resolve,
                    reject: reject,
                };
            }.bind(this),
        );
    };

    // special case of emit where we don't expect a reply
    CrossFrame.prototype.send = function (event, data) {
        this.postMessage({
            type: 'tx',
            event: event,
            data: data,
            callbackId: null,
        });
    };

    // special case of emit where we expect many replies
    CrossFrame.prototype.registerRealtimeListener = function (name, listener) {
        var callbackId = ++this._callbackId;
        this.postMessage({
            type: 'tx',
            event: 'registerRealtimeListener',
            data: name,
            callbackId: callbackId,
        });

        this._realtimeListeners[callbackId] = listener;
    };

    CrossFrame.prototype.destroy = function () {
        // TODO: reject all this._unresolved?

        if (webview) {
            if (document && document.removeEventListener) {
                document.removeEventListener('message', this.onMessage);
            }
        } else {
            if (window && window.removeEventListener) {
                window.removeEventListener('message', this.onMessage);
            }
        }
    };

    CrossFrame.prototype.ready = function (callback) {
        if (typeof callback === 'function') {
            this._readyPromise.then(callback);
        }
        return this._readyPromise;
    };

    CrossFrame.prototype._ready = function () {
        this._readyResolve();
    };

    CrossFrame.prototype.postReady = function (message) {
        this._post(EVENT_READY);
    };

    CrossFrame.prototype.postMessage = function (message) {
        this._post(EVENT_MESSAGE + hexEncode(objToJson(message)));
    };

    CrossFrame.prototype._post = function (message) {
        this._debug('_post()', message);

        this._postQueue.push(message);
        this._postNext();
    };

    CrossFrame.prototype._postNext = function () {
        if (this._posting) return;

        var message = this._postQueue.shift();
        if (typeof message === 'undefined') return;

        var target;
        if (this._el) {
            target = this._el.contentWindow ? this._el.contentWindow : this._el;
        } else if (window && window.parent) {
            target = window.parent;
        }

        if (target) {
            this._posting = true;
            target.postMessage(message, '*');

            setTimeout(
                function () {
                    this._posting = false;
                    this._postNext();
                }.bind(this),
                this._opts.delay,
            );
        }
    };

    if (typeof module !== 'undefined' && typeof module.exports !== 'undefined') {
        module.exports = CrossFrame;
    } else {
        window.CrossFrame = CrossFrame;
    }

    function onWebviewReady(callback) {
        if (webviewReady) {
            callback();
        } else {
            webviewReadyFns.push(callback);
        }
    }

    function getParameterByName(name, url) {
        if (window && window.location && window.location.href) {
            if (!url) url = window.location.href;
            name = name.replace(/[\[\]]/g, '\\$&');
            var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'),
                results = regex.exec(url);
            if (!results) return null;
            if (!results[2]) return '';
            return decodeURIComponent(results[2].replace(/\+/g, ' '));
        }

        return '';
    }

    function objToJson(obj) {
        return JSON.stringify(obj, function replacer(key, value) {
            if (value instanceof Error) {
                return {
                    type: 'Error',
                    data: value.message,
                };
            }

            return value;
        });
    }

    function jsonToObj(json) {
        return JSON.parse(json, function reviver(key, value) {
            if (value && value.type) {
                if (value.type === 'Error') {
                    return new Error(value.data);
                }
            }
            return value;
        });
    }

    function hexEncode(input) {
        var hex, i;

        var result = '';
        for (i = 0; i < input.length; i++) {
            hex = input.charCodeAt(i).toString(16);
            result += ('000' + hex).slice(-4);
        }

        return result;
    }

    function hexDecode(input) {
        var j;
        var hexes = input.match(/.{1,4}/g) || [];
        var back = '';
        for (j = 0; j < hexes.length; j++) {
            back += String.fromCharCode(parseInt(hexes[j], 16));
        }

        return back;
    }
})();

/*
 * Homey Internationalization
 */
(function () {
    function t(input, tokens) {
        var result = (function () {
            if (typeof input === 'object') {
                var language = window.i18nLanguage || 'en';
                if (typeof input[language] === 'string') return input[language];
                if (typeof input.en === 'string') return input.en;
            } else if (typeof input === 'string') {
                var strings = window.i18nStrings || {};
                var text = getFromObjectByPath(strings, input);
                return replaceTokens(text, tokens);
            }
        })();

        if (typeof result === 'undefined') return input;

        return result;
    }

    // Attach to jsrender & jquery
    if (typeof $ !== 'undefined') {
        if (typeof $.fn.render === 'function') {
            $.views.helpers({
                i18n(input, tokens) {
                    return t(input, tokens);
                },
            });
        }
    }

    function replaceTokens(string, tokens) {
        tokens = tokens || {};
        for (var key in tokens) {
            var re = new RegExp(`__${key}__`, 'g');
            string = string.replace(re, tokens[key]);
        }
        return string;
    }

    function getFromObjectByPath(o, s) {
        try {
            s = s.replace(/\[(\w+)\]/g, '.$1'); // convert indexes to properties
            s = s.replace(/^\./, ''); // strip a leading dot
            var a = s.split('.');
            for (var i = 0, n = a.length; i < n; ++i) {
                var k = a[i];
                if (k in o) {
                    o = o[k];
                } else {
                    return;
                }
            }
            return o;
        } catch (err) {
            return s;
        }
    }

    function i18nReady(callback) {
        console.warning('i18nReady() is deprecated');
        callback();
    }

    /*
        This function translates an element with attribute data-i18n
    */
    function translateElement(element) {
        if (!element) return console.error('Invalid element:', element);

        (function () {
            var elements = element.querySelectorAll('[data-i18n]');
            [].forEach.call(elements, function (element) {
                var attr = element.dataset.i18n;
                var opts = element.dataset.i18nOpts;
                if (typeof opts === 'string') {
                    opts = JSON.parse(opts);
                } else {
                    opts = {};
                }

                var tr = t(attr, opts);
                if (tr !== attr) {
                    element.innerHTML = tr;
                    delete element.dataset.i18n;
                    delete element.dataset.i18nOpts;
                }
            });
        })();

        (function () {
            var elements = element.querySelectorAll('[data-i18n-placeholder]');
            [].forEach.call(elements, function (element) {
                var attr = element.dataset.i18nPlaceholder;
                var tr = t(attr);
                if (tr !== attr) {
                    element.placeholder = tr;
                    delete element.dataset.i18nPlaceholder;
                }
            });
        })();

        (function () {
            var elements = element.querySelectorAll('[data-i18n-title]');
            [].forEach.call(elements, function (element) {
                var attr = element.dataset.i18nTitle;
                var tr = t(attr);
                if (tr !== attr) {
                    element.title = tr;
                    delete element.dataset.i18nTitle;
                }
            });
        })();

        (function () {
            var elements = element.querySelectorAll('[data-i18n-value]');
            [].forEach.call(elements, function (element) {
                var attr = element.dataset.i18nValue;
                var tr = t(attr);
                if (tr !== attr) {
                    element.value = tr;
                    delete element.dataset.i18nValue;
                }
            });
        })();
    }

    window.__ = t;
    window.__h = t;
    window.translateElement = translateElement;
})();

/*
 * Homey
 */
function Homey() {
    this._ready = false;
    this._origin = undefined;
    this._cf = undefined;

    this._init();
}

Homey.prototype._init = function () {
    this._cf = new CrossFrame();
    this._cf.ready().then(
        function () {
            if (document.readyState === 'complete') {
                this._onWindowLoad();
            } else {
                window.addEventListener('load', this._onWindowLoad.bind(this));
            }
        }.bind(this),
    );
};

Homey.prototype._getOrigin = function () {
    var scriptEl = document.querySelector('script[data-origin]');
    if (scriptEl) {
        this._origin = scriptEl.dataset.origin;
    }

    if (typeof this._origin !== 'string') {
        return Promise.resolve();
    }

    return Homey.loadScript('/js/homey.' + this._origin + '.js');
};

Homey.prototype._onWindowLoad = function () {
    this._getOrigin()
        .then(
            function () {
                if (typeof this._onWindowLoadExtended === 'function') {
                    return this._onWindowLoadExtended();
                }
            }.bind(this),
        )
        .then(
            function () {
                window.Homey = this;
                window.onHomeyReady && window.onHomeyReady(this);
            }.bind(this),
        )
        .catch(
            function (error) {
                this.alert(error);
            }.bind(this),
        );
};

Homey.prototype._loadI18n = function () {
    return this.getLanguage().then(function (language) {
        window.i18nLanguage = language;
        Homey.language = language;
        Homey.prototype.__ = window.__;
        Homey.prototype.translateElement = window.translateElement;

        // Translate the DOM
        if (document.readyState === 'complete') {
            window.translateElement(document.body);
        } else {
            window.addEventListener('load', function () {
                window.translateElement(document.body);
            });
        }
    });
};

Homey.prototype.ready = function () {
    if (this._ready !== true) {
        this._ready = true;
        this._cf.send('ready');
    }
};
