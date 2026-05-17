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

    function today() {
        var now = new Date();
        return new Date(now.getFullYear(), now.getMonth(), now.getDate());
    }

    function setMin(input, value) {
        if (input.min !== value) {
            input.min = value;
        }
    }

    function initRange(container) {
        var startInput = container.querySelector("[data-date-range-start]");
        var endInput = container.querySelector("[data-date-range-end]");
        if (!startInput || !endInput) {
            return;
        }

        // "today" (default, fuer @FutureOrPresent) oder "tomorrow" (fuer @Future)
        var minRule = container.dataset.dateRangeMin || "today";

        function validate() {
            var minDate = minRule === "tomorrow" ? addDays(today(), 1) : today();
            var startDate = parseDate(startInput.value);
            var endDate = parseDate(endInput.value);
            var startMessage = "";
            var endMessage = "";

            setMin(startInput, formatDate(minDate));
            if (startDate) {
                setMin(endInput, formatDate(addDays(startDate, 1)));
            } else {
                setMin(endInput, formatDate(minDate));
            }

            if (startDate && startDate < minDate) {
                startMessage = minRule === "tomorrow"
                    ? "Startdatum muss in der Zukunft liegen"
                    : "Startdatum darf nicht in der Vergangenheit liegen";
            }

            if (endDate && endDate < minDate) {
                endMessage = minRule === "tomorrow"
                    ? "Enddatum muss in der Zukunft liegen"
                    : "Enddatum darf nicht in der Vergangenheit liegen";
            }

            if (startDate && endDate && endDate <= startDate) {
                if (!startMessage) {
                    startMessage = "Startdatum muss vor dem Enddatum liegen";
                }
                endMessage = "Enddatum muss nach Startdatum liegen";
            }

            startInput.setCustomValidity(startMessage);
            endInput.setCustomValidity(endMessage);
        }

        startInput.addEventListener("input", validate);
        endInput.addEventListener("input", validate);
        validate();
    }

    function init() {
        document.querySelectorAll("[data-date-range]").forEach(initRange);
    }

    document.addEventListener("DOMContentLoaded", init);
})();
