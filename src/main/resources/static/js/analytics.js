// =====================================================
// ANALYTICS.JS — Chart.js Visualizations
// =====================================================

document.addEventListener('DOMContentLoaded', () => {
    initNavbarDropdown();
    renderTopUrlsChart();
    renderActivityChart();
});

// ── Navbar Dropdown ──────────────────────────────────
function toggleDropdown() {
    const dropdown = document.getElementById('profileDropdown');
    if (dropdown) dropdown.classList.toggle('open');
}

function initNavbarDropdown() {
    document.addEventListener('click', (e) => {
        const dropdown = document.getElementById('profileDropdown');
        if (dropdown && !dropdown.contains(e.target)) {
            dropdown.classList.remove('open');
        }
    });
}

// ── Top URLs Bar Chart ────────────────────────────────
function renderTopUrlsChart() {
    const canvas = document.getElementById('topUrlsChart');
    if (!canvas || !window.topUrlsData) return;

    const { labels, clicks } = window.topUrlsData;
    if (!labels || labels.length === 0) return;

    const gradients = [
        '#7c3aed', '#2563eb', '#10b981', '#f97316', '#06b6d4'
    ];

    new Chart(canvas, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Clicks',
                data: clicks,
                backgroundColor: gradients.slice(0, labels.length).map(c => c + 'CC'),
                borderColor: gradients.slice(0, labels.length),
                borderWidth: 2,
                borderRadius: 8,
                borderSkipped: false,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: (ctx) => `  ${ctx.parsed.y} clicks`
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { color: '#f1f5f9' },
                    ticks: {
                        color: '#94a3b8',
                        font: { family: 'Poppins', size: 11 }
                    }
                },
                x: {
                    grid: { display: false },
                    ticks: {
                        color: '#475569',
                        font: { family: 'Poppins', size: 11, weight: '600' }
                    }
                }
            }
        }
    });
}

// ── Activity Line Chart ───────────────────────────────
function renderActivityChart() {
    const canvas = document.getElementById('activityChart');
    if (!canvas || !window.allUrlsData) return;

    const { labels, clicks } = window.allUrlsData;
    if (!labels || labels.length === 0) return;

    new Chart(canvas, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Clicks',
                data: clicks,
                borderColor: '#7c3aed',
                backgroundColor: 'rgba(124, 58, 237, 0.1)',
                tension: 0.4,
                fill: true,
                pointBackgroundColor: '#7c3aed',
                pointBorderColor: 'white',
                pointBorderWidth: 2,
                pointRadius: 5,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: (ctx) => `  ${ctx.parsed.y} clicks`
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { color: '#f1f5f9' },
                    ticks: {
                        color: '#94a3b8',
                        font: { family: 'Poppins', size: 11 }
                    }
                },
                x: {
                    grid: { display: false },
                    ticks: {
                        color: '#475569',
                        font: { family: 'Poppins', size: 11 },
                        maxTicksLimit: 8
                    }
                }
            }
        }
    });
}
