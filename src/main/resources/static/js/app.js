const API = '/api';
const STORAGE_KEY = 'fitness_session';
const THEME_KEY = 'fitness_theme';
const PENDING_VERIFICATION_KEY = 'fitness_pending_verification';
const PENDING_RESET_KEY = 'fitness_pending_reset';

// DOM elements
const homeScreen = document.getElementById('home-screen');
const authScreen = document.getElementById('auth-screen');
const appScreen = document.getElementById('app-screen');

const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const otpForm = document.getElementById('otp-form');
const forgotForm = document.getElementById('forgot-form');
const resetForm = document.getElementById('reset-form');
const authTabs = document.getElementById('auth-tabs');
const socialLogin = document.getElementById('social-login');
const socialDivider = document.getElementById('social-divider');

const activityForm = document.getElementById('activity-form');
const recommendationForm = document.getElementById('recommendation-form');
const activitiesList = document.getElementById('activities-list');
const recommendationsList = document.getElementById('recommendations-list');
const activitySelect = document.getElementById('activity-select');
const userGreeting = document.getElementById('user-greeting');
const toast = document.getElementById('toast');
const homeNavActions = document.getElementById('home-nav-actions');

// App state
let session = loadSession();
let activities = [];
let pendingVerification = loadPendingVerification();
let pendingReset = loadPendingReset();
let oauthStatus = { google: false, github: false };

function loadPendingVerification() {
    try {
        return JSON.parse(sessionStorage.getItem(PENDING_VERIFICATION_KEY));
    } catch {
        return null;
    }
}

function savePendingVerification(data) {
    pendingVerification = data;
    if (data) {
        sessionStorage.setItem(PENDING_VERIFICATION_KEY, JSON.stringify(data));
    } else {
        sessionStorage.removeItem(PENDING_VERIFICATION_KEY);
    }
}

function loadPendingReset() {
    try {
        return JSON.parse(sessionStorage.getItem(PENDING_RESET_KEY));
    } catch {
        return null;
    }
}

function savePendingReset(data) {
    pendingReset = data;
    if (data) {
        sessionStorage.setItem(PENDING_RESET_KEY, JSON.stringify(data));
    } else {
        sessionStorage.removeItem(PENDING_RESET_KEY);
    }
}

// Theme toggle
let lastThemeToggle = 0;

function initTheme() {
    const savedTheme = localStorage.getItem(THEME_KEY) || 'light';
    applyTheme(savedTheme);
}

function applyTheme(theme) {
    const targetTheme = (theme === 'dark') ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', targetTheme);
    if (document.body) {
        document.body.setAttribute('data-theme', targetTheme);
    }
    try {
        localStorage.setItem(THEME_KEY, targetTheme);
    } catch (e) {
        // storage disabled in private browsing
    }
    const isDark = targetTheme === 'dark';
    document.querySelectorAll('.theme-icon').forEach(icon => {
        icon.textContent = isDark ? '☀️' : '🌙';
    });
}

function toggleTheme(e) {
    if (e && typeof e.preventDefault === 'function') {
        e.preventDefault();
        e.stopPropagation();
    }
    const now = Date.now();
    if (now - lastThemeToggle < 200) {
        return; // debounce rapid clicks
    }
    lastThemeToggle = now;

    const currentTheme = document.documentElement.getAttribute('data-theme') || localStorage.getItem(THEME_KEY) || 'light';
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    applyTheme(newTheme);
}

window.toggleTheme = toggleTheme;

// Delegated listener for theme toggle buttons
document.addEventListener('click', (e) => {
    const themeBtn = e.target.closest('.theme-toggle-btn');
    if (themeBtn) {
        toggleTheme(e);
    }
});

// Storage and API helpers
function loadSession() {
    try {
        return JSON.parse(localStorage.getItem(STORAGE_KEY));
    } catch {
        return null;
    }
}

function saveSession(data) {
    session = data;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
    updateHomeNav();
}

function clearSession() {
    session = null;
    localStorage.removeItem(STORAGE_KEY);
    updateHomeNav();
}

function showToast(message, isError = false) {
    toast.textContent = message;
    toast.classList.toggle('error', isError);
    toast.classList.remove('hidden');
    clearTimeout(showToast.timer);
    showToast.timer = setTimeout(() => toast.classList.add('hidden'), 3500);
}

