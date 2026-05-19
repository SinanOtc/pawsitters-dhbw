(function () {
    var fallbackPositions = [
        [49.1427, 9.2109],
        [49.1505, 9.2184],
        [49.1368, 9.1987],
        [49.1472, 9.1918],
        [49.1329, 9.2231]
    ];

    function createPopup(markerData) {
        var wrapper = document.createElement("div");
        wrapper.className = "map-popup";

        var title = document.createElement("strong");
        title.textContent = markerData.name;
        wrapper.appendChild(title);

        var address = document.createElement("span");
        address.textContent = markerData.address;
        wrapper.appendChild(address);

        var price = document.createElement("span");
        price.textContent = markerData.price + " Euro pro Woche";
        wrapper.appendChild(price);

        var link = document.createElement("a");
        link.href = markerData.profileUrl;
        link.textContent = "Zum Angebot";
        wrapper.appendChild(link);

        return wrapper;
    }

    function initHostMap() {
        var mapElement = document.getElementById("host-map");
        var markerElements = document.querySelectorAll("[data-host-map-marker]");

        if (!mapElement || markerElements.length === 0 || typeof L === "undefined") {
            return;
        }

        var map = L.map(mapElement, {
            scrollWheelZoom: false
        }).setView([49.1427, 9.2109], 13);

        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            attribution: "&copy; OpenStreetMap contributors"
        }).addTo(map);

        markerElements.forEach(function (element, index) {
            var position = fallbackPositions[index % fallbackPositions.length];
            var markerData = {
                name: element.dataset.hostName || "Tierbetreuer",
                address: element.dataset.hostAddress || "Adresse im PLZ-Bereich",
                price: element.dataset.hostPrice || "0,00",
                profileUrl: element.dataset.hostProfileUrl || "#"
            };

            L.marker(position)
                .addTo(map)
                .bindPopup(createPopup(markerData));
        });
    }

    document.addEventListener("DOMContentLoaded", initHostMap);
})();
