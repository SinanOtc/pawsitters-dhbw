(function () {
    function parseDate(value) {
        var parts = value ? value.split("-") : [];
        if (parts.length !== 3) {
            return null;
        }
        return new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]));
    }

    function formatDate(date) {
        var month = String(date.getMonth() + 1).padStart(2, "0");
        var day = String(date.getDate()).padStart(2, "0");
        return date.getFullYear() + "-" + month + "-" + day;
    }

    function addDays(date, days) {
        var copy = new Date(date.getTime());
        copy.setDate(copy.getDate() + days);
        return copy;
    }

    function initCareRequestForm() {
        var form = document.querySelector("[data-care-request-form]");
        if (!form) {
            return;
        }

        sessionStorage.removeItem("careRequestCreated");

        var startDateInput = form.querySelector("[data-start-date]");
        var endDateInput = form.querySelector("[data-end-date]");
        if (!startDateInput || !endDateInput) {
            return;
        }

        var tomorrow = addDays(new Date(), 1);
        startDateInput.min = formatDate(tomorrow);
        endDateInput.min = formatDate(tomorrow);

        function validateDateRange() {
            var startDate = parseDate(startDateInput.value);
            var endDate = parseDate(endDateInput.value);

            if (startDate) {
                endDateInput.min = formatDate(addDays(startDate, 1));
            } else {
                endDateInput.min = formatDate(tomorrow);
            }

            if (startDate && endDate && endDate <= startDate) {
                endDateInput.setCustomValidity("Enddatum muss nach Startdatum liegen");
            } else {
                endDateInput.setCustomValidity("");
            }
        }

        startDateInput.addEventListener("input", validateDateRange);
        endDateInput.addEventListener("input", validateDateRange);
        validateDateRange();

        form.addEventListener("submit", function () {
            validateDateRange();
            if (form.checkValidity()) {
                sessionStorage.setItem("careRequestCreated", "true");
            }
        });
    }

    function initCreatedMessage() {
        var message = document.querySelector("[data-care-request-created-message]");
        if (!message) {
            return;
        }

        if (sessionStorage.getItem("careRequestCreated") === "true") {
            message.hidden = false;
            sessionStorage.removeItem("careRequestCreated");
        }
    }

    document.addEventListener("DOMContentLoaded", function () {
        initCareRequestForm();
        initCreatedMessage();
    });
})();