async function api(path, options = {}) {
    const headers = { 'Content-Type': 'application/json', ...options.headers };
    if (session?.token) {
        headers.Authorization = `Bearer ${session.token}`;
    }

    const response = await fetch(`${API}${path}`, { ...options, headers });
    let body = null;

    const text = await response.text();
    if (text) {
        try {
            body = JSON.parse(text);
        } catch {
            body = { message: text };
        }
    }

    if (!response.ok) {
        let message = `Request failed (${response.status})`;
        if (body?.message) {
            message = body.message;
        } else if (body?.errors && typeof body.errors === 'object') {
            message = Object.values(body.errors).filter(Boolean).join(', ') || message;
        }
        throw new Error(message);
    }

    return body;
}

function formatType(type) {
    return type ? type.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase()) : '';
}

function formatDate(iso) {
    if (!iso) return '';
    return new Date(iso).toLocaleString(undefined, {
        dateStyle: 'medium',
        timeStyle: 'short'
    });
}

function splitList(value) {
    return value
        ? value.split(',').map(s => s.trim()).filter(Boolean)
        : [];
}

function updateHomeNav() {
    if (!homeNavActions) return;
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    const icon = isDark ? '☀️' : '🌙';
    if (session?.token && session?.userId) {
        homeNavActions.innerHTML = `
            <button type="button" class="theme-toggle-btn" aria-label="Toggle theme">
                <span class="theme-icon">${icon}</span>
            </button>
            <span style="font-size:0.9rem;font-weight:600;color:var(--text-secondary);">Hi, ${session.firstName}</span>
            <a href="/dashboard" class="btn btn-primary btn-sm" data-route="/dashboard">Dashboard →</a>
        `;
    } else {
        homeNavActions.innerHTML = `
            <button type="button" class="theme-toggle-btn" aria-label="Toggle theme">
                <span class="theme-icon">${icon}</span>
            </button>
            <a href="/login" class="btn btn-outline btn-sm" data-route="/login">Sign in</a>
            <a href="/register" class="btn btn-primary btn-sm" data-route="/register">Register</a>
        `;
    }
}

// Screen rendering
function showHomeScreen() {
    if (homeScreen) homeScreen.classList.remove('hidden');
    if (authScreen) authScreen.classList.add('hidden');
    if (appScreen) appScreen.classList.add('hidden');
    updateHomeNav();
}

function showLoginRegister(isLogin = true) {
    if (homeScreen) homeScreen.classList.add('hidden');
    if (authScreen) authScreen.classList.remove('hidden');
    if (appScreen) appScreen.classList.add('hidden');

    authTabs.classList.remove('hidden');
    socialLogin.classList.remove('hidden');
    socialDivider.classList.remove('hidden');
    otpForm.classList.add('hidden');
    forgotForm.classList.add('hidden');
    resetForm.classList.add('hidden');
    loginForm.classList.toggle('hidden', !isLogin);
    registerForm.classList.toggle('hidden', isLogin);
}

