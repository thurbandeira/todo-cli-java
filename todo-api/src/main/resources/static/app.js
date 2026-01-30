const state = {
  baseUrl: localStorage.getItem("baseUrl") || "http://localhost:8080",
  token: localStorage.getItem("token") || "",
  page: 0,
  size: 8,
  last: true,
  currentStatus: "all",
};

const el = (id) => document.getElementById(id);
const toastEl = el("toast");
let toastTimeout = null;

function toast(message, type = "success") {
  toastEl.textContent = message;
  toastEl.className = `toast show ${type}`;
  if (toastTimeout) clearTimeout(toastTimeout);
  toastTimeout = setTimeout(() => {
    toastEl.className = "toast";
  }, 2500);
}

function setAuthStatus() {
  const status = el("authStatus");
  if (state.token) {
    status.textContent = "Autenticado";
    status.style.color = "#6ee7b7";
  } else {
    status.textContent = "Nao autenticado";
    status.style.color = "#b8c0d9";
  }
}

function setActiveUser(name) {
  el("activeUser").textContent = name ? `Usuario: ${name}` : "Usuario: -";
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
  headers.Accept = "application/json";
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

async function refreshToken() {
  try {
    const response = await fetch(`${state.baseUrl}/api/auth/refresh`, {
      method: "POST",
      headers: { Authorization: `Bearer ${state.token}` },
    });
    if (!response.ok) return false;
    const data = await response.json();
    saveToken(data.token);
    toast("Token renovado.");
    return true;
  } catch {
    return false;
  }
}

async function register() {
  const username = el("username").value.trim();
  const password = el("password").value.trim();
  if (!username || !password) {
    toast("Informe usuario e senha.", "error");
    return;
  }
  const data = await request("/api/auth/register", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  saveToken(data.token);
  setActiveUser(username);
  toast("Conta criada e autenticado.");
  await listAll();
}

async function login() {
  const username = el("username").value.trim();
  const password = el("password").value.trim();
  if (!username || !password) {
    toast("Informe usuario e senha.", "error");
    return;
  }
  const data = await request("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  saveToken(data.token);
  setActiveUser(username);
  toast("Login realizado.");
  await listAll();
}

function logout() {
  saveToken("");
  setActiveUser("");
  renderTasks([], "taskList");
  renderTasks([], "taskListTasks");
  toast("Logout.");
}

async function addTask(inputId) {
  const title = el(inputId).value.trim();
  if (!title) {
    toast("Titulo invalido.", "error");
    return;
  }
  const data = await request("/api/tasks", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ title }),
  });
  el(inputId).value = "";
  toast(`Tarefa criada (#${data.id}).`);
  await listPaged(0, state.currentStatus);
}

async function editTask(id, currentTitle) {
  const title = prompt("Editar titulo:", currentTitle);
  if (title === null) return;
  const trimmed = title.trim();
  if (!trimmed) {
    toast("Titulo invalido.", "error");
    return;
  }
  await request(`/api/tasks/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ title: trimmed }),
  });
  toast(`Tarefa #${id} atualizada.`);
  await listPaged(state.page, state.currentStatus);
}

function renderTasks(tasks, listId) {
  const list = el(listId);
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
        toast(`Tarefa #${task.id} concluida.`);
        await listPaged(state.page, state.currentStatus);
      };
      actions.appendChild(doneBtn);
    }

    const editBtn = document.createElement("button");
    editBtn.className = "btn ghost";
    editBtn.textContent = "Editar";
    editBtn.onclick = () => editTask(task.id, task.title);
    actions.appendChild(editBtn);

    const removeBtn = document.createElement("button");
    removeBtn.className = "btn danger";
    removeBtn.textContent = "Remover";
    removeBtn.onclick = async () => {
      await request(`/api/tasks/${task.id}`, { method: "DELETE" });
      toast(`Tarefa #${task.id} removida.`);
      await listPaged(state.page, state.currentStatus);
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
  el("kpiTotal").textContent = data.total;
  el("kpiPending").textContent = data.pending;
  el("kpiDone").textContent = data.done;
  const rate = data.total === 0 ? 0 : Math.round((data.done / data.total) * 100);
  el("completionRate").textContent = `${rate}% concluidas`;
  el("progressBar").style.width = `${rate}%`;
  el("barPending").style.setProperty("--scale", data.total ? data.pending / data.total : 0.1);
  el("barDone").style.setProperty("--scale", data.total ? data.done / data.total : 0.1);
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
    toast("Informe a palavra-chave.", "error");
    return;
  }
  const data = await request(
    `/api/tasks/search/page?keyword=${encodeURIComponent(keyword)}&page=0&size=${state.size}&sort=id,asc`
  );
  renderTasks(data.items, "taskList");
  renderTasks(data.items, "taskListTasks");
  state.page = data.page;
  state.last = data.last;
  updatePager();
  await updateSummary();
}

