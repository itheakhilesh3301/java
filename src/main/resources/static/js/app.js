const authSection = document.getElementById('auth-section');
const dashboardSection = document.getElementById('dashboard-section');
const adminDashboardSection = document.getElementById('admin-dashboard-section');
const loginForm = document.getElementById('login-form');
const authError = document.getElementById('auth-error');
const roleToggle = document.getElementById('role-toggle');
const labelUser = document.getElementById('label-user');
const labelAdmin = document.getElementById('label-admin');
const loginTitle = document.getElementById('login-title');
const loginSubtitle = document.getElementById('login-subtitle');

const journalGrid = document.getElementById('journal-grid');
const searchInput = document.getElementById('search-input');
const btnNewEntry = document.getElementById('btn-new-entry');
const btnLogout = document.getElementById('btn-logout');
const btnAdminLogout = document.getElementById('btn-admin-logout');

const adminUsersList = document.getElementById('admin-users-list');
const adminAuditLogs = document.getElementById('admin-audit-logs');

const modalOverlay = document.getElementById('modal-overlay');
const entryForm = document.getElementById('entry-form');
const btnCancel = document.getElementById('btn-cancel');
const entryError = document.getElementById('entry-error');

const btnPrev = document.getElementById('btn-prev');
const btnNext = document.getElementById('btn-next');
const pageInfo = document.getElementById('page-info');

const btnProfile = document.getElementById('btn-profile');
const profileModalOverlay = document.getElementById('profile-modal-overlay');
const profileForm = document.getElementById('profile-form');
const btnProfileCancel = document.getElementById('btn-profile-cancel');
const profileError = document.getElementById('profile-error');
const profileSuccess = document.getElementById('profile-success');

// State
let credentials = null;
let currentUser = null;
let currentPage = 0;
let currentKeyword = '';
const pageSize = 12;

// API Base
const API_BASE = 'http://localhost:8080';

// Role Toggle Logic
roleToggle.addEventListener('change', (e) => {
    if (e.target.checked) {
        labelAdmin.classList.add('active');
        labelUser.classList.remove('active');
        loginTitle.textContent = 'Admin Portal';
        loginSubtitle.textContent = 'Log in to manage the system.';
    } else {
        labelUser.classList.add('active');
        labelAdmin.classList.remove('active');
        loginTitle.textContent = 'Welcome Back';
        loginSubtitle.textContent = 'Log in to access your 3D Journal.';
    }
});

// Event Listeners
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const user = document.getElementById('username').value;
    const pass = document.getElementById('password').value;
    
    // Test auth by fetching page 0
    credentials = btoa(`${user}:${pass}`);
    try {
        const meRes = await fetch(`${API_BASE}/user/me`, {
            headers: { 'Authorization': `Basic ${credentials}` }
        });
        if (!meRes.ok) throw new Error('Invalid login');
        
        const me = await meRes.json();

        // Enforce toggle constraints BEFORE hiding auth section
        if (!(me.roles && me.roles.includes('ADMIN')) && roleToggle.checked) {
            throw new Error('You do not have Admin privileges.');
        }
        
        currentUser = me;
        
        authSection.classList.remove('active');
        document.getElementById('username').value = '';
        document.getElementById('password').value = '';
        authError.textContent = '';

        if (me.roles && me.roles.includes('ADMIN') && roleToggle.checked) {
            adminDashboardSection.classList.add('active');
            fetchAdminData();
        } else {
            dashboardSection.classList.add('active');
            fetchJournals(0);
        }
    } catch (err) {
        authSection.classList.add('active');
        authError.textContent = err.message === 'Invalid login' ? 'Invalid username or password' : err.message;
        credentials = null;
    }
});

btnLogout.addEventListener('click', logout);
btnAdminLogout.addEventListener('click', logout);

function logout() {
    credentials = null;
    dashboardSection.classList.remove('active');
    adminDashboardSection.classList.remove('active');
    authSection.classList.add('active');
}

// Search with debounce
let searchTimeout;
searchInput.addEventListener('input', (e) => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        currentKeyword = e.target.value;
        fetchJournals(0); // Reset to page 0 on new search
    }, 500);
});

