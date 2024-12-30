class MapInterop {
    add(key, value) {
        this[key] = value;
    }
}

const DeviceService = {
    getAll() {
        return new Promise((resolve, reject) => {
            // Homey.api("GET", "/devices", (error, result) => {
            //     if (error) {
            //         return reject(error);
            //     }
            //     resolve(result);
            // })
            resolve([
                {
                    id: "b058b2a5-6610-4744-a993-099741fb04cf",
                    icon: "/api/icon/3dd84226-4210-403c-adeb-0f3a2f58048a",
                    name: "Shelly Wave 2PM",
                    room: "living room",
                },
                {
                    id: "b058b2a5-6610-4744-a993-099741fb04df",
                    icon: "/api/icon/c5c0f993-0fbc-47ff-9979-6fa80b879c83",
                    name: "Shelly Wave 2PM",
                    room: "living room",
                },
            ])
        });
    },
}

const MatterService = {
    get() {
        return new Promise((resolve, reject) => {
            // Homey.api("GET", "/matter/bridge/info", (error, result) => {
            //     if (error) {
            //         return reject(error);
            //     }
            //     resolve(result);
            // })
            resolve(
                {
                    qrCode: "He;;lo qr from js",
                    passcode: "123456789"
                }
            )
        });
    },
}

const HomeyWasmBridge = {
    is_light_mode_on: window.getComputedStyle(document.documentElement).getPropertyValue("--theme-color-mono-000").toLowerCase() === "#ffffff",
    device_service: DeviceService,
    matter_service: MatterService,
    // url: "https://10-0-0-40.homey.homeylocal.com",
    url: window.location.origin,
    map() {
        return new MapInterop()
    },

    translate(key, tokens) {
        // return key
        return Homey.__(`settings.${key}`, tokens)
    },
}