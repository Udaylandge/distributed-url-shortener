/**
 * Shortify - Authentication Interactive Scripts
 * Handles validation, password strength meters, matching validation, and loading spinners.
 */

document.addEventListener("DOMContentLoaded", () => {

    // 1. ✅ Password Visibility Toggle
    const toggleButtons = document.querySelectorAll(".toggle-password");
    toggleButtons.forEach(btn => {
        btn.addEventListener("click", function () {
            const input = this.closest(".input-group").querySelector("input");
            const icon = this.querySelector("i");
            if (input.type === "password") {
                input.type = "text";
                icon.classList.replace("fa-eye", "fa-eye-slash");
            } else {
                input.type = "password";
                icon.classList.replace("fa-eye-slash", "fa-eye");
            }
        });
    });

    // Helper: Show inline error
    const showError = (input, message) => {
        const group = input.closest(".input-group") || input;
        group.classList.add("is-invalid");
        const errorDiv = group.parentElement.querySelector(".inline-error");
        if (errorDiv) {
            if (message) errorDiv.innerText = message;
            errorDiv.style.display = "block";
        }
    };

    // Helper: Clear inline error
    const clearError = (input) => {
        const group = input.closest(".input-group") || input;
        group.classList.remove("is-invalid");
        const errorDiv = group.parentElement.querySelector(".inline-error");
        if (errorDiv) {
            errorDiv.style.display = "none";
        }
    };

    // 2. ✅ Login Form Validation & Loading Spinner
    const loginForm = document.getElementById("loginForm");
    if (loginForm) {
        loginForm.addEventListener("submit", function (e) {
            let isValid = true;
            const email = document.getElementById("email");
            const password = document.getElementById("password");

            clearError(email);
            clearError(password);

            // Basic Email Regex
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!email.value.trim() || !emailRegex.test(email.value)) {
                showError(email, "Please enter a valid email address.");
                isValid = false;
            }

            if (!password.value.trim()) {
                showError(password, "Please enter your password.");
                isValid = false;
            }

            if (!isValid) {
                e.preventDefault();
            } else {
                // ✅ Trigger Loading Spinner
                const submitBtn = document.getElementById("submitBtn");
                submitBtn.disabled = true;
                submitBtn.querySelector(".btn-text").innerText = "Signing in...";
                submitBtn.querySelector(".spinner-border").classList.remove("d-none");
            }
        });

        // Clear errors on input
        loginForm.querySelectorAll("input").forEach(input => {
            input.addEventListener("input", () => clearError(input));
        });
    }

    // 3. ✅ Register Form Validation, Matching & Strength Meter
    const registerForm = document.getElementById("registerForm");
    if (registerForm) {
        const regPassword = document.getElementById("regPassword");
        const confirmPassword = document.getElementById("confirmPassword");
        const strengthContainer = document.getElementById("strengthContainer");
        const strengthBar = document.getElementById("strengthBar");
        const strengthText = document.getElementById("strengthText");

        // ✅ Password Strength Indicator Algorithm
        regPassword.addEventListener("input", function () {
            clearError(regPassword);
            const val = this.value;
            if (val.length === 0) {
                strengthContainer.classList.add("d-none");
                return;
            }
            strengthContainer.classList.remove("d-none");

            let score = 0;
            if (val.length >= 8) score++;
            if (val.length >= 12) score++;
            if (/\d/.test(val)) score++;          // Has Number
            if (/[A-Z]/.test(val)) score++;       // Has Uppercase
            if (/[^A-Za-z0-9]/.test(val)) score++; // Has Special Char

            if (score <= 2) {
                strengthBar.style.width = "33%";
                strengthBar.className = "progress-bar bg-danger";
                strengthText.innerText = "Weak";
                strengthText.className = "small fw-bold text-danger";
            } else if (score === 3 || score === 4) {
                strengthBar.style.width = "66%";
                strengthBar.className = "progress-bar bg-warning";
                strengthText.innerText = "Medium";
                strengthText.className = "small fw-bold text-warning";
            } else {
                strengthBar.style.width = "100%";
                strengthBar.className = "progress-bar bg-success";
                strengthText.innerText = "Strong";
                strengthText.className = "small fw-bold text-success";
            }

            // Check match if confirm password is already populated
            if (confirmPassword.value.length > 0) {
                checkPasswordsMatch();
            }
        });

        // ✅ Validate Passwords Match
        const checkPasswordsMatch = () => {
            if (confirmPassword.value.length === 0) return true;
            if (regPassword.value !== confirmPassword.value) {
                showError(confirmPassword, "Passwords do not match.");
                return false;
            } else {
                clearError(confirmPassword);
                return true;
            }
        };

        confirmPassword.addEventListener("input", checkPasswordsMatch);

        // Clear errors on input for other fields
        registerForm.querySelectorAll("input:not(#regPassword):not(#confirmPassword)").forEach(input => {
            input.addEventListener("input", () => clearError(input));
        });

        // Register Submit Handler
        registerForm.addEventListener("submit", function (e) {
            let isValid = true;
            const fullName = document.getElementById("fullName");
            const email = document.getElementById("regEmail");
            const termsCheck = document.getElementById("termsCheck");

            // Clear all previous errors
            registerForm.querySelectorAll("input").forEach(input => clearError(input));

            if (!fullName.value.trim() || fullName.value.trim().length < 2) {
                showError(fullName, "Please enter your full name (at least 2 characters).");
                isValid = false;
            }

            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!email.value.trim() || !emailRegex.test(email.value)) {
                showError(email, "Please enter a valid email address.");
                isValid = false;
            }

            if (regPassword.value.length < 8) {
                showError(regPassword, "Password must be at least 8 characters long.");
                isValid = false;
            }

            if (!checkPasswordsMatch() || confirmPassword.value.length === 0) {
                showError(confirmPassword, "Please confirm your password correctly.");
                isValid = false;
            }

            if (!termsCheck.checked) {
                showError(termsCheck, "You must agree to the terms to continue.");
                isValid = false;
            }

            if (!isValid) {
                e.preventDefault();
            } else {
                // ✅ Trigger Loading Spinner
                const regSubmitBtn = document.getElementById("regSubmitBtn");
                regSubmitBtn.disabled = true;
                regSubmitBtn.querySelector(".btn-text").innerText = "Creating Account...";
                regSubmitBtn.querySelector(".spinner-border").classList.remove("d-none");
            }
        });
    }
});