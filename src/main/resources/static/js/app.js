/* ════════════════════════════════════════════════════
   RAG Knowledge Base - Frontend Application
   ════════════════════════════════════════════════════ */

const API = {
  BASE:      '/api/v1',
  CHAT:      '/api/v1/chat',
  SEARCH:    '/api/v1/search',
  DOCS:      '/api/v1/documents',
  SESSIONS:  '/api/v1/chat/sessions',
};

// ── State ──────────────────────────────────────────────
let state = {
  currentTab:      'chat',
  currentSessionId: null,
  userIdentifier:  'user_' + Math.random().toString(36).slice(2, 9),
  documents:       [],
  isLoading:       false,
};

// ── Tab switching ──────────────────────────────────────
function switchTab(tab) {
  document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));

  document.getElementById(`tab-${tab}`).classList.add('active');
  document.querySelector(`[data-tab="${tab}"]`).classList.add('active');
  state.currentTab = tab;

  document.getElementById('sessionsPanel').style.display = tab === 'chat' ? 'flex' : 'none';

  if (tab === 'documents') loadDocuments();
  if (tab === 'chat')      loadSessions();
}

// ── Chat ───────────────────────────────────────────────
function handleChatKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    sendMessage();
  }
}

function autoResize(el) {
  el.style.height = 'auto';
  el.style.height = Math.min(el.scrollHeight, 200) + 'px';
}

async function sendMessage() {
  const input = document.getElementById('chatInput');
  const question = input.value.trim();
  if (!question || state.isLoading) return;

  state.isLoading = true;
  document.getElementById('sendBtn').disabled = true;
  input.value = '';
  input.style.height = 'auto';

  // Remove welcome message
  const welcome = document.querySelector('.welcome-message');
  if (welcome) welcome.remove();

  // Show user message
  appendMessage('user', question);

  // Show typing indicator
  const typingId = appendTypingIndicator();

  try {
    const body = {
      question,
      userIdentifier: state.userIdentifier,
      sessionId:      state.currentSessionId || undefined,
      topK:           5,
      similarityThreshold: 0.7,
    };

    const res  = await fetch(API.CHAT, {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body:    JSON.stringify(body),
    });

    const data = await res.json();

    removeTypingIndicator(typingId);

    if (data.success) {
      const chat = data.data;
      state.currentSessionId = chat.sessionId;
      updateSessionInfo(chat.sessionId);
      appendMessage('assistant', chat.answer, chat.sources);
      loadSessions();
    } else {
      appendMessage('assistant', `Sorry, I encountered an error: ${data.message}`);
    }
  } catch (err) {
    removeTypingIndicator(typingId);
    appendMessage('assistant', 'Network error. Please check your connection and try again.');
    console.error(err);
  } finally {
    state.isLoading = false;
    document.getElementById('sendBtn').disabled = false;
  }
}

function appendMessage(role, content, sources = []) {
  const container = document.getElementById('chatMessages');
  const div       = document.createElement('div');
  div.className   = `message ${role}`;

  const avatar    = role === 'user' ? '👤' : '🤖';
  const timeStr   = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

  const sourcesHtml = sources && sources.length > 0
    ? `<div class="msg-sources">${sources.map(s =>
        `<span class="source-chip" title="${s.excerpt || ''}">📄 ${s.documentName || s.documentId?.slice(0,8) || 'Source'}</span>`
      ).join('')}</div>`
    : '';

  div.innerHTML = `
    <div class="msg-avatar">${avatar}</div>
    <div class="msg-body">
      <div class="msg-bubble">${escapeHtml(content).replace(/\n/g, '<br>')}</div>
      ${sourcesHtml}
      <div class="msg-time">${timeStr}</div>
    </div>
  `;

  container.appendChild(div);
  container.scrollTop = container.scrollHeight;
}

function appendTypingIndicator() {
  const id = 'typing-' + Date.now();
  const container = document.getElementById('chatMessages');
  const div = document.createElement('div');
  div.id = id;
  div.className = 'message assistant typing-indicator';
  div.innerHTML = `
    <div class="msg-avatar">🤖</div>
    <div class="msg-body">
      <div class="msg-bubble">
        <div class="dot"></div><div class="dot"></div><div class="dot"></div>
      </div>
    </div>
  `;
  container.appendChild(div);
  container.scrollTop = container.scrollHeight;
  return id;
}

function removeTypingIndicator(id) {
  const el = document.getElementById(id);
  if (el) el.remove();
}

function updateSessionInfo(sessionId) {
  const el = document.getElementById('sessionInfo');
  el.textContent = 'Session: ' + sessionId.slice(0, 8) + '...';
}

