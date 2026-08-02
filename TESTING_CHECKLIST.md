# Testing & Verification Checklist for Shortify

## 1. Core Link Shortening & Redirect Tests
- [x] Shorten standard link with protocol (e.g., `https://github.com`) -> Verify short URL generated.
- [x] Shorten link without protocol (e.g., `google.com`) -> Verify saved as `https://google.com` and redirects properly.
- [x] Access valid short link -> Verify 302 redirect & click count incremented by 1.
- [x] Access expired short link -> Verify custom `url/expired.html` page displayed without redirecting.
- [x] Access disabled short link (`active=false`) -> Verify custom `url/disabled.html` page displayed.
- [x] Access password-protected link -> Verify password prompt page displayed and unlocked only with correct passkey.
- [x] Access one-time link -> Verify first click redirects and deactivates link for subsequent clicks.

## 2. Redis & Fallback Resilience Tests
- [x] Redis Cache Hit -> Verify ultra-fast resolution.
- [x] Redis Service Down -> Stop Redis instance; verify system falls back to MongoDB Atlas without errors or 500 pages.

## 3. Account & Security Tests
- [x] User Registration & Sign In -> Verify form login and authentication.
- [x] Forgot Password & Reset Token -> Verify token generation and password reset flow.
- [x] Email Verification Token -> Verify verification link processing.
- [x] Remember Me Checkbox -> Verify persistent login cookie set.

## 4. UI & Management Tests
- [x] Custom Alias -> Verify custom short code creation and uniqueness validation.
- [x] QR Code Download -> Verify PNG QR code image stream (`/urls/qr/{id}`).
- [x] CSV Export -> Download CSV report (`/urls/export/csv`).
- [x] Soft Delete & Restore -> Move link to Recycle Bin and restore or purge link.
- [x] Rebranding -> Verify "Shortify" branding across all views, titles, and headers.
