(() => {
    const form = document.querySelector("#addition-form");
    const firstNumber = document.querySelector("#first-number");
    const secondNumber = document.querySelector("#second-number");
    const submitButton = document.querySelector("#submit-button");
    const progressSection = document.querySelector("#progress-section");
    const progressBar = document.querySelector("#progress-bar");
    const progressContainer = progressBar.closest(".progress");
    const progressLabel = document.querySelector("#progress-label");
    const progressCount = document.querySelector("#progress-count");
    const errorMessage = document.querySelector("#error-message");
    const resultSection = document.querySelector("#result-section");
    const resultValue = document.querySelector("#result-value");
    let eventSource;

    const resetVisualState = () => {
        errorMessage.hidden = true;
        resultSection.hidden = true;
        progressSection.hidden = true;
        progressBar.style.width = "0%";
        progressContainer.setAttribute("aria-valuenow", "0");
    };

    const closeStream = () => {
        if (eventSource) {
            eventSource.close();
            eventSource = undefined;
        }
    };

    const displayError = (message) => {
        errorMessage.textContent = message;
        errorMessage.hidden = false;
    };

    const validateInputs = () => {
        let valid = true;
        [firstNumber, secondNumber].forEach((input) => {
            const feedback = input.nextElementSibling;
            const message = /^\d+$/.test(input.value) ? "" : "Enter decimal digits only.";
            input.setCustomValidity(message);
            input.classList.toggle("is-invalid", Boolean(message));
            feedback.textContent = message;
            valid &&= !message;
        });
        return valid;
    };

    const applyStatus = (status) => {
        if (status.progress) {
            const { completedSteps, totalSteps, percentage } = status.progress;
            progressSection.hidden = false;
            progressBar.style.width = `${percentage}%`;
            progressContainer.setAttribute("aria-valuenow", String(percentage));
            progressLabel.textContent = `${percentage}% complete`;
            progressCount.textContent = `${completedSteps} / ${totalSteps}`;
        }
        if (status.status === "COMPLETED") {
            progressSection.hidden = false;
            progressBar.style.width = "100%";
            progressContainer.setAttribute("aria-valuenow", "100");
            progressLabel.textContent = "Completed";
            resultValue.textContent = status.result.sum;
            resultSection.hidden = false;
            submitButton.disabled = false;
            closeStream();
        } else if (status.status === "FAILED") {
            displayError(status.error || "The addition could not be completed.");
            submitButton.disabled = false;
            closeStream();
        }
    };

    const loadStatus = async (url) => {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error("The calculation is no longer available.");
        }
        applyStatus(await response.json());
    };

    const openStream = (eventsUrl, statusUrl) => {
        eventSource = new EventSource(eventsUrl);
        ["status", "progress", "completed", "failed"].forEach((eventName) => {
            eventSource.addEventListener(eventName, (event) => applyStatus(JSON.parse(event.data)));
        });
        eventSource.onerror = async () => {
            closeStream();
            try {
                await loadStatus(statusUrl);
            } catch (error) {
                displayError(error.message);
                submitButton.disabled = false;
            }
        };
    };

    form.addEventListener("reset", () => {
        closeStream();
        resetVisualState();
        submitButton.disabled = false;
        form.querySelectorAll(".is-invalid").forEach((input) => input.classList.remove("is-invalid"));
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        closeStream();
        resetVisualState();
        if (!validateInputs() || !form.checkValidity()) {
            form.classList.add("was-validated");
            return;
        }
        submitButton.disabled = true;
        progressSection.hidden = false;
        progressLabel.textContent = "Starting calculation";
        try {
            const response = await fetch("/api/additions", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ firstNumber: firstNumber.value, secondNumber: secondNumber.value })
            });
            if (!response.ok) {
                const errors = await response.json();
                throw new Error(Object.values(errors).join(" "));
            }
            const job = await response.json();
            openStream(job.eventsUrl, job.statusUrl);
        } catch (error) {
            displayError(error.message);
            submitButton.disabled = false;
        }
    });
})();