function hideMainAuth() {
    authTabs.classList.add('hidden');
    loginForm.classList.add('hidden');
    registerForm.classList.add('hidden');
    socialLogin.classList.add('hidden');
    socialDivider.classList.add('hidden');
    otpForm.classList.add('hidden');
    forgotForm.classList.add('hidden');
    resetForm.classList.add('hidden');
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function showForgotStep() {
    if (homeScreen) homeScreen.classList.add('hidden');
    if (authScreen) authScreen.classList.remove('hidden');
    if (appScreen) appScreen.classList.add('hidden');

    hideMainAuth();
    forgotForm.classList.remove('hidden');
    const emailInput = forgotForm.querySelector('[name="email"]');
    if (emailInput) {
        setTimeout(() => emailInput.focus(), 50);
    }
}

function showResetStep(info) {
    if (homeScreen) homeScreen.classList.add('hidden');
    if (authScreen) authScreen.classList.remove('hidden');
    if (appScreen) appScreen.classList.add('hidden');

    const activeInfo = info || loadPendingReset() || {};
    pendingReset = activeInfo;
    hideMainAuth();
    if (resetForm) {
        resetForm.classList.remove('hidden');
        const emailInput = resetForm.querySelector('[name="email"]');
        if (emailInput && activeInfo.email) {
            emailInput.value = activeInfo.email;
        }
        const hintElem = document.getElementById('reset-otp-hint');
        if (hintElem) {
            hintElem.textContent = activeInfo.otp
                ? `Verification Code: ${activeInfo.otp}`
                : (activeInfo.email ? `Check ${activeInfo.email} for the password reset code.` : 'Enter your email and the 6-digit reset code.');
        }
        if (activeInfo.otp) {
            const codeInput = resetForm.querySelector('[name="code"]');
            if (codeInput) codeInput.value = activeInfo.otp;
        }
        const codeInput = resetForm.querySelector('[name="code"]');
        if (codeInput) {
            setTimeout(() => codeInput.focus(), 50);
        }
    }
}

function showOtpStep(info) {
    if (homeScreen) homeScreen.classList.add('hidden');
    if (authScreen) authScreen.classList.remove('hidden');
    if (appScreen) appScreen.classList.add('hidden');

    const activeInfo = info || loadPendingVerification() || {};
    pendingVerification = activeInfo;
    hideMainAuth();
    if (otpForm) {
        otpForm.classList.remove('hidden');

        const emailInput = otpForm.querySelector('[name="email"]');
        if (emailInput && activeInfo.email) {
            emailInput.value = activeInfo.email;
        }

        const emailOtpInput = otpForm.querySelector('[name="emailOtp"]');
        if (emailOtpInput) {
            emailOtpInput.value = activeInfo.emailOtp || '';
        }

        const smsInput = otpForm.querySelector('[name="smsOtp"]');
        if (smsInput) {
            smsInput.value = activeInfo.smsOtp || '';
        }

        const hasPhone = Boolean(activeInfo.phoneNumber && activeInfo.phoneNumber.trim());
        const mobileGroup = document.getElementById('mobile-otp-group');
        const resendSmsBtn = document.getElementById('resend-sms-otp');
        const introElem = document.getElementById('otp-intro');

        if (mobileGroup) mobileGroup.classList.toggle('hidden', !hasPhone);
        if (resendSmsBtn) resendSmsBtn.classList.toggle('hidden', !hasPhone);
        if (introElem) {
            introElem.textContent = hasPhone 
                ? 'Enter the 6-digit codes sent to your email and mobile number.'
                : 'Enter the 6-digit verification code sent to your email.';
        }

        const hintElem = document.getElementById('otp-hint');
        if (hintElem) {
            if (activeInfo.email) {
                let content = `<div style="font-size:13px;color:var(--text-secondary);padding:10px 14px;background:var(--bg-alt);border-radius:8px;margin-bottom:12px;border:1px solid var(--border);">
                    ✉️ Verification code sent to: <strong>${escapeHtml(activeInfo.email)}</strong>`;
                if (hasPhone) {
                    content += `<br>📱 SMS code sent to: <strong>${escapeHtml(activeInfo.phoneNumber)}</strong>`;
                }
                content += `</div>`;
                if (activeInfo.emailOtp || (hasPhone && activeInfo.smsOtp)) {
                    content += `<button type="button" id="fill-dev-otp" class="btn btn-outline btn-sm" style="margin-bottom:12px;">⚡ Auto-Fill Code</button>`;
                }
                hintElem.innerHTML = content;
                const fillBtn = document.getElementById('fill-dev-otp');
                if (fillBtn) {
                    fillBtn.addEventListener('click', () => {
                        if (activeInfo.emailOtp && emailOtpInput) emailOtpInput.value = activeInfo.emailOtp;
                        if (hasPhone && activeInfo.smsOtp && smsInput) smsInput.value = activeInfo.smsOtp;
                    });
                }
            } else {
                hintElem.innerHTML = '';
            }
        }

        if (emailOtpInput) {
            setTimeout(() => emailOtpInput.focus(), 50);
        }
    }
}

function showApp() {
    if (homeScreen) homeScreen.classList.add('hidden');
    if (authScreen) authScreen.classList.add('hidden');
    if (appScreen) appScreen.classList.remove('hidden');

    if (userGreeting) {
        userGreeting.textContent = `Hi, ${session.firstName}`;
    }
    setDefaultStartTime();
    refreshData();
}

function setDefaultStartTime() {
    const input = activityForm.querySelector('[name="startTime"]');
    if (!input) return;
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    input.value = now.toISOString().slice(0, 16);
}

function updateStats() {
    const totalMinutes = activities.reduce((sum, a) => sum + (a.durationMinutes || 0), 0);
    const totalCalories = activities.reduce((sum, a) => sum + (a.caloriesBurned || 0), 0);
    const recCount = recommendationsList.querySelectorAll('.recommendation-item').length;

    const elAct = document.getElementById('stat-activities');
    const elMin = document.getElementById('stat-minutes');
    const elCal = document.getElementById('stat-calories');
    const elRec = document.getElementById('stat-recommendations');

    if (elAct) elAct.textContent = activities.length;
    if (elMin) elMin.textContent = totalMinutes;
    if (elCal) elCal.textContent = totalCalories;
    if (elRec) elRec.textContent = recCount;
}

function fillActivitySelect() {
    if (!activitySelect) return;
    if (!activities.length) {
        activitySelect.innerHTML = '<option value="">Save an activity first</option>';
        activitySelect.disabled = true;
        return;
    }

    activitySelect.disabled = false;
    activitySelect.innerHTML = '<option value="">Select a logged activity</option>' +
        activities.map(a =>
            `<option value="${a.id}">${formatType(a.type)} — ${a.durationMinutes} min (${formatDate(a.startTime)})</option>`
        ).join('');
}

function renderActivities() {
    if (!activitiesList) return;
    if (!activities.length) {
        activitiesList.className = 'list-container empty-box';
        activitiesList.textContent = 'No activities recorded yet. Save your first workout above.';
        fillActivitySelect();
        updateStats();
        return;
    }

    activitiesList.className = 'list-container';
    activitiesList.innerHTML = activities.map(a => `
        <article class="activity-item">
            <span class="type-pill">${formatType(a.type)}</span>
            <div class="activity-meta">
                <span><strong>${a.durationMinutes}</strong> mins</span>
                <span><strong>${a.caloriesBurned}</strong> kcal</span>
                <span>${formatDate(a.startTime)}</span>
            </div>
        </article>
    `).join('');

    fillActivitySelect();
    updateStats();
}

function renderRecommendations(items = []) {
    if (!recommendationsList) return;
    if (!items.length) {
        recommendationsList.className = 'list-container empty-box';
        recommendationsList.textContent = 'No recommendations saved yet.';
        updateStats();
        return;
    }

    recommendationsList.className = 'list-container';
    recommendationsList.innerHTML = items.map(r => {
        const improvements = r.improvements || [];
        const suggestions = r.suggestions || [];
        const safety = r.safety || [];

        return `
            <article class="recommendation-item">
                <div class="rec-header-row">
                    <span class="rec-badge">${formatType(r.type || 'RECOMMENDATION')}</span>
                </div>
                <p class="rec-text">${r.recommendation}</p>
                <div class="tag-list">
                    ${improvements.map(tag => `<span class="tag-badge">Improve: ${tag}</span>`).join('')}
                    ${suggestions.map(tag => `<span class="tag-badge">Tip: ${tag}</span>`).join('')}
                    ${safety.map(tag => `<span class="tag-badge">Safety: ${tag}</span>`).join('')}
                </div>
            </article>
        `;
    }).join('');

    updateStats();
}

async function refreshData() {
    if (!session?.userId) return;
    try {
        const [activityData, recommendationData] = await Promise.all([
            api(`/activities/user/${session.userId}`),
            api(`/recommendations/user/${session.userId}`)
        ]);
        activities = Array.isArray(activityData) ? activityData : [];
        renderActivities();
        renderRecommendations(Array.isArray(recommendationData) ? recommendationData : []);
    } catch (err) {
        showToast(err.message, true);
    }
}

// Router & navigation
function navigate(path, replace = false) {
    if (replace) {
        history.replaceState({}, '', path);
    } else if (window.location.pathname !== path) {
        history.pushState({}, '', path);
    }
    renderRoute(path);
}

function renderRoute(path = window.location.pathname) {
    const cleanPath = (path.length > 1 && path.endsWith('/')) ? path.slice(0, -1) : path;

    if (cleanPath === '' || cleanPath === '/' || cleanPath === '/home') {
        showHomeScreen();
        window.scrollTo({ top: 0, behavior: 'smooth' });
        return;
    }

    if (session?.token && session?.userId) {
        if (cleanPath === '/login' || cleanPath === '/register' || cleanPath === '/forgot-password' || cleanPath === '/reset-password' || cleanPath === '/verify-otp') {
            navigate('/dashboard', true);
            return;
        }

        showApp();
        document.querySelectorAll('.nav-item').forEach(link => {
            const route = link.getAttribute('data-route') || link.getAttribute('href');
            link.classList.toggle('active', route === cleanPath || (cleanPath === '/dashboard' && route === '/dashboard'));
        });

        if (cleanPath === '/activities') {
            document.getElementById('activities-section')?.scrollIntoView({ behavior: 'smooth' });
        } else if (cleanPath === '/recommendations') {
            document.getElementById('recommendations-section')?.scrollIntoView({ behavior: 'smooth' });
        } else if (cleanPath === '/dashboard') {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    } else {
        if (cleanPath === '/dashboard' || cleanPath === '/activities' || cleanPath === '/recommendations') {
            navigate('/login', true);
            return;
        }

        if (cleanPath === '/register') {
            document.querySelectorAll('.tab-btn').forEach(t => t.classList.toggle('active', t.dataset.tab === 'register'));
            showLoginRegister(false);
        } else if (cleanPath === '/forgot-password') {
            showForgotStep();
        } else if (cleanPath === '/reset-password') {
            showResetStep(pendingReset || loadPendingReset() || { email: '', otp: '' });
        } else if (cleanPath === '/verify-otp') {
            showOtpStep(pendingVerification || loadPendingVerification() || { email: '', phoneNumber: '', emailOtp: '', smsOtp: '' });
        } else if (cleanPath === '/login') {
            document.querySelectorAll('.tab-btn').forEach(t => t.classList.toggle('active', t.dataset.tab === 'login'));
            showLoginRegister(true);
        } else {
            showHomeScreen();
        }
    }
}

window.addEventListener('popstate', () => {
    session = loadSession();
    updateHomeNav();
    if (!captureOAuthRedirect()) {
        renderRoute(window.location.pathname);
    }
});

window.addEventListener('pageshow', (e) => {
    session = loadSession();
    updateHomeNav();
    if (!captureOAuthRedirect()) {
        renderRoute(window.location.pathname);
    }
});

// Intercept local data-route links
document.addEventListener('click', (e) => {
    const routeLink = e.target.closest('[data-route]');
    if (routeLink) {
        const route = routeLink.getAttribute('data-route');
        if (route && !route.startsWith('http') && !route.includes('swagger')) {
            e.preventDefault();
            navigate(route);
        }
    }
});

// Auth tab switcher
document.querySelectorAll('.tab-btn').forEach(tab => {
    tab.addEventListener('click', () => {
        const isRegister = tab.dataset.tab === 'register';
        navigate(isRegister ? '/register' : '/login');
    });
});

// Form listeners
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    e.stopPropagation();
    const submitBtn = document.getElementById('login-submit');
    const data = Object.fromEntries(new FormData(loginForm));
    try {
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Signing in...';
        }
        const res = await api('/auth/login', {
            method: 'POST',
            body: JSON.stringify(data)
        });
        saveSession({
            token: res.token,
            userId: res.userId,
            firstName: res.firstName,
            lastName: res.lastName,
            email: res.email
        });
        showToast('Welcome back!');
        navigate('/dashboard');
    } catch (err) {
        const msg = err.message || 'Could not sign in';
        showToast(msg, true);
        if (msg.toLowerCase().includes('verify')) {
            savePendingVerification({
                email: (data.email || '').trim(),
                password: data.password || ''
            });
            setTimeout(() => {
                navigate('/verify-otp');
            }, 800);
        }
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Sign In';
        }
    }
});

