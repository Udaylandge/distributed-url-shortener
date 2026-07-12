/**
 * URLSHORTNER - Unified Manage, Analytics & Profile Scripts
 * Handles live search filtering, status tab switching, Chart.js graphs, and clipboard actions.
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

    // 2. Toast Notification Helper
    const toastElement = document.getElementById("actionToast");
    const toastMsg = document.getElementById("toastMsg");
    let toastInstance = null;
    if (toastElement && typeof bootstrap !== 'undefined') {
        toastInstance = new bootstrap.Toast(toastElement, { delay: 2500 });
    }

    const showToast = (msg, isSuccess = true) => {
        if (!toastInstance) return;
        const icon = document.getElementById("toastIcon");
        if (toastMsg) toastMsg.innerText = msg;
        if (icon) {
            icon.className = isSuccess 
                ? "fa-solid fa-circle-check text-success fs-5 me-2" 
                : "fa-solid fa-circle-exclamation text-warning fs-5 me-2";
        }
        toastInstance.show();
    };

    // 3. MODULE 5: My URLs Management (Live Search & Tab Filtering)
    const liveSearchInput = document.getElementById("liveSearchInput");
    const filterButtons = document.querySelectorAll(".filter-btn");
    const urlRows = document.querySelectorAll(".url-row");
    const noResultsBox = document.getElementById("noResultsBox");
    const showingCountText = document.getElementById("showingCountText");

    let currentStatusFilter = "all";
    let currentSearchQuery = "";

    const filterUrlsTable = () => {
        let visibleCount = 0;
        urlRows.forEach(row => {
            const rowStatus = row.getAttribute("data-status");
            const rowSearchData = (row.getAttribute("data-search") || "").toLowerCase();

            const matchesStatus = (currentStatusFilter === "all" || rowStatus === currentStatusFilter);
            const matchesSearch = rowSearchData.includes(currentSearchQuery);

            if (matchesStatus && matchesSearch) {
                row.style.display = "";
                visibleCount++;
            } else {
                row.style.display = "none";
            }
        });

        // Toggle No Results Box
        if (noResultsBox) {
            noResultsBox.classList.toggle("d-none", visibleCount > 0);
        }
        if (showingCountText) {
            showingCountText.innerText = `Showing ${visibleCount} of ${urlRows.length} shortened links`;
        }
    };

    if (liveSearchInput) {
        liveSearchInput.addEventListener("input", function () {
            currentSearchQuery = this.value.trim().toLowerCase();
            filterUrlsTable();
        });
    }

    if (filterButtons.length > 0) {
        filterButtons.forEach(btn => {
            btn.addEventListener("click", function () {
                filterButtons.forEach(b => b.classList.remove("active"));
                this.classList.add("active");
                currentStatusFilter = this.getAttribute("data-filter");
                filterUrlsTable();
            });
        });
    }

    // Copy Button Logic
    document.querySelectorAll(".btn-copy").forEach(btn => {
        btn.addEventListener("click", function () {
            const url = this.getAttribute("data-url");
            if (!url) return;
            navigator.clipboard.writeText(url).then(() => {
                showToast(`Copied ${url} to clipboard!`);
            }).catch(() => showToast("Failed to copy link.", false));
        });
    });

    // QR Modal Trigger
    document.querySelectorAll(".btn-qr").forEach(btn => {
        btn.addEventListener("click", function () {
            const url = this.getAttribute("data-url");
            const alias = this.getAttribute("data-alias") || "qr";
            const img = document.getElementById("qrModalImg");
            const text = document.getElementById("qrModalText");
            const dl = document.getElementById("downloadQrBtn");
            if (!url || !img) return;

            const apiUrl = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&margin=10&data=${encodeURIComponent(url)}`;
            img.src = apiUrl;
            if (text) text.innerText = url;
            if (dl) {
                dl.href = apiUrl;
                dl.setAttribute("download", `shortify-${alias}.png`);
            }
        });
    });

    // Delete Button Simulation
    document.querySelectorAll(".btn-delete").forEach(btn => {
        btn.addEventListener("click", function () {
            const alias = this.getAttribute("data-alias");
            if (confirm(`Are you sure you want to delete /${alias}? This action cannot be undone.`)) {
                const row = this.closest("tr");
                if (row) row.remove();
                showToast(`Short URL /${alias} deleted successfully.`);
            }
        });
    });

    // 4. MODULE 6: Analytics Chart.js Initialization
    const ctxVolume = document.getElementById("volumeAnalyticsChart");
    if (ctxVolume && typeof Chart !== 'undefined') {
        new Chart(ctxVolume, {
            type: 'bar',
            data: {
                labels: ['Jul 06', 'Jul 07', 'Jul 08', 'Jul 09', 'Jul 10', 'Jul 11', 'Jul 12'],
                datasets: [{
                    label: 'Daily Redirect Volume',
                    data: [4200, 5100, 4800, 6300, 5900, 7200, 8400],
                    backgroundColor: '#2563EB',
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    const ctxDevice = document.getElementById("deviceDoughnutChart");
    if (ctxDevice && typeof Chart !== 'undefined') {
        new Chart(ctxDevice, {
            type: 'doughnut',
            data: {
                labels: ['Desktop', 'Mobile', 'Tablet'],
                datasets: [{
                    data: [58, 35, 7],
                    backgroundColor: ['#2563EB', '#7C3AED', '#06B6D4'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '70%',
                plugins: { legend: { display: false } }
            }
        });
    }
});