/* ═══════════════════════════════════════════════════════════════
   IronPulse Gym — Frontend Application Logic
   Talks to GymServer (Java) via fetch() calls to /api/*
═══════════════════════════════════════════════════════════════ */

const API = '';   // same origin — GymServer serves both frontend + API

let currentUser = null;

/* ── Page Boot ─────────────────────────────────────────────── */
window.addEventListener('DOMContentLoaded', () => {
  const saved = sessionStorage.getItem('gymUser');
  if (saved) {
    currentUser = JSON.parse(saved);
    onLoggedIn();
  }

  // Set today's date as default in the slot date picker
  const today = new Date().toISOString().split('T')[0];
  const slotDateEl = document.getElementById('slotDate');
  if (slotDateEl) slotDateEl.value = today;

  const newSlotDateEl = document.getElementById('newSlotDate');
  if (newSlotDateEl) newSlotDateEl.value = today;
});

/* ── Auth Tab Toggle ────────────────────────────────────────── */
function switchTab(tab) {
  document.getElementById('loginForm').classList.toggle('hidden', tab !== 'login');
  document.getElementById('registerForm').classList.toggle('hidden', tab !== 'register');
  document.querySelectorAll('.tab-switch .tab').forEach((btn, i) => {
    btn.classList.toggle('active', (tab === 'login' && i === 0) || (tab === 'register' && i === 1));
  });
}

/* ── Register ──────────────────────────────────────────────── */
async function register() {
  const name     = v('regName');
  const email    = v('regEmail');
  const phone    = v('regPhone');
  const password = v('regPassword');
  const msg      = document.getElementById('regMsg');

  if (!name || !email || !phone || !password) {
    setMsg(msg, 'All fields are required.', false); return;
  }

  const res  = await post('/api/register', { name, email, phone, password });
  const data = await res.json();

  if (data.success) {
    setMsg(msg, '✓ Account created! Please log in.', true);
    setTimeout(() => switchTab('login'), 1500);
  } else {
    setMsg(msg, data.message, false);
  }
}

/* ── Login ─────────────────────────────────────────────────── */
async function login() {
  const email    = v('loginEmail');
  const password = v('loginPassword');
  const msg      = document.getElementById('loginMsg');

  if (!email || !password) {
    setMsg(msg, 'Enter email and password.', false); return;
  }

  const res  = await post('/api/login', { email, password });
  const data = await res.json();

  if (data.success) {
    currentUser = data.user;
    sessionStorage.setItem('gymUser', JSON.stringify(currentUser));
    onLoggedIn();
  } else {
    setMsg(msg, data.message, false);
  }
}

/* ── Logout ────────────────────────────────────────────────── */
function logout() {
  currentUser = null;
  sessionStorage.removeItem('gymUser');
  document.getElementById('navbar').classList.add('hidden');
  showOnlyPage('authPage');
}

/* ── After Successful Login ─────────────────────────────────── */
function onLoggedIn() {
  document.getElementById('navbar').classList.remove('hidden');
  document.getElementById('userLabel').textContent = currentUser.name;

  if (currentUser.role === 'admin') {
    document.getElementById('adminNav').classList.remove('hidden');
  }

  showPage('dashboard');
}

/* ── Page Routing ───────────────────────────────────────────── */
const PAGES = {
  dashboard:   { id: 'dashboardPage',   load: loadDashboard  },
  bookSlot:    { id: 'bookSlotPage',    load: loadSlots       },
  myBookings:  { id: 'myBookingsPage',  load: loadMyBookings  },
  adminPanel:  { id: 'adminPanelPage',  load: loadAdmin       },
};

function showPage(name) {
  const page = PAGES[name];
  if (!page) return;
  showOnlyPage(page.id);
  page.load();
}

function showOnlyPage(activeId) {
  Object.values(PAGES).forEach(p => {
    document.getElementById(p.id)?.classList.add('hidden');
  });
  document.getElementById('authPage')?.classList.add('hidden');
  document.getElementById(activeId)?.classList.remove('hidden');
}

/* ── Dashboard ─────────────────────────────────────────────── */
async function loadDashboard() {
  document.getElementById('dashName').textContent = currentUser.name;

  const res  = await fetch(`${API}/api/bookings?userId=${currentUser.id}`);
  const list = await res.json();

  const total     = list.length;
  const confirmed = list.filter(b => b.status === 'confirmed').length;
  const cancelled = list.filter(b => b.status === 'cancelled').length;

  document.getElementById('statTotal').textContent     = total;
  document.getElementById('statConfirmed').textContent = confirmed;
  document.getElementById('statCancelled').textContent = cancelled;

  const upcoming = list.filter(b => b.status === 'confirmed').slice(0, 5);
  const el = document.getElementById('upcomingList');
  el.innerHTML = upcoming.length
    ? upcoming.map(b => bookingCard(b, false)).join('')
    : '<div class="empty-state">No upcoming sessions. Book a slot to get started!</div>';
}