loginForm.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
        e.preventDefault();
        loginForm.requestSubmit();
    }
});

document.querySelectorAll('.toggle-password').forEach((button) => {
    button.addEventListener('click', () => {
        const input = button.parentElement.querySelector('input');
        const show = input.type === 'password';
        input.type = show ? 'text' : 'password';
        button.textContent = show ? 'Hide' : 'Show';
        button.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
    });
});

document.getElementById('forgot-link').addEventListener('click', () => {
    const email = loginForm.querySelector('[name="email"]').value;
    if (email) forgotForm.querySelector('[name="email"]').value = email;
    navigate('/forgot-password');
});

document.getElementById('back-to-login').addEventListener('click', () => {
    navigate('/login');
});

document.getElementById('back-to-forgot').addEventListener('click', () => {
    navigate('/forgot-password');
});

document.getElementById('reset-back-to-login')?.addEventListener('click', () => {
    navigate('/login');
});

document.getElementById('otp-back-to-login')?.addEventListener('click', () => {
    navigate('/login');
});

forgotForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const submitBtn = forgotForm.querySelector('button[type="submit"]');
    const data = Object.fromEntries(new FormData(forgotForm));
    try {
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Sending OTP...';
        }
        const res = await api('/auth/forgot-password', {
            method: 'POST',
            body: JSON.stringify({ email: data.email.trim() })
        });
        showToast(res.message || 'OTP sent to your email');
        savePendingReset({ email: data.email.trim(), otp: res.otp });
        navigate('/reset-password');
    } catch (err) {
        showToast(err.message || 'Failed to send reset code', true);
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Send Reset Code';
        }
    }
});

resetForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    e.stopPropagation();
    const data = Object.fromEntries(new FormData(resetForm));
    const activeReset = pendingReset || loadPendingReset() || {};
    const email = (data.email || activeReset.email || '').trim();
    const code = (data.code || '').trim();
    const newPassword = data.newPassword;

    if (!email) {
        showToast('Please enter your email address.', true);
        return;
    }
    if (!code) {
        showToast('Please enter the 6-digit verification code.', true);
        return;
    }
    if (!newPassword || newPassword.length < 8) {
        showToast('Password must be at least 8 characters.', true);
        return;
    }

    const submitBtn = resetForm.querySelector('button[type="submit"]');
    try {
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Updating Password...';
        }
        const res = await api('/auth/reset-password', {
            method: 'POST',
            body: JSON.stringify({
                email: email,
                code: code,
                newPassword: newPassword
            })
        });
        showToast(res.message || 'Password updated successfully');
        savePendingReset(null);
        if (loginForm) {
            const loginEmail = loginForm.querySelector('[name="email"]');
            if (loginEmail) loginEmail.value = res.email || email;
        }
        navigate('/login');
    } catch (err) {
        showToast(err.message || 'Failed to update password', true);
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Update Password';
        }
    }
});

async function loadOauthStatus() {
    try {
        oauthStatus = await api('/auth/oauth-status');
    } catch {
        oauthStatus = { google: false, github: false };
    }
    bindSocialButtons();
}

