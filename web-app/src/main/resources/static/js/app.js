(() => {
    const form = document.querySelector("#addition-form");
    const firstNumber = document.querySelector("#first-number");
    const secondNumber = document.querySelector("#second-number");
    const submitButton = document.querySelector("#submit-button");
    const progressSection = document.querySelector("#progress-section");
    const progressBar = document.querySelector("#progress-bar");
    const progressContainer = progressBar.closest(".progress-track");
    const progressLabel = document.querySelector("#progress-label");
    const progressCount = document.querySelector("#progress-count");
    const errorMessage = document.querySelector("#error-message");
    const resultSection = document.querySelector("#result-section");
    const resultValue = document.querySelector("#result-value");
    let eventSource;

        const practicePanel = document.querySelector("#practice-panel");
        const startPractice = document.querySelector("#start-practice");
        const closePractice = document.querySelector("#close-practice");
        const expression = document.querySelector("#exercise-expression");
        const answer = document.querySelector("#exercise-answer");
        const feedback = document.querySelector("#exercise-feedback");
        const checkAnswer = document.querySelector("#check-answer");
        const exerciseNumber = document.querySelector("#exercise-number");
        const exerciseProgressBar = document.querySelector("#exercise-progress-bar");
        let currentExercise;
        let exerciseIndex = 0;

        const exercises = [
            { left: "1234", right: "897", operation: "+" },
            { left: "2500", right: "748", operation: "-" },
            { left: "9999", right: "1", operation: "+" },
            { left: "6000", right: "2755", operation: "-" },
            { left: "4876", right: "912", operation: "+" },
            { left: "10000", right: "999", operation: "-" },
            { left: "704", right: "296", operation: "+" },
            { left: "8300", right: "1645", operation: "-" },
            { left: "12345", right: "6789", operation: "+" },
            { left: "5000", right: "2345", operation: "-" }
        ];

        const formatNumber = (value) => value.replace(/\B(?=(\d{3})+(?!\d))/g, ".");

        const calculate = (exercise) => exercise.operation === "+"
            ? BigInt(exercise.left) + BigInt(exercise.right)
            : BigInt(exercise.left) - BigInt(exercise.right);

        const loadExercise = () => {
            currentExercise = exercises[exerciseIndex];
            expression.textContent = `${formatNumber(currentExercise.left)} ${currentExercise.operation} ${formatNumber(currentExercise.right)} =`;
            exerciseNumber.textContent = String(exerciseIndex + 1);
            exerciseProgressBar.style.width = `${((exerciseIndex + 1) / exercises.length) * 100}%`;
            answer.value = "";
            answer.disabled = false;
            feedback.textContent = "";
            feedback.className = "exercise-feedback";
            checkAnswer.textContent = "Kiểm tra đáp án →";
            answer.focus();
        };

        const showPractice = () => {
            exerciseIndex = 0;
            practicePanel.hidden = false;
            loadExercise();
            practicePanel.scrollIntoView({ behavior: "smooth", block: "center" });
        };

        startPractice.addEventListener("click", showPractice);
        closePractice.addEventListener("click", () => { practicePanel.hidden = true; });

        checkAnswer.addEventListener("click", () => {
            const submitted = answer.value.trim();
            if (!/^\d+$/.test(submitted)) {
                feedback.textContent = "Hãy nhập một số tự nhiên, không để trống nhé.";
                feedback.className = "exercise-feedback wrong";
                return;
            }
            const expected = calculate(currentExercise).toString();
            if (BigInt(submitted) === BigInt(expected)) {
                feedback.textContent = "Chính xác! Bạn đã xử lý đúng từng hàng số.";
                feedback.className = "exercise-feedback correct";
                answer.disabled = true;
                if (exerciseIndex < exercises.length - 1) {
                    checkAnswer.textContent = "Câu tiếp theo →";
                    checkAnswer.onclick = () => { exerciseIndex += 1; loadExercise(); checkAnswer.onclick = null; };
                } else {
                    checkAnswer.textContent = "Hoàn thành bài học ✓";
                    checkAnswer.onclick = () => { feedback.textContent = "Bạn đã hoàn thành 10 câu. Hẹn gặp lại ở bài tiếp theo!"; };
                }
            } else {
                feedback.textContent = `Chưa đúng. Hãy thử đặt tính theo từng hàng rồi kiểm tra phần nhớ.`;
                feedback.className = "exercise-feedback wrong";
            }
        });

        answer.addEventListener("keydown", (event) => {
            if (event.key === "Enter") { event.preventDefault(); checkAnswer.click(); }
        });

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
            const message = /^\d+$/.test(input.value.trim()) ? "" : "Chỉ nhập các chữ số thập phân.";
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
            progressLabel.textContent = `${percentage}% hoàn thành`;
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
            displayError(status.error || "Không thể thực hiện phép cộng.");
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
        progressLabel.textContent = "Đang bắt đầu phép tính";
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