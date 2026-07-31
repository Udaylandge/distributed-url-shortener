/**
 * URLSHORTNER - Create URL Interactive Scripts
 * Handles mobile sidebar toggling, password switch visibility, live demo submission, and copy toasts.
 */

document.addEventListener("DOMContentLoaded", () => {
    
    // 1. Sidebar Toggle for Mobile/Tablet (< 992px)
    const sidebarToggle = document.getElementById("sidebarToggle");
    const wrapper = document.getElementById("wrapper");
    if (sidebarToggle && wrapper) {
        sidebarToggle.addEventListener("click", () => {
            wrapper.classList.toggle("toggled");
        });
    }

    // 2. Toggle Password Protection Input Box
    const passwordSwitch = document.getElementById("passwordProtectSwitch");
    const passwordContainer = document.getElementById("passwordInputContainer");
    const linkPasswordInput = document.getElementById("linkPassword");

    if (passwordSwitch && passwordContainer) {
        passwordSwitch.addEventListener("change", function () {
            if (this.checked) {
                passwordContainer.classList.remove("d-none");
                if (linkPasswordInput) linkPasswordInput.required = true;
            } else {
                passwordContainer.classList.add("d-none");
                if (linkPasswordInput) {
                    linkPasswordInput.required = false;
                    linkPasswordInput.value = "";
                }
            }
        });
    }

    // 3. Form Submission Simulation & Live Result Display
    const createForm = document.getElementById("createUrlForm");
    const generateBtn = document.getElementById("generateBtn");
    const resultBox = document.getElementById("resultBox");
    const resOriginalUrl = document.getElementById("resOriginalUrl");
    const resShortUrl = document.getElementById("resShortUrl");

    // Array of mock Base62 hashes if alias is left blank
    const mockHashes = ['a8Kd92', 'J8dk2L', 'X9vM3q', 'mK9b2Y', 'R3xW1z'];

    if (createForm) {
        createForm.addEventListener("submit", async function (e) {
            e.preventDefault();

            const originalUrlVal = document.getElementById("originalUrl").value.trim();
            const customAliasVal = document.getElementById("customAlias") ? document.getElementById("customAlias").value.trim() : "";

            if (!originalUrlVal) return;

            // UI Loading State
            const originalBtnText = generateBtn.innerHTML;
            generateBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Shortening...`;
            generateBtn.disabled = true;

            try {
                const response = await fetch("/api/shorten", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        originalUrl: originalUrlVal,
                        customAlias: customAliasVal
                    })
                });

                const data = await response.json();

                if (!response.ok) {
                    throw new Error(data.error || "Failed to shorten URL");
                }

                const generatedUrl = data.shortUrl;

                // Update Result Box DOM
                resOriginalUrl.innerText = data.originalUrl;
                resShortUrl.innerText = generatedUrl;
                resShortUrl.setAttribute("href", generatedUrl);
                resShortUrl.setAttribute("target", "_blank");

                // Update QR Modal DOM
                const qrImg = document.getElementById("qrModalImg");
                const qrText = document.getElementById("qrModalText");
                const qrDownload = document.getElementById("downloadQrBtn");
                const qrApiUrl = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&margin=10&data=${encodeURIComponent(generatedUrl)}`;

                if (qrImg) qrImg.src = qrApiUrl;
                if (qrText) qrText.innerText = generatedUrl;
                if (qrDownload) {
                    qrDownload.href = qrApiUrl;
                    qrDownload.setAttribute("download", `short-${data.shortCode}-qr.png`);
                }

                // Show result box with fade-in animation
                resultBox.classList.remove("d-none");
                resultBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            } catch (err) {
                console.error("Shorten URL error:", err);
                alert("Error: " + err.message);
            } finally {
                // Restore Button State
                generateBtn.innerHTML = originalBtnText;
                generateBtn.disabled = false;
            }
        });
    }

    // 4. Copy to Clipboard & Toast Notification
    const copyResultBtn = document.getElementById("copyResultBtn");
    const toastElement = document.getElementById("copyToast");
    const toastMsg = document.getElementById("toastMsg");

    if (copyResultBtn && toastElement && typeof bootstrap !== 'undefined') {
        const toast = new bootstrap.Toast(toastElement, { delay: 2500 });

        copyResultBtn.addEventListener("click", () => {
            const textToCopy = resShortUrl.innerText;
            
            navigator.clipboard.writeText(textToCopy).then(() => {
                const originalHtml = copyResultBtn.innerHTML;
                copyResultBtn.innerHTML = `<i class="fa-solid fa-check me-2"></i><span>Copied!</span>`;
                copyResultBtn.classList.replace("btn-primary", "btn-success");

                toastMsg.innerText = `Copied ${textToCopy} to clipboard!`;
                toast.show();

                setTimeout(() => {
                    copyResultBtn.innerHTML = originalHtml;
                    copyResultBtn.classList.replace("btn-success", "btn-primary");
                }, 2000);
            }).catch(err => console.error("Copy failed: ", err));
        });
    }
});