function bindSocialButtons() {
    const google = socialLogin.querySelector('a[href*="google"]');
    const github = socialLogin.querySelector('a[href*="github"]');
    const handleSocial = (link, enabled, name) => {
        if (!link) return;
        link.addEventListener('click', async (e) => {
            if (enabled) return;
            e.preventDefault();
            try {
                showToast(`Signing in with ${name}...`);
                const res = await api(`/auth/demo-login/${name.toLowerCase()}`, { method: 'POST' });
                saveSession({
                    token: res.token,
                    userId: res.userId,
                    firstName: res.firstName,
                    lastName: res.lastName,
                    email: res.email
                });
                showToast(`Signed in with ${name}!`);
                navigate('/dashboard');
            } catch (err) {
                showToast(err.message || `${name} sign-in failed`, true);
            }
        });
    };
    handleSocial(google, oauthStatus.google, 'Google');
    handleSocial(github, oauthStatus.github, 'GitHub');
}

registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    e.stopPropagation();
    const submitBtn = document.getElementById('register-submit') || registerForm.querySelector('button[type="submit"]');
    const data = Object.fromEntries(new FormData(registerForm));

    if (!data.phoneNumber || !data.phoneNumber.trim()) {
        delete data.phoneNumber;
    } else {
        data.phoneNumber = data.phoneNumber.trim();
    }

    try {
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Creating Account...';
        }
        const res = await api('/auth/register', {
            method: 'POST',
            body: JSON.stringify(data)
        });
        const verifData = {
            email: data.email.trim(),
            phoneNumber: data.phoneNumber || '',
            password: data.password,
            emailOtp: res.emailOtp,
            smsOtp: res.smsOtp
        };
        savePendingVerification(verifData);
        showToast(res.message || 'Account created! Please verify your OTP.');
        navigate('/verify-otp');
    } catch (err) {
        showToast(err.message || 'Registration failed. Please check your details.', true);
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Create Account';
        }
    }
});

otpForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    e.stopPropagation();
    const data = Object.fromEntries(new FormData(otpForm));
    const activeVerif = pendingVerification || loadPendingVerification() || {};
    const email = (data.email || activeVerif.email || '').trim();
    const phoneNumber = (activeVerif.phoneNumber || '').trim();
    const emailOtp = (data.emailOtp || '').trim();
    const smsOtp = (data.smsOtp || '').trim();

    if (!email) {
        showToast('Please enter your email address.', true);
        return;
    }
    if (!emailOtp) {
        showToast('Please enter the 6-digit email OTP.', true);
        return;
    }

    const submitBtn = otpForm.querySelector('button[type="submit"]');
    try {
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Verifying...';
        }
        await api('/auth/verify-otp', {
            method: 'POST',
            body: JSON.stringify({
                email: email,
                channel: 'EMAIL',
                code: emailOtp
            })
        });

        if (phoneNumber && smsOtp) {
            try {
                await api('/auth/verify-otp', {
                    method: 'POST',
                    body: JSON.stringify({
                        phoneNumber: phoneNumber,
                        channel: 'SMS',
                        code: smsOtp
                    })
                });
            } catch (smsErr) {
                console.warn('SMS OTP verification notice:', smsErr);
            }
        }

        showToast('Verified! Signing you in...');
        if (activeVerif.password) {
            try {
                const res = await api('/auth/login', {
                    method: 'POST',
                    body: JSON.stringify({
                        email: email,
                        password: activeVerif.password
                    })
                });
                saveSession({
                    token: res.token,
                    userId: res.userId,
                    firstName: res.firstName,
                    lastName: res.lastName,
                    email: res.email
                });
                savePendingVerification(null);
                navigate('/dashboard');
                return;
            } catch (loginErr) {
                // fall through to login
            }
        }
        savePendingVerification(null);
        if (loginForm) {
            const loginEmail = loginForm.querySelector('[name="email"]');
            if (loginEmail) loginEmail.value = email;
        }
        navigate('/login');
    } catch (err) {
        showToast(err.message || 'OTP verification failed', true);
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Verify & Continue';
        }
    }
});

