/* ============================================================
   ProjectPulse — WebSocket Chat (SockJS + STOMP)
   ============================================================ */

let stompClient = null;

function connectChat(projectId, currentUsername) {
  const socket = new SockJS('/ws');
  stompClient = Stomp.over(socket);
  stompClient.debug = null; // silence debug logs

  stompClient.connect({}, function () {
    // Subscribe to project chat topic
    stompClient.subscribe('/topic/chat/' + projectId, function (msg) {
      const data = JSON.parse(msg.body);
      appendMessage(data, currentUsername);
      scrollToBottom();
    });
  }, function (err) {
    console.warn('WebSocket disconnected, using HTTP fallback.', err);
  });
}

function sendWsMessage(projectId, currentUsername, fullName) {
  const input = document.getElementById('wsMessageInput');
  const content = input.value.trim();
  if (!content) return;

  if (stompClient && stompClient.connected) {
    stompClient.send('/app/chat.send', {}, JSON.stringify({
      content:        content,
      projectId:      projectId,
      senderUsername: currentUsername,
      senderFullName: fullName
    }));
    input.value = '';
  } else {
    // fallback: submit the HTTP form
    document.getElementById('httpChatForm').querySelector('input[name=content]').value = content;
    document.getElementById('httpChatForm').submit();
  }
}

function appendMessage(msg, currentUsername) {
  const container = document.getElementById('chatMessages');
  const isOwn = msg.senderUsername === currentUsername;

  const initials = (msg.senderFullName || msg.senderUsername)
    .split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);

  const time = msg.sentAt
    ? new Date(msg.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    : new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

  const div = document.createElement('div');
  div.className = 'chat-message fade-in' + (isOwn ? ' own' : '');
  div.innerHTML = `
    <div class="chat-avatar">${initials}</div>
    <div>
      <div class="chat-bubble">
        <div class="chat-sender">${isOwn ? 'You' : msg.senderFullName}</div>
        <div class="chat-text">${escapeHtml(msg.content)}</div>
        <div class="chat-time">${time}</div>
      </div>
    </div>
  `;
  container.appendChild(div);
}

function escapeHtml(text) {
  const d = document.createElement('div');
  d.textContent = text;
  return d.innerHTML;
}

function scrollToBottom() {
  const c = document.getElementById('chatMessages');
  if (c) c.scrollTop = c.scrollHeight;
}

// Send on Enter key
document.addEventListener('DOMContentLoaded', function () {
  const wsInput = document.getElementById('wsMessageInput');
  if (wsInput) {
    wsInput.addEventListener('keydown', function (e) {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        document.getElementById('wsSendBtn').click();
      }
    });
  }
  scrollToBottom();
});
