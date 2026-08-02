/**
 * Shortify Global JavaScript Module
 * Handles UI interactions, copy to clipboard, password visibility toggle,
 * custom alias validation, and form loading indicators.
 */
document.addEventListener("DOMContentLoaded", () => {
    
    // 1. Password Visibility Toggle
    document.querySelectorAll(".toggle-password").forEach(btn => {
        btn.addEventListener("click", function () {
            const input = this.closest(".input-group").querySelector("input");
            const icon = this.querySelector("i");
            if (input) {
                if (input.type === "password") {
                    input.type = "text";
                    if (icon) {
                        icon.classList.remove("fa-eye");
                        icon.classList.add("fa-eye-slash");
                    }
                } else {
                    input.type = "password";
                    if (icon) {
                        icon.classList.remove("fa-eye-slash");
                        icon.classList.add("fa-eye");
                    }
                }
            }
        });
    });

    // 2. Global Copy Button Handler
    document.querySelectorAll(".copy-btn").forEach(btn => {
        btn.addEventListener("click", function () {
            const targetUrl = this.getAttribute("data-url");
            if (targetUrl) {
                navigator.clipboard.writeText(targetUrl).then(() => {
                    const originalHtml = this.innerHTML;
                    this.innerHTML = '<i class="fa-solid fa-check text-success"></i>';
                    setTimeout(() => {
                        this.innerHTML = originalHtml;
                    }, 2000);
                });
            }
        });
    });

    // 3. Custom Alias Validation Guard
    const customAliasInput = document.getElementById("customAlias");
    if (customAliasInput) {
        customAliasInput.addEventListener("input", function () {
            const val = this.value;
            const aliasRegex = /^[a-zA-Z0-9_-]{0,30}$/;
            const errorDiv = document.getElementById("aliasErrorMsg");
            
            if (val.includes("@") || val.includes(".") || val.includes(" ")) {
                this.classList.add("is-invalid");
                if (errorDiv) {
                    errorDiv.innerText = "Custom alias cannot contain '@', '.', spaces, or URLs. Only letters, numbers, hyphens, and underscores are allowed.";
                    errorDiv.style.display = "block";
                }
            } else if (!aliasRegex.test(val)) {
                this.classList.add("is-invalid");
                if (errorDiv) {
                    errorDiv.innerText = "Custom alias must be 3-30 characters long and contain only letters, numbers, hyphens, or underscores.";
                    errorDiv.style.display = "block";
                }
            } else {
                this.classList.remove("is-invalid");
                if (errorDiv) {
                    errorDiv.style.display = "none";
                }
            }
        });
    }

    // 4. Form Submit Loading Spinner
    document.querySelectorAll("form").forEach(form => {
        form.addEventListener("submit", function () {
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn && !submitBtn.disabled && !this.checkValidity || this.checkValidity()) {
                const spinner = submitBtn.querySelector(".spinner-border");
                if (spinner) {
                    spinner.classList.remove("d-none");
                }
            }
        });
    });
});