// Modal Toggles
btnNewEntry.addEventListener('click', () => {
    modalOverlay.classList.add('active');
    entryForm.reset();
    document.getElementById('entry-id').value = '';
    entryError.textContent = '';
});

btnCancel.addEventListener('click', () => {
    modalOverlay.classList.remove('active');
});

// Pagination
btnPrev.addEventListener('click', () => {
    if (currentPage > 0) fetchJournals(currentPage - 1);
});
btnNext.addEventListener('click', () => {
    fetchJournals(currentPage + 1);
});

// Profile Delete Logic
btnProfile.addEventListener('click', () => {
    profileModalOverlay.classList.add('active');
    profileError.textContent = '';
    profileSuccess.textContent = '';
});

btnProfileCancel.addEventListener('click', () => {
    profileModalOverlay.classList.remove('active');
});

profileForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    try {
        const response = await fetch(`${API_BASE}/user/${currentUser.id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Basic ${credentials}`
            }
        });

        if (!response.ok) {
            let errorMsg = 'Failed to delete account';
            try {
                const data = await response.json();
                errorMsg = Object.values(data).join(' | ');
            } catch (err) {
                errorMsg = await response.text() || 'Failed to delete account';
            }
            throw new Error(errorMsg);
        }

        profileSuccess.textContent = 'Account deleted successfully...';
        profileError.textContent = '';
        
        setTimeout(() => {
            profileModalOverlay.classList.remove('active');
            logout(); // Log them out immediately
        }, 1500);

    } catch (err) {
        profileError.textContent = err.message;
        profileSuccess.textContent = '';
    }
});