document.getElementById('resend-email-otp')?.addEventListener('click', async () => {
    const activeVerif = pendingVerification || loadPendingVerification() || {};
    const email = (otpForm.querySelector('[name="email"]')?.value || activeVerif.email || '').trim();
    if (!email) {
        showToast('Please enter your email address first.', true);
        return;
    }
    const btn = document.getElementById('resend-email-otp');
    try {
        if (btn) {
            btn.disabled = true;
            btn.textContent = 'Sending...';
        }
        const res = await api('/auth/resend-otp', {
            method: 'POST',
            body: JSON.stringify({ email: email, channel: 'EMAIL' })
        });
        activeVerif.email = email;
        if (res.emailOtp) activeVerif.emailOtp = res.emailOtp;
        savePendingVerification(activeVerif);
        const emailInput = otpForm.querySelector('[name="emailOtp"]');
        if (emailInput && res.emailOtp) emailInput.value = res.emailOtp;
        showOtpStep(activeVerif);
        showToast(res.message || 'Email verification code sent');
    } catch (err) {
        showToast(err.message || 'Failed to resend email code', true);
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.textContent = 'Resend Email OTP';
        }
    }
});

document.getElementById('resend-sms-otp')?.addEventListener('click', async () => {
    const activeVerif = pendingVerification || loadPendingVerification() || {};
    if (!activeVerif || !activeVerif.phoneNumber) {
        showToast('No phone number registered with this session.', true);
        return;
    }
    const btn = document.getElementById('resend-sms-otp');
    try {
        if (btn) {
            btn.disabled = true;
            btn.textContent = 'Sending...';
        }
        const res = await api('/auth/resend-otp', {
            method: 'POST',
            body: JSON.stringify({ phoneNumber: activeVerif.phoneNumber, channel: 'SMS' })
        });
        activeVerif.smsOtp = res.smsOtp;
        savePendingVerification(activeVerif);
        const smsInput = otpForm.querySelector('[name="smsOtp"]');
        if (smsInput && res.smsOtp) smsInput.value = res.smsOtp;
        showOtpStep(activeVerif);
        showToast(res.message || 'SMS verification code sent');
    } catch (err) {
        showToast(err.message || 'Failed to resend SMS code', true);
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.textContent = 'Resend SMS OTP';
        }
    }
});

activityForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(activityForm));
    const payload = {
        type: data.type,
        durationMinutes: Number(data.durationMinutes),
        caloriesBurned: Number(data.caloriesBurned),
        startTime: new Date(data.startTime).toISOString().slice(0, 19)
    };

    try {
        const saved = await api(`/activities/user/${session.userId}`, {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        if (saved?.id) {
            activities = [saved, ...activities.filter(a => a.id !== saved.id)];
            renderActivities();
        }
        showToast('Workout activity saved!');
        activityForm.reset();
        setDefaultStartTime();
        await refreshData();
    } catch (err) {
        showToast(err.message, true);
    }
});

recommendationForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(recommendationForm));
    const payload = {
        activityId: data.activityId,
        type: data.type,
        recommendation: data.recommendation,
        improvements: splitList(data.improvements),
        suggestions: splitList(data.suggestions),
        safety: splitList(data.safety)
    };

    try {
        await api(`/recommendations/user/${session.userId}`, {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        showToast('Recommendation saved!');
        recommendationForm.reset();
        await refreshData();
    } catch (err) {
        showToast(err.message, true);
    }
});

document.getElementById('logout-btn').addEventListener('click', () => {
    clearSession();
    navigate('/login');
    showToast('Signed out');
});

document.getElementById('refresh-btn').addEventListener('click', refreshData);

