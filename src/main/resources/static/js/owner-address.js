(function () {
    function splitAddress(address) {
        var parts = {
            street: "",
            streetNumber: "",
            postalCode: "",
            city: ""
        };

        if (!address) {
            return parts;
        }

        var sections = address.split(",");
        var streetSection = sections[0] ? sections[0].trim() : "";
        var citySection = sections[1] ? sections.slice(1).join(",").trim() : "";
        var streetMatch = streetSection.match(/^(.*)\s+(\S+)$/);
        var cityMatch = citySection.match(/^(\d{5})\s+(.+)$/);

        parts.street = streetMatch ? streetMatch[1] : streetSection;
        parts.streetNumber = streetMatch ? streetMatch[2] : "";
        parts.postalCode = cityMatch ? cityMatch[1] : "";
        parts.city = cityMatch ? cityMatch[2] : citySection;

        return parts;
    }

    function composeAddress(form) {
        var street = form.querySelector("[data-address-field='street']");
        var streetNumber = form.querySelector("[data-address-field='streetNumber']");
        var postalCode = form.querySelector("[data-address-field='postalCode']");
        var city = form.querySelector("[data-address-field='city']");

        return [
            [street.value.trim(), streetNumber.value.trim()].filter(Boolean).join(" "),
            [postalCode.value.trim(), city.value.trim()].filter(Boolean).join(" ")
        ].filter(Boolean).join(", ");
    }

    document.querySelectorAll("[data-composed-address]").forEach(function (hiddenAddress) {
        var form = hiddenAddress.form;
        var addressParts = splitAddress(hiddenAddress.value);

        Object.keys(addressParts).forEach(function (key) {
            var field = form.querySelector("[data-address-field='" + key + "']");
            if (field && !field.value) {
                field.value = addressParts[key];
            }
        });

        form.addEventListener("submit", function () {
            hiddenAddress.value = composeAddress(form);
        });
    });

    document.querySelectorAll("[data-address-part]").forEach(function (element) {
        var addressParts = splitAddress(element.dataset.address);
        var part = element.dataset.addressPart;

        if (addressParts[part]) {
            element.textContent = addressParts[part];
        }
    });
}());