// Create Entry
entryForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const title = document.getElementById('entry-title').value;
    const content = document.getElementById('entry-content').value;
    const tagsInput = document.getElementById('entry-tags').value;
    
    const tags = tagsInput.split(',')
        .map(t => t.trim())
        .filter(t => t.length > 0)
        .map(t => ({ name: t }));

    const payload = { title, content, tags };

    try {
        const response = await fetch(`${API_BASE}/journal`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Basic ${credentials}`
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            let errorMsg = 'Failed to save';
            try {
                const data = await response.json();
                errorMsg = Object.values(data).join(' | ');
            } catch (err) {
                errorMsg = await response.text() || 'Failed to save';
            }
            throw new Error(errorMsg);
        }

        modalOverlay.classList.remove('active');
        fetchJournals(0); // Refresh grid
    } catch (err) {
        entryError.textContent = err.message;
    }
});

// API Calls
async function fetchJournals(page) {
    let url = `${API_BASE}/journal?page=${page}&size=${pageSize}`;
    if (currentKeyword) {
        url += `&keyword=${encodeURIComponent(currentKeyword)}`;
    }

    const response = await fetch(url, {
        headers: {
            'Authorization': `Basic ${credentials}`
        }
    });

    if (!response.ok) {
        throw new Error('Unauthorized');
    }

    const data = await response.json();
    currentPage = data.number;
    
    renderJournals(data.content);
    updatePagination(data);
}

// Render Logic
function renderJournals(entries) {
    journalGrid.innerHTML = '';
    
    if (entries.length === 0) {
        journalGrid.innerHTML = '<p style="grid-column: 1 / -1; text-align: center;">No entries found. Create one!</p>';
        return;
    }

    entries.forEach(entry => {
        const date = new Date(entry.date).toLocaleDateString(undefined, { 
            year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute:'2-digit' 
        });

        const card = document.createElement('div');
        card.className = 'journal-card';
        card.innerHTML = `
            <div class="card-content-wrapper" style="padding-bottom: 2rem;">
                <h3>${entry.title}</h3>
                <div class="date">${date}</div>
                <p>${entry.content}</p>
                <div class="tags">${tagsHtml}</div>
            </div>
            <button onclick="deleteJournal(${entry.id})" class="ghost-button small" style="position: absolute; bottom: 15px; right: 15px; color: #ff4d4f; border: none; z-index: 10;">🗑️ Delete</button>
        `;
        
        // Dynamic 3D mouse move effect
        card.addEventListener('mousemove', handleCardMouseMove);
        card.addEventListener('mouseleave', handleCardMouseLeave);

        const tagsHtml = (entry.tags || []).map(t => `<span class="tag">${t.name}</span>`).join('');

        card.innerHTML = `
            <div class="card-content-wrapper">
                <h3>${entry.title}</h3>
                <div class="date">${date}</div>
                <p>${entry.content}</p>
                <div class="tags">${tagsHtml}</div>
            </div>
        `;
        
        journalGrid.appendChild(card);
    });
}

function updatePagination(data) {
    pageInfo.textContent = `Page ${data.number + 1} of ${data.totalPages || 1}`;
    btnPrev.disabled = data.first;
    btnNext.disabled = data.last;
}

// 3D Hover Logic
function handleCardMouseMove(e) {
    const card = this;
    const rect = card.getBoundingClientRect();
    const x = e.clientX - rect.left; // x position within the element
    const y = e.clientY - rect.top;  // y position within the element
    
    const centerX = rect.width / 2;
    const centerY = rect.height / 2;
    
    const rotateX = ((y - centerY) / centerY) * -10; // Max 10 deg rotation
    const rotateY = ((x - centerX) / centerX) * 10;
    
    card.style.transform = `perspective(1000px) translateZ(50px) rotateX(${rotateX}deg) rotateY(${rotateY}deg)`;
}

function handleCardMouseLeave() {
    this.style.transform = 'perspective(1000px) translateZ(0) rotateX(0) rotateY(0)';
}

// --- ADMIN LOGIC ---
async function fetchAdminData() {
    try {
        const [usersRes, logsRes] = await Promise.all([
            fetch(`${API_BASE}/admin/all-users`, { headers: { 'Authorization': `Basic ${credentials}` } }),
            fetch(`${API_BASE}/admin/logs`, { headers: { 'Authorization': `Basic ${credentials}` } })
        ]);
        
        if (usersRes.ok) {
            const users = await usersRes.json();
            renderAdminUsers(users);
        }
        if (logsRes.ok) {
            const logs = await logsRes.json();
            renderAdminLogs(logs);
        }
    } catch (err) {
        console.error(err);
    }
}

function renderAdminUsers(users) {
    adminUsersList.innerHTML = '';
    users.forEach(user => {
        const roles = (user.roles || []).join(', ');
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${user.id}</td>
            <td>${user.username}</td>
            <td>${user.email || 'N/A'}</td>
            <td>${roles}</td>
            <td>
                <button onclick="deleteUser(${user.id})" class="ghost-button small">Delete</button>
            </td>
        `;
        adminUsersList.appendChild(tr);
    });
}

function renderAdminLogs(logs) {
    adminAuditLogs.innerHTML = '';
    logs.forEach(log => {
        const date = new Date(log.timestamp).toLocaleString();
        const div = document.createElement('div');
        div.className = 'audit-item';
        div.innerHTML = `
            <div class="audit-action">${log.action}</div>
            <div class="audit-meta">By <strong>${log.adminUsername}</strong> on ${log.targetUsername} | ${date}</div>
            <div class="audit-meta" style="margin-top: 5px;">${log.details || ''}</div>
        `;
        adminAuditLogs.appendChild(div);
    });
}

async function deleteUser(id) {
    if (!confirm('Are you sure you want to delete this user?')) return;
    try {
        await fetch(`${API_BASE}/admin/delete-user/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Basic ${credentials}` }
        });
        fetchAdminData();
    } catch (err) {
        alert('Failed to delete user');
    }
}

// --- JOURNAL DELETE LOGIC ---
async function deleteJournal(id) {
    if (!confirm('Are you sure you want to delete this journal?')) return;
    try {
        const response = await fetch(`${API_BASE}/journal/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Basic ${credentials}` }
        });
        if (!response.ok) throw new Error('Failed to delete journal');
        fetchJournals(currentPage);
    } catch (err) {
        alert('Failed to delete journal');
    }
}