function newChat() {
  state.currentSessionId = null;
  document.getElementById('chatMessages').innerHTML = `
    <div class="welcome-message">
      <div class="welcome-icon">🤖</div>
      <h2>New conversation started</h2>
      <p>Ask anything about your uploaded documents.</p>
    </div>
  `;
  document.getElementById('sessionInfo').textContent = 'New session';
  document.querySelectorAll('.session-item').forEach(i => i.classList.remove('active'));
}

async function loadSessions() {
  try {
    const res  = await fetch(`${API.SESSIONS}?userIdentifier=${state.userIdentifier}`);
    const data = await res.json();
    if (!data.success) return;

    const list = document.getElementById('sessionsList');
    list.innerHTML = '';

    data.data.forEach(session => {
      const item = document.createElement('div');
      item.className = 'session-item' + (session.id === state.currentSessionId ? ' active' : '');
      item.textContent = session.sessionName || `Chat ${session.id.slice(0, 8)}`;
      item.title = `${session.messageCount} messages`;
      item.onclick = () => loadSessionHistory(session.id);
      list.appendChild(item);
    });
  } catch (err) {
    console.error('Failed to load sessions:', err);
  }
}

async function loadSessionHistory(sessionId) {
  state.currentSessionId = sessionId;
  const container = document.getElementById('chatMessages');
  container.innerHTML = '<div class="loading-spinner">Loading history...</div>';

  try {
    const res  = await fetch(`${API.SESSIONS}/${sessionId}/history`);
    const data = await res.json();
    container.innerHTML = '';

    if (data.success && data.data.length > 0) {
      data.data.forEach(msg => appendMessage(msg.role === 'USER' ? 'user' : 'assistant', msg.answer || msg.question || ''));
    } else {
      container.innerHTML = '<div class="empty-state">No messages in this session.</div>';
    }

    document.querySelectorAll('.session-item').forEach(i => {
      i.classList.toggle('active', i.textContent.includes(sessionId.slice(0, 8)));
    });

    updateSessionInfo(sessionId);
  } catch (err) {
    container.innerHTML = '<div class="empty-state">Failed to load history.</div>';
  }
}

// ── Documents ──────────────────────────────────────────
async function loadDocuments() {
  const grid = document.getElementById('documentsGrid');
  grid.innerHTML = '<div class="loading-spinner">Loading documents...</div>';

  try {
    const res  = await fetch(`${API.DOCS}?pageNo=0&pageSize=50`);
    const data = await res.json();

    if (!data.success) {
      grid.innerHTML = '<div class="empty-state">Failed to load documents.</div>';
      return;
    }

    state.documents = data.data.content || [];
    renderDocuments(state.documents);
  } catch (err) {
    grid.innerHTML = '<div class="empty-state">Failed to load documents.</div>';
  }
}

function renderDocuments(docs) {
  const grid = document.getElementById('documentsGrid');

  if (docs.length === 0) {
    grid.innerHTML = '<div class="empty-state">No documents uploaded yet. Drop a file above!</div>';
    return;
  }

  grid.innerHTML = docs.map(doc => `
    <div class="doc-card">
      <div class="doc-icon">${getDocIcon(doc.contentType)}</div>
      <div class="doc-name" title="${escapeHtml(doc.originalName)}">${escapeHtml(doc.originalName)}</div>
      <div class="doc-meta">
        <span>${formatSize(doc.fileSize)}</span>
        <span>${formatDate(doc.createdAt)}</span>
      </div>
      ${doc.tags && doc.tags.length > 0
        ? `<div class="doc-tags">${doc.tags.map(t => `<span class="tag-chip">${t}</span>`).join('')}</div>`
        : ''}
      <div class="doc-status status-${doc.status}">
        ${doc.status === 'INDEXED' ? '✓' : doc.status === 'PROCESSING' ? '⏳' : '✗'}
        ${doc.status} ${doc.chunkCount > 0 ? `· ${doc.chunkCount} chunks` : ''}
      </div>
      <div class="doc-actions">
        <button class="btn btn-secondary btn-sm" onclick="askAboutDoc('${doc.id}', '${escapeHtml(doc.originalName)}')">💬 Ask</button>
        <button class="btn btn-danger btn-sm" onclick="deleteDocument('${doc.id}')">🗑 Delete</button>
      </div>
    </div>
  `).join('');
}

function filterDocuments(query) {
  const filtered = query
    ? state.documents.filter(d => d.originalName.toLowerCase().includes(query.toLowerCase()))
    : state.documents;
  renderDocuments(filtered);
}

async function handleFileSelect(event) {
  const files = Array.from(event.target.files);
  for (const file of files) await uploadFile(file);
  event.target.value = '';
}

async function handleDrop(event) {
  event.preventDefault();
  document.getElementById('uploadZone').classList.remove('dragover');
  const files = Array.from(event.dataTransfer.files);
  for (const file of files) await uploadFile(file);
}

