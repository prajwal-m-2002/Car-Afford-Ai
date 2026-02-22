/**
 * CarAfford AI – Premium Frontend Engine (Stable V2)
 * Architecture: SPA, Glass UI, Smooth Routing
 */

'use strict';

const EMI_SAFETY_RATIO = 0.40;
const ENDPOINTS = {
    register: '/api/auth/register',
    login: '/api/auth/login',
    finance: '/api/finance/submit',
    recommend: '/api/cars/recommend',
};

const AppState = {
    userId: null,
    userName: null,
    monthlyIncome: 0,
    reportData: null,
    reset() { this.userId = null; this.userName = null; this.मासिकIncome = 0; this.reportData = null; }
};

// ─── UI Engine ───────────────────────────────────────────
const UI = {
    formatINR: (val) => '₹' + Math.round(val || 0).toLocaleString('en-IN'),

    toast(msg, type = 'info') {
        const container = document.getElementById('toastContainer');
        if (!container) return;
        const t = document.createElement('div');
        t.className = `toast toast-${type}`;

        let icon = 'ℹ️';
        if (type === 'success') icon = '✅';
        if (type === 'error') icon = '🚨';

        t.innerHTML = `<span>${icon}</span> <span>${msg}</span>`;
        container.appendChild(t);
        setTimeout(() => { t.style.opacity = '0'; t.style.transform = 'translateX(100%)'; setTimeout(() => t.remove(), 400); }, 4000);
    },

    setLoading(btn, loading, text = 'Processing...') {
        if (!btn) return;
        if (loading) {
            btn.dataset.original = btn.innerHTML;
            btn.innerHTML = `<span style="opacity:0.7">${text}</span>`;
            btn.disabled = true;
        } else {
            btn.innerHTML = btn.dataset.original || btn.textContent;
            btn.disabled = false;
        }
    }
};

// ─── Router ──────────────────────────────────────────────
function navigateTo(pageId) {
    console.log(`[Router] Navigation -> ${pageId}`);

    // Auth Guard
    const protectedPages = ['finances', 'results', 'stress-score', 'recommendations', 'loading'];
    if (protectedPages.includes(pageId) && !AppState.userId) {
        pageId = 'auth';
        UI.toast('Please authenticate to access the engine.', 'info');
    }

    // Update DOM pages smoothly
    const pages = document.querySelectorAll('.page');
    pages.forEach(p => {
        if (p.id === `page-${pageId}`) {
            p.classList.add('active');
        } else {
            p.classList.remove('active');
        }
    });

    // Nav UI adjustments
    const mainNav = document.getElementById('mainNav');
    const startBtn = document.getElementById('nav-start-btn');
    const userChip = document.getElementById('userChip');

    if (AppState.userId) {
        mainNav?.classList.remove('hidden');
        startBtn?.classList.add('hidden');
        userChip?.classList.remove('hidden');
        document.getElementById('userChipName').textContent = AppState.userName.split(' ')[0];
    } else {
        mainNav?.classList.add('hidden');
        startBtn?.classList.remove('hidden');
        userChip?.classList.add('hidden');
    }

    document.querySelectorAll('.nav-btn').forEach(btn => btn.classList.toggle('active', btn.id === `nav-${pageId}`));

    if (pageId !== 'loading') window.scrollTo({ top: 0, behavior: 'smooth' });

    // Page Specific Hooks
    if (pageId === 'finances') updateLiveEmi();
}

// ─── API Helper ──────────────────────────────────────────
async function apiCall(endpoint, method = 'GET', body = null) {
    try {
        const options = {
            method,
            headers: { 'Content-Type': 'application/json' }
        };
        if (body) options.body = JSON.stringify(body);

        const response = await fetch(endpoint, options);
        let data = {};
        const text = await response.text();
        if (text) {
            try { data = JSON.parse(text); } catch (e) { console.error("JSON Error", text); }
        }

        if (!response.ok) {
            console.error(`API Error ${response.status}:`, data);
            if (data.fieldErrors) return { error: Object.values(data.fieldErrors)[0] };
            return { error: data.message || 'Server anomaly detected.' };
        }
        return { data, error: null };
    } catch (err) {
        return { error: 'Neural network connection severed.' };
    }
}

// ─── Core Logic ──────────────────────────────────────────
function updateLiveEmi() {
    const expenses = parseFloat(document.getElementById('fixedExpenses')?.value) || 0;
    const existing = parseFloat(document.getElementById('existingEmi')?.value) || 0;
    const safeEmi = Math.max(0, (AppState.monthlyIncome - expenses) * EMI_SAFETY_RATIO - existing);
    const el = document.getElementById('liveEmiValue');
    if (el) el.textContent = UI.formatINR(safeEmi);
}