// Home interactive workout calculator
function initInteractiveCalculator() {
    const activitySelect = document.getElementById('demo-activity');
    const durationInput = document.getElementById('demo-duration');
    const durationBadge = document.getElementById('demo-duration-val');
    const intensityPills = document.querySelectorAll('.intensity-btn');
    const caloriesElem = document.getElementById('demo-calories');
    const metElem = document.getElementById('demo-met');
    const zoneElem = document.getElementById('demo-zone');
    const aiTipElem = document.getElementById('demo-ai-tip');

    if (!activitySelect || !durationInput) return;

    let currentMultiplier = 1.0;

    const tips = {
        RUNNING: {
            light: "🏃 Light Jog: Great for active recovery and building aerobic base. Keep your posture tall.",
            moderate: "🏃 Moderate Run: Excellent stamina builder. Maintain even breathing and hydrate post-run with 300ml fluids.",
            intense: "🔥 High-Intensity Run: Peak metabolic burn! Spend 5-10 minutes stretching hamstrings and calves."
        },
        CYCLING: {
            light: "🚴 Leisure Spin: Great low-impact session to flush lactic acid and protect joints.",
            moderate: "🚴 Steady Cadence: Aim for 80-90 RPM with consistent pedal strokes to maximize endurance.",
            intense: "⚡ Power Intervals: High wattage effort. Focus on hydration and quad recovery stretches."
        },
        WALKING: {
            light: "🚶 Gentle Stroll: Good for digestion, stress relief, and daily step count targets.",
            moderate: "🚶 Brisk Walk: Excellent for cardiovascular baseline health. Maintain an upright posture.",
            intense: "⛰️ Power Walk / Incline: Engages glutes and calves effectively with low joint impact."
        },
        WEIGHT_TRAINING: {
            light: "🏋️ Form & Mobility: Focus on controlled range of motion and joint stability.",
            moderate: "🏋️ Hypertrophy / Strength: Rest 60-90 seconds between working sets for optimal growth.",
            intense: "💥 Heavy Strength / Circuit: Maximum muscle recruitment. Ensure protein intake within 2 hours."
        },
        CROSS_TRAINER: {
            light: "⚡ Easy Glide: Smooth elliptical movement to loosen tight hip flexors.",
            moderate: "⚡ Total Body Burn: Push and pull actively on handles to engage chest, back, and shoulders.",
            intense: "🔥 High-Resistance Sprint: Intense cardio burn with zero knee impact. Cool down for 3 minutes."
        }
    };

    function recalculate() {
        const selectedOption = activitySelect.selectedOptions[0];
        const met = parseFloat(selectedOption?.getAttribute('data-met') || '7.0');
        const duration = parseInt(durationInput.value, 10);
        durationBadge.textContent = `${duration} min`;

        const baseCalories = (met * 3.5 * 70 / 200) * duration * currentMultiplier;
        const totalCalories = Math.round(baseCalories);

        caloriesElem.textContent = totalCalories;
        
        if (currentMultiplier <= 0.85) {
            metElem.textContent = 'Light';
        } else if (currentMultiplier >= 1.2) {
            metElem.textContent = 'High';
        } else {
            metElem.textContent = 'Moderate';
        }

        const effectiveScore = met * currentMultiplier;
        if (effectiveScore < 5) {
            zoneElem.textContent = 'Fat Burn';
        } else if (effectiveScore < 9) {
            zoneElem.textContent = 'Cardio Burn';
        } else {
            zoneElem.textContent = 'Peak Power';
        }

        const actType = activitySelect.value;
        const intensityKey = currentMultiplier <= 0.9 ? 'light' : (currentMultiplier >= 1.2 ? 'intense' : 'moderate');
        if (tips[actType] && tips[actType][intensityKey]) {
            aiTipElem.textContent = tips[actType][intensityKey];
        }
    }

    activitySelect.addEventListener('change', recalculate);
    durationInput.addEventListener('input', recalculate);

    intensityPills.forEach(pill => {
        pill.addEventListener('click', () => {
            intensityPills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            currentMultiplier = parseFloat(pill.getAttribute('data-mult') || '1.0');
            recalculate();
        });
    });

    recalculate();
}

function captureOAuthRedirect() {
    const params = new URLSearchParams(window.location.search);
    if (params.get('oauthError')) {
        const banner = document.getElementById('oauth-error');
        if (banner) {
            banner.textContent = 'Social sign-in was cancelled or failed. You can sign in with your email or try again.';
            banner.classList.remove('hidden');
        }
        history.replaceState({}, '', '/login');
        renderRoute('/login');
        return true;
    }
    const token = params.get('token');
    const userId = params.get('userId');
    if (token && userId) {
        saveSession({
            token,
            userId,
            firstName: params.get('firstName') || 'there',
            lastName: params.get('lastName') || '',
            email: params.get('email')
        });
        showToast('Signed in with social account');
        navigate('/dashboard', true);
        return true;
    }
    return false;
}

// App bootstrap
initTheme();
initInteractiveCalculator();
loadOauthStatus();
if (!captureOAuthRedirect()) {
    renderRoute(window.location.pathname);
}
