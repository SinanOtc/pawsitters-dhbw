(function () {
    var NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    var REQUEST_DELAY_MS = 1100;

    function parseArea(address) {
        if (!address || typeof address !== "string") {
            return null;
        }
        var match = address.match(/(\d{5})\s+([^,]+)/);
        if (!match) {
            return null;
        }
        var plz = match[1];
        var city = match[2].trim();
        return {
            query: plz + " " + city,
            label: plz + " " + city
        };
    }

    function geocode(query) {
        var url = NOMINATIM_URL +
            "?q=" + encodeURIComponent(query) +
            "&format=json&countrycodes=de&limit=1" +
            "&accept-language=de";
        return fetch(url)
            .then(function (response) {
                return response.ok ? response.json() : [];
            })
            .then(function (results) {
                if (!results || results.length === 0) {
                    return null;
                }
                return [parseFloat(results[0].lat), parseFloat(results[0].lon)];
            })
            .catch(function () {
                return null;
            });
    }

    function delay(ms) {
        return new Promise(function (resolve) {
            setTimeout(resolve, ms);
        });
    }

    function createPopup(markerData) {
        var wrapper = document.createElement("div");
        wrapper.className = "map-popup";

        var title = document.createElement("strong");
        title.textContent = markerData.name;
        wrapper.appendChild(title);

        var area = document.createElement("span");
        area.textContent = markerData.areaLabel;
        wrapper.appendChild(area);

        var price = document.createElement("span");
        price.textContent = markerData.price + " € / Woche";
        wrapper.appendChild(price);

        var link = document.createElement("a");
        link.href = markerData.profileUrl;
        link.textContent = "Zum Angebot";
        wrapper.appendChild(link);

        return wrapper;
    }

    function hideMapSection(mapElement) {
        var section = mapElement.closest(".map-section");
        if (section) {
            section.hidden = true;
        }
    }

    function placeMarkers(map, markerElements) {
        var bounds = [];

        function step(i) {
            if (i >= markerElements.length) {
                if (bounds.length === 0) {
                    hideMapSection(map.getContainer());
                    return;
                }
                map.fitBounds(bounds, { padding: [40, 40], maxZoom: 13 });
                return;
            }

            var element = markerElements[i];
            var area = parseArea(element.dataset.hostAddress);

            if (!area) {
                step(i + 1);
                return;
            }

            geocode(area.query).then(function (position) {
                if (position) {
                    var markerData = {
                        name: element.dataset.hostName || "Tierbetreuer",
                        areaLabel: area.label,
                        price: element.dataset.hostPrice || "0,00",
                        profileUrl: element.dataset.hostProfileUrl || "#"
                    };
                    L.marker(position).addTo(map).bindPopup(createPopup(markerData));
                    bounds.push(position);
                }
                delay(REQUEST_DELAY_MS).then(function () { step(i + 1); });
            });
        }

        step(0);
    }

    function initHostMap() {
        var mapElement = document.getElementById("host-map");
        var markerElements = document.querySelectorAll("[data-host-map-marker]");

        if (!mapElement || markerElements.length === 0 || typeof L === "undefined") {
            return;
        }

        var map = L.map(mapElement, { scrollWheelZoom: false }).setView([51.0, 10.0], 6);

        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            attribution: "&copy; OpenStreetMap contributors"
        }).addTo(map);

        placeMarkers(map, markerElements);
    }

    document.addEventListener("DOMContentLoaded", initHostMap);
})();