/* ── Slots ─────────────────────────────────────────────────── */
async function loadSlots() {
  const date = document.getElementById('slotDate').value ||
               new Date().toISOString().split('T')[0];
  const res   = await fetch(`${API}/api/slots?date=${date}`);
  const slots = await res.json();

  const grid = document.getElementById('slotsGrid');
  if (!slots.length) {
    grid.innerHTML = '<div class="empty-state">No slots available for this date.</div>';
    return;
  }
  grid.innerHTML = slots.map(s => slotCard(s)).join('');
}

function slotCard(s) {
  const pct     = s.maxCapacity > 0 ? (s.currentBookings / s.maxCapacity) * 100 : 0;
  const fillCls = pct >= 100 ? 'full' : pct >= 75 ? 'warn' : '';
  const badge   = s.isFull
    ? '<span class="slot-badge badge-full">Full</span>'
    : '<span class="slot-badge badge-available">Available</span>';

  return `
  <div class="slot-card${s.isFull ? ' full' : ''}">
    <div>
      <div class="slot-name">${esc(s.slotName)}</div>
      <div class="slot-time">${s.startTime} – ${s.endTime}</div>
    </div>
    ${badge}
    <div class="slot-capacity">
      <div class="capacity-bar"><div class="capacity-fill ${fillCls}" style="width:${Math.min(pct,100)}%"></div></div>
      <span class="capacity-text">${s.currentBookings}/${s.maxCapacity}</span>
    </div>
    <button class="btn-book" onclick="bookSlot(${s.id})" ${s.isFull ? 'disabled' : ''}>
      ${s.isFull ? 'Fully Booked' : 'Reserve Spot'}
    </button>
  </div>`;
}

async function bookSlot(slotId) {
  const res  = await post('/api/book', { userId: currentUser.id, slotId });
  const data = await res.json();
  toast(data.message, data.success);
  if (data.success) loadSlots();
}

/* ── My Bookings ────────────────────────────────────────────── */
async function loadMyBookings() {
  const res  = await fetch(`${API}/api/bookings?userId=${currentUser.id}`);
  const list = await res.json();
  const el   = document.getElementById('myBookingsList');
  el.innerHTML = list.length
    ? list.map(b => bookingCard(b, true)).join('')
    : '<div class="empty-state">No bookings yet.</div>';
}

function bookingCard(b, showCancel) {
  const cancelBtn = showCancel && b.status === 'confirmed'
    ? `<button class="btn-danger" onclick="cancelBooking(${b.id})">Cancel</button>`
    : '';
  return `
  <div class="booking-card">
    <div class="booking-info">
      <div class="booking-slot">${esc(b.slotName)}</div>
      <div class="booking-meta">${b.slotDate} · ${b.startTime} – ${b.endTime}</div>
    </div>
    <span class="status-badge status-${b.status}">${b.status}</span>
    ${cancelBtn}
  </div>`;
}

async function cancelBooking(bookingId) {
  if (!confirm('Cancel this booking?')) return;
  const res  = await post('/api/cancel', { bookingId });
  const data = await res.json();
  toast(data.message, data.success);
  if (data.success) { loadMyBookings(); loadDashboard(); }
}

/* ── Admin Panel ────────────────────────────────────────────── */
let currentAdminTab = 'members';

function adminTab(tab) {
  currentAdminTab = tab;
  document.getElementById('membersSection').classList.toggle('hidden',    tab !== 'members');
  document.getElementById('slotsSection').classList.toggle('hidden',      tab !== 'slots');
  document.getElementById('allBookingsSection').classList.toggle('hidden', tab !== 'allBookings');

  document.querySelectorAll('.admin-tabs .tab').forEach((btn, i) => {
    const names = ['members','slots','allBookings'];
    btn.classList.toggle('active', names[i] === tab);
  });

  if (tab === 'members')     loadAdminMembers();
  if (tab === 'slots')       loadAdminSlots();
  if (tab === 'allBookings') loadAdminBookings();
}

function loadAdmin() { adminTab('members'); }

