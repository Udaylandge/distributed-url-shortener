/**
 * URLSHORTNER - Dashboard Interactive Scripts
 * Handles Chart.js initialization, QR generation, copy toast animations, and dark mode toggling.
 */

document.addEventListener("DOMContentLoaded", () => {
    
    // 1. Sidebar Toggle for Mobile/Tablet
    const sidebarToggle = document.getElementById("sidebarToggle");
    const wrapper = document.getElementById("wrapper");
    if (sidebarToggle) {
        sidebarToggle.addEventListener("click", () => {
            wrapper.classList.toggle("toggled");
        });
    }

    // 2. Dark Mode Toggle
    const themeToggle = document.getElementById("themeToggle");
    const themeIcon = document.getElementById("themeIcon");
    const isDark = localStorage.getItem("shortify_theme") === "dark";

    const applyTheme = (dark) => {
        if (dark) {
            document.body.classList.add("dark-mode");
            themeIcon.classList.replace("fa-moon", "fa-sun");
        } else {
            document.body.classList.remove("dark-mode");
            themeIcon.classList.replace("fa-sun", "fa-moon");
        }
    };

    applyTheme(isDark);

    if (themeToggle) {
        themeToggle.addEventListener("click", () => {
            const currentDark = document.body.classList.contains("dark-mode");
            applyTheme(!currentDark);
            localStorage.setItem("shortify_theme", !currentDark ? "dark" : "light");
        });
    }

    // 3. Initialize Chart.js - Clicks Timeline Chart
    const ctxTimeline = document.getElementById("clicksTimelineChart");
    if (ctxTimeline) {
        new Chart(ctxTimeline, {
            type: 'line',
            data: {
                labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
                datasets: [{
                    label: 'Total Redirect Clicks',
                    data: [1820, 2450, 3100, 2800, 3900, 4200, 4890],
                    borderColor: '#2563EB',
                    backgroundColor: 'rgba(37, 99, 235, 0.08)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.4,
                    pointBackgroundColor: '#2563EB',
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2,
                    pointRadius: 5,
                    pointHoverRadius: 7
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#0F172A',
                        padding: 12,
                        titleFont: { size: 13, family: 'Inter' },
                        bodyFont: { size: 14, weight: 'bold', family: 'Inter' },
                        cornerRadius: 8
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { color: 'rgba(0, 0, 0, 0.05)', drawBorder: false },
                        ticks: { font: { family: 'Inter' } }
                    },
                    x: {
                        grid: { display: false },
                        ticks: { font: { family: 'Inter' } }
                    }
                }
            }
        });
    }

    // 4. Initialize Chart.js - Browser Traffic Pie Chart
    const ctxPie = document.getElementById("browserPieChart");
    if (ctxPie) {
        new Chart(ctxPie, {
            type: 'doughnut',
            data: {
                labels: ['Chrome', 'Safari', 'Firefox', 'Edge'],
                datasets: [{
                    data: [52, 24, 15, 9],
                    backgroundColor: ['#2563EB', '#7C3AED', '#06B6D4', '#64748B'],
                    borderWidth: 0,
                    hoverOffset: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '72%',
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#0F172A',
                        padding: 10,
                        cornerRadius: 8
                    }
                }
            }
        });
    }

    // 5. Professional Toast Notification Setup
    const toastElement = document.getElementById("actionToast");
    const toastMessage = document.getElementById("toastMessage");
    let toastInstance = null;
    if (toastElement && typeof bootstrap !== 'undefined') {
        toastInstance = new bootstrap.Toast(toastElement, { delay: 2500 });
    }

    const showToast = (message, isSuccess = true) => {
        if (!toastInstance) return;
        const icon = document.getElementById("toastIcon");
        toastMessage.innerText = message;
        if (isSuccess) {
            icon.className = "fa-solid fa-circle-check text-success fs-5 me-2";
        } else {
            icon.className = "fa-solid fa-circle-exclamation text-warning fs-5 me-2";
        }
        toastInstance.show();
    };

    // 6. Copy to Clipboard with Button Animation
    const copyButtons = document.querySelectorAll(".btn-copy");
    copyButtons.forEach(btn => {
        btn.addEventListener("click", function () {
            const urlToCopy = this.getAttribute("data-url");
            if (!urlToCopy) return;

            navigator.clipboard.writeText(urlToCopy).then(() => {
                // Button Animation
                const originalHtml = this.innerHTML;
                this.innerHTML = `<i class="fa-solid fa-check"></i>`;
                this.classList.add("copied");

                showToast(`Copied ${urlToCopy} to clipboard!`);

                setTimeout(() => {
                    this.innerHTML = originalHtml;
                    this.classList.remove("copied");
                }, 2000);
            }).catch(err => {
                console.error("Failed to copy URL: ", err);
                showToast("Failed to copy URL to clipboard.", false);
            });
        });
    });

    // 7. Dynamic QR Code Generator Trigger
    const qrButtons = document.querySelectorAll(".btn-qr");
    const qrModalImage = document.getElementById("qrModalImage");
    const qrModalUrlDisplay = document.getElementById("qrModalUrlDisplay");
    const qrDownloadBtn = document.getElementById("qrDownloadBtn");

    qrButtons.forEach(btn => {
        btn.addEventListener("click", function () {
            const targetUrl = this.getAttribute("data-url");
            const alias = this.getAttribute("data-alias") || "qr";
            if (!targetUrl) return;

            const qrApiUrl = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&margin=10&data=${encodeURIComponent(targetUrl)}`;

            // Populate Modal Elements
            if (qrModalImage) qrModalImage.src = qrApiUrl;
            if (qrModalUrlDisplay) qrModalUrlDisplay.innerText = targetUrl;
            if (qrDownloadBtn) {
                qrDownloadBtn.href = qrApiUrl;
                qrDownloadBtn.setAttribute("download", `shortify-${alias}-qr.png`);
            }
        });
    });
});