async function uploadFile(file) {
  const progress = document.getElementById('uploadProgress');
  const fill     = document.getElementById('progressFill');
  const text     = document.getElementById('progressText');

  progress.style.display = 'block';
  text.textContent = `Uploading ${file.name}...`;

  // Animate progress (simulated during upload)
  let pct = 0;
  const interval = setInterval(() => {
    pct = Math.min(pct + 5, 90);
    fill.style.width = pct + '%';
  }, 200);

  try {
    const formData = new FormData();
    formData.append('file', file);

    const res  = await fetch(`${API.DOCS}/upload`, { method: 'POST', body: formData });
    const data = await res.json();

    clearInterval(interval);
    fill.style.width = '100%';
    text.textContent = 'Processing...';

    if (data.success) {
      showToast(`✓ "${file.name}" uploaded and queued for indexing`, 'success');
      setTimeout(() => loadDocuments(), 1500);
    } else {
      showToast(`✗ Upload failed: ${data.message}`, 'error');
    }
  } catch (err) {
    clearInterval(interval);
    showToast(`✗ Upload error: ${err.message}`, 'error');
  } finally {
    setTimeout(() => {
      progress.style.display = 'none';
      fill.style.width = '0%';
    }, 2000);
  }
}

async function deleteDocument(id) {
  if (!confirm('Are you sure you want to delete this document?')) return;

  try {
    const res  = await fetch(`${API.DOCS}/${id}`, { method: 'DELETE' });
    const data = await res.json();

    if (data.success) {
      showToast('Document deleted', 'info');
      loadDocuments();
    } else {
      showToast('Failed to delete document', 'error');
    }
  } catch (err) {
    showToast('Delete error: ' + err.message, 'error');
  }
}

function askAboutDoc(docId, docName) {
  switchTab('chat');
  const input = document.getElementById('chatInput');
  input.value = `Tell me about the document "${docName}": `;
  input.focus();
  autoResize(input);
}

// ── Semantic Search ─────────────────────────────────────
async function performSearch() {
  const query     = document.getElementById('searchInput').value.trim();
  const topK      = parseInt(document.getElementById('topK').value) || 5;
  const threshold = parseInt(document.getElementById('threshold').value) / 100;

  if (!query) { showToast('Enter a search query', 'info'); return; }

  const container = document.getElementById('searchResults');
  container.innerHTML = '<div class="loading-spinner">Searching knowledge base...</div>';

  try {
    const res  = await fetch(API.SEARCH, {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body:    JSON.stringify({ query, topK, similarityThreshold: threshold }),
    });

    const data = await res.json();
    container.innerHTML = '';

    if (!data.success) {
      container.innerHTML = '<div class="search-empty">Search failed. Please try again.</div>';
      return;
    }

    const results = data.data.results || [];

    if (results.length === 0) {
      container.innerHTML = '<div class="search-empty">No results found. Try a different query or lower the similarity threshold.</div>';
      return;
    }

    results.forEach((result, i) => {
      const card = document.createElement('div');
      card.className = 'search-result-card';
      card.innerHTML = `
        <div class="result-header">
          <span class="result-doc">📄 ${result.documentName || `Document ${(result.documentId || '').slice(0, 8)}`}</span>
          ${result.similarity ? `<span class="result-similarity">${(result.similarity * 100).toFixed(1)}% match</span>` : ''}
        </div>
        <div class="result-content">${escapeHtml(result.content || '').substring(0, 600)}${result.content?.length > 600 ? '...' : ''}</div>
        ${result.chunkIndex !== undefined ? `<div style="margin-top:8px;font-size:11px;color:var(--text-muted)">Chunk #${result.chunkIndex + 1}</div>` : ''}
      `;
      container.appendChild(card);
    });
  } catch (err) {
    container.innerHTML = '<div class="search-empty">Network error. Please try again.</div>';
  }
}

// ── Toast Notifications ─────────────────────────────────
function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  const toast     = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.textContent = message;
  container.appendChild(toast);
  setTimeout(() => { toast.style.opacity = '0'; setTimeout(() => toast.remove(), 300); }, 3500);
}

// ── Helpers ─────────────────────────────────────────────
function escapeHtml(str = '') {
  return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function formatSize(bytes) {
  if (!bytes) return '0 B';
  if (bytes < 1024)       return bytes + ' B';
  if (bytes < 1024*1024)  return (bytes/1024).toFixed(1) + ' KB';
  return (bytes/1024/1024).toFixed(1) + ' MB';
}

function formatDate(dateStr) {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' });
}

function getDocIcon(contentType = '') {
  if (contentType.includes('pdf'))  return '📕';
  if (contentType.includes('word') || contentType.includes('docx')) return '📘';
  if (contentType.includes('text') || contentType.includes('plain')) return '📄';
  if (contentType.includes('markdown')) return '📝';
  return '📂';
}

// ── Init ────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  loadSessions();
  document.getElementById('sessionsPanel').style.display = 'flex';
});
