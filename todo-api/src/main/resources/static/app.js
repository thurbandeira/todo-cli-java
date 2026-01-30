const state = {
  baseUrl: localStorage.getItem("baseUrl") || "http://localhost:8080",
  token: localStorage.getItem("token") || "",
  page: 0,
  size: 10,
  last: true,
  currentStatus: "all",
};

const el = (id) => document.getElementById(id);
const logEl = el("log");

function log(message) {
  const time = new Date().toLocaleTimeString();
  logEl.textContent = `[${time}] ${message}\n` + logEl.textContent;
}

function setAuthStatus() {
  const status = el("authStatus");
  if (state.token) {
    status.textContent = "Autenticado.";
  } else {
    status.textContent = "Nao autenticado.";
  }
}

function saveToken(token) {
  state.token = token || "";
  localStorage.setItem("token", state.token);
  setAuthStatus();
}

function saveBaseUrl(url) {
  state.baseUrl = url;
  localStorage.setItem("baseUrl", state.baseUrl);
}

async function request(path, options = {}) {
  const url = `${state.baseUrl}${path}`;
  const headers = options.headers || {};
  if (state.token) {
    headers.Authorization = `Bearer ${state.token}`;
  }
  const response = await fetch(url, { ...options, headers });
  const text = await response.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }
  if (response.status === 401 && state.token) {
    const refreshed = await refreshToken();
    if (refreshed) {
      return request(path, options);
    }
  }
  if (!response.ok) {
    const msg = data && data.detail ? data.detail : response.statusText;
    throw new Error(msg);
  }
  return data;
}

async function register() {
  const username = el("username").value.trim();
  const password = el("password").value.trim();
  if (!username || !password) {
    log("Informe usuario e senha.");
    return;
  }
  const data = await request("/api/auth/register", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  saveToken(data.token);
  log("Registrado e autenticado.");
}

async function login() {
  const username = el("username").value.trim();
  const password = el("password").value.trim();
  if (!username || !password) {
    log("Informe usuario e senha.");
    return;
  }
  const data = await request("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  saveToken(data.token);
  log("Login realizado.");
}

function logout() {
  saveToken("");
  log("Logout.");
}

async function refreshToken() {
  try {
    const response = await fetch(`${state.baseUrl}/api/auth/refresh`, {
      method: "POST",
      headers: { Authorization: `Bearer ${state.token}` },
    });
    if (!response.ok) {
      return false;
    }
    const data = await response.json();
    saveToken(data.token);
    log("Token renovado.");
    return true;
  } catch {
    return false;
  }
}

async function addTask() {
  const title = el("newTitle").value.trim();
  if (!title) {
    log("Titulo invalido.");
    return;
  }
  const data = await request("/api/tasks", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ title }),
  });
  el("newTitle").value = "";
  log(`Tarefa criada (#${data.id}).`);
  await listPaged(0);
}

function renderTasks(tasks) {
  const list = el("taskList");
  list.innerHTML = "";
  tasks.forEach((task) => {
    const li = document.createElement("li");
    li.className = "list-item";
    const info = document.createElement("div");
    const title = document.createElement("strong");
    title.textContent = task.title;
    const tag = document.createElement("span");
    tag.className = "tag";
    tag.textContent = task.completed ? "Concluida" : "Pendente";
    info.appendChild(title);
    info.appendChild(tag);

    const actions = document.createElement("div");
    actions.className = "actions";

    if (!task.completed) {
      const doneBtn = document.createElement("button");
      doneBtn.className = "btn secondary";
      doneBtn.textContent = "Concluir";
      doneBtn.onclick = async () => {
        await request(`/api/tasks/${task.id}/complete`, { method: "POST" });
        log(`Tarefa #${task.id} concluida.`);
        await listAll();
      };
      actions.appendChild(doneBtn);
    }

    const removeBtn = document.createElement("button");
    removeBtn.className = "btn danger";
    removeBtn.textContent = "Remover";
    removeBtn.onclick = async () => {
      await request(`/api/tasks/${task.id}`, { method: "DELETE" });
      log(`Tarefa #${task.id} removida.`);
      await listAll();
    };
    actions.appendChild(removeBtn);

    li.appendChild(info);
    li.appendChild(actions);
    list.appendChild(li);
  });
}

async function updateSummary() {
  const data = await request("/api/tasks/summary");
  el("summary").textContent = `Total: ${data.total} | Pendentes: ${data.pending} | Concluidas: ${data.done}`;
}

async function listAll() {
  state.currentStatus = "all";
  await listPaged(0, state.currentStatus);
}

async function listPending() {
  state.currentStatus = "pending";
  await listPaged(0, state.currentStatus);
}

async function listCompleted() {
  state.currentStatus = "completed";
  await listPaged(0, state.currentStatus);
}

async function searchTasks() {
  const keyword = el("searchText").value.trim();
  if (!keyword) {
    log("Informe a palavra-chave.");
    return;
  }
  const data = await request(
    `/api/tasks/search/page?keyword=${encodeURIComponent(keyword)}&page=0&size=${state.size}&sort=id,asc`
  );
  renderTasks(data.items);
  state.page = data.page;
  state.last = data.last;
  updatePager();
  await updateSummary();
}

async function clearCompleted() {
  const data = await request("/api/tasks/clear-completed", { method: "POST" });
  el("summary").textContent = `Total: ${data.total} | Pendentes: ${data.pending} | Concluidas: ${data.done}`;
  await listPaged(0);
}

async function listPaged(page, status = "all") {
  const data = await request(
    `/api/tasks/page?status=${status}&page=${page}&size=${state.size}&sort=completed,asc;id,asc`
  );
  renderTasks(data.items);
  state.page = data.page;
  state.last = data.last;
  updatePager();
  await updateSummary();
}

function updatePager() {
  el("pageInfo").textContent = `Pagina ${state.page + 1}`;
  el("prevPageBtn").disabled = state.page <= 0;
  el("nextPageBtn").disabled = state.last;
}

function init() {
  el("baseUrl").value = state.baseUrl;
  setAuthStatus();

  el("saveBaseUrl").onclick = () => {
    saveBaseUrl(el("baseUrl").value.trim());
    log("Base URL atualizada.");
  };

  el("registerBtn").onclick = () => register().catch((e) => log(e.message));
  el("loginBtn").onclick = () => login().catch((e) => log(e.message));
  el("logoutBtn").onclick = logout;

  el("addBtn").onclick = () => addTask().catch((e) => log(e.message));
  el("listAllBtn").onclick = () => listAll().catch((e) => log(e.message));
  el("listPendingBtn").onclick = () => listPending().catch((e) => log(e.message));
  el("listCompletedBtn").onclick = () => listCompleted().catch((e) => log(e.message));
  el("searchBtn").onclick = () => searchTasks().catch((e) => log(e.message));
  el("clearCompletedBtn").onclick = () => clearCompleted().catch((e) => log(e.message));
  el("prevPageBtn").onclick = () =>
    listPaged(Math.max(0, state.page - 1), state.currentStatus || "all").catch((e) => log(e.message));
  el("nextPageBtn").onclick = () =>
    listPaged(state.page + 1, state.currentStatus || "all").catch((e) => log(e.message));
}

init();
