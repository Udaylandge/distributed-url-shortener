// =====================================================
// SIDEBAR.JS — Sidebar Toggle & Collapse Logic
// =====================================================

document.addEventListener('DOMContentLoaded', () => {

    const toggleBtn = document.getElementById('sidebar-toggle');
    const body = document.body;

    // Restore saved state
    const isCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';
    if (isCollapsed) {
        body.classList.add('sidebar-collapsed');
    }

    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            body.classList.toggle('sidebar-collapsed');
            const collapsed = body.classList.contains('sidebar-collapsed');
            localStorage.setItem('sidebarCollapsed', collapsed);
        });
    }

    // Mobile: close sidebar on overlay click
    document.addEventListener('click', (e) => {
        if (window.innerWidth <= 768) {
            const sidebar = document.getElementById('sidebar');
            const toggleButton = document.getElementById('sidebar-toggle');
            if (sidebar && !sidebar.contains(e.target) && !toggleButton?.contains(e.target)) {
                body.classList.remove('sidebar-open');
            }
        }
    });

    if (toggleBtn && window.innerWidth <= 768) {
        toggleBtn.addEventListener('click', () => {
            body.classList.toggle('sidebar-open');
        });
    }
});