function handleAuth(user) {
    AppState.userId = user.id;
    AppState.userName = user.name;
    AppState.monthlyIncome = user.monthlyIncome;
    UI.toast(`Biometrics confirmed. Welcome ${user.name.split(' ')[0]}.`, 'success');
    navigateTo('finances');
}

function renderResults(report) {
    navigateTo('results');

    const isBuy = report.verdict === 'BUY';
    const banner = document.getElementById('verdictBanner');
    if (banner) banner.className = `verdict-banner ${isBuy ? 'buy' : 'avoid'}`;

    document.getElementById('verdictIcon').innerHTML = isBuy ? '✅' : '⛔';
    document.getElementById('verdictTitle').textContent = isBuy ? 'Parameters Greenlit' : 'Acquisition Denied';
    document.getElementById('adviceText').textContent = report.upgradeAdvice;

    document.getElementById('safeEmiDisplay').textContent = UI.formatINR(report.safeEmi);
    document.getElementById('maxCarDisplay').textContent = UI.formatINR(report.maxCarPrice);
    document.getElementById('savingsDisplay').textContent = UI.formatINR(report.monthlySavings);
    document.getElementById('tenureDisplay').textContent = `${report.tenureYears} Yrs`;

    // Stress Engine
    const score = report.stressScore;
    document.getElementById('stressValue').textContent = score;
    const badge = document.getElementById('stressLevelBadge');
    badge.textContent = report.stressLevel.toUpperCase();

    let stressColor = 'var(--color-success)';
    if (score >= 30) stressColor = 'var(--color-warning)';
    if (score >= 60) stressColor = 'var(--color-danger)';

    badge.style.color = stressColor;

    const fg = document.getElementById('stressMeterFg');
    if (fg) {
        const offset = 283 - (283 * Math.min(score, 100)) / 100;
        fg.style.strokeDashoffset = offset;
        fg.style.stroke = stressColor;
    }
    document.getElementById('stressExplanation').textContent = report.stressExplanation;

    const totalEmi = (report.existingEmi || 0) + (report.safeEmi || 0);
    const emiRatio = Math.min(100, (totalEmi / AppState.monthlyIncome) * 150);

    const fBar1 = document.getElementById('factorBar1');
    if (fBar1) {
        fBar1.style.width = `${emiRatio}%`;
        fBar1.style.background = emiRatio > 60 ? 'var(--color-danger)' : 'var(--brand-gradient)';
    }
    document.getElementById('factorVal1').textContent = `${Math.round(emiRatio)}%`;

    renderCars(report.recommendedCars);
}

function renderCars(cars) {
    const grid = document.getElementById('carCardsGrid');
    if (!grid) return;
    grid.innerHTML = '';

    if (!cars || cars.length === 0) {
        document.getElementById('noCarsMsg')?.classList.remove('hidden');
        return;
    }
    document.getElementById('noCarsMsg')?.classList.add('hidden');

    cars.forEach(car => {
        const div = document.createElement('div');
        div.className = 'car-card';

        div.onclick = () => {
            const detail = document.getElementById('carDetailContent');
            detail.innerHTML = `
                <div class="detail-layout">
                    <div class="detail-content" style="grid-column: 1 / -1; max-width: 800px; margin: 0 auto; text-align: center;">
                        <span class="brand brand-font">${car.brand}</span>
                        <h1 class="brand-font">${car.model}</h1>
                        <p style="font-size:1.2rem; color:var(--text-secondary)">${car.variant || 'Standard Edition'}</p>
                        
                        <div class="detail-price">${UI.formatINR(car.price)}</div>
                        
                        <div class="car-stats-big">
                            <div class="s-card"><span>Monthly Amortization</span><strong>${UI.formatINR(car.monthlyEmi)}</strong></div>
                            <div class="s-card"><span>Mileage</span><strong>${car.mileage} <small>km/l</small></strong></div>
                            <div class="s-card"><span>Fuel Core</span><strong style="font-size:1.2rem">${car.fuelType}</strong></div>
                        </div>

                        <div class="detail-insight" style="text-align: left;">
                            <h3 class="brand-font"><i>✨</i> AI Recommendation Synopsis</h3>
                            <p>${car.whyRecommended}</p>
                        </div>
                    </div>
                </div>
            `;
            navigateTo('car-detail');
        };

        div.innerHTML = `
            <div class="car-info">
                <div class="car-badge" style="position: relative; top: 0; right: 0; display: inline-block; margin-bottom: 1rem;">100% Match</div>
                <div class="c-brand">${car.brand}</div>
                <div class="c-model">${car.model}</div>
                
                <div class="c-stats">
                    <div class="c-stat-box">
                        <span class="c-stat-lbl">Price</span>
                        <span class="c-stat-val highlight">${UI.formatINR(car.price)}</span>
                    </div>
                    <div class="c-stat-box">
                        <span class="c-stat-lbl">Safe EMI</span>
                        <span class="c-stat-val">${UI.formatINR(car.monthlyEmi)}</span>
                    </div>
                </div>

                <div class="c-why">${car.whyRecommended}</div>
            </div>
        `;
        grid.appendChild(div);
    });
}

