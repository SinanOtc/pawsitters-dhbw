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
        var hasStreetNumber = streetMatch && /\d/.test(streetMatch[2]);
        var cityMatch = citySection.match(/^(\d{5})\s+(.+)$/);

        parts.street = hasStreetNumber ? streetMatch[1] : streetSection;
        parts.streetNumber = hasStreetNumber ? streetMatch[2] : "";
        parts.postalCode = cityMatch ? cityMatch[1] : "";
        parts.city = cityMatch ? cityMatch[2] : citySection;

        return parts;
    }

    function getAddressFields(form) {
        var fields = {
            street: form.querySelector("[data-address-field='street']"),
            streetNumber: form.querySelector("[data-address-field='streetNumber']"),
            postalCode: form.querySelector("[data-address-field='postalCode']"),
            city: form.querySelector("[data-address-field='city']")
        };

        return Object.keys(fields).every(function (key) {
            return fields[key];
        }) ? fields : null;
    }

    function hasCompleteAddress(parts) {
        return parts.street && parts.streetNumber && parts.postalCode && parts.city;
    }

    function composeAddress(fields) {
        if (!fields) {
            return null;
        }

        return [
            [fields.street.value.trim(), fields.streetNumber.value.trim()].filter(Boolean).join(" "),
            [fields.postalCode.value.trim(), fields.city.value.trim()].filter(Boolean).join(" ")
        ].filter(Boolean).join(", ");
    }

    document.querySelectorAll("[data-composed-address]").forEach(function (addressInput) {
        var form = addressInput.form;
        var splitGroup = form ? form.querySelector("[data-address-split]") : null;
        var fallbackGroup = form ? form.querySelector("[data-address-fallback-group]") : null;
        var fields = form ? getAddressFields(form) : null;

        if (!form || !splitGroup || !fields) {
            return;
        }

        var addressParts = splitAddress(addressInput.value);
        var hasStoredAddress = Boolean(addressInput.value.trim());

        if (hasStoredAddress && !hasCompleteAddress(addressParts)) {
            return;
        }

        Object.keys(addressParts).forEach(function (key) {
            var field = fields[key];
            if (field && !field.value) {
                field.value = addressParts[key];
            }
        });

        if (fallbackGroup) {
            fallbackGroup.hidden = true;
        }

        addressInput.required = false;
        splitGroup.hidden = false;
        Object.keys(fields).forEach(function (key) {
            fields[key].required = true;
        });

        form.addEventListener("submit", function () {
            var composedAddress = composeAddress(fields);
            if (composedAddress) {
                addressInput.value = composedAddress;
            }
        });
    });

    document.querySelectorAll("[data-address-fallback-row]").forEach(function (fallbackRow) {
        var addressParts = splitAddress(fallbackRow.dataset.address);
        var table = fallbackRow.closest("table");

        if (!table || !hasCompleteAddress(addressParts)) {
            return;
        }

        fallbackRow.hidden = true;
        table.querySelectorAll("[data-address-split-row]").forEach(function (row) {
            var element = row.querySelector("[data-address-part]");
            var part = element ? element.dataset.addressPart : null;

            if (part && addressParts[part]) {
                element.textContent = addressParts[part];
                row.hidden = false;
            }
        });
    });
}());