async function loadAdminMembers() {
  const res   = await fetch(`${API}/api/users`);
  const users = await res.json();
  const tbody = document.querySelector('#membersTable tbody');
  tbody.innerHTML = users.map(u => `
    <tr>
      <td>${u.id}</td>
      <td>${esc(u.name)}</td>
      <td>${esc(u.email)}</td>
      <td>${esc(u.phone)}</td>
      <td><span class="status-badge ${u.role==='admin'?'status-confirmed':'status-cancelled'}">${u.role}</span></td>
      <td>${u.createdAt ? u.createdAt.split(' ')[0] : '—'}</td>
      <td>${u.role !== 'admin' ? `<button class="btn-danger" onclick="deleteUser(${u.id})">Delete</button>` : '—'}</td>
    </tr>`).join('');
}

async function deleteUser(id) {
  if (!confirm('Delete this user and all their bookings?')) return;
  const res  = await fetch(`${API}/api/users?id=${id}`, { method: 'DELETE' });
  const data = await res.json();
  toast(data.message, data.success);
  if (data.success) loadAdminMembers();
}

async function loadAdminSlots() {
  const res   = await fetch(`${API}/api/slots/all`);
  const slots = await res.json();
  const tbody = document.querySelector('#slotsTable tbody');
  tbody.innerHTML = slots.map(s => `
    <tr>
      <td>${s.id}</td>
      <td>${esc(s.slotName)}</td>
      <td>${s.slotDate}</td>
      <td>${s.startTime} – ${s.endTime}</td>
      <td>${s.currentBookings}/${s.maxCapacity}</td>
      <td><span class="slot-badge ${s.isFull?'badge-full':'badge-available'}">${s.isFull?'Full':'Open'}</span></td>
      <td><button class="btn-danger" onclick="deleteSlot(${s.id})">Delete</button></td>
    </tr>`).join('');
}

async function addSlot() {
  const slotName    = v('newSlotName');
  const slotDate    = v('newSlotDate');
  const startTime   = v('newSlotStart');
  const endTime     = v('newSlotEnd');
  const maxCapacity = v('newSlotCap');
  const msg         = document.getElementById('slotMsg');

  if (!slotName || !slotDate || !startTime || !endTime || !maxCapacity) {
    setMsg(msg, 'All slot fields are required.', false); return;
  }

  const res  = await post('/api/slots', { slotName, slotDate, startTime, endTime, maxCapacity });
  const data = await res.json();
  setMsg(msg, data.message, data.success);
  if (data.success) loadAdminSlots();
}

async function deleteSlot(id) {
  if (!confirm('Delete this slot and all its bookings?')) return;
  // We'll handle via a query param workaround using POST
  const res  = await fetch(`${API}/api/slots?id=${id}`, { method: 'DELETE' });
  const data = await res.json().catch(() => ({ success: false, message: 'Error' }));
  toast(data.message ?? 'Slot deleted.', data.success ?? true);
  loadAdminSlots();
}

async function loadAdminBookings() {
  const res      = await fetch(`${API}/api/bookings/all`);
  const bookings = await res.json();
  const tbody    = document.querySelector('#allBookingsTable tbody');
  tbody.innerHTML = bookings.map(b => `
    <tr>
      <td>${b.id}</td>
      <td>${esc(b.userName)}</td>
      <td>${esc(b.slotName)}</td>
      <td>${b.slotDate}</td>
      <td>${b.startTime} – ${b.endTime}</td>
      <td><span class="status-badge status-${b.status}">${b.status}</span></td>
      <td>${b.bookedAt ? b.bookedAt.split('.')[0] : '—'}</td>
      <td>${b.status === 'confirmed'
           ? `<button class="btn-danger" onclick="adminCancelBooking(${b.id})">Cancel</button>`
           : '—'}</td>
    </tr>`).join('');
}

async function adminCancelBooking(bookingId) {
  if (!confirm('Cancel this booking?')) return;
  const res  = await post('/api/cancel', { bookingId });
  const data = await res.json();
  toast(data.message, data.success);
  if (data.success) loadAdminBookings();
}

/* ── Helpers ────────────────────────────────────────────────── */

/** POST with url-encoded body */
async function post(url, data) {
  return fetch(API + url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams(data).toString()
  });
}

/** Get trimmed input value by id */
function v(id) { return document.getElementById(id)?.value.trim() ?? ''; }

/** Escape HTML to prevent XSS */
function esc(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g,'&amp;')
    .replace(/</g,'&lt;')
    .replace(/>/g,'&gt;')
    .replace(/"/g,'&quot;');
}

/** Set an inline message element */
function setMsg(el, text, ok) {
  el.textContent = text;
  el.className = `msg ${ok ? 'success' : 'error'}`;
}

/** Show a floating toast notification */
function toast(message, ok) {
  const t = document.getElementById('toast');
  t.textContent = message;
  t.className = `toast show ${ok ? 'ok' : 'fail'}`;
  setTimeout(() => { t.className = 'toast'; }, 3500);
}