// ─── Initialization ──────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    console.log('[Init] CarAfford AI Engine Online');

    // Routing Binding
    document.getElementById('logo-home').onclick = () => AppState.userId ? navigateTo('finances') : navigateTo('landing');
    document.getElementById('nav-finances').onclick = () => navigateTo('finances');
    document.getElementById('nav-results').onclick = () => navigateTo('results');
    document.getElementById('nav-stress').onclick = () => navigateTo('stress-score');
    document.getElementById('nav-recommendations').onclick = () => navigateTo('recommendations');

    document.getElementById('nav-start-btn').onclick = () => navigateTo('auth');
    document.getElementById('landing-start-btn').onclick = () => AppState.userId ? navigateTo('finances') : navigateTo('auth');
    document.getElementById('landing-features-btn').onclick = () => document.getElementById('features').scrollIntoView({ behavior: 'smooth' });

    document.getElementById('btn-logout').onclick = () => { AppState.reset(); navigateTo('landing'); UI.toast('Engine disengaged.', 'info'); };
    document.getElementById('btn-back-to-cars').onclick = () => navigateTo('recommendations');

    // Auth Tabs
    const tabIndicator = document.getElementById('auth-tab-indicator');
    document.getElementById('btn-login-tab').onclick = () => {
        document.getElementById('loginForm').classList.remove('hidden');
        document.getElementById('registerForm').classList.add('hidden');
        document.getElementById('btn-login-tab').classList.add('active');
        document.getElementById('btn-reg-tab').classList.remove('active');
        if (tabIndicator) tabIndicator.style.transform = 'translateX(0)';
    };
    document.getElementById('btn-reg-tab').onclick = () => {
        document.getElementById('registerForm').classList.remove('hidden');
        document.getElementById('loginForm').classList.add('hidden');
        document.getElementById('btn-reg-tab').classList.add('active');
        document.getElementById('btn-login-tab').classList.remove('active');
        if (tabIndicator) tabIndicator.style.transform = 'translateX(100%)';
    };

    // Form Handlers
    document.getElementById('loginForm').onsubmit = async (e) => {
        e.preventDefault();
        const email = document.getElementById('loginEmail').value;
        const password = document.getElementById('loginPassword').value;
        const btn = e.target.querySelector('button');

        UI.setLoading(btn, true, 'Authenticating...');
        const { data, error } = await apiCall(ENDPOINTS.login, 'POST', { email, password });
        UI.setLoading(btn, false);

        if (error) return UI.toast(error, 'error');
        handleAuth(data);
    };

    document.getElementById('registerForm').onsubmit = async (e) => {
        e.preventDefault();
        const name = document.getElementById('regName').value;
        const email = document.getElementById('regEmail').value;
        const monthlyIncome = parseFloat(document.getElementById('regIncome').value);
        const password = document.getElementById('regPassword').value;
        const confirm = document.getElementById('regConfirmPassword').value;
        const btn = e.target.querySelector('button');

        if (password !== confirm) return UI.toast('Security keys do not match.', 'error');

        UI.setLoading(btn, true, 'Binding Profile...');
        const { data, error } = await apiCall(ENDPOINTS.register, 'POST', { name, email, monthlyIncome, password });
        UI.setLoading(btn, false);

        if (error) return UI.toast(error, 'error');
        handleAuth(data);
    };

    // Finance Flow
    document.getElementById('financeForm').onsubmit = async (e) => {
        e.preventDefault();
        const fixedExpenses = parseFloat(document.getElementById('fixedExpenses').value) || 0;
        const existingEmi = parseFloat(document.getElementById('existingEmi').value) || 0;
        const downPayment = parseFloat(document.getElementById('downPayment').value) || 0;
        const tenure = parseInt(document.querySelector('input[name="tenure"]:checked').value);

        navigateTo('loading');

        const { error: fErr } = await apiCall(ENDPOINTS.finance, 'POST', {
            userId: AppState.userId, fixedExpenses, existingEmi, downPayment, preferredTenureYears: tenure
        });
        if (fErr) { UI.toast(fErr, 'error'); return navigateTo('finances'); }

        const { data: report, error: rErr } = await apiCall(ENDPOINTS.recommend + '?userId=' + AppState.userId);
        if (rErr) { UI.toast(rErr, 'error'); return navigateTo('finances'); }

        AppState.reportData = report;

        // Let the loading screen render a bit
        setTimeout(() => renderResults(report), 1500);
    };

    // Live Listeners
    ['fixedExpenses', 'existingEmi'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener('input', updateLiveEmi);
    });

    navigateTo('landing');
});