async function clearCompleted() {
  const data = await request("/api/tasks/clear-completed", { method: "POST" });
  el("summary").textContent = `Total: ${data.total} | Pendentes: ${data.pending} | Concluidas: ${data.done}`;
  await listPaged(0, state.currentStatus);
}

async function listPaged(page, status = "all") {
  const data = await request(
    `/api/tasks/page?status=${status}&page=${page}&size=${state.size}&sort=completed,asc;id,asc`
  );
  renderTasks(data.items, "taskList");
  renderTasks(data.items, "taskListTasks");
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

function initTabs() {
  const tabs = document.querySelectorAll(".nav-item");
  tabs.forEach((btn) => {
    btn.addEventListener("click", () => {
      tabs.forEach((t) => t.classList.remove("active"));
      btn.classList.add("active");
      const target = btn.getAttribute("data-tab");
      document.querySelectorAll(".tab").forEach((tab) => {
        tab.classList.remove("active");
      });
      document.getElementById(`tab-${target}`).classList.add("active");
    });
  });
}

function init() {
  el("baseUrl").value = state.baseUrl;
  setAuthStatus();
  initTabs();

  el("saveBaseUrl").onclick = () => {
    saveBaseUrl(el("baseUrl").value.trim());
    toast("Base URL atualizada.");
  };

  el("registerBtn").onclick = () => register().catch((e) => toast(e.message, "error"));
  el("loginBtn").onclick = () => login().catch((e) => toast(e.message, "error"));
  el("logoutBtn").onclick = logout;
  el("openAuthBtn").onclick = () => {
    document.querySelector('[data-tab="settings"]').click();
  };
  el("refreshBtn").onclick = () => listAll().catch((e) => toast(e.message, "error"));
  el("refreshTokenBtn").onclick = () => refreshToken().catch(() => {});

  el("addBtn").onclick = () => addTask("newTitle").catch((e) => toast(e.message, "error"));
  el("quickAddBtn").onclick = () => addTask("quickTitle").catch((e) => toast(e.message, "error"));
  el("listAllBtn").onclick = () => listAll().catch((e) => toast(e.message, "error"));
  el("listPendingBtn").onclick = () => listPending().catch((e) => toast(e.message, "error"));
  el("listCompletedBtn").onclick = () => listCompleted().catch((e) => toast(e.message, "error"));
  el("quickPendingBtn").onclick = () => listPending().catch((e) => toast(e.message, "error"));
  el("quickCompletedBtn").onclick = () => listCompleted().catch((e) => toast(e.message, "error"));
  el("searchBtn").onclick = () => searchTasks().catch((e) => toast(e.message, "error"));
  el("clearCompletedBtn").onclick = () => clearCompleted().catch((e) => toast(e.message, "error"));
  el("prevPageBtn").onclick = () =>
    listPaged(Math.max(0, state.page - 1), state.currentStatus || "all").catch((e) => toast(e.message, "error"));
  el("nextPageBtn").onclick = () =>
    listPaged(state.page + 1, state.currentStatus || "all").catch((e) => toast(e.message, "error"));
}

init();
